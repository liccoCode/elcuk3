package jobs;

import helper.Dates;
import helper.FLog;
import helper.HTTP;
import models.Jobex;
import models.market.AmazonListingReview;
import models.market.Feedback;
import models.market.M;
import models.support.Ticket;
import models.support.TicketState;
import org.apache.commons.lang.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 * 用来补充性质的更新已经在系统中的 Feedback 的信息;
 * 会加载所有有 OsTicketId 的 Feedback , 然后对她们进行更新:
 * 1. 如果发现 Feedback 消失了, 则记录此 Feedback 被处理完了
 * 2. 同时可以更新 Feedback 记录的最新时间(包含时分秒)
 * 周期:
 * - 轮询周期: 5mn
 * - Duration: 40mn
 * </pre>
 * User: wyattpan
 * Date: 8/9/12
 * Time: 3:44 PM
 */
@Every("5mn")
public class FeedbackInfoFetchJob extends Job {
    @Override
    public void doJob() {
        if(!Jobex.findByClassName(FeedbackInfoFetchJob.class.getName()).isExcute()) return;
        // 处理还没有关闭的 Ticket, 每次更新 30
        int size = 30;
        if(Play.mode.isDev()) size = 10;
        List<Ticket> tickets = Ticket.find("type=? AND isSuccess=? AND state NOT IN (?,?) ORDER BY lastSyncTime",
                Ticket.T.FEEDBACK, false, TicketState.PRE_CLOSE, TicketState.CLOSE).fetch(size);
        Logger.info("FeedbackInfoFetchJob to Amazon sync %s tickets.", tickets.size());
        for(Ticket ticket : tickets) {
            FeedbackInfoFetchJob.checkFeedbackDealState(ticket);
            ticket.lastSyncTime = new Date();
            ticket.save();
        }
    }

    /**
     * 检查 Feedback 的处理状态
     * 1. 判断是否未 UK 账号在 DE 市场销售的, 如果是, 则跳过第二个检查
     * 2. 向 Amazon 检查这个 Feedback 是否被删除?(无法修改评价)
     * 3. 检查 Ticket 对应的 Feedback 是否已经超时?
     */
    public static void checkFeedbackDealState(Ticket ticket) {
        if(ticket.feedback == null) {
            Logger.warn("FeedbackInfoFetchJob deal an no Feedback Ticket(id|fid) [%s|%s]", ticket.id, ticket.fid);
            return;
        }

        // 1.
        if(ticket.feedback.market != null && ticket.feedback.account.type != null) {
            if(!ticket.feedback.market.equals(ticket.feedback.account.type) && !ticket.feedback.market.equals(M.AMAZON_FR)/*法国市场还是需要处理, 因为现在 FR 的订单都是 UK 账号的*/) {
                ticket.state = TicketState.PRE_CLOSE;
                ticket.memo = ticket.feedback.account.type.nickName() + " 账号在 " + ticket.feedback.market.nickName() + " 销售产品时的 Feedback 不再处理.\r\n" + ticket.memo;
            } else { // 如果 1 满足则跳过 2 的原因是因为如果两着不一样, 抓取不到正确的 Feedback 信息
                // 2.
                String html = FeedbackInfoFetchJob.fetchAmazonFeedbackHtml(ticket.feedback);
                ticket.feedback.isRemove = FeedbackInfoFetchJob.isFeedbackRemove(html);
                ticket.isSuccess = ticket.feedback.isRemove;
                if(ticket.isSuccess) {
                    ticket.state = TicketState.PRE_CLOSE;
                    TicketState.PRE_CLOSE.nextState(ticket, new ArrayList<TicketStateSyncJob.OsMsg>(), new ArrayList<TicketStateSyncJob.OsResp>());
                    ticket.feedback.comment(String.format("Feedback 已经被删除(%s)", Dates.date2Date()));
                }
            }
        }

        // 3.
        if(ticket.feedback.isExpired()) {
            ticket.state = TicketState.PRE_CLOSE;
            ticket.memo = "Feedback 已经过期, 无法再处理, 请标记原因.\r\n" + ticket.memo;
        }
    }

    /**
     * 判断这个 Feedback 是否已经被 Remove
     *
     * @param body
     * @return
     */
    public static boolean isFeedbackRemove(String body) {
        String feedbackViewHtml = StringUtils.substringBetween(body, "newform");
        if(feedbackViewHtml == null) return false;
        feedbackViewHtml = feedbackViewHtml.toLowerCase();
        String[] removeFlag = new String[]{
                //TODO 如果有 fr 市场, 还需要添加发问的删除
                "bestellung wurde entfernt", // de: order was removed
                "order was removed", // uk: order was removed
                "orden fue eliminado" // fr: order was removed
        };
        for(String flag : removeFlag) {
            if(StringUtils.contains(feedbackViewHtml, flag))
                return true;
        }
        return false;
    }

    /**
     * 检查某一个 Feedback 此刻的状态, 由于 Feedback 不可更改, 所以只有下面几个情况
     * 1. 有了一个新的 Feedback
     * 2. 没有 Feedback
     * 3. Feedback 被删除了
     */
    public static String fetchAmazonFeedbackHtml(Feedback feedback) {
        String url = feedback.market.feedbackLink();
        Logger.info("Sync Feedback %s", feedback.orderId);
        String body = HTTP.post(feedback.account.cookieStore(), url, Arrays.asList(
                new BasicNameValuePair("action", "show-feedback"),
                new BasicNameValuePair("orderID", feedback.orderId),
                new BasicNameValuePair("applicationPath", "/gp/orders-v2")
        ));

        if(Play.mode.isDev())
            FLog.fileLog(String.format("feedback.check.%s.html", feedback.orderId), body, FLog.T.HTTP_ERROR);
        return body;
    }

}
