package helper;

import com.google.gson.JsonElement;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.internal.matchers.StringContains.containsString;

/**
 * jibit  API
 * <p/>
 * http://www.jitbit.com/web-helpdesk/helpdesk-API/
 */
public class JitbitTest extends UnitTest {


    @Test
    public void addUserTest(){
       String json = Jitbit.addUser("124820430@qq1.com","tom1");
    }


    /**
     * 通过密钥和邮箱账户创建Ticket
     */
    public void addTicketTest() {
        //String submitterEmail = "wppurking@gmail.com";
        String submitterEmail = "124820430@qq.com";

        String ticketId = Jitbit.addTicket(submitterEmail,"tom", null, null,
                Jitbit.Category.SOFTWARE);

        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sharedSecret", Jitbit.SHAREDSECRET));
        param.add(new BasicNameValuePair("submitterEmail", submitterEmail));
        param.add(new BasicNameValuePair("id", ticketId));

        JsonElement jsonElement = HTTP.postJson("https://easyacc.jitbit.com/helpdesk/api/GetTicket", param);

        assertThat(jsonElement.toString(), is(containsString(ticketId)));
    }


}
