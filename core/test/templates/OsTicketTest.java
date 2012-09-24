package templates;

import jobs.works.ListingReviewWork;
import jobs.works.ListingWorkers;
import models.market.AmazonListingReview;
import org.junit.Test;
import play.test.UnitTest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/1/12
 * Time: 11:20 AM
 */
public class OsTicketTest extends UnitTest {
    //    @Test
    public void testCreateReviewTicket() {
        AmazonListingReview review = AmazonListingReview.findById("B007TR9VRU_AMAZON.CO.UK_A14TVA83T35IG1");
        review.openTicket(null);
        review.save();
        System.out.println(review.osTicketId);
    }

    @Test
    public void testListingReviewJob() throws ExecutionException, TimeoutException, InterruptedException {
        new ListingReviewWork("B005UO263U_amazon.fr").now().get(10, TimeUnit.SECONDS);
    }
}
