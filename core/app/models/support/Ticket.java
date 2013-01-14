package models.support;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import helper.Dates;
import helper.OsTicket;
import jobs.FeedbackInfoFetchJob;
import jobs.TicketStateSyncJob;
import models.ElcukRecord;
import models.User;
import models.market.AmazonListingReview;
import models.market.Feedback;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Duration;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Model;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
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
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Ticket extends Model {

    public Ticket() {
        this.state = TicketState.NEW;
    }

    public Ticket(String osTicketId, Date createAt, String title) {
        this();
        this.osTicketId = osTicketId;
        if(StringUtils.isNotBlank(title))
            this.fid = title.substring(0, (200 > title.length() ? title.length() : 200));
        this.createAt = createAt;
        this.type = T.TICKET;
    }

    public Ticket(AmazonListingReview review) {
        this();
        this.osTicketId = review.osTicketId;
        this.type = T.REVIEW;
        this.fid = review.listing.product.sku;
        this.review = review;
        this.createAt = review.reviewDate;
    }

    public Ticket(Feedback feedback) {
        this();
        this.osTicketId = feedback.osTicketId;
        this.type = T.FEEDBACK;
        this.fid = feedback.orderId;
        this.feedback = feedback;
        this.createAt = feedback.createDate;
    }

    /**
     * toggle 加星
     */
    public boolean toggleStar() {
        this.isStart = !this.isStart;
        this.save();
        return this.isStart;
    }

    public enum T {
        REVIEW,
        FEEDBACK,
        /**
         * 默认的咨询邮件
         */
        TICKET
    }

    /**
     * 这个 Ticket 可能拥有的问题
     */
    @ManyToMany
    public List<TicketReason> reasons = new ArrayList<TicketReason>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    public List<TicketResponse> responses = new ArrayList<TicketResponse>();

    @Expose
    @Required
    @Unique
    public String osTicketId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Expose
    public T type;

    @Enumerated(EnumType.STRING)
    @Expose
    public TicketState state;

    /**
     * 必须指定到是哪一个产品出现问题!
     * - 对于 Review 为 SKU,
     * - 对于 Feedback 为 OrderId
     */
    @Required
    @Column(nullable = false)
    @Expose
    public String fid;

    /**
     * 最后一次发送邮件联系客户的时间
     */
    @Expose
    public Date lastResponseTime;

    /**
     * 最后一个收到回复的时间
     */
    @Expose
    public Date lastMessageTime;

    /**
     * 此 Ticket 最后一次向 OsTicket 更新的时间
     */
    @Expose
    public Date lastSyncTime;

    /**
     * 客户联系的次数
     */
    public int messageTimes;

    /**
     * 客服联系的次数
     */
    public int responseTimes;

    /**
     * 是否成功的除了这个 Ticket
     */
    @Expose
    public boolean isSuccess = false;

    /**
     * 什么时候创建的?
     */
    @Expose
    public Date createAt;

    /**
     * 此 Ticket 指定被谁来进行处理
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    public User resolver;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    public AmazonListingReview review;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    public Feedback feedback;

    @Lob
    public String memo = " ";

    /**
     * 是否加星, 标记为重点产品
     */
    public boolean isStart = false;

    /**
     * 是否为自己自建销售的产品?
     */
    public boolean isSelfSale = true;

    /**
     * 判断 Ticket 是否超时.
     * 1. 如果有 Ticket 有客户最后回复时间, 那么回复 24 小后过, 表示邮件超时!
     * 2. 如果没有客户最后回复时间, 那么从 Ticket 创建时间开始计算 24 小时后超时.
     *
     * @return
     */
    public boolean isOverdue() {
        if(this.lastResponseTime != null) {
            Duration duration = new Duration(this.lastResponseTime.getTime(),
                    System.currentTimeMillis());
            return duration.getStandardHours() > 24;
        } else {
            Duration duration = new Duration(this.createAt.getTime(), System.currentTimeMillis());
            return duration.getStandardHours() > 24;
        }
    }

    public Ticket isNotSelf() {
        this.state = TicketState.PRE_CLOSE;
        this.isSelfSale = false;
        this.memo = "为非自建, 请归类后关闭.";
        return this;
    }

    /**
     * 主要是过滤掉类似 98202-noemail 这种格式
     *
     * @return
     */
    public String osTicketId() {
        return this.osTicketId.split("-")[0];
    }

    public String title() {
        if(this.type == T.TICKET)
            return this.fid;
        else if(this.type == T.FEEDBACK)
            return this.feedback.email;
        else if(this.type == T.REVIEW)
            return this.review.title;
        else
            return "None";
    }

    /**
     * 是否为有正确客户 Email 地址
     *
     * @return
     */
    public boolean isNoEmail() {
        return StringUtils.contains(this.osTicketId, "noemail");
    }

    public Ticket tagReason(TicketReason tr) {
        if(this.type == T.FEEDBACK) {
            if(!this.feedback.relateCats().contains(tr.category))
                throw new FastRuntimeException("此原因与这个 Listing 不属于一个类别");
        } else if(this.type == T.REVIEW) {
            if(!this.review.listing.product.category.equals(tr.category))
                throw new FastRuntimeException("此原因与这个 Listing 不属于一个类别");
        }
        if(this.reasons.contains(tr))
            throw new FastRuntimeException("此原因已经存在, 不需要重复添加");

        this.reasons.add(tr);
        return this.save();
    }

    public Ticket unTagReason(TicketReason tr) {
        if(!this.reasons.contains(tr))
            throw new FastRuntimeException("没有这个原因, 不需要接触 Tag.");
        else
            this.reasons.remove(tr);
        return this.save();
    }

    /**
     * 关闭这个 Ticket
     *
     * @param reason
     */
    public Ticket close(String reason) {
        /**
         * 1. 检查是否给这个 Ticket 归类
         * 2. 检查是否有填写原因
         * 3. 检查这个 Ticket 是否有 resolver
         */
        if(this.state == TicketState.CLOSE) throw new FastRuntimeException("已经关闭了, 不需要重新关闭.");
        if(this.reasons.size() == 0)
            throw new FastRuntimeException("要关闭此 Ticket, 必须要对此 Ticket 先进行归类(什么问题).");
        if(this.resolver == null) throw new FastRuntimeException("请给 Ticket 添加负责人.");
        if(StringUtils.isBlank(reason)) throw new FastRuntimeException("必须要输入原因.");
        this.state = TicketState.CLOSE;
        this.memo = String.format("Closed By %s At [%s] for [ %s ]\r\n",
                ElcukRecord.username(),
                Dates.date2DateTime(),
                reason.trim()) + this.memo;
        StringBuilder note = new StringBuilder(reason).append("\r\n");
        if(this.type == T.FEEDBACK)
            note.append(this.feedback.memo);
        else if(this.type == T.REVIEW) {
            note.append(this.review.comment);
            // 关闭前再去 Amazon 抓取一次订单的状态
            FeedbackInfoFetchJob.checkFeedbackDealState(this);
        }
        // 特意在保存之后再进行 Ticket 关闭, 如果保存出错也不用 OsTicket 中的 Ticket 了
        this.save();
        OsTicket.closeOsTicket(this.osTicketId(), this.resolver.username, note.toString());
        return this;
    }

    /**
     * 将 OsTicket 的 Response 给解析出来, 并且保存
     *
     * @param ticketResps
     */
    public void saveResponses(List<TicketStateSyncJob.OsResp> ticketResps) {
        if(ticketResps == null || ticketResps.size() == 0) return;
        for(TicketStateSyncJob.OsResp res : ticketResps) {
            // 只记录新的, 不更新老的.
            if(TicketResponse.count("responseId=?", res.response_id) <= 0) {
                TicketResponse r = new TicketResponse(res);
                r.ticket = this;
                r.save();
            } else {
                TicketResponse r = TicketResponse.findById(res.response_id);
                r.updateAttrs(new TicketResponse(res));
            }
        }
    }

    /**
     * 将 Ticket 与 OsTicket 同步一次
     *
     * @return
     */
    public F.Option<F.T2<List<Ticket>, List<Ticket>>> syncFromOsticket() {
        if(StringUtils.isBlank(this.osTicketId())) return F.Option.None();
        JsonObject rsObj = OsTicket.communicationWithOsTicket(Arrays.asList(this.osTicketId()));
        return F.Option.Some(
                TicketStateSyncJob.syncOsTicketDetailsIntoSystem(rsObj, Arrays.asList(this))
        );
    }


    /**
     * 加载所有需要更新 State 状态的 Ticket
     *
     * @return
     */
    public static List<Ticket> checkStateTickets(int size) {
        return Ticket.find("state!=? AND osTicketId IS NOT NULL AND osTicketId NOT LIKE ?" +
                " ORDER BY lastSyncTime", TicketState.CLOSE, "%-noemail").fetch(size);
    }

    public static Ticket findByOsTicketId(String osTicketId) {
        return Ticket.find("osTicketId=?", osTicketId).first();
    }


    /**
     * 作为第一次修复使用的代码, 后续需要删除.
     */
    public static void initReviewFix() {
        List<AmazonListingReview> reviews = AmazonListingReview.find("osTicketId IS NOT NULL")
                .fetch();
        for(AmazonListingReview review : reviews) {
            if(review.ticket != null) continue;
            Ticket ticket = new Ticket(review);
            if(review.orderr == null) review.orderr = review.tryToRelateOrderByUserId();
            review.ticket = ticket;
            review.save();
        }
    }

    public static void initFeedbackFix() {
        List<Feedback> feedbacks = Feedback.find("osTicketId IS NOT NULL").fetch();
        for(Feedback feedback : feedbacks) {
            if(feedback.ticket != null) continue;
            Ticket ticket = new Ticket(feedback);
            ticket.save();
        }
    }
}
