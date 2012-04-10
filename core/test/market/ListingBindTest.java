package market;

import models.market.Listing;
import models.product.Product;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import play.libs.IO;
import play.test.FunctionalTest;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/18/12
 * Time: 11:34 AM
 */
public class ListingBindTest extends FunctionalTest {
    // 初始化 已经存在的 Product 的 Listing.
    @Before
    public void aSKU_Listing() {
        List<String> lines = IO.readLines(new File("/Volumes/wyatt/Programer/Projects/elcuk2/sku_listing.txt"));
//        List<String> lines = IO.readLines(new File("/Volumes/wyatt/Programer/Projects/elcuk2/sku_listing_test.txt"));
        for(String line : lines) {
            if(line.startsWith("#") || StringUtils.isBlank(line)) continue;
            String[] args = StringUtils.split(line, " ");
            // 每一个 SKU 都帮顶 3 个国家的 Listing.
            String sku = Product.merchantSKUtoSKU(args[0]);
            GET("/listings/crawl?market=uk&asin=" + args[1] + "&sku=" + sku);
            GET("/listings/crawl?market=de&asin=" + args[1] + "&sku=" + sku);
            GET("/listings/crawl?market=fr&asin=" + args[1] + "&sku=" + sku);
        }
    }

    // 初始化 已经存在的 Listing 的 Selling
    @Test
    public void bindSelling() {
        List<String> lines = IO.readLines(new File("/Volumes/wyatt/Programer/Projects/elcuk2/sku_listing.txt"));
//        List<String> lines = IO.readLines(new File("/Volumes/wyatt/Programer/Projects/elcuk2/sku_listing_test.txt"));
        for(String line : lines) {
            if(line.startsWith("#") || StringUtils.isBlank(line)) continue;
            String[] args = StringUtils.split(line, " ");

            List<Listing> listings = Listing.find("asin=?", args[1].trim()).fetch();
            for(Listing listing : listings) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("lid", listing.listingId);

                params.put("s.account.uniqueName", "amazon.co.uk_easyacc.eu@gmail.com");
                params.put("s.asin", listing.asin);
                params.put("s.market", listing.market.name());
                params.put("s.merchantSKU", args[0]);
                Float basicPrice = listing.displayPrice == null ? 3 : listing.displayPrice;
                params.put("s.priceStrategy.cost", (basicPrice * 0.5) + "");
                params.put("s.priceStrategy.lowest", (basicPrice * 0.15) + "");
                params.put("s.priceStrategy.margin", 0.5 + "");
                params.put("s.priceStrategy.max", (basicPrice * 1.5) + "");
                params.put("s.priceStrategy.shippingPlus", "0");
                params.put("s.priceStrategy.shippingPrice", "0");
                params.put("s.priceStrategy.type", "FixedPrice");
                params.put("s.ps", "5");
                params.put("s.state", "NEW");
                params.put("s.type", "FBA");

                POST("/listings/bindSelling", params);
            }
        }
    }
}
