package models.support;

import com.google.gson.annotations.Expose;
import models.product.Category;
import org.apache.commons.lang.StringUtils;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Model;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;

/**
 * Ticket 产生的问题的原因
 * <p/>
 * User: wyattpan
 * Date: 7/26/12
 * Time: 4:29 PM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class TicketReason extends Model {

    @Column(length = 55)
    @Required
    @MaxSize(55)
    @MinSize(3)
    @Unique
    @Expose
    public String reason;

    @ManyToOne
    @Required
    public Category category;

    /**
     * 这个 Tag 所含有的 Ticket
     */
    @ManyToMany(mappedBy = "reasons")
    @OrderBy("createAt DESC")
    public List<Ticket> tickets = new ArrayList<Ticket>();

    @Lob
    @Required
    @MinSize(3)
    public String memo = " ";

    public TicketReason checkAndSave() {
        this.check();
        return this.save();
    }

    public TicketReason checkAndUpdate() {
        this.check();
        return this.save();
    }

    private void check() {
        this.reason = this.reason.trim();
        this.memo = this.memo.trim();
    }

    public static TicketReason findByReason(String reason) {
        if(StringUtils.contains(reason, ":")) reason = StringUtils.split(reason, ":")[1];
        return TicketReason.find("reason=?", reason).first();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        TicketReason that = (TicketReason) o;

        if(id != null ? !id.equals(that.id) : that.id != null) return false;
        if(reason != null ? !reason.equals(that.reason) : that.reason != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (reason != null ? reason.hashCode() : 0);
        return result;
    }

    public String name() {
        return String.format("%s:%s", this.category.categoryId, this.reason);
    }

    /**
     * 将此 Reaton 所关联的 Ticket 拆分成 Review 与 Feedback 与 Ticket
     *
     * @return T2: _.1[Review] , _.2[Feedback], _.3[Ticket]
     */
    public F.T3<List<Ticket>, List<Ticket>, List<Ticket>> divTicketsInToRAndF() {
        List<Ticket> reviews = new ArrayList<Ticket>();
        List<Ticket> feedbacks = new ArrayList<Ticket>();
        List<Ticket> tickets = new ArrayList<Ticket>();
        if(this.tickets.size() != 0) {
            for(Ticket t : this.tickets) {
                if(t.type == Ticket.T.REVIEW)
                    reviews.add(t);
                else if(t.type == Ticket.T.FEEDBACK)
                    feedbacks.add(t);
                else if(t.type == Ticket.T.TICKET)
                    tickets.add(t);
            }
        }
        return new F.T3<List<Ticket>, List<Ticket>, List<Ticket>>(reviews, feedbacks, tickets);
    }

    /**
     * 检查并且删除
     */
    public TicketReason checkAndRemove() {
        int relateTicketSize = this.tickets.size();
        if(relateTicketSize > 0) throw new FastRuntimeException("有 Ticket 关联了 TicketReason " + this.name() + ", 无法删除.");
        return this.delete();
    }
}
