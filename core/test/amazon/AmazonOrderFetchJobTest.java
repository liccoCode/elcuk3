package amazon;

import helper.J;
import jobs.AmazonFBAWatchPlusJob;
import jobs.AmazonOrderFetchJob;
import models.market.Account;
import models.market.JobRequest;
import models.procure.FBAShipment;
import models.procure.ShipItem;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/19/12
 * Time: 4:03 PM
 */
public class AmazonOrderFetchJobTest extends UnitTest {
    AmazonOrderFetchJob fetchJob = new AmazonOrderFetchJob();

    //    @Test
    public void testAmazonUS() {
        Account acc = Account.findById(131l);
        JobRequest job = JobRequest.checkJob(acc, fetchJob, acc.marketplaceId());
        job.request();
    }

    //    @Test
    public void testUpdateState() {
        JobRequest.updateState(fetchJob.type());
    }

    @Test
    public void testFetchShipItems() {
        FBAShipment fbaShipment = FBAShipment.findById(21l);
        List<ShipItem> items = ShipItem.find("shipment.fbaShipment=?", fbaShipment).fetch();
        Collections.sort(items, new AmazonFBAWatchPlusJob.SortShipItemQtyDown());
        for(ShipItem item : items) {
            System.out.println(J.G(item));
        }
    }
}
