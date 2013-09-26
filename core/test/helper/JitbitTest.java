package helper;

import com.google.gson.JsonElement;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Ignore;
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
     * <p/>
     * 测试 错误的密钥
     */

    @Test
    public void tokenTest() {
        Jitbit.SHAREDSECRET = "12345";
        String submitterEmail = "wppurking@gmail.com";
        Throwable t = null;
        try{
          String ticketId = Jitbit.addTicket(submitterEmail, "admin", null, null,
                Jitbit.Category.SOFTWARE);
        }catch(Exception e){
            t = e;
        }
        assertNotNull(t);
        assertTrue(t instanceof RuntimeException);
        assertTrue(t.getMessage().contains("密钥失效! JitBit 创建 Ticket."));
    }

    /**
     * 通过密钥和邮箱账户创建Ticket
     * <p/>
     * 已存在的用户
     */
    @Ignore
    @Test
    public void addTicketTest() {
        String submitterEmail = "wppurking@gmail.com";

        String ticketId = Jitbit.addTicket(submitterEmail, "admin", "123", "312",
                Jitbit.Category.SOFTWARE);

        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sharedSecret", Jitbit.SHAREDSECRET));
        param.add(new BasicNameValuePair("submitterEmail", submitterEmail));
        param.add(new BasicNameValuePair("id", ticketId));

        JsonElement jsonElement = HTTP.postJson("https://easyacc.jitbit.com/helpdesk/api/GetTicket", param);

        assertThat(jsonElement.toString(), is(containsString(ticketId)));
    }

    /**
     * 通过密钥和邮箱账户创建Ticket
     * <p/>
     * 不存在的用户
     */
    @Ignore
    @Test
    public void addTicketNegationTest() {
        String submitterEmail = "124820430@qq.com";

        String ticketId = Jitbit.addTicket(submitterEmail, "tom", "我不存在哦,我是新来的哦。", "我不存在哦,我是新来的哦。",
                Jitbit.Category.SOFTWARE);

        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sharedSecret", Jitbit.SHAREDSECRET));
        param.add(new BasicNameValuePair("submitterEmail", submitterEmail));
        param.add(new BasicNameValuePair("id", ticketId));

        JsonElement jsonElement = HTTP.postJson("https://easyacc.jitbit.com/helpdesk/api/GetTicket", param);

        assertThat(jsonElement.toString(), is(containsString(ticketId)));
    }

}
