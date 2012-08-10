package jobs;

import helper.Dates;
import helper.FLog;
import helper.HTTP;
import models.market.Feedback;
import models.support.Ticket;
import models.support.TicketState;
import org.apache.commons.lang.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.jobs.Job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 用来补充性质的更新已经在系统中的 Feedback 的信息;
 * 会加载所有有 OsTicketId 的 Feedback , 然后对她们进行更新:
 * 1. 如果发现 Feedback 消失了, 则记录此 Feedback 被处理完了
 * 2. 同时可以更新 Feedback 记录的最新时间(包含时分秒)
 * User: wyattpan
 * Date: 8/9/12
 * Time: 3:44 PM
 */
public class FeedbackInfoFetchJob extends Job {
    @Override
    public void doJob() {
        DateTime now = DateTime.now();
        // 处理 2 个月内的所有订单, 每次更新 50
        int size = 30;
        if(Play.mode.isDev()) size = 10;
        List<Ticket> tickets = Ticket.find("type=? AND isSuccess=? AND createAt>=? AND createAt<=? ORDER BY lastSyncTime",
                Ticket.T.FEEDBACK, false, Dates.morning(now.minusDays(60).toDate()), Dates.night(now.toDate())).fetch(size);
        Logger.info("FeedbackInfoFetchJob to Amazon sync %s tickets.", tickets.size());
        for(Ticket ticket : tickets) {
            String html = FeedbackInfoFetchJob.fetchAmazonFeedbackHtml(ticket.feedback);
            ticket.isSuccess = FeedbackInfoFetchJob.isFeedbackRemove(html);
            if(ticket.isSuccess) {
                ticket.state = TicketState.PRE_CLOSE;
                TicketState.PRE_CLOSE.nextState(ticket, new ArrayList<TicketStateSyncJob.OsMsg>(), new ArrayList<TicketStateSyncJob.OsResp>());
            }
            ticket.lastSyncTime = new Date();
            ticket.save();
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
