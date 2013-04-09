package templates;

import jobs.works.ListingReviewsWork;
import models.market.AmazonListingReview;
import org.junit.Test;
import play.test.UnitTest;

import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/1/12
 * Time: 11:20 AM
 */
public class OsTicketTest extends UnitTest {
    //    @Test
    public void testCreateReviewTicket() {
        AmazonListingReview review = AmazonListingReview
                .findById("B007TR9VRU_AMAZON.CO.UK_A14TVA83T35IG1");
        review.openTicket(null);
        review.save();
        System.out.println(review.osTicketId);
    }

    @Test
    public void testListingReviewJob() throws Exception {
        new ListingReviewsWork("B007K4WYMQ_amazon.de").now().get(1, TimeUnit.MINUTES);
    }
}
