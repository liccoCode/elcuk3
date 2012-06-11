package market;

import jobs.AmazonSellingSyncJob;
import models.market.Account;
import models.market.Listing;
import models.market.Selling;
import org.junit.Test;
import play.libs.F;
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
        F.T2<List<Selling>, List<Listing>> sells = AmazonSellingSyncJob.dealSellingFromActiveListingsReport(new File("/Users/wyattpan/elcuk2-data/2012/4/6/11294650784.txt"), acc, Account.M.AMAZON_UK);
        F.T2<List<Selling>, List<Listing>> sells2 = AmazonSellingSyncJob.dealSellingFromActiveListingsReport(new File("/Users/wyattpan/elcuk2-data/2012/4/6/11294784464.txt"), acc, Account.M.AMAZON_DE);
        F.T2<List<Selling>, List<Listing>> sells3 = AmazonSellingSyncJob.dealSellingFromActiveListingsReport(new File("/Users/wyattpan/elcuk2-data/2012/4/6/11294794364.txt"), acc, Account.M.AMAZON_FR);
    }
}
