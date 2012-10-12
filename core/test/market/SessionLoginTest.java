package market;

import jobs.FeedbackCrawlJob;
import jobs.FeedbackInfoFetchJob;
import jobs.KeepSessionJob;
import jobs.OrderInfoFetchJob;
import models.market.Feedback;
import models.market.Orderr;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/15/12
 * Time: 9:29 AM
 */
public class SessionLoginTest extends UnitTest {
    //    @Test
    public void testLoginTwice() throws InterruptedException {
        new KeepSessionJob().doJob();
        new FeedbackCrawlJob().doJob();
    }

    Feedback feedback = null;

    // 都是 de 的订单
    @Before
    public void Login() {
        feedback = Feedback.findById("303-8171136-0010717");
        feedback.account.loginAmazonSellerCenter();
    }

    @Test
    public void testFetchFeedback() {
        System.out.println("======================================================");
        // feedback 已经删除了的
        FeedbackInfoFetchJob.fetchAmazonFeedbackHtml(feedback.account, feedback.orderId);
        // 有新 feedback 的
        feedback = Feedback.findById("028-8330984-9689160");
        FeedbackInfoFetchJob.fetchAmazonFeedbackHtml(feedback.account, feedback.orderId);
        // 没有 feedback 的
        feedback = Feedback.<Feedback>findById("028-6451688-5682726");
        FeedbackInfoFetchJob.fetchAmazonFeedbackHtml(feedback.account, feedback.orderId);
    }

    @Test
    public void orderDetail() {
        // feedback 已经删除了的
        OrderInfoFetchJob.fetchOrderDetailHtml(Orderr.<Orderr>findById("303-8171136-0010717"));
        // 有新 feedback 的
        OrderInfoFetchJob.fetchOrderDetailHtml(Orderr.<Orderr>findById("028-8330984-9689160"));
        // 没有 feedback 的
        OrderInfoFetchJob.fetchOrderDetailHtml(Orderr.<Orderr>findById("028-6451688-5682726"));
    }
}
