package models.market;

import com.google.gson.annotations.Expose;
import models.User;
import play.db.jpa.Model;
import play.libs.F;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 记录 Listing被添加到用户WishList的记录
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 13-4-19
 * Time: 下午2:47
 */
@Entity
public class AmazonWishListRecord extends Model {
    @Expose
    public String asin;


    @Expose
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public M market;


    /**
     * 账户
     */
    @OneToOne
    public Account account;

    /**
     * 哪个系统用户添加的
     */
    @Expose
    public String userName;

    @Expose
    public String listingId;

    /**
     * 添加时间
     */
    @Expose
    public Date createAt;


    public AmazonWishListRecord(Listing listing, Account account) {
        this.asin = listing.asin;
        this.market = listing.market;
        this.account = account;
        this.createAt = new Date();
        this.userName = User.username();
        this.listingId = listing.listingId;
    }


    /**
     * 从opendAccs中过滤出没有添加指定Listing到wishlist的accs
     *
     * @param opendAccs
     * @param lid
     * @return
     */
    public static List<Account> nonAddWishListAccs(List<Account> opendAccs, String lid) {
        F.T2<String, M> lidT2 = Listing.unLid(lid);
        List<Account> nonAddWishListAccs = new ArrayList<>();
        for(Account acc : opendAccs) {
            if(AmazonWishListRecord.count("asin=? and market=? and account=?", lidT2._1, lidT2._2, acc) != 0)
                continue;
            nonAddWishListAccs.add(acc);
        }

        return nonAddWishListAccs;
    }

    /**
     * 获得listing的wishlist统计情况
     *
     * @param asin
     * @param market
     * @return T2<已经添加的数量,总数量 >
     */
    public static F.T2<Long, Long> wishList(String asin, M market) {
        long addedNumb = AmazonWishListRecord.count("asin=? and market=?", asin, market);
        long totalNumb = Account.count("type=?", market);
        return new F.T2<>(addedNumb, totalNumb);
    }

    /**
     * 查询 AmazonWishList的记录
     *
     * @param listingId
     * @return
     */
    public static List<AmazonWishListRecord> wishListInfos(String listingId) {
        List<AmazonWishListRecord> records = AmazonWishListRecord.find("ListingId=? order by createAt desc", listingId).fetch();
        return records;
    }
}
