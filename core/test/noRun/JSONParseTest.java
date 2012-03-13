package noRun;

import helper.PH;
import models.market.Selling;
import models.procure.PItem;
import models.product.Product;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/11/12
 * Time: 8:41 PM
 */
public class JSONParseTest {
    @Test
    public void testMarsh() {
        PItem pItem = new PItem();
        pItem.in = 29;
        pItem.inDay = 49f;
        pItem.onWay = 28;
        pItem.onWayDay = 2.3f;
        pItem.qty = 234;
        pItem.product = new Product("90-d9jdj-dfj");
        Selling selling = new Selling();
        selling.sellingId = "93498_amazon.co.uk";
        pItem.selling = selling;

        PH.marsh(pItem);
    }

    @Test
    public void testUnMarsh() {
        System.out.println(PH.unMarsh("90-d9jdj-dfj_93498_amazon.co.uk"));
    }
}
