package jobs;

import helper.Dates;
import models.market.AmazonListingReview;
import models.market.Feedback;
import notifiers.SystemMails;
import org.joda.time.DateTime;
import play.jobs.Job;

import java.util.Date;
import java.util.List;

/**
 * 将昨天产生的 Feedback 进行邮件通知
 * User: wyattpan
 * Date: 8/23/12
 * Time: 12:18 PM
 */
public class FeedbackNotificationJob extends Job {
    @Override
    public void doJob() {
        Date yesterday = DateTime.now().minusDays(1).toDate();
        List<Feedback> feedbacks = Feedback.find("createDate>=? AND createDate<=? ORDER BY score",
                Dates.morning(yesterday), Dates.night(yesterday)).fetch();
        SystemMails.dailyFeedbackMail(feedbacks);


        List<AmazonListingReview> reviews = AmazonListingReview.find("reviewDate>=? AND reviewDate<=? ORDER BY rating",
                Dates.morning(yesterday), Dates.night(yesterday)).fetch();
        SystemMails.dailyReviewMail(reviews);
    }
}
