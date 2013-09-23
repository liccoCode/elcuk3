package jobs;

import factory.FactoryBoy;
import helper.Dates;
import helper.Webs;
import models.market.Account;
import models.market.Feedback;
import models.market.M;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.Play;
import play.libs.IO;
import play.test.UnitTest;

import java.io.IOException;
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

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
    }

    private Account loginDeSellercentral() throws IOException, ClassNotFoundException {
        Account account = FactoryBoy.create(Account.class, "de");
        Webs.dev_login(account);
        return account;
    }

    @Ignore("选择性测试")
    @Test
    public void testFetchFeedbackES() throws IOException, ClassNotFoundException {
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
    public void testFetchFeedbackIT() throws IOException, ClassNotFoundException {
        Account account = loginDeSellercentral();

        account.changeRegion(M.AMAZON_IT);
        String html = FeedbackCrawlJob.fetchFeedback(account, 1);
        account.changeRegion(account.type);
        Document doc = Jsoup.parse(html);
        Element table = doc.select("table").last();
        assertThat(table.select("tr").size(), is(51));
        List<Feedback> feedbacks = FeedbackCrawlJob.parseFeedBackFromHTML(html);
        for(Feedback f : feedbacks) {
            assertThat(f.email, is(containsString("marketplace.amazon.it")));
        }
    }

    @Ignore("选择性测试")
    @Test
    public void testFetchFeedbackFR() throws IOException, ClassNotFoundException {
        Account account = loginDeSellercentral();

        account.changeRegion(M.AMAZON_FR);
        String html = FeedbackCrawlJob.fetchFeedback(account, 1);
        account.changeRegion(account.type);
        Document doc = Jsoup.parse(html);
        Element table = doc.select("table").last();
        assertThat(table.select("tr").size(), is(11));
    }

    @Test
    public void testParseFeedBackFromHTMLUK() {
        String html = IO.readContentAsString(Play.getFile("test/jobs/feedback_uk.1.html"));
        List<Feedback> feedbacks = FeedbackCrawlJob.parseFeedBackFromHTML(html);

        assertThat(feedbacks.size(), is(50));

        Feedback feedback = feedbacks.get(8);

        assertThat(feedback.market, is(M.AMAZON_UK));
        assertThat(feedback.score, is(3f));
        assertThat(feedback.feedback, is(containsString("Didn't state that the battery didn't come with")));
        assertThat(feedback.orderId, is("026-3994958-5082728"));
        assertThat(feedback.email, is("rcn4xzp2wq4g839@marketplace.amazon.co.uk"));
        assertThat(Dates.date2Date(feedback.createDate), is("2013-09-22"));
        assertThat(feedback.isRemove, is(true));
    }

    @Test
    public void testParseFeedBackFromHTMLDE() {
        String html = IO.readContentAsString(Play.getFile("test/jobs/feedback_de.2.html"));
        List<Feedback> feedbacks = FeedbackCrawlJob.parseFeedBackFromHTML(html);

        assertThat(feedbacks.size(), is(50));

        Feedback feedback = feedbacks.get(33);

        assertThat(feedback.market, is(M.AMAZON_DE));
        assertThat(feedback.score, is(2f));
        assertThat(feedback.feedback, is(containsString("Powerbank ist prima")));
        assertThat(feedback.orderId, is("304-8159162-8832356"));
        assertThat(feedback.email, is("f1yzcp4t6vfl6fn@marketplace.amazon.de"));
        assertThat(Dates.date2Date(feedback.createDate), is("2013-09-22"));
        assertThat(feedback.isRemove, is(false));
    }

    @Test
    public void testParseFeedBackFromHTMLFR() {
        String html = IO.readContentAsString(Play.getFile("test/jobs/feedback_fr.2.html"));
        List<Feedback> feedbacks = FeedbackCrawlJob.parseFeedBackFromHTML(html);

        assertThat(feedbacks.size(), is(50));

        Feedback feedback = feedbacks.get(9);

        assertThat(feedback.market, is(M.AMAZON_FR));
        assertThat(feedback.score, is(1f));
        assertThat(feedback.feedback, is(containsString("la poste, sauf que")));
        assertThat(feedback.orderId, is("403-1023276-1477917"));
        assertThat(feedback.email, is("tg5n0mf82l6qqd9@marketplace.amazon.fr"));
        assertThat(Dates.date2Date(feedback.createDate), is("2013-09-19"));
        assertThat(feedback.isRemove, is(true));
    }

    @Test
    public void testParseFeedBackFromHTMLIT() {
        String html = IO.readContentAsString(Play.getFile("test/jobs/feedback_it.2.html"));
        List<Feedback> feedbacks = FeedbackCrawlJob.parseFeedBackFromHTML(html);

        assertThat(feedbacks.size(), is(50));

        Feedback feedback = feedbacks.get(7);

        assertThat(feedback.market, is(M.AMAZON_IT));
        assertThat(feedback.score, is(2f));
        assertThat(feedback.feedback, is(containsString("SPIEGAZIONI E PER TIPO")));
        assertThat(feedback.orderId, is("402-7215246-1836338"));
        assertThat(feedback.email, is("n7ct87j17kh9c5n@marketplace.amazon.it"));
        assertThat(Dates.date2Date(feedback.createDate), is("2013-09-22"));
        assertThat(feedback.isRemove, is(false));
    }

    @Test
    public void testParseFeedBackFromHTMLES() {
        String html = IO.readContentAsString(Play.getFile("test/jobs/feedback_es.2.html"));
        List<Feedback> feedbacks = FeedbackCrawlJob.parseFeedBackFromHTML(html);
        assertThat(feedbacks.size(), is(50));

        Feedback feedback = feedbacks.get(11);

        assertThat(feedback.market, is(M.AMAZON_ES));
        assertThat(feedback.score, is(1f));
        assertThat(feedback.feedback, is(containsString("contactado conmigo el transportista")));
        assertThat(feedback.orderId, is("403-1302069-0276358"));
        assertThat(feedback.email, is("lxms17c8f2lw0v6@marketplace.amazon.es"));
        assertThat(Dates.date2Date(feedback.createDate), is("2013-09-20"));
        assertThat(feedback.isRemove, is(true));
    }

    @Test
    public void testParseFeedBackFromHTMLUS() {
        String html = IO.readContentAsString(Play.getFile("test/jobs/feedback_us.131.html"));
        List<Feedback> feedbacks = FeedbackCrawlJob.parseFeedBackFromHTML(html);
        assertThat(feedbacks.size(), is(50));

        Feedback feedback = feedbacks.get(46);

        assertThat(feedback.market, is(M.AMAZON_US));
        assertThat(feedback.score, is(1f));
        assertThat(feedback.feedback, is(containsString("pouch to carry this product")));
        assertThat(feedback.orderId, is("108-8665486-4620268"));
        assertThat(feedback.email, is("t84hk24d89hc12f@marketplace.amazon.com"));
        assertThat(Dates.date2Date(feedback.createDate), is("2013-09-19"));
        assertThat(feedback.isRemove, is(false));
    }
}
