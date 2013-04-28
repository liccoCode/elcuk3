package models.procure;

import models.market.M;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 4/28/13
 * Time: 3:48 PM
 */
public class ProcureUnitTest extends UnitTest {
    @Test
    public void testPostFbaShipment() throws Exception {
        ProcureUnit unit = ProcureUnit.findById(827l);
        assertEquals(unit.qty(), 50);
        assertEquals(unit.selling.market, M.AMAZON_UK);

        FBAShipment fba = unit.postFbaShipment();
        assertEquals(fba.state, FBAShipment.S.WORKING);
        assertEquals(unit.fba, fba);
    }
}
