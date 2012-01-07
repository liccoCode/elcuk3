package market;

import org.junit.Test;
import play.Logger;
import play.libs.F;
import play.libs.WS;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午7:10
 */
public class ListingControllerTest extends UnitTest {
    @Test
    public void testCrawler() throws ExecutionException, TimeoutException, InterruptedException {
        //http://localhost:9000/market/listings/crawl?market=uk&asin=B005QSWWUW
        List<String> asins = Arrays.asList("B006QKA8RI", "B005K8TW6A", "B001OQOK5U", "B005K8OHEC");
        List<F.Promise> promises = new ArrayList<F.Promise>();
        for(String asin : asins) {
            Logger.info("http://localhost:9000/market/listings/crawl?market=uk&asin=" + asin);
            promises.add(WS.url("http://localhost:9000/market/listings/crawl?market=uk&asin=" + asin).getAsync());
        }
        for(F.Promise proms : promises) {
            System.out.println(proms.get(10, TimeUnit.SECONDS).toString());
            System.out.println("===========================================");
        }
    }
}
