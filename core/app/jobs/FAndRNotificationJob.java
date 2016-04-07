package jobs;

import helper.Dates;
import helper.LogUtils;
import helper.Webs;
import models.Jobex;
import models.market.AmazonListingReview;
import models.market.Feedback;
import notifiers.SystemMails;
import org.joda.time.DateTime;
import play.Play;
import play.jobs.Job;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 * 将昨天产生的 Feedback 进行邮件通知
 * 周期:
 * - 轮询周期: 1h
 * - Duration: 0 20 0 * * ?
 * </pre>
 * User: wyattpan
 * Date: 8/23/12
 * Time: 12:18 PM
 * @deprecated
 */
public class FAndRNotificationJob extends Job {
    @Override
    public void doJob() {
        long begin = System.currentTimeMillis();
        if(!Jobex.findByClassName(FAndRNotificationJob.class.getName()).isExcute()) return;
        Date yesterday = DateTime.now().minusDays(Play.mode.isDev() ? 10 : 1).toDate();
        List<Feedback> feedbacks = Feedback.find("createDate>=? ORDER BY score",
                Dates.morning(yesterday)).fetch();
        if(!SystemMails.dailyFeedbackMail(feedbacks)) {
            Webs.systemMail("Feedback Daily Mail send Error.", feedbacks.size() + " feedbacks.");
        }


        List<AmazonListingReview> reviews = AmazonListingReview
                .find("reviewDate>=? ORDER BY rating",
                        Dates.morning(yesterday)).fetch();
        if(!SystemMails.dailyReviewMail(reviews)) {
            Webs.systemMail("Review Daily Mail send Error.", reviews.size() + " reviews.");
        }
        if(LogUtils.isslow(System.currentTimeMillis() - begin,"FAndRNotificationJob")) {
            LogUtils.JOBLOG.info(String
                    .format("FAndRNotificationJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }

    /**
     * 将 Feedback 切分成 5 星, 5 份; View 中使用
     *
     * @param feedbacks
     * @return
     */
    public static F.T5<List<Feedback>, List<Feedback>, List<Feedback>, List<Feedback>, List<Feedback>> divFeedbackIntoFive(
            List<Feedback> feedbacks) {
        List<Feedback> one = new ArrayList<Feedback>();
        List<Feedback> two = new ArrayList<Feedback>();
        List<Feedback> three = new ArrayList<Feedback>();
        List<Feedback> four = new ArrayList<Feedback>();
        List<Feedback> five = new ArrayList<Feedback>();
        for(Feedback fe : feedbacks) {
            if(fe.score <= 1) one.add(fe);
            else if(fe.score <= 2) two.add(fe);
            else if(fe.score <= 3) three.add(fe);
            else if(fe.score <= 4) four.add(fe);
            else if(fe.score <= 5) five.add(fe);
        }
        return new F.T5<List<Feedback>, List<Feedback>, List<Feedback>, List<Feedback>, List<Feedback>>(
                one, two, three, four, five);
    }

    /**
     * 将 Review 切分成 5 星, 5 份; View 中使用
     *
     * @param reviews
     * @return
     */
    public static F.T5<List<AmazonListingReview>, List<AmazonListingReview>,
            List<AmazonListingReview>, List<AmazonListingReview>,
            List<AmazonListingReview>> divReviewInToFive(List<AmazonListingReview> reviews) {
        List<AmazonListingReview> one = new ArrayList<AmazonListingReview>();
        List<AmazonListingReview> two = new ArrayList<AmazonListingReview>();
        List<AmazonListingReview> three = new ArrayList<AmazonListingReview>();
        List<AmazonListingReview> four = new ArrayList<AmazonListingReview>();
        List<AmazonListingReview> five = new ArrayList<AmazonListingReview>();
        for(AmazonListingReview rv : reviews) {
            if(rv.rating <= 1) one.add(rv);
            else if(rv.rating <= 2) two.add(rv);
            else if(rv.rating <= 3) three.add(rv);
            else if(rv.rating <= 4) four.add(rv);
            else if(rv.rating <= 5) five.add(rv);
        }
        return new F.T5<List<AmazonListingReview>, List<AmazonListingReview>, List<AmazonListingReview>, List<AmazonListingReview>, List<AmazonListingReview>>(
                one, two, three, four, five
        );
    }
}
