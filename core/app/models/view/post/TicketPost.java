package models.view.post;

import helper.Dates;
import models.support.Ticket;
import models.support.TicketState;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/30/12
 * Time: 5:36 PM
 */
public class TicketPost extends Post<Ticket> {
    public List<TicketState> states = new ArrayList<TicketState>();
    public Date from = super.from;
    public Date to = super.to;

    /**
     * 对 Ticket 是成功;
     * 对 Feedback 与 Review 是升高或者删除
     */
    public Boolean isSuccess = null;

    public long userid = 0;

    public DT dateType = DT.createAt;

    public Ticket.T type = Ticket.T.REVIEW;

    public Boolean start = null;

    /**
     * 只在内部使用;
     * - -|| private 竟然通过 Tickets.index 不会设置值
     */
    public boolean isHangUp = false;
    public boolean isDealing = false;

    public static enum DT {
        createAt,
        lastSyncTime,
        lastMessageTime,
        lastResponseTime;

        /**
         * 因为 Play! 为枚举也调用 toString() 而不是 name(), 导致 Tickets.index(obj) 这样调用 Controller
         * 传递含有枚举值的时候需要将枚举的 toString() 保持为不变才行. 否则会出现:
         * 期盼的: p.dateType=createAt
         * 变为: p.dateType=创建时间
         *
         * @return
         */
        public String to_s() {
            switch(this) {
                case createAt:
                    return "创建时间";
                case lastSyncTime:
                    return "最后同步时间";
                case lastMessageTime:
                    return "客户最后联系时间";
                case lastResponseTime:
                    return "客户最后回复时间";
                default:
                    return "None";
            }
        }

    }

    public TicketPost() {
        this.from = DateTime.now().minusMonths(2).toDate();
        this.to = new Date();
        this.states = new ArrayList<TicketState>();
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT t FROM Ticket t WHERE 1=1 AND ");
        List<Object> params = new ArrayList<Object>();
        if(dateType == null) dateType = DT.createAt;
        // 必须保证有时间因素
        sbd.append("t.").append(dateType.name()).append(">=? and ")
                .append("t.").append(dateType.name()).append("<=? ");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));


        if(this.type != null) {
            sbd.append("AND t.type=? ");
            params.add(this.type);
        }

        if(this.states.size() > 0) {
            // 因为 Play 会自动填充
            if(!this.states.contains(null)) {
                sbd.append("AND (");
                for(TicketState s : this.states) {
                    sbd.append("t.state=? OR ");
                    params.add(s);
                }
                sbd.delete(sbd.lastIndexOf("OR"), sbd.length()).append(") ");
            }
        }

        if(isHangUp) {
            this.hangUp(sbd, params);
            return new F.T2<String, List<Object>>(sbd.toString(), params);
        }
        if(isDealing) {
            this.dealing(sbd, params);
            return new F.T2<String, List<Object>>(sbd.toString(), params);
        }

        if(this.isSuccess != null) {
            if(this.type == Ticket.T.REVIEW) {
                sbd.append("AND (");
                if(this.isSuccess) {
                    /**
                     * 1. rating >= 4 , isRemove == false, not in NEW(状态在这不做限制)
                     * 2. isRemove == true
                     */
                    sbd.append("(t.review.isRemove=false").append(" AND ")
                            .append("t.review.rating>=4)").append(" OR ")
                            .append("(t.review.isRemove=true)");
                } else {
                    /**
                     * lastRating>rating
                     */
                    sbd.append("t.review.lastRating>t.review.rating AND t.review.isRemove=false");
                }
                sbd.append(") ");
            } else if(this.type == Ticket.T.FEEDBACK) {
                if(this.isSuccess) {
                    sbd.append("AND t.feedback.isRemove=? ");
                    params.add(this.isSuccess);
                } else {
                    sbd.append("AND t.feedback.isRemove=? ");
                    // 创建时间在 60 天前, 并且没有删除
                    params.set(1, DateTime.now().minusDays(60).toDate());
                    params.add(this.isSuccess);
                }
            }
        }

        if(this.start != null) {
            sbd.append("AND t.isStart=? ");
            params.add(this.start);
        }

        if(this.userid > 0) {
            sbd.append("AND t.resolver.id=? ");
            params.add(this.userid);
        }

        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sbd.append("AND (")
                    .append("t.memo LIKE ?").append(" OR ")
                    .append("t.osTicketId LIKE ?").append(" OR ")
                    .append("t.review.alrId LIKE ?").append(" OR ")
                    .append("t.feedback.orderId LIKE ? ").append(" OR ")
                    .append("t.fid LIKE ?").append(")");
            // 5 个 ?
            for(int i = 0; i < 5; i++) {
                params.add(word);
            }
        }

        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }

    public Long count(F.T2<String, List<Object>> params) {
        StringBuilder sbd = new StringBuilder(params._1);
        String where = sbd.subSequence(sbd.indexOf("1=1"), sbd.length()).toString();
        return Ticket.count("SELECT COUNT(t) FROM Ticket t WHERE " + where, params._2.toArray());
    }

    @Override
    public Long getTotalCount() {
        return Ticket.count();
    }

    @Override
    public List<Ticket> query() {
        F.T2<String, List<Object>> t2 = this.params();
        this.count = this.count(t2);
        return Ticket.find(t2._1 + " ORDER BY t.createAt DESC", t2._2.toArray())
                .fetch(this.page, this.perSize);
    }

    /**
     * 对于 HangUp Group 的参数
     *
     * @param sbd
     * @param params
     */
    private void hangUp(StringBuilder sbd, List<Object> params) {
        if(this.type == Ticket.T.REVIEW) {
            sbd.append(" AND t.review.isRemove=false AND t.review.lastRating=t.review.rating ");
        } else if(this.type == Ticket.T.FEEDBACK) {
            sbd.append(" AND t.feedback.isRemove=false");
        }
    }

    /**
     * 对于 dealing Group 的参数
     * ps: 尽管参数一样, 但还暴露一个接口
     *
     * @param sbd
     * @param params
     */
    private void dealing(StringBuilder sbd, List<Object> params) {
        this.hangUp(sbd, params);
    }

    /**
     * 对于不同分组的搜索
     *
     * @return 调整自身参数, 为对应的 Group 搜索; self
     */
    public TicketPost groupSearch(String group, Ticket.T t, int days) {
        DateTime now = DateTime.now();
        this.from = now.minusDays(days).toDate();
        this.type = t;
        if(StringUtils.equalsIgnoreCase("SUCC", group)) {
            this.isSuccess = true;
            if(t == Ticket.T.TICKET)
                this.states = Arrays.asList(TicketState.CLOSE, TicketState.PRE_CLOSE);
        } else if(StringUtils.equalsIgnoreCase("FAIL", group)) {
            this.isSuccess = false;
        } else if(StringUtils.equalsIgnoreCase("HANGUP", group)) {
            this.states = Arrays.asList(TicketState.NO_RESP);
            this.isHangUp = true;
        } else if(StringUtils.equalsIgnoreCase("DEALING", group)) {
            this.isDealing = true;
            this.states = Arrays.asList(TicketState.NEW, TicketState.TWO_MAIL,
                    TicketState.NEW_MSG, TicketState.MAILED);
        }
        return this;
    }

    public TicketPost groupSearch(String group, Ticket.T t) {
        return groupSearch(group, t, 90);
    }
}
