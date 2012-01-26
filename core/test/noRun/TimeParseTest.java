package noRun;

import helper.Dates;
import models.market.Account;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import play.libs.IO;
import play.libs.Time;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/29/11
 * Time: 12:57 AM
 */
public class TimeParseTest {

    @Test
    public void testTimeParser() {
        System.out.println(Time.parseDuration("10j"));
    }

    @Test
    public void TestMarket() {
        System.out.println(Account.M.AMAZON_DE.toString());
    }

    @Test
    public void testParseMarket() {
        System.out.println(Account.M.val("amazon_uk"));
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
}
