package elasticsearch;

import models.market.M;
import org.junit.Test;
import play.test.UnitTest;
import services.MetricProfitService;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 10/24/13
 * Time: 4:00 PM
 */
public class ProfitTest extends UnitTest {


    @Test
    public void testSearch() throws Exception {
        Date begin = new Date();
        Date end = new Date();
        M market = M.AMAZON_UK;
        String sku = "";
        String sellingId = "";

//        MetricProfitService service = new MetricProfitService(begin, end, market, sku, sellingId);
//        float a = service.sellingAmazonTotalFee();
//        System.out.println("aaaaaaaaa::" + a);

    }
}
