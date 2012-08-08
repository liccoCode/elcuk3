package models.market;

import helper.GTs;
import helper.Webs;
import models.support.Ticket;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;
import play.Logger;
import play.data.validation.Email;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.GenericModel;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/14/12
 * Time: 5:15 PM
 */
@Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Feedback extends GenericModel {

    public enum S {
        HANDLING,
        SLOVED,
        END,
        /**
         * 没有办法处理了, 只能留着
         */
        LEFT
    }

    @OneToOne
    public Orderr orderr;

    @OneToOne
    public Account account;

    @OneToOne(mappedBy = "feedback", fetch = FetchType.LAZY)
    public Ticket ticket;

    /**
     * Feedback 每一个订单只有一个, 所以直接使用 OrderId 作为 feedback Id
     */
    @Id
    @Required
    public String orderId;

    public Date createDate;

    @Required
    public Float score;

    @Email
    public String email;

    /**
     * 客户留下的 Feedback
     */
    @Lob
    public String comment;

    @Lob
    public String memo = " ";

    @Enumerated(EnumType.STRING)
    public Account.M market;

    /**
     * 发送邮件警告的次数, 最多 3 次;
     */
    public Integer mailedTimes = 0;

    /**
     * 关联的 OsTicket 系统中的 Id, 如果没有则需要向 OsTicket 系统指定的 URL 创建 Ticket.
     */
    public String osTicketId;

    /**
     * 是否解决了,等等状态
     */
    public S state;

    public Feedback() {
    }

    /**
     * 检查这个 Feedback, 如果 <= 3 则发送警告邮件, 并且没有创建 OsTicket 则去创建 OsTicket;
     */
    public void checkMailAndTicket() {
        /**
         * 1. 判断是否需要发送警告邮件;
         * 2. 判断是否需要去 OsTicket 系统中创建 Ticket.
         */
        if(this.score > 3 || this.state == S.SLOVED || this.state == S.END || this.state == S.LEFT) return;

        if(this.score <= 3 && this.isSelfBuildListing())
            this.ticket = this.openOsTicket(null);

        if(this.mailedTimes == null)
            Mails.feedbackWarnning(this);

        this.save();
    }

    /**
     * 判断产生这个 Feedback 的 Listing(s) 是否含有自建的 Listing;
     * ps: 因为 Feedback 对应 Order, 而 Order 可以拥有很多 OrderItem
     *
     * @return
     */
    private boolean isSelfBuildListing() {
        List<OrderItem> oitems = OrderItem.find("order.orderId=?", this.orderId).fetch();
        for(OrderItem oi : oitems) {
            if(Listing.isSelfBuildListing(oi.listingName))
                return true;
        }
        return false;
    }

    public Feedback updateAttr(Feedback newFeedback) {
        if(!this.orderId.equalsIgnoreCase(newFeedback.orderId))
            throw new FastRuntimeException("Feedback OrderId is not the same!");
        if(newFeedback.score != null && !newFeedback.score.equals(this.score)) // score 不一样了, 需要记录
            this.memo = String.format("Score from %s to %s At %s\r\n", this.score, newFeedback.score, DateTime.now().toString("yyyy-MM-dd HH:mm:ss")) + this.memo;

        if(newFeedback.score != null) this.score = newFeedback.score;
        if(StringUtils.isNotBlank(newFeedback.comment)) this.comment = newFeedback.comment;
        if(StringUtils.isNotBlank(newFeedback.email)) this.email = newFeedback.email;

        if(newFeedback.state != null && this.state == S.HANDLING) this.state = newFeedback.state;

        return this.save();
    }

    public Feedback checkAndSave(Account acc) {
        if(!Validation.current().valid(this).ok)
            throw new FastRuntimeException("Not Valid, more details see Validation.errors()");
        this.account = acc;
        this.orderr = Orderr.findById(this.orderId);
        if(!this.isSelfBuildListing())
            this.memo = String.format("这个 Feedback 对应的 Listing 非自建.\r\n") + this.memo;
        return this.save();
    }

    public Feedback checkSaveOrUpdate() {
        Feedback manager = Feedback.findById(this.orderId);
        if(manager != null && manager.isPersistent()) { // 系统中存在过的
            return manager.updateAttr(this);
        } else {
            return this.save();
        }
    }

    /**
     * 向 OsTicket 系统开启一个新的 Ticket
     *
     * @param title 可以调整的在 OsTicket 中创建的 Ticket 的 title, 回复给客户的邮件 Title 也是如此.
     */
    public Ticket openOsTicket(String title) {
        if(StringUtils.isNotBlank(this.osTicketId)) {
            Logger.info("Feedback OsTicket is exist! %s", this.osTicketId);
            return null;
        }
        String name = this.orderId;
        String email = this.email;
        String subject = title;
        String content = GTs.render("OsTicketFeedbackWarn", GTs.newMap("f", this).build());

        if(this.orderr != null)
            name = String.format("%s - %s", this.orderr.buyer, this.market.toString());
        if(StringUtils.isBlank(subject))
            subject = "You left a negative feedback, Please give us a chance to make up!";

        this.osTicketId = Webs.openOsTicket(name, email, subject, content, Webs.TopicID.FEEDBACK, "Feedback " + this.orderId);
        if(StringUtils.isBlank(this.osTicketId)) { // 这表示没在 OsTicket 系统没有创建成功
            return null;
        } else {
            if(this.ticket == null) return new Ticket(this);
            else return this.ticket;
        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Feedback");
        sb.append("{orderId='").append(orderId).append('\'');
        sb.append(", createDate=").append(createDate);
        sb.append(", score=").append(score);
        sb.append(", email='").append(email).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append(", memo='").append(memo).append('\'');
        sb.append(", state=").append(state);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Feedback feedback = (Feedback) o;

        if(orderId != null ? !orderId.equals(feedback.orderId) : feedback.orderId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (orderId != null ? orderId.hashCode() : 0);
        return result;
    }
}
