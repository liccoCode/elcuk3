package jobs;

import jobs.promise.AmazonFBAWatchPlusPromise;
import models.market.Account;
import models.procure.FBAShipment;
import org.joda.time.DateTime;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 10/29/12
 * Time: 4:05 PM
 */
public class AmazonFBAWatchJobTest extends UnitTest {
    //    @Test
    public void testWatchFBAs() throws Exception {
        Account acc = Account.findById(2l);
        // test close state
        FBAShipment fbaSHipments = FBAShipment.findById(54l);
        fbaSHipments.state = FBAShipment.S.RECEIVING;
        AmazonFBAWatchJob.watchFBAs(acc, Collections.singletonList(fbaSHipments));
    }

    @Test
    public void testWatchFBAsReceving() {
        Account acc = Account.findById(2l);
        List<FBAShipment> fbas = FBAShipment.find("account=? AND state NOT IN (?,?,?,?)", acc, FBAShipment.S.PLAN, FBAShipment.S.CANCELLED, FBAShipment.S.CLOSED, FBAShipment.S.DELETED).fetch();
        //TODO 注意这里的测试需要 Amazon 的 FBA 状态符合
        AmazonFBAWatchJob.watchFBAs(acc, fbas);
    }

    //    @Test
    public void testlistFBAShipmentItems() {
        FBAShipment shipment = FBAShipment.findById(43l);
        shipment.receivingAt = DateTime.parse("2012-10-20").toDate();
        new AmazonFBAWatchPlusPromise(Arrays.asList(shipment)).syncFBAShipmentItems();
    }

    //    @Test
    public void testJos() throws ExecutionException, InterruptedException {
        new AmazonFBAWatchJob().now().get();
    }
}
