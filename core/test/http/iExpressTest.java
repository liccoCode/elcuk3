package http;

import models.procure.Shipment;
import models.procure.iExpress;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/26/12
 * Time: 4:45 PM
 */
public class iExpressTest extends UnitTest {
    //    @Test
    public void testDHLHTML() throws IOException {
        String tNo = "7757392230";
        String html = iExpress.DHL.fetchStateHTML(tNo);
        FileUtils.writeStringToFile(new File("/tmp/dhl." + tNo + ".html"), iExpress.DHL.parseExpress(html, tNo));
    }

    //    @Test
    public void testFedexHTML() throws IOException {
        String tNo = "533252312364";
        String html = iExpress.FEDEX.fetchStateHTML(tNo);
        FileUtils.writeStringToFile(new File("/tmp/fedex." + tNo + ".html"), iExpress.FEDEX.parseExpress(html, tNo));
    }

    @Test
    public void testIsDelivered() {
        Shipment ship = Shipment.findById("SP|201206|01");
        System.out.println(ship.internationExpress.isDelivered(ship.iExpressHTML));
        ship = Shipment.findById("SP|201206|00");
        System.out.println(ship.internationExpress.isDelivered(ship.iExpressHTML));
    }
}
