package jobs;

import factory.FactoryBoy;
import helper.Dates;
import models.market.Account;
import models.market.Feedback;
import models.market.M;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Ignore;
import org.junit.Test;
import play.Play;
import play.libs.IO;
import play.test.UnitTest;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.internal.matchers.StringContains.containsString;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 6/3/13
 * Time: 11:10 AM
 */
public class FeedbackCrawlJobTest extends UnitTest {

    private Account loginDeSellercentral() {
        Account account = FactoryBoy.build(Account.class, "de");
        account.cookieStore().clear();
        account.loginAmazonSellerCenter();

        return account;
    }

    @Ignore("选择性测试")
    @Test
    public void testFetchFeedback() {
        Account account = loginDeSellercentral();

        String html = FeedbackCrawlJob.fetchFeedback(account, 1);
        Document doc = Jsoup.parse(html);
        Element table = doc.select("table").last();
        assertThat(table.select("tr").size(), is(51));
    }

    @Ignore("选择性测试")
    @Test
    public void testFetchFeedbackES() {
        Account account = loginDeSellercentral();

        account.changeRegion(M.AMAZON_ES);
        String html = FeedbackCrawlJob.fetchFeedback(account, 1);
        account.changeRegion(account.type);
        Document doc = Jsoup.parse(html);
        Element table = doc.select("table").last();
        assertThat(table.select("tr").size(), is(4));
    }


    @Ignore("选择性测试")
    @Test
    public void testFetchFeedbackIT() {
        Account account = loginDeSellercentral();

        account.changeRegion(M.AMAZON_IT);
        assertThat(M.AMAZON_IT.amid().name(), is("APJ6JRA9NG5V4"));
        String html = FeedbackCrawlJob.fetchFeedback(account, 1);
        account.changeRegion(account.type);
        Document doc = Jsoup.parse(html);
        Element table = doc.select("table").last();
        assertThat(table.select("tr").size(), is(4));
    }

    @Ignore("选择性测试")
    @Test
    public void testFetchFeedbackFR() {
        Account account = loginDeSellercentral();

        account.changeRegion(M.AMAZON_FR);
        String html = FeedbackCrawlJob.fetchFeedback(account, 1);
        account.changeRegion(account.type);
        Document doc = Jsoup.parse(html);
        Element table = doc.select("table").last();
        assertThat(table.select("tr").size(), is(11));
    }

    @Test
    public void testParseFeedBackFromHTML() {
        String html = IO.readContentAsString(Play.getFile("test/html/AMAZON_DE.id_2feedback_p1.html"));
        List<Feedback> feedbacks = FeedbackCrawlJob.parseFeedBackFromHTML(html);

        assertThat(feedbacks.size(), is(50));

        Feedback feedback = feedbacks.get(32);

        assertThat(Dates.date2Date(feedback.createDate), is("2013-06-02"));
        assertThat(feedback.market, is(M.AMAZON_DE));
        assertThat(feedback.score, is(2f));
        assertThat(feedback.feedback, is(containsString("Miserable")));
        assertThat(feedback.orderId, is("304-0788820-7260310"));
        assertThat(feedback.email, is("m1p4ylpcndrvt44@marketplace.amazon.de"));
    }

    @Test
    public void testParseFeedBackFromHTMLES() {
        String html = IO.readContentAsString(Play.getFile("test/html/AMAZON_DE.id_2feedback_p1_es.html"));
        List<Feedback> feedbacks = FeedbackCrawlJob.parseFeedBackFromHTML(html);
        assertThat(feedbacks.size(), is(3));

        Feedback feedback = feedbacks.get(0);

        assertThat(feedback.market, is(M.AMAZON_ES));
        assertThat(feedback.score, is(5f));
        assertThat(feedback.orderId, is("402-9566619-8795551"));
        assertThat(feedback.email, is("fxv72ykv1r041x8@marketplace.amazon.es"));
        assertThat(Dates.date2Date(feedback.createDate), is("2013-05-26"));
    }
}
