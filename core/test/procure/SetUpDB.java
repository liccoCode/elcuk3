package procure;

import models.User;
import models.market.Selling;
import models.procure.PItem;
import models.procure.Plan;
import models.procure.Supplier;
import models.product.Product;
import models.product.Whouse;
import org.joda.time.DateTime;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/5/12
 * Time: 10:48 AM
 */
public class SetUpDB extends UnitTest {

    @Test
    public void setup() {
        Plan p = new Plan();
        p.creater = User.findById(1l);
        DateTime dt = DateTime.now();
        p.createDate = dt.toDate();
        p.planDate = dt.plusDays(15).toDate();
        p.id = Plan.cId();
        p.save();

        for(int i = 0; i < 5; i++) {
            PItem pi = new PItem();
            pi.whouse = Whouse.findById(1l);
            pi.state = PItem.S.PLAN;
            pi.price = new Random().nextFloat() * 100;
            pi.qty = 20;
            pi.title = "System Produce - " + i;
            pi.plan = p;
            pi.product = Product.find("sku=?", "80-QW1A56-BE").first();
            pi.selling = Selling.findById("10HTCG14-1900S_amazon.co.uk");
            pi.supplier = Supplier.findById("YD");

            pi.save();
        }
    }
}
