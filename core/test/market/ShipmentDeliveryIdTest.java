package market;

import models.procure.Shipment;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/3/12
 * Time: 4:29 PM
 */
public class ShipmentDeliveryIdTest extends UnitTest {
    @Test
    public void testShipmentId() {
        System.out.println(Shipment.id());
    }
}
