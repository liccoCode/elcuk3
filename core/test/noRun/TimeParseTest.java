package noRun;

import helper.Dates;
import models.market.Account;
import models.market.M;
import models.market.Selling;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import play.libs.IO;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/29/11
 * Time: 12:57 AM
 */
public class TimeParseTest {

    @Test
    public void testTimeParser() {
        System.out.println(DigestUtils.shaHex("sdfksjdf"));
        System.out.println("bff7406309f5b3537172efa46c1c70e5f76e84df");
        System.out.println("bff7406309f5b3537172efa46c1c70e5f76e84df".length());
        System.out.println(DigestUtils.md5Hex("sdfksjdf"));
        System.out.println("7701f76159896a7baf114e92064dcbd5");
        System.out.println("7701f76159896a7baf114e92064dcbd5".length());
        System.out.println("10HTCEVO3D-1900S,614444720150_amazon.de".length());
        System.out.println("68-MAGGLASS-3X75BG,B001OQOK5U_amazon.co.uk".length());
        Account acc = new Account();
        acc.id = 1l;
        System.out.println(Selling.sid("10HTCEVO3D-1900S,614444720150", M.AMAZON_UK, acc));
    }

    @Test
    public void TestMarket() {
        System.out.println(M.AMAZON_DE.toString());
    }

    @Test
    public void testParseMarket() {
        System.out.println(M.val("amazon_uk"));
    }

    @Test
    public void testFiles() {
        List<File> files = new ArrayList<File>(FileUtils.listFiles(new File("/Users/wyattpan/elcuk-data/2011/"), new String[]{"xml"}, true));
        for(File file : files) {
            System.out.println(":::" + file.getAbsolutePath());
        }
    }

    @Test
    public void testParse() {
        List<String> lines = IO.readLines(new File("/Users/wyattpan/elcuk2-data/2012/1/24/9968684844.csv"));
        lines.remove(0);
        for(String line : lines) {
            String[] args = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, "\t");
            System.out.println(Arrays.toString(args));
        }
    }

    @Test
    public void testParseDate() throws DatatypeConfigurationException {
        System.out.println(Dates.parseXMLGregorianDate("2012-01-23T10:33:33+00:00"));
    }

    @Test
    public void testFilesName() {
        List<File> files = new ArrayList<File>(FileUtils.listFiles(new File("/Users/wyattpan/elcuk-data/2012/back/"), new String[]{"xml", "csv"}, true));
        for(File f : files) {
            System.out.println(f.getName());
        }
    }

    @Test
    public void testJodeTime() {
        System.out.println("---------" + Instant.parse("2012-02-21").getMillis());
        Instant it = Instant.now();
        System.out.println(it.minus(Duration.standardDays(7)));
        System.out.println(it.minus(Duration.standardDays(7)).getMillis());
        System.out.println(new Date().getTime() - TimeUnit.DAYS.toMillis(7));
        System.out.println(Instant.parse("2012-01-29").getMillis());

        // ------
        System.out.println("------------------------");
        Instant it2 = Instant.now();
        DateTime day7Before = it2.minus(Duration.standardDays(7)).toDateTime();
        System.out.println(day7Before.year().get());
        System.out.println(day7Before.monthOfYear().get());
        System.out.println(day7Before.dayOfMonth().get());
        System.out.println(day7Before.toString("yyyy-MM-dd"));

        System.out.println("----------------------");
        System.out.println(it.toDateTime().toString("yyyy-MM-dd"));
        System.out.println(Instant.now().getMillis());
        System.out.println(Instant.parse("2012-02-05").toDateTime().toString("yyyy-MM-dd"));
        System.out.println(new DateTime(1328446988397l).toString("yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    public void testSign() {
        System.out.println(0x00f0);
        System.out.println(0x00ff);
        System.out.println(0x01f9);
        System.out.println(0x0000);

        System.out.println(0xff0f);
    }

    @Test
    public void testJodeDate() {
        DateTime dt = DateTime.now();
        System.out.println(DateTime.parse(String.format("%s-%s-%s", dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth())));
        System.out.println(dt.toString("yyyy.MM.dd_HH'h'"));
    }

    @Test
    public void testDate() {
        DateTime d1 = DateTime.now();
        String format = "yyyy-MM-dd HH:mm:ss";
        System.out.println(d1.plusDays(-12).toString(format));
        System.out.println(d1.plusDays(-46).toString(format));
    }

    @Test
    public void testDatePlus1() {
        DateTime dt = DateTime.parse(DateTime.now().plusDays(1).toString("yyyy-MM-dd"));
        System.out.println(dt.toString("yyyy-MM-dd HH:mm:ss"));
        System.out.println(dt.plusDays(-180).toString("yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    public void testCompareJSUTC() {
        // Date.UTC(2010, 0, 1) -> 1262304000000
        //2010-01-01 -> 1262275200000
//        Assert.assertEquals(DateTime.parse("2010-01-01").getMillis(), 1262304000000l);
        System.out.println("JS Data.UTC: " + Dates.date2DateTime(new DateTime(1262304000000l).toDate()));
        System.out.println("JS Data.UTC 2: " + Dates.date2DateTime(new DateTime(1333065600000l).toDate()));
        System.out.println("Java Date: " + Dates.date2DateTime(new DateTime(1262275200000l).toDate()));
        System.out.println(Dates.date2DateTime(new Date(1338348242273l)));
    }

    @Test
    public void testDateMorningAndNight() {
        Date now = new Date();
        System.out.println(Dates.morning(now));
        System.out.println(Dates.night(now));
    }

    @Test
    public void testParseChineseTime() {
        String time = "星期二, 五月 29, 2012";
        System.out.println(DateTime.now().toString("E, MMM dd, yyyy", Locale.CHINESE));
        DateTimeFormatter fmt = DateTimeFormat.forPattern("E, MMM dd, yyyy");
        System.out.println(DateTime.now().toString(fmt.withLocale(Locale.CHINESE)));
        System.out.println(DateTime.parse(time, fmt.withLocale(Locale.CHINESE)));
    }

    @Test
    public void testAmDatetime() {
        String time = "Jun 25, 2012 8:11 AM";
        System.out.println(DateTime.parse(time, DateTimeFormat.forPattern("MMM dd, yyyy hh:mm a")));
    }
}
