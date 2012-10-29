package jobs;

import models.market.Account;
import models.procure.FBAShipment;
import org.joda.time.DateTime;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Collections;

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

    //    @Test
    public void testWatchFBAsReceving() {
        Account acc = Account.findById(2l);
        FBAShipment fbaShipment = FBAShipment.findById(53l);
        //TODO 注意这里的测试需要 Amazon 的 FBA 状态符合
        fbaShipment.receiptAt = DateTime.parse("2012-10-25").toDate();
        AmazonFBAWatchJob.watchFBAs(acc, Collections.singletonList(fbaShipment));
    }

    @Test
    public void testlistFBAShipmentItems() {
        FBAShipment shipment = FBAShipment.findById(43l);
        shipment.receivingAt = DateTime.parse("2012-10-20").toDate();
        AmazonFBAWatchPlusJob.listFBAShipmentItems(shipment);
    }
}
