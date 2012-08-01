package jobs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import helper.HTTP;
import models.support.Ticket;
import org.apache.commons.lang.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import play.jobs.Job;

import java.util.*;

/**
 * 向 OsTicket 系统进行 Ticket 的数据同步
 * User: wyattpan
 * Date: 7/30/12
 * Time: 5:05 PM
 */
public class TicketStateSyncJob extends Job {

    @Override
    public void doJob() {
        List<Ticket> tickets = Ticket.checkStateTickets(10);
        List<String> ticketIds = new ArrayList<String>();
        for(Ticket t : tickets) ticketIds.add(t.osTicketId());

        JsonElement rs = HTTP.json("http://t.easyacceu.com/api/tickt_sync?",
                Arrays.asList(new BasicNameValuePair("ticketIds", StringUtils.join(ticketIds, ",")))
        );
        JsonObject rsObj = rs.getAsJsonObject();

        Map<String, List<OsMsg>> msgMap = OsMsg.msgsMap(rsObj.getAsJsonArray("msgs"));
        Map<String, List<OsResp>> respMap = OsResp.respsMap(rsObj.getAsJsonArray("resps"));

        // 处理一个一个的 Ticket 的状态改变
        for(Ticket t : tickets) {

        }

    }


    /**
     * 内部用来解析 OsTicket 的 Message
     */
    public static class OsMsg {
        public String msg_id;
        public String ticket_id;
        public Date created;
        public String ticketId;

        public static List<OsMsg> msgs(JsonArray json) {
            List<OsMsg> msgs = new ArrayList<OsMsg>();
            for(JsonElement el : json) {
                JsonObject obj = el.getAsJsonObject();
                OsMsg msg = new OsMsg();
                msg.msg_id = obj.get("msg_id").getAsString();
                msg.ticket_id = obj.get("ticket_id").getAsString();
                msg.created = DateTime.parse(obj.get("created").getAsString()).toDate();
                msg.ticketId = obj.get("ticketId").getAsString();
                msgs.add(msg);
            }
            return msgs;
        }

        public static Map<String, List<OsMsg>> msgsMap(JsonArray json) {
            List<OsMsg> msgs = OsMsg.msgs(json);
            Map<String, List<OsMsg>> msgMap = new HashMap<String, List<OsMsg>>();
            for(OsMsg msg : msgs) {
                if(msgMap.containsKey(msg.ticketId))
                    msgMap.get(msg.ticketId).add(msg);
                else
                    msgMap.put(msg.ticketId, new ArrayList<OsMsg>(Arrays.asList(msg)));
            }
            return msgMap;
        }

        /**
         * 从 msgs[List] 中寻找出最新的 msg
         *
         * @param msgs
         * @return
         */
        public static OsMsg lastestMsg(List<OsMsg> msgs) {
            OsMsg tmp = null;
            if(msgs == null || msgs.size() == 0) return null;
            for(OsMsg msg : msgs) {
                if(tmp == null) tmp = msg;
                else {
                    if(tmp.created.getTime() < msg.created.getTime())
                        tmp = msg;
                }
            }
            return tmp;
        }
    }

    /**
     * 内部用来解析 OsTicket 的 Response
     */
    public static class OsResp {
        public String response_id;
        public String msg_id;
        public Date created;
        public String ticketId;

        public static List<OsResp> resps(JsonArray json) {
            List<OsResp> resps = new ArrayList<OsResp>();
            for(JsonElement el : json) {
                JsonObject obj = el.getAsJsonObject();
                OsResp resp = new OsResp();
                resp.response_id = obj.get("response_id").getAsString();
                resp.msg_id = obj.get("msg_id").getAsString();
                resp.created = DateTime.parse(obj.get("created").getAsString()).toDate();
                resp.ticketId = obj.get("ticketId").getAsString();
                resps.add(resp);
            }
            return resps;
        }

        public static Map<String, List<OsResp>> respsMap(JsonArray json) {
            List<OsResp> resps = OsResp.resps(json);
            Map<String, List<OsResp>> respMap = new HashMap<String, List<OsResp>>();
            for(OsResp resp : resps) {
                if(respMap.containsKey(resp.ticketId))
                    respMap.get(resp.ticketId).add(resp);
                else
                    respMap.put(resp.ticketId, new ArrayList<OsResp>(Arrays.asList(resp)));
            }
            return respMap;
        }

        /**
         * 从 resps[List] 中寻找出最新的 resp
         *
         * @param resps
         * @return
         */
        public static OsResp lastestResp(List<OsResp> resps) {
            OsResp tmp = null;
            if(resps == null || resps.size() == 0) return null;
            for(OsResp resp : resps) {
                if(tmp == null) tmp = resp;
                else {
                    if(tmp.created.getTime() < resp.created.getTime())
                        tmp = resp;
                }
            }
            return tmp;
        }
    }
}
