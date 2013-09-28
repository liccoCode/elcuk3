package jobs;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import helper.Webs;
import models.market.Account;
import models.market.Feedback;
import models.market.M;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.io.IOException;

import static org.hamcrest.Matchers.is;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 9/26/13
 * Time: 11:09 AM
 */
public class FeedbackCheckJobTest extends UnitTest {

    @Before
    public void setUP() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testFetchAmazonFeedbackHtmlES() throws IOException, ClassNotFoundException {
        Account account = FactoryBoy.create(Account.class, "de");
        Webs.dev_login(account);

        Feedback feedback = FactoryBoy.create(Feedback.class, new BuildCallback<Feedback>() {
            @Override
            public void build(Feedback target) {
                target.orderId = "404-5990960-2930737";
                target.market = M.AMAZON_ES;
            }
        });

        String body = FeedbackCheckJob.ajaxLoadFeedbackOnOrderDetailPage(account, feedback.orderId);
        assertThat(FeedbackCheckJob.isRequestSuccess(body), is(true));

        assertThat(FeedbackCheckJob.isFeedbackRemove(body), is(false));
    }

    @Test
    public void testFetchAmazonFeedbackHtmlIT() throws IOException, ClassNotFoundException {
        Account account = FactoryBoy.create(Account.class, "de");
        Webs.dev_login(account);

        Feedback feedback = FactoryBoy.create(Feedback.class, new BuildCallback<Feedback>() {
            @Override
            public void build(Feedback target) {
                target.orderId = "171-4054153-2266756";
                target.market = M.AMAZON_IT;
            }
        });

        String body = FeedbackCheckJob.ajaxLoadFeedbackOnOrderDetailPage(account, feedback.orderId);
        assertThat(FeedbackCheckJob.isRequestSuccess(body), is(true));

        assertThat(FeedbackCheckJob.isFeedbackRemove(body), is(false));
        //171-4054153-2266756
    }

    @Test
    public void testCheck() throws IOException, ClassNotFoundException {
        Account account = FactoryBoy.create(Account.class, "de");
        Webs.dev_login(account);

        Feedback feedback = FactoryBoy.create(Feedback.class, new BuildCallback<Feedback>() {
            @Override
            public void build(Feedback target) {
                target.orderId = "402-0723977-5880316";
                target.market = M.AMAZON_FR;
            }
        });
        Feedback f = FeedbackCheckJob.check(feedback);

        assertThat(f.isRemove, is(false));
        assertThat(f.isPersistent(), is(true));
    }
}
