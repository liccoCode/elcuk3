package models.procure;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 12/28/12
 * Time: 11:37 AM
 */
public class FBAShipmentTest extends UnitTest {

    @Before
    public void setUp() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("/models/procure/FBAShipmentTest.yml");
    }


    @Test
    public void testNewShipment() {
        List<Shipment> ships = Shipment.findUnitRelateShipmentByWhouse((long) 1, null);
        assertEquals(64, ships.size());
        assertEquals(new DateTime().getDayOfWeek(), ships.get(0).beginDate);


    }


}
