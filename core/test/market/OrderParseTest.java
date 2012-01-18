package market;

import models.market.Account;
import models.market.Orderr;
import models.market.Selling;
import models.product.Product;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 4:23 PM
 */
public class OrderParseTest extends UnitTest {
    //    @Before
    public void setup() {
        Fixtures.delete(Product.class, Selling.class);
        Fixtures.loadModels("Product.yml", "Selling.yml");
    }

    @Test
    public void testParse() {
        List<Orderr> orders = Orderr.parseALLOrderXML(new File("/Users/wyattpan/elcuk-data/2011/10/11/8141580584.xml"));
        Account acc = Account.findById(1l);
//        List<Orderr> orders = Orderr.parseALLOrderXML(new File("F:/elcuk-data/2011/12/01/9018095104.xml"));
        for(Orderr or : orders) {
            or.account = acc;
            or.save();
        }
    }
}
