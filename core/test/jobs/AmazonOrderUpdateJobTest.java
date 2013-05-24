package jobs;

import models.market.Account;
import models.market.JobRequest;
import models.market.M;
import models.market.Orderr;
import org.junit.Test;
import play.Play;
import play.test.UnitTest;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/24/13
 * Time: 6:28 PM
 */
public class AmazonOrderUpdateJobTest extends UnitTest {
    @Test
    public void testUpdateOrderXML() {
        List<Orderr> orders = AmazonOrderUpdateJob.updateOrderXML(
                Play.getFile("test/html/10833010723.csv"),
                M.AMAZON_US
        );
        for(Orderr order : orders) {
            System.out.println("====================================================");
            System.out.println("OrderId: " + order.orderId);
            System.out.println("Address: " + order.address);
            System.out.println("Address1: " + order.address1);
            System.out.println("Email: " + order.email);
            System.out.println("Buyer/Reciver: " + order.buyer + "/" + order.reciver);
            System.out.println("ShippingService: " + order.shippingService);
            System.out.println("TrackNo: " + order.trackNo);
            System.out.println("ArriveDate: " + order.arriveDate);
        }
    }

    @Test
    public void testJob() {
        AmazonOrderUpdateJob worker = new AmazonOrderUpdateJob();
        JobRequest job = new JobRequest();
        job.path = "/Users/wyatt/Programer/repos/elcuk2/core/test/html/10833010723.csv";
        job.account = Account.findById(131l);
        worker.callBack(job);
    }
}
