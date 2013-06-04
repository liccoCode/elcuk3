package jobs.promise;

import jobs.AmazonFinancePatchJob;
import models.market.Account;
import org.joda.time.DateTime;
import org.junit.Test;
import play.Play;
import play.template2.IO;
import play.test.UnitTest;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 11/24/12
 * Time: 8:15 PM
 */
public class FinanceCrawlJobTest extends UnitTest {
    String deUrl = "https://sellercentral.amazon.de/gp/reports/documents/_GET_V2_SETTLEMENT_REPORT_DATA__15836299764.txt?ie=UTF8&contentType=text%2Fxls";
    String ukUrl = "https://sellercentral.amazon.co.uk/gp/reports/documents/_GET_V2_SETTLEMENT_REPORT_DATA__15847303604.txt?ie=UTF8&contentType=text%2Fxls";
    String usUrl = "https://sellercentral.amazon.com/gp/reports/documents/_GET_V2_SETTLEMENT_REPORT_DATA__8226396183.txt?ie=UTF8&contentType=text%2Fxls";

    @Test
    public void testPickReportUrlDe() {
        Account acc = Account.findById(2l);
        AmazonFinancePatchJob worker = testPickReportUrlBase(Play.getFile("test/html/settlements.de.html"), acc, deUrl);
        assertEquals("15836299764.txt", worker.fileName());
    }

    @Test
    public void testPickReportUrlUk() {
        Account acc = Account.findById(1l);
        AmazonFinancePatchJob worker = testPickReportUrlBase(Play.getFile("test/html/settlements.uk.html"), acc, ukUrl);
        assertEquals("15847303604.txt", worker.fileName());
    }

    @Test
    public void testPickReportUrlUs() {
        Account acc = Account.findById(131l);
        AmazonFinancePatchJob worker = testPickReportUrlBase(Play.getFile("test/html/settlements.us.html"), acc, usUrl);
        assertEquals("8226396183.txt", worker.fileName());
    }

    public AmazonFinancePatchJob testPickReportUrlBase(File file, Account acc, String eUrl) {
        String html = IO.readContentAsString(file);
        AmazonFinancePatchJob worker = new AmazonFinancePatchJob(acc, DateTime.parse("2012-11-14").toDate());
        String url = worker.pickReportUrl(html);
        assertEquals(eUrl, url);

        worker = new AmazonFinancePatchJob(acc, DateTime.parse("2012-11-09").toDate());
        url = worker.pickReportUrl(html);
        assertEquals("", url);
        return worker;
    }

}
