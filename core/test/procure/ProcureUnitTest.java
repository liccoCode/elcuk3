package procure;

import models.procure.ProcureUnit;
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
    public void testProcureUnitCanBeShipOver() {
        ProcureUnit unit = ProcureUnit.findById(4l);
        assertEquals(true, unit.canBeShipOver());
    }
}
