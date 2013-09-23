package jobs;

import helper.FLog;
import helper.HTTP;
import models.Jobex;
import models.market.Account;
import org.apache.commons.lang.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import play.Logger;
import play.Play;
import play.jobs.Job;

import java.util.Arrays;

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
public class FeedbackInfoFetchJob extends Job {
    @Override
    public void doJob() {
        if(!Jobex.findByClassName(FeedbackInfoFetchJob.class.getName()).isExcute()) return;
        // TODO 检查最近 90 天的 Feedback , 用于检查是否删除.
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
                "bestellung wurde entfernt", // de: order was removed
                "order was removed", // uk/us: order was removed
                "orden fue eliminado", // fr: order was removed
                "Se ha eliminado la", // es: Removed
                "stato rimosso" // it: been removed
        };
        for(String flag : removeFlag) {
            if(StringUtils.contains(feedbackViewHtml, flag))
                return true;
        }
        return false;
    }

    /**
     * 检查返回的内容, 判断请求是否成功.
     *
     * @param body
     * @return
     */
    public static boolean isRequestSuccess(String body) {
        //<status>login</status>
        //<status>success</status>
        return StringUtils.contains(StringUtils.substringBetween(body, "<status>", "</status>"), "success");
    }

    /**
     * 检查某一个 Feedback 此刻的状态, 由于 Feedback 不可更改, 所以只有下面几个情况
     * 1. 有了一个新的 Feedback
     * 2. 没有 Feedback
     * 3. Feedback 被删除了
     */
    public static String fetchAmazonFeedbackHtml(Account account, String orderId) {
        // 需要使用 Feedback 相关联的账户, 而非 Feedback 所在的市场. (出在历史的 uk 账号在 de 购买东西, 以后不会有这样的情况,但需要修复这个问题)
        String url = account.type.feedbackLink();
        Logger.info("Sync Feedback %s [%s]", url, orderId);
        String body = HTTP.post(account.cookieStore(), url, Arrays.asList(
                new BasicNameValuePair("action", "show-feedback"),
                new BasicNameValuePair("orderID", orderId),
                new BasicNameValuePair("applicationPath", "/gp/orders-v2")
        ));

        if(Play.mode.isDev())
            FLog.fileLog(String.format("feedback.check.%s.%s.html", account.prettyName(), orderId),
                    body, FLog.T.HTTP_ERROR);
        return body;
    }

}
