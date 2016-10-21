package models.market;

import com.google.gson.annotations.Expose;
import models.User;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.libs.F;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 记录账号点击的数据
 * <p/>
 * User: wyattpan
 * Date: 7/19/12
 * Time: 11:42 AM
 */
@Entity
public class AmazonReviewRecord extends GenericModel {

    public AmazonReviewRecord() {
    }

    /**
     * 根据点击的 AmazonListingReivew 转换成为 Click Record
     *
     * @param review
     * @param acc
     * @param isUp
     */
    public AmazonReviewRecord(AmazonListingReview review, Account acc, boolean isUp) {
        this.account = acc;
        this.username = User.username();
        this.createAt = new Date();
        this.isUp = isUp;
        F.T2<String, M> unLid = Listing.unLid(review.listingId);
        this.asin = unLid._1;
        this.market = unLid._2;
        this.userId = review.userid;
        this.reviewId = review.reviewId;
        this.review = review.review;
        this.ups = review.helpUp;
        this.downs = review.helpClick - review.helpUp;
        this.rating = review.rating;
        this.ownerReview = review;
    }

    /**
     * 起附属的 Review
     */
    @ManyToOne
    public AmazonListingReview ownerReview;

    @Id
    @GeneratedValue
    @Required
    @Expose
    public Long id;

    /**
     * 要知道哪一个账号点击的
     */
    @OneToOne
    public Account account;

    /**
     * 要知道是什么时候点击的
     */
    @Expose
    public Date createAt;

    /**
     * 知道是谁点击的
     */
    @Required
    @Column(length = 60)
    @Expose
    public String username;

    /**
     * 要知道点击的是 up 还是 down
     */
    @Required
    @Expose
    public boolean isUp;

    /**
     * 要知道点击的是哪一个 Asin
     */
    @Required
    @Column(length = 32)
    @Expose
    public String asin;

    /**
     * 是哪一个市场的 Listing
     */
    @Required
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    @Expose
    public M market;

    /**
     * 知道是谁的 Review
     */
    @Required
    @Column(length = 60)
    @Expose
    public String userId;

    /**
     * 知道是 Amazon 中的哪一个 Review
     */
    @Required
    @Column(length = 60)
    @Expose
    public String reviewId;

    /**
     * 点击时候的 Review 是什么样子的
     */
    @Lob
    public String review;

    /**
     * 点击的时候, 他拥有的 Up 数量
     */
    @Expose
    @Required
    public int ups;

    /**
     * 点击的时候, 他拥有的 down 数量
     */
    @Expose
    @Required
    public int downs;

    /**
     * 点击的时候 Rating 是多少?
     */
    @Required
    @Expose
    public float rating;

    /**
     * 检查传入的 Account 哪一些是没有点击过指定 AmazonListingReview 的.
     *
     * @param accs
     * @param amazonListingReview
     * @return 返回没有点击过的 Account
     */
    public static List<Account> checkNonClickAccounts(List<Account> accs,
                                                      AmazonListingReview amazonListingReview) {
        List<Account> nonClickAccounts = new ArrayList<>();
        for(Account acc : accs) {
            if(AmazonReviewRecord
                    .count("account=? AND reviewId=?", acc, amazonListingReview.reviewId) == 0)
                nonClickAccounts.add(acc);
        }
        return nonClickAccounts;
    }
}
