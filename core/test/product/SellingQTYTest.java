package product;

import models.market.SellingQTY;
import models.product.Whouse;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 4/11/12
 * Time: 9:53 AM
 */
public class SellingQTYTest extends UnitTest {
    //    @Test
    public void addNew() {
        SellingQTY qty = new SellingQTY();
        qty.qty = 20;
        qty.inbound = 200;
        qty.pending = 10;

        qty.attach2Selling("80-QW1A56-BE", Whouse.<Whouse>findById(1l));
    }


}
