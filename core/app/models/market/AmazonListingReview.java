package models.market;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import helper.Dates;
import helper.GTs;
import helper.J;
import helper.Webs;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.data.validation.Unique;
import play.db.helper.JpqlSelect;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 某一个 Listing 所拥有的 Review 消息
 * User: wyattpan
 * Date: 4/17/12
 * Time: 4:33 PM
 */
@Entity
public class AmazonListingReview extends GenericModel {

    /**
     * 查找关联的订单, 找到了则关联上
     */
    @OneToOne
    public Orderr orderr;

    /**
     * 如果这个 Review 是负的, 原因是什么?
     */
    @ManyToMany
    public List<ListingReason> reasons = new ArrayList<ListingReason>();

    @Enumerated(EnumType.STRING)
    public ReviewState state;

    @ManyToOne(fetch = FetchType.LAZY)
    public Listing listing;

    /**
     * Amazon Listing Review 的 Id:
     * [listingId]_[username]_[userId] .toUpperCase()
     */
    @Id
    @Expose
    public String alrId;

    /**
     * 一个冗余字段
     */
    @Expose
    public String listingId;


    /**
     * Review 的得分
     */
    @Expose
    public Float rating;

    @Expose
    public String title;

    /**
     * 具体的 Review 的内容
     */
    @Lob
    @Expose
    public String review;


    /**
     * 点击了 Helpful 按钮的 YES
     */
    @Expose
    public Integer helpUp;


    /**
     * 所有点击了 HelpFul Click 的统计
     */
    @Expose
    public Integer helpClick;

    /**
     * 用户名字
     */
    @Expose
    public String username;

    /**
     * 关联的用户 Id
     */
    @Expose
    public String userid;

    @Expose
    public Date reviewDate;

    /**
     * Review 创建的时间
     */
    @Expose
    public Date createDate;

    /**
     * Amazon 会判断这个 Review 是不是为购买了这个商品的客户发出.
     */
    @Expose
    public Boolean purchased = false;

    /**
     * 标记为是否解决了这个 Review; 当然只有当 Review <= 3 的时候才需要进行处理!
     */
    @Expose
    public Boolean resolved = false;

    /**
     * 上次一的 LastRating;
     * 更新这个字段的时机为当当前的 rating 与 lastRating 的数据不一样的时候才进行更新, 并且还会记录到 Comment 中去
     */
    @Expose
    public Float lastRating;

    /**
     * 记录发送的邮件的次数. 大于 3 次则不再提醒.
     */
    @Expose
    public Integer mailedTimes = 0;

    /**
     * 给程序自己使用的, 非人为使用的 Comment; 用来记录变化的
     */
    @Lob
    public String comment = "";

    /**
     * 是否为视频 Review
     */
    @Expose
    public Boolean isVedio = false;
    /**
     * 是不是 VineVoice
     */
    @Expose
    public Boolean isVineVoice = false;

    /**
     * 是不是真名
     */
    @Expose
    public Boolean isRealName = false;

    /**
     * 是 Top 多少?
     */
    @Expose
    public Integer topN;


    /**
     * Amazon 给与的每个 Review 的 ID
     */
    @Expose
    @Unique
    @Column(unique = true)
    public String reviewId;

    /**
     * 视频的预览图片链接
     */
    @Expose
    public String vedioPicUrl;

    @Column(columnDefinition = "varchar(32) DEFAULT ''")
    @Expose
    public String osTicketId;


    /**
     * 是否打电话处理了?
     */
    @Expose
    public boolean isPhoned;

    /**
     * 每一个 Review 创建的时候都记录一次 originJson 字符串, 记录初始状态
     */
    @Lob
    public String originJson = " ";

    /**
     * 判断这个 Review 是否为自己的 Listing 产生的
     */
    @Expose
    public boolean isOwner = false;

    /**
     * 记录 AmazonListingReview 的点击记录, 一般给前台参看使用
     */
    @OneToMany(mappedBy = "ownerReview")
    public List<AmazonReviewRecord> reviewRecords = new ArrayList<AmazonReviewRecord>();

    /**
     * 主要是为了记录 createDate 日期
     *
     * @return
     */
    public AmazonListingReview createReview() {
        this.createDate = new Date();
        // 将初始的 Review 数据全部记录下来
        this.originJson = J.G(this);
        return this.save();
    }

    @PostPersist
    public void postPersist() {
        if(this.purchased == null) this.purchased = false;
    }

