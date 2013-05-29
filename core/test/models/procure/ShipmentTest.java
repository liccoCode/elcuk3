package models.procure;

import factory.FactoryBoy;
import helper.Dates;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.libs.F;
import play.libs.IO;
import play.test.UnitTest;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/29/13
 * Time: 6:37 PM
 */
public class ShipmentTest extends UnitTest {
    String dhlFile = IO.readContentAsString(Play.getFile("test/html/track.dhl.html"));
    String fedexFile = IO.readContentAsString(Play.getFile("test/html/track.fedex.html"));
    String upsFile = IO.readContentAsString(Play.getFile("test/html/track.ups.html"));

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testDHLisClearance() {
        F.T2<Boolean, DateTime> flag = iExpress.DHL
                .isClearance(iExpress.DHL.parseExpress(dhlFile, "8259536213"));

        assertEquals(true, flag._1);
        assertEquals("2013-03-22 01:17:00", Dates.date2DateTime(flag._2));
    }

    @Test
    public void testDHLisDelivered() {
        F.T2<Boolean, DateTime> flag = iExpress.DHL
                .isDelivered(iExpress.DHL.parseExpress(dhlFile, "8259536213"));

        assertEquals(true, flag._1);
        assertEquals("2013-03-22 14:57:00", Dates.date2DateTime(flag._2));
    }

    @Test
    public void testDHLisReceipt() {
        F.T2<Boolean, DateTime> flag = iExpress.DHL
                .isReceipt(iExpress.DHL.parseExpress(dhlFile, "8259536213"));

        assertEquals(true, flag._1);
        assertEquals("2013-03-22 16:27:00", Dates.date2DateTime(flag._2));
    }

    @Test
    public void testFedexisClearance() {
        F.T2<Boolean, DateTime> flag = iExpress.FEDEX
                .isClearance(iExpress.FEDEX.parseExpress(fedexFile, "802100421382"));

        assertEquals(true, flag._1);
        assertEquals("2013-03-25 22:41:00", Dates.date2DateTime(flag._2));
    }

    @Test
    public void testFedexisDelivered() {
        F.T2<Boolean, DateTime> flag = iExpress.FEDEX
                .isDelivered(iExpress.FEDEX.parseExpress(fedexFile, "802100421382"));

        assertEquals(true, flag._1);
        assertEquals("2013-03-27 08:08:00", Dates.date2DateTime(flag._2));
    }

    @Test
    public void testFedexisReceipt() {
        F.T2<Boolean, DateTime> flag = iExpress.FEDEX
                .isReceipt(iExpress.FEDEX.parseExpress(fedexFile, "802100421382"));

        assertEquals(true, flag._1);
        assertEquals("2013-03-27 10:14:00", Dates.date2DateTime(flag._2));
    }

    @Test
    public void testUPSisClearance() {
        F.T2<Boolean, DateTime> flag = iExpress.UPS
                .isClearance(iExpress.UPS.parseExpress(upsFile, "1Z936E3E0441531828"));

        assertEquals(true, flag._1);
        assertEquals("2013-01-02 23:28:00", Dates.date2DateTime(flag._2));
    }

    @Test
    public void testUPSisDelivered() {
        F.T2<Boolean, DateTime> flag = iExpress.UPS
                .isDelivered(iExpress.UPS.parseExpress(upsFile, "1Z936E3E0441531828"));

//        assertEquals(false, flag._1);
//        assertEquals(Dates.date2DateTime(new Date()), Dates.date2DateTime(flag._2));
        assertEquals(true, flag._1);
        assertEquals("2013-01-03 10:02:00", Dates.date2DateTime(flag._2));
    }

    @Test
    public void testUPSisReceipt() {
        F.T2<Boolean, DateTime> flag = iExpress.UPS
                .isReceipt(iExpress.UPS.parseExpress(upsFile, "1Z936E3E0441531828"));

        assertEquals(true, flag._1);
        assertEquals("2013-01-03 10:02:00", Dates.date2DateTime(flag._2));
    }


    @Test
    public void testDHLMonitor() {
        Shipment shipment = FactoryBoy.create(Shipment.class);
        shipment.internationExpress = iExpress.DHL;

        shipment.state = Shipment.S.SHIPPING;
        shipment.iExpressHTML = shipment.internationExpress.parseExpress(dhlFile, "8259536213");

        // SHIPPING -> CLEARANCE
        shipment.monitor();
        assertEquals(Shipment.S.CLEARANCE, shipment.state);
        assertEquals("2013-03-22 01:17:00", Dates.date2DateTime(shipment.dates.atPortDate));
        // CLEARANCE -> DELIVERYING
        shipment.monitor();
        assertEquals(Shipment.S.DELIVERYING, shipment.state);
        assertEquals("2013-03-22 14:57:00", Dates.date2DateTime(shipment.dates.deliverDate));
        // DELIVERYING -> RECEIPTD
        shipment.monitor();
        assertEquals(Shipment.S.RECEIPTD, shipment.state);
        assertEquals("2013-03-22 16:27:00", Dates.date2DateTime(shipment.dates.receiptDate));
    }

