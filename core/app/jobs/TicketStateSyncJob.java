package jobs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import helper.OsTicket;
import helper.Webs;
import models.Jobex;
import models.support.Ticket;
import models.support.TicketState;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import play.Logger;
import play.cache.Cache;
import play.jobs.Every;
import play.jobs.Job;
import play.libs.F;

import java.util.*;

/**
 * <pre>
 * 向 OsTicket 系统进行 Ticket 的数据同步
 * 周期:
 * - 轮询周期: 1mn
 * - Duration: 10mn
 * </pre>
 * User: wyattpan
 * Date: 7/30/12
 * Time: 5:05 PM
 */
@Every("1mn")
public class TicketStateSyncJob extends Job {

    @Override
    public void doJob() {
        if(!Jobex.findByClassName(TicketStateSyncJob.class.getName()).isExcute()) return;
        List<Ticket> tickets = Ticket.checkStateTickets(100);
        List<String> ticketIds = new ArrayList<String>();
        for(Ticket t : tickets) ticketIds.add(t.osTicketId());

        try {
            JsonObject rsObj = OsTicket.communicationWithOsTicket(ticketIds);
            TicketStateSyncJob.syncOsTicketDetailsIntoSystem(rsObj, tickets);
        } catch(IllegalStateException e) {
            if(StringUtils.contains(e.getMessage(), "77"))
                Logger.warn("The Request IP is not valid.");
            else
                Logger.warn(Webs.E(e));
        } finally {
            Cache.delete("review.index"); // 此 Job 更新完成后, 需要清空一次首页的缓存.
        }

    }

    /**
     * 将 OsTicket 的信息与系统内的 Tickets 信息进行同步处理
     *
     * @param rsObj
     * @param tickets
     * @return 成功与失败的 Ticket, Tuple._1:success, Tuple._2:faled
     */
    public static F.T2<List<Ticket>, List<Ticket>> syncOsTicketDetailsIntoSystem(JsonObject rsObj,
                                                                                 List<Ticket> tickets) {
        Map<String, List<OsMsg>> msgMap = OsMsg.msgsMap(rsObj.getAsJsonArray("msgs"));
        Map<String, List<OsResp>> respMap = OsResp.respsMap(rsObj.getAsJsonArray("resps"));

        F.T2<List<Ticket>, List<Ticket>> rtTickets = new F.T2<List<Ticket>, List<Ticket>>(
                new ArrayList<Ticket>(), new ArrayList<Ticket>());
        // 处理一个一个的 Ticket 的状态改变
        for(Ticket t : tickets) {
            try {
                if(t.state == null) t.state = TicketState.NEW;
                // 此 Ticket 客户的邮件
                List<OsMsg> ticketMsg = msgMap.get(t.osTicketId());
                // 此 Ticket 我们的回信
                List<OsResp> ticketResp = respMap.get(t.osTicketId());

                t.state = t.state.nextState(t, ticketMsg, ticketResp);
                F.T3<Date, Date, Date> lastXXXDateTime = TicketStateSyncJob
                        .lastXXXDatetime(ticketMsg, ticketResp);
                t.lastResponseTime = lastXXXDateTime._1;
                t.lastMessageTime = lastXXXDateTime._2;
                t.lastSyncTime = lastXXXDateTime._3;
                if(ticketMsg == null) t.messageTimes = 0;
                else t.messageTimes = ticketMsg.size();
                if(ticketResp == null) t.responseTimes = 0;
                else t.responseTimes = ticketResp.size();

                t.parseResponse(ticketResp);

                rtTickets._1.add(t.<Ticket>save());
            } catch(Exception e) {
                Logger.warn("syncOsTicketDetailsIntoSystem %s", Webs.E(e));
                rtTickets._2.add(t);
            }
        }
        return rtTickets;
    }

    /**
     * 检查这个 Ticket 是否有客户的新回复
     *
     * @param resps
     * @param msgs
     * @return
     */
    public static F.T2<Boolean, TicketStateSyncJob.OsMsg> ishaveNewCustomerEmail(
            List<TicketStateSyncJob.OsResp> resps, List<TicketStateSyncJob.OsMsg> msgs) {
        TicketStateSyncJob.OsMsg newMsg = TicketStateSyncJob.OsMsg.lastestMsg(msgs);
        if(msgs.size() == 1) newMsg = null; // 需要排除自行在 OsTicket 中创建 Ticket 的时候的那一个客户 Message
        TicketStateSyncJob.OsResp newResp = TicketStateSyncJob.OsResp.lastestResp(resps);
        if(newMsg != null && newResp != null) {
            if(newMsg.created.getTime() > newResp.created.getTime())
                return new F.T2<Boolean, TicketStateSyncJob.OsMsg>(true, newMsg);
        }
        return new F.T2<Boolean, TicketStateSyncJob.OsMsg>(false, null);
    }

    /**
     * 检查这个 Ticket 是否有新的操作人员的回复
     *
     * @param resps
     * @param msgs
     * @return
     */
    public static F.T2<Boolean, TicketStateSyncJob.OsResp> ishaveNewOperatorResponse(
            List<TicketStateSyncJob.OsResp> resps, List<TicketStateSyncJob.OsMsg> msgs) {
        TicketStateSyncJob.OsMsg newMsg = TicketStateSyncJob.OsMsg.lastestMsg(msgs);
        TicketStateSyncJob.OsResp newResp = TicketStateSyncJob.OsResp.lastestResp(resps);
        if(newMsg != null && newResp != null) {
            if(newResp.created.getTime() > newMsg.created.getTime())
                return new F.T2<Boolean, TicketStateSyncJob.OsResp>(true, newResp);
        }
        return new F.T2<Boolean, TicketStateSyncJob.OsResp>(false, null);
    }


    /**
     * 联系客户的最后时间, 客户回复的最后时间, 最后进行同步的时间
     */
    private static F.T3<Date, Date, Date> lastXXXDatetime(List<OsMsg> msgs, List<OsResp> resps) {
        OsResp resp = OsResp.lastestResp(resps);
        OsMsg msg = OsMsg.lastestMsg(msgs);
        return new F.T3<Date, Date, Date>(resp == null ? null : resp.created,
                msg == null ? null : msg.created, new Date());
    }


    /**
     * 用来解析 OsTicket 的 Message(客户回信)
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
                msg.created = DateTime.parse(obj.get("created").getAsString(),
                        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
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
     * 用来解析 OsTicket 的 Response(我方联系)
     */
    public static class OsResp {
        public String response_id;
        public String msg_id;
        public Date created;
        public String ticketId;
        public String ticket_id;

        public static List<OsResp> resps(JsonArray json) {
            List<OsResp> resps = new ArrayList<OsResp>();
            for(JsonElement el : json) {
                JsonObject obj = el.getAsJsonObject();
                OsResp resp = new OsResp();
                resp.response_id = obj.get("response_id").getAsString();
                resp.msg_id = obj.get("msg_id").getAsString();
                resp.created = DateTime.parse(obj.get("created").getAsString(),
                        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
                resp.ticketId = obj.get("ticketId").getAsString();
                resp.ticket_id = obj.get("ticket_id").getAsString();
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
