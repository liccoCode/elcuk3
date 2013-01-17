package query;

import helper.DBUtils;
import helper.Dates;
import models.support.Ticket;
import models.support.TicketState;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/29/12
 * Time: 6:29 PM
 */
public class TicketQuery {
    /**
     * 等待回信的 Ticket/Feedback/Review
     *
     * @param type
     * @return
     */
    public long waitForReply(Ticket.T type, Long userid) {
        StringBuilder sql = new StringBuilder("SELECT count(*) as c FROM Ticket WHERE ")
                .append("type=? AND responseTimes<=messageTimes AND state NOT IN ('PRE_CLOSE', 'CLOSE')");
        if(userid != null && userid > 0)
            sql.append(" AND resolver_id=").append(userid);
        return (Long) DBUtils.row(sql.toString(), type.name()).get("c");
    }

    public long ticketTotal(Ticket.T type, Date from, Date to) {
        // 为了避免自己循环调用
        return ticketTotal(type, from, to, new TicketState[]{});
    }

    /**
     * 根据 Ticket.T 与 TicketState 来计算 Ticket 数量
     *
     * @return
     */
    public long ticketTotal(Ticket.T type, Date from, Date to, TicketState... state) {
        StringBuilder sbd = new StringBuilder("SELECT COUNT(t.id) as c FROM Ticket t WHERE" +
                " t.createAt>=? AND t.createAt<=?");
        if(type != null)
            sbd.append(" AND t.type='").append(type.name()).append("'");
        if(state != null && state.length > 0) {
            sbd.append(" AND (");
            for(TicketState s : state) {
                sbd.append("t.state='").append(s.name()).append("' OR ");
            }
            sbd.delete(sbd.lastIndexOf("OR"), sbd.length()).append(")");
        }
        return (Long) DBUtils.row(sbd.toString(), from, to).get("c");
    }

    /**
     * @param divisor  分子(除数)
     * @param dividend 分母(被除数)
     * @return
     */
    private static float percent(long divisor, long dividend) {
        return dividend == 0 ? 0 : (divisor / (float) dividend);
    }

    /**
     * 成功处理的 Tickets 列表
     *
     * @return ticket/review/feedback: _1: success; _2:total; _3: percent
     */
    public Map<String, F.T3<Long, Long, Float>> dealSuccess(Date from, Date to) {
        // PS: 因为所有计算都是以 Ticket 为左表, 而创建 Ticket 则代表 Reivew/Feedback 需要处理
        // 所以不再进行 review.lastRating<4 或者 feedback.score<4 这些用来过滤出产生了负评的 review/feedback
        /**
         * Tickets:
         * 1. Ticket State Close
         */
        long ticketSucc = (Long) DBUtils.row("SELECT COUNT(*) as c FROM Ticket WHERE type=? AND " +
                "state IN (?,?) AND createAt>=? AND createAt<=?",
                Ticket.T.TICKET.name(), TicketState.CLOSE, TicketState.PRE_CLOSE, from, to)
                .get("c");
        long ticketTotal = ticketTotal(Ticket.T.TICKET, from, to);
        float ticketPercent = percent(ticketSucc, ticketTotal);

        /**
         * Review:
         * 1. rating>=4 and isRemove == false and state not in NEW (not remove)
         * 2. isRemove == true
         */
        long reviewSucc = (Long) DBUtils.row("SELECT COUNT(t.id) as c FROM Ticket t" +
                " LEFT JOIN AmazonListingReview r on t.review_alrId=r.alrId WHERE t.type=?" +
                " AND r.rating>=4 AND t.state NOT IN (?) AND r.isRemove=false " +
                "AND t.createAt>=? AND t.createAt<=?",
                Ticket.T.REVIEW.name(), TicketState.NEW.name(),
                from, to).get("c");
        reviewSucc += (Long) DBUtils.row("SELECT COUNT(t.id) as c FROM Ticket t" +
                " LEFT JOIN AmazonListingReview r on t.review_alrId=r.alrId WHERE t.type=?" +
                " AND r.isRemove=true AND t.createAt>=? AND t.createAt<=?",
                Ticket.T.REVIEW.name(), from, to)
                .get("c");

        long reviewTotal = ticketTotal(Ticket.T.REVIEW, from, to);
        float reviewPercent = percent(reviewSucc, reviewTotal);

        /**
         * Feedback:
         * 1. isRemove and state not in NEW
         */
        long feedbackSucc = (Long) DBUtils.row("SELECT COUNT(t.id) as c FROM Ticket t" +
                " LEFT JOIN Feedback f on t.feedback_orderId=f.orderId WHERE" +
                " t.type=? AND f.isRemove=true AND t.state NOT IN (?)" +
                " AND t.createAt>=? AND t.createAt<=?",
                Ticket.T.FEEDBACK.name(), TicketState.NEW.name(), from, to).get("c");
        long feedbackTotal = ticketTotal(Ticket.T.FEEDBACK, from, to);
        float feedbackPercent = percent(feedbackSucc, feedbackTotal);
        Map<String, F.T3<Long, Long, Float>> rows = new HashMap<String, F.T3<Long, Long, Float>>();
        rows.put("ticket", new F.T3<Long, Long, Float>(ticketSucc, ticketTotal, ticketPercent));
        rows.put("review", new F.T3<Long, Long, Float>(reviewSucc, reviewTotal, reviewPercent));
        rows.put("feedback",
                new F.T3<Long, Long, Float>(feedbackSucc, feedbackTotal, feedbackPercent));
        return rows;
    }


