package models.support;

import models.User;
import models.market.AmazonListingReview;
import models.market.Feedback;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 对需要处理的一个 Review 或者 Feedback 的一个抽象;
 * <p/>
 * 由于此系统在处理 OsTicket 与 Feedback 的时候会同步在 OsTicket 中创建一个 Ticket 来进行处理, 所以也可以将
 * 此 Ticket 与 OsTicket 中的一个 Ticket 的关系相对应.
 * <p/>
 * User: wyattpan
 * Date: 7/30/12
 * Time: 3:29 PM
 */
@Entity
public class Ticket extends Model {

    public Ticket() {
    }

    public Ticket(AmazonListingReview review) {
        this.osTicketId = review.osTicketId;
        this.type = T.REVIEW;
        this.sku = review.listing.product.sku;
        this.state = TicketState.NEW;
        this.review = review;
    }

    public Ticket(Feedback feedback) {
    }

    public enum T {
        REVIEW,
        FEEDBACK
    }

    /**
     * 这个 Ticket 可能拥有的问题
     */
    @ManyToMany
    public List<TicketReason> reasons = new ArrayList<TicketReason>();

    public String osTicketId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public T type;

    @Enumerated(EnumType.STRING)
    public TicketState state;

    /**
     * 必须指定到是哪一个产品出现问题!
     */
    @Required
    @Column(nullable = false)
    public String sku;

    /**
     * 最后一个收到回复的时间
     */
    public Date lastResponseTime;

    /**
     * 最后一次发送邮件联系客户的时间
     */
    public Date lastMailTime;

    /**
     * 此 Ticket 最后一次向 OsTicket 更新的时间
     */
    public Date lastSyncTime;

    /**
     * 是否成功的除了这个 Ticket
     */
    public boolean isSuccess = false;

    /**
     * 此 Ticket 指定被谁来进行处理
     */
    public User resolver;

    @OneToOne
    public AmazonListingReview review;

    @OneToOne
    public Feedback feedback;

    @Lob
    public String memo = " ";

    /**
     * 作为第一次修复使用的代码, 后续需要删除.
     */
    public static void initReviewFix() {
        List<AmazonListingReview> reviews = AmazonListingReview.find("osTicketId IS NOT NULL").fetch();
        for(AmazonListingReview review : reviews) {
            Ticket ticket = new Ticket(review);
            ticket.save();
        }
    }

    public static List<Ticket> reviews(TicketState state) {
        return Ticket.find("type=? AND state=?", T.REVIEW, TicketState.NEW).fetch();
    }
}
