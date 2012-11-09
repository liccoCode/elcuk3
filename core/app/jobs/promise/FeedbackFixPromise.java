package jobs.promise;

import controllers.Notifications;
import jobs.FeedbackCrawlJob;
import models.Notification;
import models.market.Account;
import models.market.Feedback;
import play.jobs.Job;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/9/12
 * Time: 6:09 PM
 */
public class FeedbackFixPromise extends Job<List<Feedback>> {
    private long aid;
    private int page;

    public FeedbackFixPromise(long aid, int page) {
        this.aid = aid;
        this.page = page;
    }

    @Override
    public List<Feedback> doJobWithResult() {
        long begin = System.currentTimeMillis();
        Account acc = Account.findById(this.aid);
        List<Feedback> feedbacks = FeedbackCrawlJob.fetchAccountFeedbackOnePage(acc, acc.type, this.page);
        Notification.notifies(String.format("更新 Account %s 的第 %s 页,共 %s 个 Feedback, 耗时: %s s",
                acc.prettyName(), page, feedbacks.size(), (System.currentTimeMillis() - begin) / 1000),
                Notification.SERVICE);
        return feedbacks;
    }
}
