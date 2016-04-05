package jobs;

import helper.HTTP;
import helper.LogUtils;
import models.Jobex;
import models.market.Account;
import models.market.Feedback;
import org.apache.commons.lang.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import play.Logger;
import play.jobs.Job;

import java.util.Arrays;
import java.util.List;

/**
 * 检查 Feedback 是否被删除.
 * * 被 Amazon 划掉
 * * 被 Amazon 删除
 * * 被用户删除
 * 周期:
 * - 轮询周期: 5mn
 * - Duration: 40mn
 * </pre>
 * User: wyattpan
 * Date: 8/9/12
 * Time: 3:44 PM
 * @deprecated
 */
public class FeedbackCheckJob extends Job {
    @Override
    public void doJob() {
        long begin = System.currentTimeMillis();
        if(!Jobex.findByClassName(FeedbackCheckJob.class.getName()).isExcute()) return;
        List<Feedback> feedbacks = Feedback.find("isRemove=? AND createDate>=? ORDER BY updateAt ASC",
                false, DateTime.now().minusDays(70).toDate()).fetch(20);
        for(Feedback feedback : feedbacks) {
            FeedbackCheckJob.ajaxLoadFeedbackOnOrderDetailPage(feedback.account, feedback.orderId);
        }
        if(LogUtils.isslow(System.currentTimeMillis() - begin,"FeedbackCheckJob")) {
            LogUtils.JOBLOG
                    .info(String.format("FeedbackCheckJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }

    /**
     * 检查一次 Feedback
     *
     * @param orderId Feedback 与 Order 相关
     */
    public static Feedback check(String orderId) {
        return check(Feedback.<Feedback>findById(orderId));
    }

    /**
     * 检查一次 Feedback
     */
    public static Feedback check(Feedback feedback) {
        String html = FeedbackCheckJob.ajaxLoadFeedbackOnOrderDetailPage(feedback.account, feedback.orderId);
        if(FeedbackCheckJob.isRequestSuccess(html)) {
            feedback.isRemove = FeedbackCheckJob.isFeedbackRemove(html);
            feedback.save();
        } else {
            Logger.warn("FeedbackCheckJob check request is not valid.");
        }
        return feedback;
    }

    /**
     * 判断这个 Feedback 是否已经被 Remove
     *
     * @param body
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
    public static String ajaxLoadFeedbackOnOrderDetailPage(Account account, String orderId) {
        // 需要使用 Feedback 相关联的账户, 而非 Feedback 所在的市场. (出在历史的 uk 账号在 de 购买东西, 以后不会有这样的情况,但需要修复这个问题)
        String url = account.type.feedbackLink();
        Logger.info("Sync Feedback %s [%s]", url, orderId);

        return HTTP.post(account.cookieStore(), url, Arrays.asList(
                new BasicNameValuePair("action", "show-feedback"),
                new BasicNameValuePair("orderID", orderId),
                new BasicNameValuePair("applicationPath", "/gp/orders-v2")
        ));
    }

}
