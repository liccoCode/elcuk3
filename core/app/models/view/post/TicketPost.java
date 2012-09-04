package models.view.post;

import helper.Dates;
import models.support.Ticket;
import models.support.TicketState;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Required;
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
public class TicketPost {
    @Required
    public Date from;
    @Required
    public Date to;
    public String search;
    public TicketState state;
    public boolean isSuccess = false;
    public boolean isRemove = false;
    public long userid = 0;

    public TicketPost() {
        this.from = DateTime.now().minusMonths(2).toDate();
        this.to = new Date();
        this.state = TicketState.NEW;
    }

    public List<Ticket> tickets() {
        // 如果有 isRemove 参与, 那么 Tickets 不参加搜索
        if(this.isRemove) return new ArrayList<Ticket>();
        F.T2<String, List<Object>> params = params(Ticket.T.TICKET);
        if(StringUtils.isBlank(this.search)) {
            return Ticket.find(params._1, params._2.toArray()).fetch();
        } else {
            // 这个循环表示为 like 添加多少个 search 参数, Review, Feedback 相同
            for(int i = 0; i < 2; i++) params._2.add(search());
            return Ticket.find(params._1 +
                    "AND (osTicketId LIKE ? OR fid LIKE ?)",
                    params._2.toArray()).fetch();
        }
    }

    public List<Ticket> feedbacks() {
        F.T2<String, List<Object>> params = params(Ticket.T.FEEDBACK);
        if(StringUtils.isBlank(this.search)) {
            return Ticket.find(params._1, params._2.toArray()).fetch();
        } else {
            for(int i = 0; i < 4; i++) params._2.add(search());

            return Ticket.find(params._1 +
                    "AND (osTicketId LIKE ? OR fid LIKE ? OR feedback.feedback LIKE ? OR feedback.email LIKE ?)",
                    params._2.toArray()).fetch();
        }
    }

    public List<Ticket> reviews() {
        F.T2<String, List<Object>> params = params(Ticket.T.REVIEW);
        if(StringUtils.isBlank(this.search)) {
            return Ticket.find(params._1, params._2.toArray()).fetch();
        } else {
            for(int i = 0; i < 7; i++) params._2.add(search());
            return Ticket.find(params._1 +
                    "AND (osTicketId LIKE ? OR fid LIKE ? OR review.review LIKE ? OR review.title LIKE ? OR review.userid LIKE ? OR review.username LIKE ? OR review.reviewId LIKE ?)",
                    params._2.toArray()).fetch();
        }
    }

    private String search() {
        return String.format("%%%s%%", this.search);
    }

    private F.T2<String, List<Object>> params(Ticket.T type) {
        StringBuilder sbd = new StringBuilder("createAt>=? AND createAt<=? AND type=?");
        List<Object> params = new ArrayList<Object>();
        params.add(this.from);
        params.add(this.to);
        params.add(type);
        if(this.state != null) {
            sbd.append(" AND state=?");
            params.add(this.state);
        }

        if(this.isSuccess) {
            sbd.append(" AND isSuccess=?");
            params.add(this.isSuccess);
        }

        if(this.isRemove) {
            switch(type) {
                case FEEDBACK:
                    sbd.append(" AND feedback.isRemove=?");
                    params.add(this.isRemove);
                    break;
                case REVIEW:
                    sbd.append(" AND review.isRemove=?");
                    params.add(this.isRemove);
                    break;
                case TICKET:
                    break;
            }
        }

        if(this.userid > 0) {
            sbd.append(" AND resolver.id=?");
            params.add(this.userid);
        }

        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }

    public void setTo(Date date) {
        this.to = Dates.night(date);
    }
}
