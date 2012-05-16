package mails;

import models.market.AmazonListingReview;
import models.market.Orderr;
import notifiers.Mails;
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
        AmazonListingReview review = AmazonListingReview.findById("8ec405dff807142c7960c2c6d0df2270");
        review.listingReviewCheck();
    }

    @Test
    public void testMailReviewMail() {
        Orderr ord = Orderr.findById("302-1888247-6527562");

        Mails.amazonUK_REVIEW_MAIL(ord);
    }

}
