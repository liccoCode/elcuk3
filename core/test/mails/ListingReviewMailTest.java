package mails;

import models.market.AmazonListingReview;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 4/20/12
 * Time: 11:39 AM
 */
public class ListingReviewMailTest extends UnitTest {
    @Test
    public void test() {
        AmazonListingReview review = AmazonListingReview.findById("034b7762514b8ccac6dfba2d3371e558");
        review.listingReviewCheck();
    }

}
