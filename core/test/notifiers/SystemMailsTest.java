package notifiers;

import models.market.AmazonListingReview;
import models.market.Feedback;
import models.market.Listing;
import models.market.Orderr;
import models.procure.Shipment;
import notifiers.Mails;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 12/30/12
 * Time: 3:25 PM
 */
public class SystemMailsTest extends UnitTest {

    /*@Before
    public void loadFile() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("/notifiers/SystemMailsTest.yml");
    }

    @Test
    public void testDailyFeedbackMail() {
        List<Feedback> feedbacks = Feedback.all().fetch(50);
        assertEquals(1, feedbacks.size());
        SystemMails.dailyFeedbackMail(feedbacks);
    }*/

    @Test
    public void testMailsRecord(){
        Mails.shipment_clearance(new Shipment("SP|201301|63"));
        Mails.shipment_isdone(new Shipment("SP|201301|63"));
        Listing listing=new Listing();
        listing.listingId="B00AJD6PK2_amazon.de";
        listing.rating=0f;

        Orderr or=new Orderr();
        or.orderId="112-9994063-4338628";
        Orderr or1=new Orderr();
                or1.orderId="112-9947729-7993040";
        Orderr or2=new Orderr();
                or2 .orderId="112-9933987-4413038";
        Mails.moreOfferOneListing(null,listing);


        Mails.amazonUK_REVIEW_MAIL(or);
        Mails.amazonDE_REVIEW_MAIL(or1);
        Mails.amazonUS_REVIEW_MAIL(or2);


        Feedback fb=new Feedback();
        fb.orderId="112-9913257-7819462";
        Mails.feedbackWarnning(fb);


        AmazonListingReview alr=new AmazonListingReview();
        alr.orderr=or1;
        alr.alrId="B00AH3BW8E_AMAZON.CO.UK_RFYMOCDZQTCYH";
        Mails.listingReviewWarn(alr);


    }

    @Test
    public void testSystemMail(){
        Orderr or1=new Orderr();
                       or1.orderId="112-9947729-7993040";
        AmazonListingReview alr=new AmazonListingReview();
                alr.orderr=or1;
                alr.alrId="B00AH3BW8E_AMAZON.CO.UK_RFYMOCDZQTCYH";
        List<AmazonListingReview> alrs=new ArrayList<AmazonListingReview>();
        alrs.add(alr);
        SystemMails.dailyReviewMail(alrs);

        List<Feedback> fbs=new ArrayList<Feedback>();
        Feedback fb=new Feedback();
                fb.orderId="112-9913257-7819462";
        fbs.add(fb);
        SystemMails.dailyFeedbackMail(fbs);


        SystemMails.productPicCheckermail(null);


    }

    @Test
    public void testFBAMail(){

    }
}
