package noRun;

import models.market.Account;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import play.libs.Time;

import java.io.File;
import java.util.ArrayList;
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
        System.out.println(Time.parseDuration("1"));
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
}