    public AmazonListingReview updateAttr(AmazonListingReview newReview) {
        if(StringUtils.isBlank(newReview.alrId))
            throw new FastRuntimeException("AmazonListingReview.alrId can not be blank!");
        if(!this.equals(newReview))
            throw new FastRuntimeException("Not the same AmazonListingReview, can not be update!");

        this.listingId = newReview.listingId;
//        if(StringUtils.isNotBlank(newReview.listingId)) this.listingId = newReview.listingId; //这个不修改
        if(newReview.rating != null && !this.rating.equals(newReview.rating)) { //如果两次 Rating 的值不一样需要记录
            this.comment += String.format("\r\n%s:%s", Dates.date2Date(null), J.G(this));
            this.lastRating = this.rating;
            this.mailedTimes = 0;// 允许重新发送一次邮件
        }
        if(newReview.rating != null) this.rating = newReview.rating;
        if(StringUtils.isNotBlank(newReview.title)) this.title = newReview.title;
        if(StringUtils.isNotBlank(newReview.review)) this.review = newReview.review;
        if(newReview.helpUp != null && newReview.helpUp > 0) this.helpUp = newReview.helpUp;
        if(newReview.helpClick != null && newReview.helpClick > 0) this.helpClick = newReview.helpClick;
        if(StringUtils.isNotBlank(newReview.userid)) this.userid = newReview.userid;
        if(StringUtils.isNotBlank(newReview.username)) this.username = newReview.username;
        //reviewDate 不修改了
        if(newReview.purchased != null) this.purchased = newReview.purchased;
        if(newReview.isVedio != null) this.isVedio = newReview.isVedio;
        if(newReview.isRealName != null) this.isRealName = newReview.isRealName;
        if(newReview.isVineVoice != null) this.isVineVoice = newReview.isVineVoice;
        if(newReview.topN != null && newReview.topN >= 0) this.topN = newReview.topN;
        if(StringUtils.isNotBlank(newReview.reviewId)) this.reviewId = newReview.reviewId;
        if(StringUtils.isNotBlank(newReview.vedioPicUrl)) this.vedioPicUrl = newReview.vedioPicUrl;
        // resolved 不做处理
        return this.save();
    }

    public List<Orderr> relateOrder() {
        return Orderr.find("userid=?", this.userid).fetch();
    }


    /**
     * 对此 AmazonListingReview 进行检查, 判断是否需要进行警告通知
     * 在 Check 方法执行完成以后将数据同步回数据库
     */
    public void listingReviewCheck() {
        if(!this.isPersistent()) return;// 如果没有保存进入数据库的, 那么则不进行判断
        if(Selling.count("listing.listingId=?", this.listingId) == 0) return;// 判断这个 Listing 是我们自己有上架的
        if(this.createDate.getTime() - DateTime.now().plusDays(-70).getMillis() < 0) return;// 超过 70 天的不处理


//        Rating < 4 的开 OsTicket
        if((this.rating != null && this.rating < 4)) this.openOsTicket(null);
//        Rating <= 4 的发送邮件提醒
        if((this.rating != null && this.rating <= 4)) Mails.listingReviewWarn(this);
        this.save();
    }

