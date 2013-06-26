package models.procure;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import org.junit.Test;
import play.test.UnitTest;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 6/21/13
 * Time: 4:41 PM
 */
public class ShipmentRemoteTest extends UnitTest {

    @Test
    public void test() {
        Shipment shipment = FactoryBoy.build(Shipment.class, new BuildCallback<Shipment>() {
            @Override
            public void build(Shipment target) {
                target.internationExpress = iExpress.DHL;
                target.trackNo = "7260410371";
            }
        });
        System.out.println(shipment.trackWebSite());
        assertThat(shipment.iExpressHTML, is(notNullValue()));
    }
}
