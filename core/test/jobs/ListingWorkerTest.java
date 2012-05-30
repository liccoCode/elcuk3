package jobs;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 5/15/12
 * Time: 10:59 AM
 */
public class ListingWorkerTest extends UnitTest {

    //    @Test
    public void testReviewWorker() {
        new ListingWorkers.R("B007LE3Y88_amazon.co.uk").now();
    }

    //    @Test
    public void testMD5Arlid() {
        System.out.println(DigestUtils.md5Hex("B007LE0UT4_amazon.co.uk_A3BMGM0RX76AXA"));
    }


    @Test
    public void testListingFullOffer() {
        new ListingWorkers.L("B006QK90YK_amazon.de").now();
    }
}
