package models.market;

import com.google.gson.annotations.Expose;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.Date;

/**
 * 记录账号点击的数据
 * <p/>
 * User: wyattpan
 * Date: 7/19/12
 * Time: 11:42 AM
 */
@Entity
public class AmazonReviewRecord extends GenericModel {
    @Id
    @GeneratedValue
    @Required
    public Long id;

    /**
     * 要知道哪一个账号点击的
     */
    @OneToOne
    public Account account;

    /**
     * 要知道是什么时候点击的
     */
    public Date createAt;

    /**
     * 知道是谁点击的
     */
    @Required
    @Column(length = 60)
    public String username;

    /**
     * 要知道点击的是 up 还是 down
     */
    @Required
    public boolean isUp;

    /**
     * 要知道点击的是哪一个 Asin
     */
    @Required
    @Column(length = 32)
    public String asin;

    /**
     * 是哪一个市场的 Listing
     */
    @Required
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    public Account.M market;

    /**
     * 知道是谁的 Review
     */
    @Required
    @Column(length = 60)
    public String userId;

    /**
     * 知道是 Amazon 中的哪一个 Review
     */
    @Required
    @Column(length = 60)
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
    public float rating;

}
