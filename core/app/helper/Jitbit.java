package helper;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import play.Logger;
import play.Play;
import play.utils.FastRuntimeException;

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
        },

        REVIEW {
            @Override
            public String value() {
                return "64618";
            }
        },

        FEEDBACK {
            @Override
            public String value() {
                return "64617";
            }
        },

        MORE_THAN_ONE_LISTING {
            @Override
            public String value() {
                return "64629";
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
        if(StringUtils.isBlank(username))
            throw new FastRuntimeException("Username 必须存在!");
        if(StringUtils.isBlank(submitterEmail))
            throw new FastRuntimeException("用户邮箱必须存在");

        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sharedSecret", SHAREDSECRET));
        param.add(new BasicNameValuePair("submitterEmail", submitterEmail));
        param.add(new BasicNameValuePair("categoryId", category.value()));
        param.add(new BasicNameValuePair("subject", subject));
        param.add(new BasicNameValuePair("body", body));

        String json = HTTP.post("https://easyacc.jitbit.com/helpdesk/api/AddTicketFromEmail", param);
        //JiBit 只能向系统内的用户发送邮箱 0: 邮箱不存在
        if("0".equals(json)) {
            addUser(submitterEmail, username);
            json = HTTP.post("https://easyacc.jitbit.com/helpdesk/api/AddTicketFromEmail", param);
        } else if(StringUtils.contains(json, "sign in")) {
            Logger.error("username:%s submitterEmail:%s JitBit创建Ticket失败,密钥失效:\n%s", username, submitterEmail, json);
            throw new RuntimeException("密钥失效! JitBit 创建 Ticket 失败!");
        } else if(!NumberUtils.isNumber(json)) {
            Logger.error("username:%s submitterEmail:%s JitBit创建Ticket失败,密钥失效:\n%s", username, submitterEmail, json);
            throw new RuntimeException("向 JitBit 创建 Ticket 失败!");
        }
        return json;
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
        String json = HTTP.post("https://easyacc.jitbit.com/helpdesk/api/AddUser", param);

        if(StringUtils.contains(json, "Something went wrong.")) {
            Logger.error("username:%s submitterEmail:%s JitBit 创建 User 失败:\n%s", username, submitterEmail, json);
            throw new RuntimeException("JitBit 创建User 失败,用户名称或者邮箱已存在!");
        } else if(!NumberUtils.isNumber(json)) {
            Logger.error("username:%s submitterEmail:%s JitBit 创建 User 失败:\n%s", username, submitterEmail, json);
            throw new RuntimeException("向 JitBit 创建User 失败!");
        }
        return json;
    }
}