    @Test
    public void testFedexMonitor() {
        Shipment shipment = FactoryBoy.create(Shipment.class);
        shipment.internationExpress = iExpress.FEDEX;

        shipment.state = Shipment.S.SHIPPING;
        shipment.iExpressHTML = shipment.internationExpress.parseExpress(fedexFile, "802100421382");

        // SHIPPING -> CLEARANCE
        shipment.monitor();
        assertEquals(Shipment.S.CLEARANCE, shipment.state);
        assertEquals("2013-03-25 22:41:00", Dates.date2DateTime(shipment.dates.atPortDate));
        // CLEARANCE -> DELIVERYING
        shipment.monitor();
        assertEquals(Shipment.S.DELIVERYING, shipment.state);
        assertEquals("2013-03-27 08:08:00", Dates.date2DateTime(shipment.dates.deliverDate));
        // DELIVERYING -> RECEIPTD
        shipment.monitor();
        assertEquals(Shipment.S.RECEIPTD, shipment.state);
        assertEquals("2013-03-27 10:14:00", Dates.date2DateTime(shipment.dates.receiptDate));
    }

    @Test
    public void testUPSMonitor() {
        Shipment shipment = FactoryBoy.create(Shipment.class);
        shipment.internationExpress = iExpress.UPS;

        shipment.state = Shipment.S.SHIPPING;
        shipment.iExpressHTML = shipment.internationExpress.parseExpress(upsFile,
                "1Z936E3E0441531828");

        // SHIPPING -> CLEARANCE
        shipment.monitor();
        assertEquals(Shipment.S.CLEARANCE, shipment.state);
        assertEquals("2013-01-02 23:28:00", Dates.date2DateTime(shipment.dates.atPortDate));
        // CLEARANCE -> DELIVERYING
        shipment.monitor();
        assertEquals(Shipment.S.DELIVERYING, shipment.state);
        assertEquals("2013-01-03 10:02:00", Dates.date2DateTime(shipment.dates.deliverDate));
        // DELIVERYING -> RECEIPTD
        shipment.monitor();
        assertEquals(Shipment.S.RECEIPTD, shipment.state);
        assertEquals("2013-01-03 10:02:00", Dates.date2DateTime(shipment.dates.receiptDate));

        FBAShipment fbaShipment = new FBAShipment();
        fbaShipment.state = FBAShipment.S.RECEIVING;
        fbaShipment.records = "2013-05-25T22:00:00+00:00\tX0005LTYP9\t80DBK10000-B,652862208225\t18\tFBA8Q0ZXS\tLEJ1\n" +
                "2013-05-24T22:00:00+00:00\tX0004J3JDP\t80DBK12000-AB,669974689736\t18\tFBA8Q0ZXS\tLEJ1\n" +
                "2013-05-24T22:00:00+00:00\tX0004J3JDP\t80DBK12000-AB,669974689736\t18\tFBA8Q0ZXS\tLEJ1\n" +
                "2013-05-23T22:00:00+00:00\tX0004J3JDP\t80DBK12000-AB,669974689736\t18\tFBA8Q0ZXS\tLEJ1\n" +
                "2013-05-23T22:00:00+00:00\tX0004J3JDP\t80DBK12000-AB,669974689736\t18\tFBA8Q0ZXS\tLEJ1\n" +
                "2013-05-23T22:00:00+00:00\tX0004J3JDP\t80DBK12000-AB,669974689736\t18\tFBA8Q0ZXS\tLEJ1\n" +
                "2013-05-23T22:00:00+00:00\tX0004J3JDP\t80DBK12000-AB,669974689736\t18\tFBA8Q0ZXS\tLEJ1\n" +
                "2013-05-23T22:00:00+00:00\tX0004J3JDP\t80DBK12000-AB,669974689736\t36\tFBA8Q0ZXS\tLEJ1\n" +
                "2013-05-23T22:00:00+00:00\tX0004J3JDP\t80DBK12000-AB,669974689736\t18\tFBA8Q0ZXS\tLEJ1\n" +
                "2013-05-23T22:00:00+00:00\tX0004J3JDP\t80DBK12000-AB,669974689736\t18\tFBA8Q0ZXS\tLEJ1";
        shipment = spy(shipment);
        when(shipment.fbas()).thenReturn(Arrays.asList(fbaShipment));
        doNothing().when(shipment)._save();
        shipment.monitor();
        assertThat(shipment.state, is(Shipment.S.RECEIVING));
        assertThat(shipment.dates.inbondDate, is(fbaShipment.getEarliestDate().get()));
    }
}
