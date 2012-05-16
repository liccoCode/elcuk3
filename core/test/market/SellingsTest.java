package market;

import helper.Webs;
import models.market.Selling;
import org.junit.Assert;
import org.junit.Test;
import play.test.FunctionalTest;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:59
 */
public class SellingsTest extends FunctionalTest {

    @Test
    public void testLoad() {
        Selling sel = Selling.findById("10HTCG14-1900S_amazon.co.uk");
        System.out.println(Webs.exposeGson(sel));

        Selling sel2 = Selling.findById("10HTCG14-1900S,2_amazon.de");
        Assert.assertNotNull(sel2.aps);
        System.out.println(Webs.exposeGson(sel2));
    }
}
