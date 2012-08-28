package http;

import com.google.gson.JsonElement;
import helper.HTTP;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/28/12
 * Time: 5:18 PM
 */
public class OsTicketSingle extends UnitTest {
    @Test
    public void seeTicketState() {
//        http://t.easya.cc/api/ticket_sync?ticketIds=717792
        JsonElement jsonEl = HTTP.postJson("http://t.easya.cc/api/ticket_sync.php?ticketIds=717792", Arrays.asList(
                new BasicNameValuePair("ticketIds", "717792")
        ));
        System.out.println(jsonEl);
    }
}
