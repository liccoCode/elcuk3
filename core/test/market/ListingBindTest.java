package market;

import models.product.Product;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import play.libs.IO;
import play.test.FunctionalTest;

import java.io.File;
import java.util.List;

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
}
