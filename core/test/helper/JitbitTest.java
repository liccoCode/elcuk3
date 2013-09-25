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

    /**
     * 通过密钥和邮箱账户创建Ticket
     */

    @Test
    public void testJitbit() {

        String submitterEmail = "wppurking@gmail.com";

        String ticketId = Jitbit.addTicket(submitterEmail, "我是一个标题qqsfsafasfGGG", "啊，这是我的内容。qqsfgggGGG",
                Jitbit.Category.SOFTWARE);

        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sharedSecret", Jitbit.SHAREDSECRET));
        param.add(new BasicNameValuePair("submitterEmail", submitterEmail));
        param.add(new BasicNameValuePair("id", ticketId));

        JsonElement jsonElement = HTTP.postJson("https://easyacc.jitbit.com/helpdesk/api/GetTicket", param);

        assertThat(jsonElement.toString(), is(containsString(ticketId)));

    }


}
