package market;

import models.market.Account;
import models.market.AmazonListingReview;
import org.junit.Test;
import play.libs.F;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/2/12
 * Time: 5:12 PM
 */
public class AccountPickUpTest extends UnitTest {

    @Test
    public void testPickUpOneAccount() {
        AmazonListingReview review = AmazonListingReview.findByReviewId("R3VLPL2RYUISA9");
        F.T2<Account, Integer> t2 = review.pickUpOneAccountToClick();
        System.out.println(t2);
    }

}
