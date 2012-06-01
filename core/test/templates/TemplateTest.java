package templates;

import helper.GTs;
import models.market.AmazonListingReview;
import models.market.Feedback;
import org.junit.Test;
import play.test.UnitTest;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试使用
 * User: wyattpan
 * Date: 5/9/12
 * Time: 11:20 AM
 */
public class TemplateTest extends UnitTest {

    @Test
    public void testGts() {
        Feedback f = Feedback.findById("303-3685104-8694754");
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("f", f);
        System.out.println(GTs.render("OsTicketFeedbackWarn", args));
    }

    @Test
    public void testReviewTp() {
        AmazonListingReview review = AmazonListingReview.findById("B007TR9VRU_AMAZON.CO.UK_A14TVA83T35IG1");
        System.out.println(GTs.render("OsTicketReviewWarn", GTs.newMap("review", review).build()));
    }
}
