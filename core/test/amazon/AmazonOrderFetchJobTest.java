package amazon;

import helper.J;
import jobs.AmazonFBAWatchPlusJob;
import jobs.AmazonOrderFetchJob;
import models.market.Account;
import models.market.JobRequest;
import models.market.Orderr;
import models.procure.FBAShipment;
import models.procure.ShipItem;
import org.junit.Test;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.test.UnitTest;

import java.io.File;
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
    public void testParseOrder() {
        List<Orderr> orders = AmazonOrderFetchJob.allOrderXML(Play.getFile("test/html/15285572344.txt"), Account.<Account>findById(2l));
        int i = 0;
        for(Orderr order : orders) {
            i++;
            try {
                Orderr managed = Orderr.findById(order.orderId);
                if(managed == null) { //保存
//                    order.save();
                    Logger.info("Save Order: " + order.orderId);
                } else { //更新
                    if(managed.state == Orderr.S.CANCEL) continue; // 如果订单已经为 CANCEL 了, 则不更新了
                    if(order.state == Orderr.S.CANCEL || order.state == Orderr.S.PENDING || order.state == Orderr.S.SHIPPED) // 新订单为 CANCEL 的则更新
                        managed.updateAttrs(order);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            if(i >= 500) {
                JPA.em().flush();
                i = 0;
                System.out.println("================================================================================");
            }
        }
    }
}