    /**
     * N 天内的成功处理列表(例如: 90)
     *
     * @return
     */
    public Map<String, F.T3<Long, Long, Float>> dealSuccess(int days) {
        DateTime now = DateTime.now();
        return dealSuccess(Dates.morning(now.minusDays(days).toDate()), Dates.night(now.toDate()));
    }

    /**
     * 指定时间段内处理失败的 Tickets 数据
     *
     * @return ticket/review/feedback: _1: success; _2:total; _3: percent
     */
    public Map<String, F.T3<Long, Long, Float>> dealFailed(Date from, Date to) {
        /**
         * Ticket: unkonw...
         * 先不计算失败的 Ticket
         */
        // ---------------------------
        /**
         * Review: lastRating>rating (and rating < 4) and isRemove == false in any state.
         *
         */
        long reviewFailed = (Long) DBUtils.row("SELECT COUNT(t.id) as c FROM Ticket t" +
                " LEFT JOIN AmazonListingReview r on t.review_alrId=r.alrId WHERE" +
                " t.type=? AND r.lastRating>r.rating AND" +
                " t.createAt>=? AND t.createAt<=?",
                Ticket.T.REVIEW.name(), from, to).get("c");
        long reviewTotal = ticketTotal(Ticket.T.REVIEW, from, to);
        float reviewPercent = percent(reviewFailed, reviewTotal);

        /**
         * Feedback: isRemove == false and createAt<=60.days.before (insteadof now)
         * 创建时间超过 60 天并且没有处理成功的 Feedback
         *
         */
        Date twoMonuthAgo = DateTime.now().minusMonths(2).toDate();
        long feedbackFailed = (Long) DBUtils.row("SELECT COUNT(t.id) as c FROM Ticket t" +
                " LEFT JOIN Feedback f on t.feedback_orderId=f.orderId WHERE" +
                " t.type=? AND f.isRemove=false AND t.createAt>=? AND t.createAt<=?",
                Ticket.T.FEEDBACK.name(), from,
                new Date(Math.min(twoMonuthAgo.getTime(), to.getTime()))
        ).get("c");
        long feedbackTotal = ticketTotal(Ticket.T.FEEDBACK, from, to);
        float feedbackPercent = percent(feedbackFailed, feedbackTotal);

        Map<String, F.T3<Long, Long, Float>> rows = new HashMap<String, F.T3<Long, Long, Float>>();
        rows.put("ticket", new F.T3<Long, Long, Float>(0l, 0l, 0f));
        rows.put("review", new F.T3<Long, Long, Float>(reviewFailed, reviewTotal, reviewPercent));
        rows.put("feedback",
                new F.T3<Long, Long, Float>(feedbackFailed, feedbackTotal, feedbackPercent));
        return rows;
    }

    /**
     * N 天内处理失败的列表(例如: 90)
     */
    public Map<String, F.T3<Long, Long, Float>> dealFailed(int days) {
        DateTime now = DateTime.now();
        return dealFailed(Dates.morning(now.minusDays(days).toDate()), Dates.night(now.toDate()));
    }

