package jobs;

import jobs.promise.AmazonFBAWatchPlusPromise;
import models.procure.FBAShipment;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/13/12
 * Time: 12:45 PM
 */
public class AmazonFBAWatchPlusJobTest extends UnitTest {

    /**
     * 测试多个 ShipItem 对应一个 FBA msku 的情况, 需要将 FBA msku 的入库数量分散到不同的 ShipItem 上.
     */
    @Test
    public void testMultiShipItemSameFBA() {
        new AmazonFBAWatchPlusPromise(Arrays.asList(FBAShipment.findByShipmentId("FBA62GM8R"))).syncFBAShipmentItems();
    }
}
