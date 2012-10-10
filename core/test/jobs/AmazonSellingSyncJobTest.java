package jobs;

import jobs.AmazonSellingSyncJob;
import models.market.Account;
import models.market.Listing;
import models.market.M;
import models.market.Selling;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import play.Play;
import play.libs.F;
import play.test.UnitTest;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 4/6/12
 * Time: 4:39 PM
 */
public class AmazonSellingSyncJobTest extends UnitTest {

    //    @Test
    public void request() {
        new AmazonSellingSyncJob().doJob();
    }

    //    @Test
    public void testParse() throws IOException {
        List<String> lines = FileUtils.readLines(Play.getFile("test/15040184064.txt"));
        for(String line : lines) {
            String[] args = StringUtils.splitPreserveAllTokens(line, "\t");

            //uk: item-name	item-description	listing-id	seller-sku(merchantSKU)	price	quantity	open-date	image-url	item-is-marketplace	product-id-type	zshop-shipping-fee	item-note	item-condition	zshop-category1	zshop-browse-path	zshop-storefront-feature	asin1	asin2	asin3(19)	will-ship-internationally	expedited-shipping	zshop-boldface	product-id	bid-for-featured-placement	add-delete	pending-quantity	fulfillment-channel
            String t_asin = args[16];
            String t_msku = args[3];
            String t_title = args[0];
            String t_price = args[4];
            String t_fulfilchannel = null;
            System.out.println(String.format("%s --- %s --- %s", args[16], args[17], args[26]));
        }
    }

    @Test
    public void testDealSellingFromActiveListingsReport() {
        Account acc = new Account();
        acc.id = 2l;
        acc.type = M.AMAZON_DE;
        output(Play.getFile("test/15040184064.txt"), acc);

        acc.id = 1l;
        acc.type = M.AMAZON_UK;
        output(Play.getFile("test/15040185124.txt"), acc);
    }

    private void output(File file, Account acc) {
        F.T2<List<Selling>, List<Listing>> t2 = AmazonSellingSyncJob.dealSellingFromActiveListingsReport(file, acc, acc.type);
        System.out.println(" ============================================ " + acc.type + " =========================================");
        for(Selling s : t2._1) {
            System.out.println(s.sellingId);
        }
        for(Listing l : t2._2) {
            System.out.println(l.asin);
        }
    }
}
