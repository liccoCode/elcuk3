package helper;

import com.google.gson.JsonParser;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import play.Logger;
import play.Play;

import java.util.ArrayList;
import java.util.List;

/**
 * JitBit API
 * http://www.jitbit.com/web-helpdesk/helpdesk-API/
 * User: DyLanM
 * Date: 13-9-23
 * Time: 上午11:02
 */
public class Jitbit {

    /**
     * 密钥
     */
    public static String SHAREDSECRET = "TDumhG4zptUACz";

    /**
     * Bootstrap init
     */
    public static void init() {
        SHAREDSECRET = Play.configuration.getProperty("Jitbit.sharedSecret");
    }

    /**
     * 分类
     */
    public enum Category {
        SOFTWARE {
            @Override
            public String value() {
                return "63513";
            }
        };

        public abstract String value();

    }

    /**
     * 向 JitBit系统创建一个 Ticket 并返回该 Ticket ID
     * param: submitterEmail   客户邮箱
     * param: username         客户名称
     * param: subject          邮件标题
     * param: body             邮件类型
     * param: category         邮件类型
     */
    public static String addTicket(String submitterEmail, String username, String subject, String body,
                                   Category category) {
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sharedSecret", SHAREDSECRET));
        param.add(new BasicNameValuePair("submitterEmail", submitterEmail));
        param.add(new BasicNameValuePair("categoryId", category.value()));
        param.add(new BasicNameValuePair("subject", subject));
        param.add(new BasicNameValuePair("body", body));


        String json = HTTP.post("https://easyacc.jitbit.com/helpdesk/api/AddTicketFromEmail", param);

        System.out.println(" ---- :  " + json);
        //JiBit 只能向系统内的用户发送邮箱 0: 邮箱不存在
        if("0".equals(json)) {
            addUser(submitterEmail, username);
            json = HTTP.post("https://easyacc.jitbit.com/helpdesk/api/AddTicketFromEmail", param);
        }

        try {
            return new JsonParser().parse(json).getAsString();
        } catch(com.google.gson.JsonSyntaxException e) {
            Logger.error("username:%s submitterEmail:%s JitBit创建Ticket,密钥失效:\n%s", username, submitterEmail, json);
            throw new RuntimeException("JitBit 创建 Ticket  密钥失效 ", e);
        }
    }

    /**
     * 创建用户
     * <p/>
     * username 或者 邮箱 不能重复
     */
    public static String addUser(String submitterEmail, String username) {

        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sharedSecret", SHAREDSECRET));
        param.add(new BasicNameValuePair("username", username));
        param.add(new BasicNameValuePair("password", "1234567890"));
        param.add(new BasicNameValuePair("email", submitterEmail));
        param.add(new BasicNameValuePair("sendWelcomeEmail", "false"));
        String json = null;
        try {
            json = HTTP.post("https://easyacc.jitbit.com/helpdesk/api/AddUser", param);
            return new JsonParser().parse(json).getAsString();
        } catch(com.google.gson.JsonSyntaxException e) {
            Logger.error("username:%s submitterEmail:%s JitBit 创建 User 失败:\n%s", username, submitterEmail, json);
            throw new RuntimeException("JitBit 创建User 失败,用户名称或者邮箱已存在 ", e);
        }
    }
}
