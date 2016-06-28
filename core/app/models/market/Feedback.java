package models.market;

import helper.Dates;
import models.product.Category;
import models.product.Product;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.Duration;
import play.data.validation.Email;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.GenericModel;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/14/12
 * Time: 5:15 PM
 */
@Entity
@DynamicUpdate
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Feedback extends GenericModel {

    public static final String FRONT_TABLE = "Feedback.frontPageTable";

    @OneToOne
    public Orderr orderr;

    @OneToOne
    public Account account;

    /**
     * Feedback 每一个订单只有一个, 所以直接使用 OrderId 作为 feedback Id
     */
    @Id
    @Required
    public String orderId;

    public Date createDate;

    /**
     * 什么时候更新
     */
    public Date updateAt;

    @Required
    public Float score;

    @Email
    public String email;

    /**
     * 客户留下的 Feedback
     */
    @Lob
    public String feedback;

    @Lob
    public String memo = " ";

    @Enumerated(EnumType.STRING)
    public M market;

    /**
     * 发送邮件警告的次数, 最多 3 次;
     */
    public Integer mailedTimes = 0;

    /**
     * 关联的 OsTicket/jitbit 系统中的 Id, 如果没有则需要向 OsTicket 系统指定的 URL 创建 Ticket.
     */
    public String osTicketId;

    /**
     * 是否被删除
     */
    public boolean isRemove = false;


    public Feedback() {
    }

    @PrePersist
    @PreUpdate
    public void beforeUpdate() {
        this.updateAt = new Date();
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
            this.comment(String.format("Score from %s to %s At %s", this.score, newFeedback.score,
                    Dates.date2DateTime()));

        if(newFeedback.score != null) this.score = newFeedback.score;
        if(StringUtils.isNotBlank(newFeedback.feedback)) this.feedback = newFeedback.feedback;
        if(StringUtils.isNotBlank(newFeedback.email)) this.email = newFeedback.email;
        if(StringUtils.isNotBlank(newFeedback.memo)) this.comment(newFeedback.memo);

        return this.save();
    }

    public Feedback checkAndSave(Account acc) {
        if(!Validation.current().valid(this).ok)
            throw new FastRuntimeException("Not Valid, more details see Validation.errors()");
        this.account = acc;
        this.orderr = Orderr.findById(this.orderId);
        if(!this.isSelfBuildListing())
            this.comment("这个 Feedback 对应的 Listing 非自建.");
        return this.save();
    }


    public void comment(String memo) {
        if(!StringUtils.contains(this.memo, memo))
            this.memo = String.format("%s%n%s", memo, this.memo);
    }


    /**
     * 判断此 Feedback 是否已经过期了? 过期了表示无法再进行处理了.
     *
     * @return
     */
    public boolean isExpired() {
        return new Duration(this.createDate.getTime(), System.currentTimeMillis()).getStandardDays() >= 60;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Feedback");
        sb.append("{orderId='").append(orderId).append('\'');
        sb.append(", createDate=").append(createDate);
        sb.append(", score=").append(score);
        sb.append(", email='").append(email).append('\'');
        sb.append(", feedback='").append(feedback).append('\'');
        sb.append(", memo='").append(memo).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Feedback feedback = (Feedback) o;

        if(orderId != null ? !orderId.equals(feedback.orderId) : feedback.orderId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (orderId != null ? orderId.hashCode() : 0);
        return result;
    }

    /**
     * 查找出这个 Feedback 所影响的 listings 的 Category
     *
     * @return
     */
    public List<Category> relateCats() {
        List<Category> cats = new ArrayList<Category>();
        for(OrderItem itm : this.orderr.items) {
            cats.add(Product.<Product>findById(
                    Product.merchantSKUtoSKU(itm.selling.merchantSKU)).category);
        }
        return cats;
    }
}
