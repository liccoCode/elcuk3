package market;

import models.market.Account;
import models.market.PriceStrategy;
import models.market.Selling;
import org.junit.Test;
import play.Logger;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:59
 */
public class SellingsTest extends FunctionalTest {

    //    @Before
    public void setup() {
        Fixtures.delete(Selling.class);
    }

    @Test
    public void testC() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("s.merchantSKU", "71ARG9101-BPU");
        params.put("s.asin", "B006QJZCYS1");
        params.put("s.market", Account.M.AMAZON_UK.name());
        params.put("s.state", Selling.S.NEW.name());
        params.put("s.type", Selling.T.AMAZON.name());
        params.put("s.priceStrategy.cost", 10 + "");
        params.put("s.priceStrategy.margin", 0.12 + "");
        params.put("s.priceStrategy.lowest", 6 + "");
        params.put("s.priceStrategy.max", 20 + "");
        params.put("s.priceStrategy.type", PriceStrategy.T.LowestPrice.name());
        POST("/market/sellings/c", params);
    }

    @Test
    public void testAssoListing() {
        Logger.info(GET("/servers/c?s.name=crawler1&s.url=http://r.easyacceu.com:9001").out.toString());
        Logger.info(GET("/market/listings/crawl?market=uk&asin=B005K8TW6A").out.toString());
        Logger.info(GET("/market/sellings/assoListing?msku=71ARG9101-BPU&listingId=B005K8TW6A_amazon.co.uk").out.toString());
    }

    @Test
    public void testPriceStrategyUpdate() {
        PriceStrategy ps = PriceStrategy.findById(1);
        Map<String, String> params = new HashMap<String, String>();
        params.put("ps.id", ps.id + "");
        params.put("ps.type", PriceStrategy.T.FixedPrice.name());
        params.put("ps.cost", ps.cost + "");
        params.put("ps.lowest", ps.lowest + "");
        params.put("ps.max", ps.margin + "");
        POST("/market/sellings/strategyU", params);
    }

    @Test
    public void testSellingUpdate() {
        Selling selling = Selling.findById(1);
        Map<String, String> params = new HashMap<String, String>();
        params.put("s.", "");
        POST("/market/sellings/u", params);
    }
}
