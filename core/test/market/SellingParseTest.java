package market;

import models.market.Account;
import models.market.Selling;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 4/9/12
 * Time: 3:41 PM
 */
public class SellingParseTest extends UnitTest {
    @Test
    public void testParseSellingFromActiveListingsReport() {
        Account acc = Account.findById(1l);
        List<Selling> sells = Selling.dealSellingFromActiveListingsReport(new File("/Users/wyattpan/elcuk2-data/2012/4/6/11294650784.txt"), acc, Account.M.AMAZON_UK);
        List<Selling> sells2 = Selling.dealSellingFromActiveListingsReport(new File("/Users/wyattpan/elcuk2-data/2012/4/6/11294784464.txt"), acc, Account.M.AMAZON_DE);
        List<Selling> sells3 = Selling.dealSellingFromActiveListingsReport(new File("/Users/wyattpan/elcuk2-data/2012/4/6/11294794364.txt"), acc, Account.M.AMAZON_FR);
    }
}
