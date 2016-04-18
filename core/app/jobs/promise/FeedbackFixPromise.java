package jobs.promise;


import jobs.FeedbackCrawlJob;
import models.market.Account;
import models.market.Feedback;
import play.jobs.Job;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/9/12
 * Time: 6:09 PM
 * @deprecated
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
        return feedbacks;
    }
}
