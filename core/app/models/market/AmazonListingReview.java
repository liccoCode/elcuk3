package models.market;

import play.db.jpa.GenericModel;

import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * 某一个 Listing 所拥有的 Review 消息
 * User: wyattpan
 * Date: 4/17/12
 * Time: 4:33 PM
 */
public class AmazonListingReview extends GenericModel {

    @ManyToOne
    public Listing listing;

    /**
     * Amazon Listing Review 的 Id, 由 [listingId]_[userId]_[date最后4位]
     */
    @Id
    public String alrId;


    /**
     * Review 的得分
     */
    public Float rating;

    /**
     * 具体的 Review 的内容
     */
    public String review;


    /**
     * 点击了 Helpful 按钮的 YES
     */
    public Integer helpUp;


    /**
     * 点击了 Helpful 按钮的 NO
     */
    public Integer helpDown;

    /**
     * 用户名字
     */
    public String username;

    /**
     * 关联的用户 Id
     */
    public String userid;

    public Date reviewDate;

    /**
     * Amazon 会判断这个 Review 是不是为购买了这个商品的客户发出.
     */
    public Boolean purchased;

    /**
     * 标记为是否解决了这个 Review; 当然只有当 Review <= 3 的时候才需要进行处理!
     */
    public Boolean resolved = false;

    /**
     * 上次一的 LastRating;
     * 更新这个字段的时机为当当前的 rating 与 lastRating 的数据不一样的时候才进行更新, 并且还会记录到 Comment 中去
     */
    public Float lastRating;

    /**
     * 给程序自己使用的, 非人为使用的 Comment; 用来记录变化的
     */
    public String comment = "";

}
