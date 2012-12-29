package models.view.post;

import helper.Dates;
import models.support.Ticket;
import models.support.TicketState;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/30/12
 * Time: 5:36 PM
 */
public class TicketPost extends Post<Ticket> {
    public TicketState state = null;

    /**
     * 对 Ticket 是成功;
     * 对 Feedback 与 Review 是升高或者删除
     */
    public Boolean isSuccess = null;

    public long userid = 0;

    public DT dateType = DT.createAt;

    public Ticket.T type = Ticket.T.REVIEW;

    public Boolean start = null;

    public static enum DT {
        createAt,
        lastSyncTime,
        lastMessageTime,
        lastResponseTime;

        @Override
        public String toString() {
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
        this.state = TicketState.NEW;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT t FROM Ticket t WHERE 1=1 AND ");
        List<Object> params = new ArrayList<Object>();
        if(dateType != null) {
            sbd.append("t.").append(dateType.name()).append(">=? and ")
                    .append("t.").append(dateType.name()).append("<=? ");
            params.add(Dates.morning(this.from));
            params.add(Dates.night(this.to));
        }


        if(this.type != null) {
            sbd.append("AND t.type=? ");
            params.add(this.type);
        }

        if(this.state != null) {
            sbd.append("AND t.state=? ");
            params.add(this.state);
        }

        if(this.isSuccess != null) {
            if(this.type == Ticket.T.REVIEW) {
                sbd.append("AND (");
                if(this.isSuccess) {
                    sbd.append("t.review.isRemove=?").append(" OR ")
                            .append("t.review.rating>t.review.lastRating");
                    params.add(this.isSuccess);
                } else {
                    sbd.append("t.review.rating<t.review.lastRating");
                }
                sbd.append(") ");
            } else if(this.type == Ticket.T.FEEDBACK) {
                sbd.append("AND t.feedback.isRemove=? ");
                params.add(this.isSuccess);
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

        System.out.println("SQL: " + sbd);
        System.out.println("Params:" + params);
        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }

    public Long count(F.T2<String, List<Object>> params) {
        StringBuilder sbd = new StringBuilder(params._1);
        String where = sbd.subSequence(sbd.indexOf("1=1"), sbd.length()).toString();
        return Ticket.count("SELECT COUNT(t) FROM Ticket t WHERE " + where, params._2.toArray());
    }

    @Override
    public List<Ticket> query() {
        F.T2<String, List<Object>> t2 = this.params();
        this.count = this.count(t2);
        return Ticket.find(t2._1 + " ORDER BY t.createAt DESC", t2._2.toArray())
                .fetch(this.page, this.perSize);
    }
}
