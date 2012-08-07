package market;

import models.market.AmazonListingReview;
import org.junit.Before;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/7/12
 * Time: 11:14 AM
 */
public class TicketStateTest extends UnitTest {
    @Before
    public void setup() {
        AmazonListingReview review = new AmazonListingReview();
        review.createReview();
    }
}
