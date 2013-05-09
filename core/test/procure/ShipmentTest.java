package procure;

import helper.Dates;
import models.procure.iExpress;
import org.joda.time.DateTime;
import org.junit.Test;
import play.Play;
import play.libs.F;
import play.template2.IO;
import play.test.UnitTest;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/18/12
 * Time: 5:00 PM
 */
public class ShipmentTest extends UnitTest {

    @Test
    public void testDHLisClearance() {
        String file = IO.readContentAsString(Play.getFile("test/html/track.dhl.html"));

        F.T2<Boolean, DateTime> flag = iExpress.DHL
                .isClearance(iExpress.DHL.parseExpress(file, "8259536213"));

        assertEquals(true, flag._1);
        assertEquals("2013-03-22 01:17:00", Dates.date2DateTime(flag._2));
    }

    @Test
    public void testDHLisDelivered() {
        String file = IO.readContentAsString(Play.getFile("test/html/track.dhl.html"));

        F.T2<Boolean, DateTime> flag = iExpress.DHL
                .isDelivered(iExpress.DHL.parseExpress(file, "8259536213"));

        assertEquals(true, flag._1);
        assertEquals("2013-03-22 14:57:00", Dates.date2DateTime(flag._2));
    }

    @Test
    public void testDHLisReceipt() {
        String file = IO.readContentAsString(Play.getFile("test/html/track.dhl.html"));

        F.T2<Boolean, DateTime> flag = iExpress.DHL
                .isReceipt(iExpress.DHL.parseExpress(file, "8259536213"));

        assertEquals(true, flag._1);
        assertEquals("2013-03-22 16:27:00", Dates.date2DateTime(flag._2));
    }

    @Test
    public void testFedexisClearance() {
        String file = IO.readContentAsString(Play.getFile("test/html/track.fedex.html"));
        F.T2<Boolean, DateTime> flag = iExpress.FEDEX
                .isClearance(iExpress.FEDEX.parseExpress(file, "802100421382"));

        assertEquals(true, flag._1);
        assertEquals("2013-03-25 22:41:00", Dates.date2DateTime(flag._2));
    }

    @Test
    public void testFedexisDelivered() {
        String file = IO.readContentAsString(Play.getFile("test/html/track.fedex.html"));
        F.T2<Boolean, DateTime> flag = iExpress.FEDEX
                .isDelivered(iExpress.FEDEX.parseExpress(file, "802100421382"));

        assertEquals(true, flag._1);
        assertEquals("2013-03-27 08:08:00", Dates.date2DateTime(flag._2));
    }

    @Test
    public void testFedexisReceipt() {
        String file = IO.readContentAsString(Play.getFile("test/html/track.fedex.html"));
        F.T2<Boolean, DateTime> flag = iExpress.FEDEX
                .isReceipt(iExpress.FEDEX.parseExpress(file, "802100421382"));

        assertEquals(true, flag._1);
        assertEquals("2013-03-27 10:14:00", Dates.date2DateTime(flag._2));
    }

    @Test
    public void testUPSisClearance() {
        String file = IO.readContentAsString(Play.getFile("test/html/track.ups.html"));
        F.T2<Boolean, DateTime> flag = iExpress.UPS
                .isClearance(iExpress.UPS.parseExpress(file, "1Z936E3E0441531828"));

        assertEquals(true, flag._1);
        assertEquals("2013-01-02 23:28:00", Dates.date2DateTime(flag._2));
    }

    @Test
    public void testUPSisDelivered() {
        String file = IO.readContentAsString(Play.getFile("test/html/track.ups.html"));
        F.T2<Boolean, DateTime> flag = iExpress.UPS
                .isDelivered(iExpress.UPS.parseExpress(file, "1Z936E3E0441531828"));

        assertEquals(false, flag._1);
        assertEquals(Dates.date2DateTime(new Date()), Dates.date2DateTime(flag._2));
    }

    @Test
    public void testUPSisReceipt() {
        String file = IO.readContentAsString(Play.getFile("test/html/track.ups.html"));
        F.T2<Boolean, DateTime> flag = iExpress.UPS
                .isReceipt(iExpress.UPS.parseExpress(file, "1Z936E3E0441531828"));

        assertEquals(true, flag._1);
        assertEquals("2013-01-03 10:02:00", Dates.date2DateTime(flag._2));
    }

}
