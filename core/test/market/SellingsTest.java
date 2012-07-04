package market;

import helper.GTs;
import helper.Webs;
import models.market.Account;
import models.market.OrderItem;
import models.market.Selling;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import play.test.FunctionalTest;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:59
 */
public class SellingsTest extends FunctionalTest {

    //    @Test
    public void testLoad() {
        Selling sel = Selling.findById("10HTCG14-1900S_amazon.co.uk");
        System.out.println(Webs.G(sel));

        Selling sel2 = Selling.findById("10HTCG14-1900S,2_amazon.de");
        Assert.assertNotNull(sel2.aps);
        System.out.println(Webs.G(sel2));
    }

    //    @Test
    public void testLoadOrderItem() {
        OrderItem.skuOrMskuAccountRelateOrderItem("all", "sku", Account.<Account>findById(1l), DateTime.parse("2012-05-13").toDate(), DateTime.parse("2012-06-13").toDate());
    }

    //    @Test
    public void testAnalyzesSKUAndMSKU() {
        List<Selling> sells = Selling.analyzesSKUAndSID("msku");
        for(Selling sell : sells) {
            System.out.println(GTs.render("debug_selling", GTs.newMap("sell", sell).build()));
        }
    }

    @Test
    public void testAnalyzesSKUAndMSKU2() {
        List<Selling> sells = Selling.analyzesSKUAndSID("sku");
        for(Selling sell : sells) {
            System.out.println(GTs.render("debug_selling", GTs.newMap("sell", sell).build()));
        }
    }
}
