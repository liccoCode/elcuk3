package helper;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import play.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 与 OsTicket 交互的相关 API
 * User: wyattpan
 * Date: 8/28/12
 * Time: 11:16 AM
 */
public class OsTicket {
    public static final String OS_TICKET = "http://t.easya.cc";
    public static final String OS_TICKET_NEW_TICKET = String.format("%s/open_api.php", OS_TICKET);
    public static final String OS_TICKET_CLOSE_TICKET = String.format("%s/api/ticket_close.php", OS_TICKET);
    public static final String OS_TICKET_SYNC_TICKETS = String.format("%s/api/ticket_sync.php", OS_TICKET);

    /**
     * 与 OsTicket 系统中的 Topic 对应的
     */
    public enum TopicID {
        BILLING {
            @Override
            public int id() {
                return 2;
            }
        },
        SUPPORT {
            @Override
            public int id() {
                return 1;
            }
        },
        REVIEW {
            @Override
            public int id() {
                return 3;
            }
        },
        FEEDBACK {
            @Override
            public int id() {
                return 4;
            }
        };

        public abstract int id();
    }

    /**
     * 根据 TicketIds 向 OsTicket 系统获取同步用的数据
     *
     * @param ticketIds
     * @return
     */
    public static JSONObject communicationWithOsTicket(List<String> ticketIds) {
        Logger.info("Update Tickets: %s", StringUtils.join(ticketIds, ","));
        return HTTP.postJson(OsTicket.OS_TICKET_SYNC_TICKETS,
                Arrays.asList(new BasicNameValuePair("ticketIds", StringUtils.join(ticketIds, ",")))
        );
    }

    /**
     * 向 OsTicket 系统开一个新的 Ticket.
     *
     * @param name     Ticket 的用户名称
     * @param email    Ticket 回复的邮箱
     * @param subject  Ticket 的标题
     * @param content  Ticket 的内容
     * @param topicId  Ticket 所处的 Topic, Topic 会有对应的优先级(1:Support, 2:Billing, 3:Review, 4:Feedback)
     * @param errorMsg 系统中需要 log 的错误信息,主要记录 orderid, reviewid 等这样的信息
     * @return 如果成功, 则返回 TicketId , 否则返回"空"字符串
     */
    public static String openOsTicket(String name, String email, String subject, String content, TopicID topicId,
                                      String errorMsg) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("phone", ""));
        params.add(new BasicNameValuePair("phone_ext", ""));
        // 如果 Topicid 不再 1~4 之间则默认使用 1(Support)
        params.add(new BasicNameValuePair("topicId", topicId.id() + ""));
        params.add(new BasicNameValuePair("submit_x", "Submit Ticket"));
        params.add(new BasicNameValuePair("subject", subject));
        params.add(new BasicNameValuePair("message", content));

        try {
            JSONObject obj = HTTP.postJson(OsTicket.OS_TICKET_NEW_TICKET, params);
            if(obj == null) {
                Logger.error("OpenOsTicket fetch content Error!");
                return "";
            }
            if(obj.getBoolean("flag")) { // 成功创建
                return obj.getString("tid");
            } else {
                Logger.warn(String.format("%s post to OsTicket failed because of [%s]", errorMsg,
                        obj.getString("message")));
            }
        } catch(Exception e) {
            Logger.error("OpenOsTicket fetch IO Error!");
        }
        return "";
    }

    /**
     * 调用 OsTicket 的 API, 进行 OsTicket 的关闭
     *
     * @param ticketId
     * @param user
     * @return
     */
    public static boolean closeOsTicket(String ticketId, String user, String note) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("id", ticketId));
        params.add(new BasicNameValuePair("user", user));
        params.add(new BasicNameValuePair("note", note));
        JSONObject obj = HTTP.postJson(OsTicket.OS_TICKET_CLOSE_TICKET, params);
        if(obj == null) {
            Logger.error("CloseOsTicket fetch content Error!");
            return false;
        }
        if(obj.getBoolean("flag"))
            return true;
        else {
            Logger.warn(String.format("%s[%s] Ticket close failed.", ticketId, user));
            return false;
        }

    }
}
