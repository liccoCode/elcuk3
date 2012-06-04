package caches;

import helper.Caches;
import helper.Dates;
import org.joda.time.DateTime;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/4/12
 * Time: 4:03 PM
 */
public class CachesTest extends UnitTest {
    @Test
    public void testCachesKey() {
        String msku = "MSKU";
        Date from = new DateTime().plusDays(-7).toDate();
        Date to = new DateTime().plusDays(-1).toDate();
        System.out.println(Caches.Q.cacheKey(msku, Dates.data2Date(from), Dates.data2Date(to)));
    }
}
