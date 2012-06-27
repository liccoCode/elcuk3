package procure;

import models.procure.Shipment;
import org.joda.time.DateTime;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/18/12
 * Time: 5:00 PM
 */
public class ShipmentTest extends UnitTest {
    @Test
    public void testUnits() {
        System.out.println(Shipment.id());
        DateTime dt = DateTime.now();
        System.out.println(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear()));
    }
}
