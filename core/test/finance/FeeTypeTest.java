package finance;

import models.finance.FeeType;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/19/12
 * Time: 11:15 AM
 */
public class FeeTypeTest extends UnitTest {

    @Before
    public void setUP() {
        Fixtures.delete(FeeType.class);
    }

    @Test
    public void addFeeType() {
        FeeType f1 = new FeeType();
        f1.name = "FBAPerUnitFulfillmentFee";

        FeeType f2 = new FeeType();
        f2.name = "FBA_FEE";

        f1.parent = f2;

        f1.save();
    }
}
