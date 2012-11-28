package procure;

import models.procure.Shipment;
import models.procure.iExpress;
import org.joda.time.DateTime;
import org.junit.Test;
import play.Play;
import play.template2.IO;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/18/12
 * Time: 5:00 PM
 */
public class ShipmentTest extends UnitTest {
    //    @Test
    public void testUnits() {
        System.out.println(Shipment.id());
        DateTime dt = DateTime.now();
        System.out.println(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear()));
    }

    private Shipment findOne(Shipment.S state, iExpress express) {
        return Shipment.find("state=? AND internationExpress=?", state, express).first();
    }

    //TODO 注意由于 monitor 会修改 db, 所以需要准备一下测试数据;

    @Test
    public void testDHLShippingToClearance() {
        Shipment ship = findOne(Shipment.S.SHIPPING, iExpress.DHL);

        ship.iExpressHTML = IO.readContentAsString(Play.getFile("test/html/shippingToClearance.DHL.html"));
        ship.monitor();

        assertEquals(Shipment.S.CLEARANCE, ship.state);
        ship.state = Shipment.S.SHIPPING;
        ship.save();
    }

    @Test
    public void testDHLCLearanceToDone() {
        Shipment ship = findOne(Shipment.S.CLEARANCE, iExpress.DHL);

        ship.iExpressHTML = IO.readContentAsString(Play.getFile("test/html/clearanceToDone.DHL.html"));
        ship.monitor();

        assertEquals("2012-11-27 14:57", new DateTime(ship.arriveDate).toString("yyyy-MM-dd HH:mm"));
        assertEquals(Shipment.S.DONE, ship.state);

        // 已经完成运输, 判断清关也需要成功
        assertEquals(true, ship.internationExpress.isClearance(ship.iExpressHTML));

        ship.state = Shipment.S.CLEARANCE;
        ship.save();
    }

    @Test
    public void testFEDEXShippingToClearance() {
        Shipment ship = findOne(Shipment.S.SHIPPING, iExpress.FEDEX);

        ship.iExpressHTML = IO.readContentAsString(Play.getFile("test/html/shippingToClearance.FEDEX.html"));
        ship.monitor();

        assertEquals(Shipment.S.CLEARANCE, ship.state);
        ship.state = Shipment.S.SHIPPING;
        ship.save();
    }

    @Test
    public void testFEDEXClearanceToDone() {
        Shipment ship = findOne(Shipment.S.CLEARANCE, iExpress.FEDEX);

        ship.iExpressHTML = IO.readContentAsString(Play.getFile("test/html/clearanceToDone.FEDEX.html"));
        ship.monitor();

        assertEquals("2012-11-19 09:41", new DateTime(ship.arriveDate).toString("yyyy-MM-dd HH:mm"));
        assertEquals(Shipment.S.DONE, ship.state);


        // 已经完成运输, 判断清关也需要成功
        assertEquals(true, ship.internationExpress.isClearance(ship.iExpressHTML));
        ship.state = Shipment.S.CLEARANCE;
        ship.save();
    }

    @Test
    public void testUPSShippingToCleance() {
        Shipment ship = findOne(Shipment.S.SHIPPING, iExpress.UPS);

        ship.iExpressHTML = IO.readContentAsString(Play.getFile("test/html/shippingToClearance.UPS.html"));
        ship.monitor();

        assertEquals(Shipment.S.CLEARANCE, ship.state);
        ship.state = Shipment.S.SHIPPING;
        ship.save();
    }

    @Test
    public void testUPSClearanceToDone() {
        Shipment ship = findOne(Shipment.S.CLEARANCE, iExpress.UPS);
        ship.internationExpress = iExpress.UPS;

        ship.iExpressHTML = IO.readContentAsString(Play.getFile("test/html/clearanceToDone.UPS.html"));
        ship.monitor();

        assertEquals("2012-09-24 09:58", new DateTime(ship.arriveDate).toString("yyyy-MM-dd HH:mm"));
        assertEquals(Shipment.S.DONE, ship.state);


        // 已经完成运输, 判断清关也需要成功
        assertEquals(true, ship.internationExpress.isClearance(ship.iExpressHTML));
        ship.state = Shipment.S.CLEARANCE;
        ship.save();
    }

}
