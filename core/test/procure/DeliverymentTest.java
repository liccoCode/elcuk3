package procure;

import models.procure.Deliveryment;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/18/12
 * Time: 5:02 PM
 */
public class DeliverymentTest extends UnitTest {
    @Test
    public void testDeliveryId() {
        System.out.println(Deliveryment.id());
    }
}
