package models.procure;

import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 12/28/12
 * Time: 11:37 AM
 */
public class FBAShipmentTest extends UnitTest {
    @Test
    public void testUpdateFBAShipment() {
        ProcureUnit unit = ProcureUnit.findById(827l);
        unit.attrs.planQty += 5;
        unit.fba.updateFBAShipment(null);
    }

}
