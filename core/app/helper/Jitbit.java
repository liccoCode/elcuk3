package helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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

    public static String addTicket(String submitterEmail, String subject, String body, Category category) {
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sharedSecret", SHAREDSECRET));
        param.add(new BasicNameValuePair("submitterEmail", submitterEmail));
        param.add(new BasicNameValuePair("categoryId", category.value()));
        param.add(new BasicNameValuePair("subject", subject));
        param.add(new BasicNameValuePair("body", body));

        String json = null;
        try {
            json = HTTP.post("https://easyacc.jitbit.com/helpdesk/api/AddTicketFromEmail", param);
            JsonElement jsonElement = new JsonParser().parse(json);
        } catch(Exception e) {
            throw new RuntimeException("Ticket创建失败。", e);
        }

        return json;
    }
}
