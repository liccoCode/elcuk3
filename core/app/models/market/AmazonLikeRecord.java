package models.market;

import com.google.gson.annotations.Expose;
import models.ElcukRecord;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.libs.F;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 点击 Listing 的 Like 按钮的 API
 * User: wyattpan
 * Date: 7/23/12
 * Time: 3:43 PM
 */
@Entity
public class AmazonLikeRecord extends GenericModel {

    public AmazonLikeRecord() {
    }

    public AmazonLikeRecord(Listing listing, Account acc) {
        this.listingId = listing.listingId;
        this.asin = listing.asin;
        this.market = listing.market;
        this.likes = listing.likes == null ? 0 : listing.likes;
        this.aveRating = listing.rating == null ? 0 : listing.rating;
        this.account = acc;
        this.createAt = new Date();
        this.username = ElcukRecord.username();
    }

    @Id
    @GeneratedValue
    @Required
    @Expose
    public Long id;

    /**
     * 点击的时候其 Like 是多少?
     */
    @Expose
    public int likes;

    /**
     * 此 Listing 的平均得分是多少?
     */
    @Expose
    public float aveRating;

    @Expose
    public String asin;

    @Enumerated(EnumType.STRING)
    @Expose
    public M market;

    /**
     * 一个冗余字段
     */
    public String listingId;

    /**
     * 知道是哪一个账户点击的
     */
    @OneToOne
    public Account account;
    /**
     * 知道是谁点击的
     */
    @Expose
    public String username;

    /**
     * 什么时间点击的
     */
    @Expose
    public Date createAt;

    /**
     * 从 OpnedAccounts 中过滤出没有点击过 Like 按钮的 Account.
     *
     * @param opendAccs
     * @return
     */
    public static List<Account> nonClickLikeAccs(List<Account> opendAccs, String lid) {
        F.T2<String, M> lidT2 = Listing.unLid(lid);
        List<Account> nonClickAccs = new ArrayList<Account>();
        for(Account acc : opendAccs) {
            if(AmazonLikeRecord.count("asin=? AND market=? AND account=?", lidT2._1, lidT2._2, acc) == 0)
                nonClickAccs.add(acc);
        }
        return nonClickAccs;
    }
}
