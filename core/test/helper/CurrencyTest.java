package helper;

import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 3/29/13
 * Time: 5:02 PM
 */
public class CurrencyTest extends UnitTest {
    public static String html;

    @BeforeClass
    public static void visitPage() {
        html = Currency.bocRatesHtml();
    }

    @Test
    public void testUsRate() {
        assertEquals(6.1976, Currency.USD.rate(html), 0.2);
        assertEquals(9.3984, Currency.GBP.rate(html), 0.2);
        assertEquals(7.9309, Currency.EUR.rate(html), 0.2);
        assertEquals(0.78309, Currency.HKD.rate(html), 0.2);
    }

    @Test
    public void testRateDateTime() {
        String actual = Dates.date2Date(Currency.rateDateTime(html));
        String expect = Dates.date2Date(DateTime.now().toDate());
        assertEquals(expect, actual);
    }

    @Test
    public void testSubList() {
        List<String> list = new ArrayList<String>();
        for(int i = 0; i < 20; i++) {
            list.add("index: " + i);
        }

        List<String> subList = list.subList(0, 10);
        System.out.println(subList);
        System.out.println(list);
        subList.clear();
        System.out.println(list);
    }
}