    public void openOsTicket(String title) {
        if(StringUtils.isNotBlank(this.osTicketId)) {
            Logger.info("Review OsTicket is exist! %s", this.osTicketId);
            return;
        }

        String name = String.format("%s - %s", this.username, this.listingId);
        String email = "";
        String subject = title;
        String content = GTs.render("OsTicketReviewWarn", GTs.newMap("review", this).build());

        if(StringUtils.isBlank(subject)) {
            if(this.listing.market == Account.M.AMAZON_DE) {
                subject = "Du hinterließ einen negativen Testbericht, können wir eine Chance haben, zu korrigieren?";
            } else { // 除了 DE 使用德语其他的默认使用'英语'
                subject = "You left a negative product review, may we have a chance to make up?";
            }
        }

        List<Orderr> orders = Orderr.findByUserId(this.userid);
        if(orders.size() > 0) email = orders.get(0).email;
        if(StringUtils.isBlank(email)) {
            Logger.warn("Review (%s) relate order have no email.", this.alrId);
            email = "support@easyacceu.com";
            subject += " - No Order found...";
            content += "\r\n检查: 1. 是我们的跟的 Listing 产生的? 2. 订单的 userId 还没抓取回来? 3. 是非购买用户留的?";

            this.osTicketId = Webs.openOsTicket(name, email, subject, content, Webs.TopicID.REVIEW, "Review " + this.alrId) + "-noemail";
        } else {
            this.osTicketId = Webs.openOsTicket(name, email, subject, content, Webs.TopicID.REVIEW, "Review " + this.alrId);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AmazonListingReview");
        sb.append("{alrId='").append(alrId).append('\'');
        sb.append(", listingId='").append(listing.listingId).append('\'');//使用了 model
        sb.append(", rating=").append(rating);
        sb.append(", title='").append(title).append('\'');
        sb.append(", review='").append(review).append('\'');
        sb.append(", helpUp=").append(helpUp);
        sb.append(", helpClick=").append(helpClick);
        sb.append(", username='").append(username).append('\'');
        sb.append(", userid='").append(userid).append('\'');
        sb.append(", reviewDate=").append(reviewDate);
        sb.append(", createDate=").append(createDate);
        sb.append(", purchased=").append(purchased);
        sb.append(", resolved=").append(resolved);
        sb.append(", lastRating=").append(lastRating);
        sb.append(", mailedTimes=").append(mailedTimes);
        sb.append(", comment='").append(comment).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /**
     * 为此 Review 添加为什么是负评
     *
     * @param lr
     */
    public AmazonListingReview addWhyNegtive(ListingReason lr) {
        /**
         * 0. 此原因是否存在?
         * 1. 检查这个 lr 是否与这个 Review 所属在与 Category 下?
         * 2. 检查这个原因是否已经在此 Review 中存在了?
         */
        if(!lr.isPersistent()) throw new FastRuntimeException("此原因不存在!");
        if(!this.listing.product.category.categoryId.equals(lr.category.categoryId))
            throw new FastRuntimeException("此原因与这个 Listing 不属于一个类别");
        if(lr.reviews.contains(this)) throw new FastRuntimeException("此原因已经存在, 不需要重复添加");
        this.reasons.add(lr);
        return this.save();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        AmazonListingReview that = (AmazonListingReview) o;

        if(reviewId != null ? !reviewId.equals(that.reviewId) : that.reviewId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (reviewId != null ? reviewId.hashCode() : 0);
        return result;
    }

    /**
     * 选择出一个能够点击此 Review 的 Account
     *
     * @return
     */
    public F.T2<Account, Integer> pickUpOneAccountToClick() {
        /**
         * 一步一步的过滤掉 Account, 然后返回一个 Account
         * 1. 找出所有的 Comment Account, 过滤掉 SaleAcc
         * 2. 从 AmazonReviewRecord 中检查此 Review 的点击日志, 首先找出点击过此 Review 的 Account
         */
        List<Account> opendAccs = Account.openedReviewAccount();
        if(opendAccs.size() == 0) throw new FastRuntimeException("没有打开的 Review 账号了.");
        List<Account> validAccs = AmazonReviewRecord.checkNonClickAccounts(opendAccs, this);
        //TODO 因为欧洲的账户可以通用点击, 所以考虑是否需要过滤掉不同市场的账号?
        List<Account> sameMarketAccs = Account.accountMarketFilter(validAccs, this.listingId);
        if(sameMarketAccs.size() == 0) throw new FastRuntimeException("系统内所有的账号都已经点击过这个 Review 了, 请添加新账号再进行点击.");
        Logger.info("To Click Review %s, hava %s valid accounts.", this.reviewId, sameMarketAccs.size());
        StringBuilder sb = new StringBuilder();
        for(Account a : sameMarketAccs) sb.append(a.id).append("|").append(a.prettyName()).append(",");
        Logger.info("Account List: %s", sb.toString());
        return new F.T2<Account, Integer>(sameMarketAccs.get(0), sameMarketAccs.size());
    }


    /**
     * 根据这个 Review 的 userId, 与 ReviewDate 去尝试 Order 并且关联上
     *
     * @return
     */
    public Orderr tryToRelateOrderByUserId() {
        if(this.orderr != null) throw new FastRuntimeException("已经找到 Review 对应的 Order");
        if(StringUtils.isBlank(this.userid)) return null;
        return Orderr.find("createDate<=? AND userid=? ORDER BY createDate DESC", this.reviewDate, this.userid).first();
    }

    /**
     * 通过状态来查看检查此 Review 联系的 OsTicket 是否有回复, 有回复了就是另外一些状态了.
     *
     * @return
     */
    public boolean isHaveResponse() {
        List<ReviewState> states = Arrays.asList(ReviewState.NEW, ReviewState.RP1, ReviewState.RP2, ReviewState.PHONE);
        for(ReviewState s : states) {
            if(s == this.state) return true;
        }
        return false;
    }

    /**
     * 输出与此 Review 合适的警告颜色
     *
     * @return
     */
    public String color() {
        if(this.rating >= 5) {
            return "3ED76A";
        } else if(this.rating >= 4) {
            return "ADFF1F";
        } else if(this.rating >= 3) {
            return "FFE107";
        } else if(this.rating >= 2) {
            return "D54C00";
        } else if(this.rating >= 1) {
            return "E03F00";
        } else {
            return "FF1101";
        }
    }

    /**
     * 检查此 Review 是否在 Top1000 内
     *
     * @return
     */
    public Boolean isWithinTop1000() {
        return this.topN != null && this.topN > 0 && this.topN <= 1000;
    }

    /**
     * 根据 Review 的长度进行不同颜色的区分
     *
     * @return
     */
    public F.T2<Integer, String> reviewLengthColor() {
        if(this.review.length() <= 100) {
            return new F.T2<Integer, String>(this.review.length(), "29FFF1");
        } else if(this.review.length() <= 240) {
            return new F.T2<Integer, String>(this.review.length(), "29D2FF");
        } else if(this.review.length() <= 500) {
            return new F.T2<Integer, String>(this.review.length(), "2997FF");
        } else if(this.review.length() <= 1000) {
            return new F.T2<Integer, String>(this.review.length(), "2962FF");
        } else if(this.review.length() <= 2000) {
            return new F.T2<Integer, String>(this.review.length(), "292AFF");
        } else {
            return new F.T2<Integer, String>(this.review.length(), "6229FF");
        }
    }

    /**
     * 解析单个 Review JsonElement
     *
     * @param jsonReviewElement
     * @return
     */
    public static AmazonListingReview parseAmazonReviewJson(JsonElement jsonReviewElement) {
        JsonObject rwObj = jsonReviewElement.getAsJsonObject();
        AmazonListingReview review = new AmazonListingReview();
        review.alrId = rwObj.get("alrId").getAsString();
        review.listingId = rwObj.get("listingId").getAsString();
        review.rating = rwObj.get("rating").getAsFloat();
        review.lastRating = rwObj.get("lastRating").getAsFloat();
        review.title = rwObj.get("title").getAsString();
        review.review = rwObj.get("review").getAsString();
        review.helpUp = rwObj.get("helpUp").getAsInt();
        review.helpClick = rwObj.get("helpClick").getAsInt();
        review.username = rwObj.get("username").getAsString();
        review.userid = rwObj.get("userid").getAsString();
        review.reviewDate = DateTime.parse(rwObj.get("reviewDate").getAsString()).toDate();
        review.purchased = rwObj.get("purchased").getAsBoolean();
        review.resolved = rwObj.get("resolved").getAsBoolean();
        review.isVedio = rwObj.get("isVedio").getAsBoolean();
        review.reviewId = rwObj.get("reviewId").getAsString();
        review.vedioPicUrl = rwObj.get("vedioPicUrl").getAsString();
        review.isRealName = rwObj.get("isRealName").getAsBoolean();
        review.isVineVoice = rwObj.get("isVineVoice").getAsBoolean();
        review.topN = rwObj.get("topN").getAsInt();

        return review;
    }

    public static AmazonListingReview findByReviewId(String reviewId) {
        return AmazonListingReview.find("reviewId=?", reviewId).first();
    }

    /**
     * 计算出指定 Listing 的 Review 个数
     *
     * @param lid
     * @return
     */
    public static long countListingReview(String lid) {
        return AmazonListingReview.count("listingId=?", lid);
    }

    public static List<F.T2<String, Integer>> reviewLeftClickTimes(List<String> reviewIds) {
        List<AmazonListingReview> reviews = AmazonListingReview.find("reviewId IN " + JpqlSelect.inlineParam(reviewIds)).fetch();
        List<F.T2<String, Integer>> reviewLeftClicks = new ArrayList<F.T2<String, Integer>>();
        for(AmazonListingReview review : reviews) {
            int leftClick = 0;
            try {
                leftClick = review.pickUpOneAccountToClick()._2;
            } catch(FastRuntimeException e) {
                leftClick = 0;
            }
            reviewLeftClicks.add(new F.T2<String, Integer>(review.reviewId, leftClick));
        }
        return reviewLeftClicks;
    }

    public static List<AmazonListingReview> negtiveReviewsFilterByState(ReviewState state) {
        return AmazonListingReview.find("rating<=? AND state=?", 4f/*小于 4 分为负的*/, state).fetch();
    }


    /**
     * @return
     */
    public F.T2<List<ListingReason>, List<String>> unTagedReasons() {
        List<ListingReason> unTaged = new ArrayList<ListingReason>();
        if(this.reasons == null || this.reasons.size() == 0) {
            unTaged.addAll(this.listing.product.category.reasons);
        } else {
            for(ListingReason rea : this.listing.product.category.reasons) {
                for(ListingReason lrea : this.reasons) {
                    if(!lrea.equals(rea)) unTaged.add(rea);
                }
            }
        }
        List<String> reasonNames = new ArrayList<String>();
        for(ListingReason ra : unTaged) reasonNames.add(ra.reason);
        return new F.T2<List<ListingReason>, List<String>>(unTaged, reasonNames);
    }
}
