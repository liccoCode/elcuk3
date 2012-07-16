package procure;

import models.procure.CooperItem;
import models.procure.Cooperator;
import org.apache.commons.lang.math.JVMRandom;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 7/16/12
 * Time: 3:03 PM
 */
public class CooperItemTest extends UnitTest {
    //    @Test
    public void testPersist() {
        CooperItem ci = new CooperItem();
        ci.sku = "this is sku";
        ci.price = 12.8f;
        ci.period = 3;
        ci.lowestOrderNum = 200;
        ci.save();
        System.out.println("------------- Persist Model Id: " + ci.id);
    }

    //    @Test
    public void testUpdate() {
        CooperItem ci = CooperItem.findById(1l);
        ci.price = 20.3f * (1 + JVMRandom.nextLong(10));
        ci.lowestOrderNum = 300;
        ci.save();
        System.out.println("------------- Update Model Id: " + ci.id);
    }

    @Test
    public void testUpdateCooperator() {
        Cooperator coper = Cooperator.findById(1l);
        coper.address = "改变的地址" + JVMRandom.nextLong(10);
        coper.save();
    }
}
