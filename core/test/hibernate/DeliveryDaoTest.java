package hibernate;

import models.procure.Deliveryment;
import models.procure.Shipment;
import org.junit.Test;
import play.test.UnitTest;
import query.ShipmentQuery;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/4/12
 * Time: 5:57 PM
 */
public class DeliveryDaoTest extends UnitTest {
    @Test
    public void testDeliverySearchProduct() {
        List<Deliveryment> dmts = Deliveryment.find("SELECT d FROM Deliveryment d, IN (d.units) u WHERE u.sku LIKE ?", "%QW1A56%").fetch();
        System.out.println(dmts);
    }


    @Test
    public void testJPSSize() {
        System.out.println(Shipment.count("SIZE(items)=0 AND state!=?", Shipment.S.CANCEL));
    }
}
