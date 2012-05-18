package models.market;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import helper.Dates;
import helper.Webs;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.db.jpa.GenericModel;
import play.utils.FastRuntimeException;

import javax.persistence.*;
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

    @ManyToOne
    public Listing listing;

    /**
     * Amazon Listing Review 的 Id:
     * [listingId]_[username]_[title] 的 md5Hex 值
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
    public Boolean purchased;

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
     * 主要是为了记录 createDate 日期
     *
     * @return
     */
    public AmazonListingReview createReview() {
        this.createDate = new Date();
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
            this.comment += String.format("\r\n%s:%s", Dates.date2Date(null), Webs.exposeGson(this));
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
        // resolved 不做处理
        return this.save();
    }

    public List<Orderr> relateOrder() {
        return Orderr.find("userid=?", this.userid).fetch();
    }


    /**
     * 对此 AmazonListingReview 进行检查, 判断是否需要进行警告通知
     */
    public void listingReviewCheck() {
        if(!this.isPersistent()) return;// 如果没有保存进入数据库的, 那么则不进行判断
        if(Selling.count("listing.listingId=?", this.listingId) == 0) return;// 判断这个 Listing 是我们自己有上架的
        if((this.rating != null && this.rating > 4) || // Rating > 4 的不处理
                this.createDate.getTime() - DateTime.now().plusDays(-70).getMillis() < 0) // 超过 70 天的不处理
            return;
        Mails.listingReviewWarn(this);
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

        return review;
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

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        AmazonListingReview that = (AmazonListingReview) o;

        if(alrId != null ? !alrId.equals(that.alrId) : that.alrId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (alrId != null ? alrId.hashCode() : 0);
        return result;
    }
}