    /**
     * N 天内悬而未决的 Ticket
     *
     * @return ticket/review/feedback: _1: success; _2:total; _3: percent
     */
    public Map<String, F.T3<Long, Long, Float>> dealHangup(Date from, Date to) {

        /**
         * Ticket: type == Ticket and state == NO_RESP
         */
        long ticketHangup = ticketTotal(Ticket.T.TICKET, from, to, TicketState.NO_RESP);
        long ticketTotal = ticketTotal(Ticket.T.TICKET, from, to);
        float ticketPercent = percent(ticketHangup, ticketTotal);

        /**
         * Review: type == Review and state == NO_RESP and isRemove == false and lastRating==rating
         */
        long reviewHangup = (Long) DBUtils.row("SELECT COUNT(t.id) as c FROM Ticket t" +
                " LEFT JOIN AmazonListingReview r on t.review_alrId=r.alrId WHERE" +
                " t.type=? AND t.state=? AND r.isRemove=false AND r.lastRating=r.rating" +
                " AND t.createAt>=? AND t.createAt<=?",
                Ticket.T.REVIEW.name(), TicketState.NO_RESP.name(), from, to).get("c");
        long reviewTotal = ticketTotal(Ticket.T.REVIEW, from, to);
        float reviewPercent = percent(reviewHangup, reviewTotal);

        /**
         * Feedback: type == Feedback and state == NO_RESP and isRemova == false
         */
        long feedbackHangup = (Long) DBUtils.row("SELECT COUNT(t.id) as c FROM Ticket t" +
                " LEFT JOIN Feedback f on t.feedback_orderId=f.orderId WHERE" +
                " t.type=? AND t.state=? AND f.isRemove=false AND" +
                " t.createAt>=? AND t.createAt<=?",
                Ticket.T.FEEDBACK.name(), TicketState.NO_RESP.name(), from, to).get("c");
        long feedbackTotal = ticketTotal(Ticket.T.FEEDBACK, from, to);
        float feedbackPercent = percent(feedbackHangup, feedbackTotal);
        Map<String, F.T3<Long, Long, Float>> rows = new HashMap<String, F.T3<Long, Long, Float>>();
        rows.put("ticket", new F.T3<Long, Long, Float>(ticketHangup, ticketTotal, ticketPercent));
        rows.put("review", new F.T3<Long, Long, Float>(reviewHangup, reviewTotal, reviewPercent));
        rows.put("feedback",
                new F.T3<Long, Long, Float>(feedbackHangup, feedbackTotal, feedbackPercent));
        return rows;
    }

    public Map<String, F.T3<Long, Long, Float>> dealHangup(int days) {
        DateTime now = DateTime.now();
        return dealHangup(Dates.morning(now.minusDays(days).toDate()), Dates.night(now.toDate()));
    }

    /**
     * N 天内正在处理的 Ticket
     *
     * @return ticket/review/feedback: _1: success; _2:total; _3: percent
     */
    public Map<String, F.T3<Long, Long, Float>> dealing(Date from, Date to) {
        /**
         * Ticket: state in [NEW, TWO_MAIL, NEW_MSG, MAILED] as &t
         */
        TicketState[] ds = new TicketState[]{TicketState.NEW, TicketState.TWO_MAIL,
                TicketState.NEW_MSG, TicketState.MAILED};
        long ticketDealing = ticketTotal(Ticket.T.TICKET, from, to, ds);
        long ticketTotal = ticketTotal(Ticket.T.TICKET, from, to);
        float ticketPercent = percent(ticketDealing, ticketTotal);

        /**
         * Review: state in [*t] and lastRating==rating and isRemove==false
         */
        long reviewDealing = (Long) DBUtils.row("SELECT COUNT(t.id) as c FROM Ticket t" +
                " LEFT JOIN AmazonListingReview r on t.review_alrId=r.alrId WHERE" +
                " t.type=? AND t.state IN (?,?,?,?) AND r.lastRating=r.rating AND" +
                " r.isRemove=false AND t.createAt>=? AND t.createAt<=?",
                Ticket.T.REVIEW.name(),
                ds[0].name(), ds[1].name(), ds[2].name(), ds[3].name(),
                from, to).get("c");
        long reviewTotal = ticketTotal(Ticket.T.REVIEW, from, to);
        float reviewPercent = percent(reviewDealing, reviewTotal);

        /**
         * Feedback: state in [*t] and isRemove==false
         */
        long feedbackDealing = (Long) DBUtils.row("SELECT COUNT(t.id) as c FROM Ticket t" +
                " LEFT JOIN Feedback f on t.feedback_orderId=f.orderId WHERE" +
                " t.type=? AND t.state IN (?,?,?,?) AND f.isRemove=false AND" +
                " t.createAt>=? AND t.createAt<=?",
                Ticket.T.FEEDBACK.name(),
                ds[0].name(), ds[1].name(), ds[2].name(), ds[3].name(),
                from, to).get("c");
        long feedbackTotal = ticketTotal(Ticket.T.FEEDBACK, from, to);
        float feedbackPercent = percent(feedbackDealing, feedbackTotal);
        Map<String, F.T3<Long, Long, Float>> rows = new HashMap<String, F.T3<Long, Long, Float>>();
        rows.put("ticket", new F.T3<Long, Long, Float>(ticketDealing, ticketTotal, ticketPercent));
        rows.put("review", new F.T3<Long, Long, Float>(reviewDealing, reviewTotal, reviewPercent));
        rows.put("feedback",
                new F.T3<Long, Long, Float>(feedbackDealing, feedbackTotal, feedbackPercent));
        return rows;
    }

    public Map<String, F.T3<Long, Long, Float>> dealing(int days) {
        DateTime now = DateTime.now();
        return dealing(Dates.morning(now.minusDays(days).toDate()), Dates.night(now.toDate()));
    }
}
