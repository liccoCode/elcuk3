package models.procure;

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
    public void testYAML() {
        // 127 拥有 2 items, 一个 fba
        Shipment s = Shipment.findById("SP|201211|127");
        assertEquals(2, s.items.size());
        assertEquals(1, s.fbas.size());

        FBAShipment fba = s.fbas.get(0);
        assertEquals("DUS2", fba.centerId);

        assertEquals(4, Shipment.count());
        assertEquals(3, FBAShipment.count());
        assertEquals(6, ShipItem.count());

        assertEquals("MUC3", FBAShipment.findByShipmentId("FBA6TW4JG").centerId);
    }

    /**
     * 拥有 4 个 Shipment, 除开自己与一个不符合的, 剩下 2 个
     */
    @Test
    public void testTargetShipments() {
        FBAShipment fba = FBAShipment.findByShipmentId("FBA6SV8RT");
        assertEquals("SP|201211|127", fba.shipment.id);
        List<Shipment> shipments = fba.targetShipments();
        // 没有 FBA 的需要出来
        assertEquals(2, shipments.size());

        // 相同 centerId 的需要出来
        for(Shipment s : shipments) {
            if(s.fbas.size() > 0) {
                assertEquals("FBA8DW4JG", s.fbas.get(0).shipmentId);
            }
        }
    }

}
