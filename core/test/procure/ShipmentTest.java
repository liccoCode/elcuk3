package procure;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import models.procure.Shipment;
import models.procure.iExpress;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import play.Logger;
import play.Play;
import play.template2.IO;
import play.test.UnitTest;
import sun.io.ByteToCharConverter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Date;

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

    @Test
    public void testTrackWebSite() {
        //Shipment shipment = Shipment.findById("SP|201209|09");
        //String html = shipment.internationExpress.fetchStateHTML("553001690741");

        //html=new String(html.getBytes(Charset.forName("unicode")),Charset.forName("UTF-8"));
        //JsonArray scanInfos = new JsonParser().parse(html).getAsJsonObject().get("TrackPackagesResponse").getAsJsonObject().get("packageList")
          //                  .getAsJsonArray().get(0).getAsJsonObject().get("scanEventList").getAsJsonArray();


        String str="24\\x2f04\\x2f2013";
        str=new String(str.getBytes(Charset.forName("ascii")));
       // String scanStr=AsciiToChineseString(html);

        assertEquals("24/04/2013",str);


       /* try {
            FileUtils.write(new File(System.getProperty("user.home")+"/Desktop/"+new Date().getTime()+".html"),scanStr);
        } catch(IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/

        //assertEquals("", scanStr);


    }

    public String AsciiToChineseString(String s) {
        if ( s == null )
            return s;
           char[] orig = s.toCharArray ();
           byte[] dest = new byte[ orig.length ];
           for ( int i = 0; i < orig.length; i++ )

            dest[ i ] = ( byte ) ( orig[ i ] & 0xFF );
           try
           {
            ByteToCharConverter toChar = ByteToCharConverter.getConverter("utf-8");
            return new String ( dest,Charset.forName("utf-8") );
           }
           catch ( Exception e )
           {
            System.out.println ( e );
            return s;
           }
    }



}
