package procure;

import models.procure.ProcureUnit;
import models.procure.Shipment;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/29/12
 * Time: 9:47 AM
 */
public class ProcureUnitTest extends UnitTest {
    @Test
    public void testWaitToShip() {
        try {
            ProcureUnit.waitToShip(1l, Shipment.T.EXPRESS);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
