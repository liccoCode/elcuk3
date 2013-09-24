package jitbitAPI;

import helper.HTTP;
import helper.Jitbit;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;
import play.test.UnitTest;
import sun.misc.BASE64Encoder;

import java.util.ArrayList;
import java.util.List;

/**
 * jibit  API
 *
 * http://www.jitbit.com/web-helpdesk/helpdesk-API/
 */
public class HTTPTest extends UnitTest {

    /**
     * 通过密钥和邮箱账户创建Ticket
     */
    //@Test
    public void addTicketFromEmail() {
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sharedSecret", "TDumhG4zptUACz"));
        param.add(new BasicNameValuePair("submitterEmail", "wppurking@gmail.com"));
        param.add(new BasicNameValuePair("categoryId", "63518"));
        param.add(new BasicNameValuePair("subject", "测试标题"));
        param.add(new BasicNameValuePair("body", "这是测试内容"));
        param.add(new BasicNameValuePair("priorityId", "1"));

       System.out.println(HTTP.postJson("https://easyacc.jitbit.com/helpdesk/api/AddTicketFromEmail", param));
    }

    //@Test
    public void getCategories() {

       String  temp = String.format(" Basic %s", new BASE64Encoder().encode("admin:13297472505".getBytes()));
        System.out.println(temp);
       System.out.println(HTTP.postJson("https://easyacc.jitbit.com/helpdesk/api/categories", null));
    }

    //@Test
    public void Authorization() {
    //post.setHeader("Authorization",String.format("Basic %s",new BASE64Encoder().encode("admin:13297472505".getBytes())));
       String  temp = String.format(" Basic %s", new BASE64Encoder().encode("admin:13297472505".getBytes()));
        System.out.println("-------------: "+temp);
       System.out.println(HTTP.postJson("https://easyacc.jitbit.com/helpdesk/api/Authorization", null));
    }

    @Test
    public void TestAPI(){

        Jitbit.addTicket("wppurking@gmail.com","我是一个标题qqsfsafasfGGG","啊，这是我的内容。qqsfgggGGG",Jitbit.Category.SOFTWARE);
    }
}
