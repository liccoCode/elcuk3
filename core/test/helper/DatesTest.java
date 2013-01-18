package helper;

import models.market.M;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/18/13
 * Time: 11:52 AM
 */
public class DatesTest extends UnitTest {

    @Test
    public void UTCTimeZone() {
        String utc = "2013-01-14T07:56:16+00:00";
        // CST = UTC + 8:00
        assertEquals("2013-01-14 15:56:16", Dates.date2DateTime(Dates.parseXMLGregorianDate(utc)));
    }

    @Test
    public void usTimeZone() {
        // 2012-11-30 00:00:00
        String us = "November 30, 2012";
        // +16
        assertEquals("2012-11-30 16:00:00",
                Dates.date2DateTime(Dates.transactionDate(M.AMAZON_US, us)));

    }
}
