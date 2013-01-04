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
    public static long waitForReply(Ticket.T type, Long userid) {
        StringBuilder sql = new StringBuilder("SELECT count(*) as c FROM Ticket WHERE ")
                .append("type=? AND responseTimes<=messageTimes AND state NOT IN ('PRE_CLOSE', 'CLOSE')");
        if(userid != null && userid > 0)
            sql.append(" AND resolver_id=").append(userid);
        return (Long) DBUtils.row(sql.toString(), type.name()).get("c");
    }

    /**
     * 成功处理的 Tickets 列表
     *
     * @return ticket/review/feedback: _1: success; _2:total; _3: percent
     */
    public static Map<String, F.T3<Long, Long, Float>> dealSuccess(Date from, Date to) {
        // PS: 因为所有计算都是以 Ticket 为左表, 而创建 Ticket 则代表 Reivew/Feedback 需要处理
        // 所以不再进行 review.lastRating<4 或者 feedback.score<4 这些用来过滤出产生了负评的 review/feedback
        /**
         * tickets:
         * 1. Ticket State Close
         */
        long ticketSucc = (Long) DBUtils.row("SELECT COUNT(*) as c FROM Ticket WHERE type=? AND " +
                "state=? AND createAt>=? AND createAt<=?",
                Ticket.T.TICKET.name(), TicketState.CLOSE, from, to).get("c");
        long ticketTotal = (Long) DBUtils.row("SELECT COUNT(*) as c FROM Ticket WHERE type=? AND " +
                "createAt>=? AND createAt<=?",
                Ticket.T.TICKET.name(), from, to).get("c");
        float ticketPercent = ticketTotal == 0 ? 0 : (ticketSucc / (float) ticketTotal);

        /**
         * review:
         * 1. rating>=4 and state NOT in new (not remove)
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

        long reviewTotal = (Long) DBUtils.row("SELECT COUNT(t.id) as c FROM Ticket t" +
                " WHERE t.type=? AND t.createAt>=? AND t.createAt<=?",
                Ticket.T.REVIEW.name(), from, to).get("c");
        float reviewPercent = reviewTotal == 0 ? 0 : (reviewSucc / (float) reviewTotal);

        /**
         * feedback:
         * 1. isRemove and state in pre_close and close
         */
        long feedbackSucc = (Long) DBUtils.row("SELECT COUNT(t.id) as c FROM Ticket t" +
                " LEFT JOIN Feedback f on t.feedback_orderId=f.orderId WHERE" +
                " t.type=? AND f.isRemove=true AND t.createAt>=? AND t.createAt<=?",
                Ticket.T.FEEDBACK.name(), from, to).get("c");
        long feedbackTotal = (Long) DBUtils.row("SELECT COUNT(t.id) as c FROM Ticket t" +
                " WHERE t.type=? AND t.createAt>=? AND t.createAt<=?",
                Ticket.T.FEEDBACK.name(), from, to).get("c");
        float feedbackPercent = feedbackTotal == 0 ? 0 : (feedbackSucc / (float) feedbackTotal);
        Map<String, F.T3<Long, Long, Float>> rows = new HashMap<String, F.T3<Long, Long, Float>>();
        rows.put("ticket", new F.T3<Long, Long, Float>(ticketSucc, ticketTotal, ticketPercent));
        rows.put("review", new F.T3<Long, Long, Float>(reviewSucc, reviewTotal, reviewPercent));
        rows.put("feedback",
                new F.T3<Long, Long, Float>(feedbackSucc, feedbackTotal, feedbackPercent));
        return rows;
    }


    /**
     * 30 天内的成功处理列表
     *
     * @return
     */
    public static Map<String, F.T3<Long, Long, Float>> dealSuccess() {
        DateTime now = DateTime.now();
        return dealSuccess(Dates.morning(now.minusDays(30).toDate()), Dates.night(now.toDate()));
    }

}
