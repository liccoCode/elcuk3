package models.support;

import helper.GTs;
import helper.Webs;
import jobs.TicketStateSyncJob;
import models.User;
import models.market.Account;
import models.market.AmazonListingReview;
import models.market.Feedback;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.F;

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
        this.createAt = new Date();
    }

    public Ticket(Feedback feedback) {
        this.createAt = new Date();
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
     * 什么时候创建的?
     */
    public Date createAt;

    /**
     * 此 Ticket 指定被谁来进行处理
     */
    @OneToOne
    public User resolver;

    @OneToOne
    public AmazonListingReview review;

    @OneToOne
    public Feedback feedback;

    @Lob
    public String memo = " ";

    public void openOsTicket(String title) {
        if(StringUtils.isNotBlank(this.osTicketId)) {
            Logger.info("Review OsTicket is exist! %s", this.osTicketId);
            return;
        }

        String name = String.format("%s - %s", this.review.username, this.review.listingId);
        String subject = title;
        String content = GTs.render("OsTicketReviewWarn", GTs.newMap("review", this).build());

        if(StringUtils.isBlank(subject)) {
            if(this.review.listing.market == Account.M.AMAZON_DE) {
                subject = "Du hinterließ einen negativen Testbericht, können wir eine Chance haben, zu korrigieren?";
            } else { // 除了 DE 使用德语其他的默认使用'英语'
                subject = "You left a negative product review, may we have a chance to make up?";
            }
        }

        this.review.orderr = this.review.tryToRelateOrderByUserId();
        if(this.review.orderr == null) {
            Logger.warn("Review (%s) relate order have no email.", this.review.alrId);
            subject += " - No Order found...";
            content += "\r\n检查: 1. 是我们的跟的 Listing 产生的? 2. 订单的 userId 还没抓取回来? 3. 是非购买用户留的?";

            this.osTicketId = Webs.openOsTicket(name, "support@easyacceu.com", subject, content, Webs.TopicID.REVIEW, "Review " + this.review.alrId) + "-noemail";
        } else {
            this.osTicketId = Webs.openOsTicket(name, this.review.orderr.email, subject, content, Webs.TopicID.REVIEW, "Review " + this.review.alrId);
        }
    }

    public boolean isOverdue() {
        //  不同状态有不同的 overdue 判断, 所以将方法填写到 TicketState 中去
        return true;
    }

    /**
     * 主要是过滤掉类似 98202-noemail 这种格式
     *
     * @return
     */
    public String osTicketId() {
        return this.osTicketId.split("-")[0];
    }

    /**
     * 作为第一次修复使用的代码, 后续需要删除.
     */
    public static void initReviewFix() {
        List<AmazonListingReview> reviews = AmazonListingReview.find("osTicketId IS NOT NULL").fetch();
        for(AmazonListingReview review : reviews) {
            Ticket ticket = new Ticket(review);
            review.orderr = review.tryToRelateOrderByUserId();
            review.ticket = ticket;
            review.save();
        }
    }

    public static List<Ticket> reviews(TicketState state) {
        return Ticket.find("type=? AND state=?", T.REVIEW, TicketState.NEW).fetch();
    }

    /**
     * 加载所有需要更新 State 状态的 Ticket
     *
     * @return
     */
    public static List<Ticket> checkStateTickets(int size) {
        return Ticket.find("state!=? ORDER BY lastSyncTime", TicketState.CLOSE).fetch(size);
    }

    /**
     * 检查这个 Ticket 是否有客户的新回复
     *
     * @param resps
     * @param msgs
     * @return
     */
    public static F.T2<Boolean, TicketStateSyncJob.OsMsg> ishaveNewCustomerEmail(List<TicketStateSyncJob.OsResp> resps, List<TicketStateSyncJob.OsMsg> msgs) {
        TicketStateSyncJob.OsMsg newMsg = TicketStateSyncJob.OsMsg.lastestMsg(msgs);
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
    public static F.T2<Boolean, TicketStateSyncJob.OsResp> ishaveNewOperatorResponse(List<TicketStateSyncJob.OsResp> resps, List<TicketStateSyncJob.OsMsg> msgs) {
        TicketStateSyncJob.OsMsg newMsg = TicketStateSyncJob.OsMsg.lastestMsg(msgs);
        TicketStateSyncJob.OsResp newResp = TicketStateSyncJob.OsResp.lastestResp(resps);
        if(newMsg != null && newResp != null) {
            if(newResp.created.getTime() > newMsg.created.getTime() && resps.size() > 1/*需要排除自己在创建 OsTicket 的那一个 Responose*/)
                return new F.T2<Boolean, TicketStateSyncJob.OsResp>(true, newResp);
        }
        return new F.T2<Boolean, TicketStateSyncJob.OsResp>(false, null);
    }
}
