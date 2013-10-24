package models.market;

import com.alibaba.fastjson.JSONObject;
import factory.FactoryBoy;
import helper.HTTP;
import helper.Jitbit;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.internal.matchers.StringContains.containsString;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 9/27/13
 * Time: 3:15 PM
 */
public class AmazonListingReviewTest extends UnitTest {
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testOpenTicket() {
        AmazonListingReview review = FactoryBoy.create(AmazonListingReview.class);
        String ticketId = review.openTicket(null);

        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sharedSecret", Jitbit.SHAREDSECRET));
//        param.add(new BasicNameValuePair("submitterEmail", submitterEmail));
        param.add(new BasicNameValuePair("id", ticketId));

        JSONObject jsonObj = HTTP.postJson("https://easyacc.jitbit.com/helpdesk/api/GetTicket", param);

        assertThat(jsonObj.toString(), is(containsString(ticketId)));
    }
}
