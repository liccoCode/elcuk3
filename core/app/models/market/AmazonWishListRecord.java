package models.market;

import com.google.gson.annotations.Expose;
import helper.DBUtils;
import play.db.helper.SqlSelect;
import play.db.jpa.Model;
import play.libs.F;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    public M market;


    /**
     * 账户
     */
    @OneToOne
    public Account account;

    /**
     * 用户名
     */
    @Expose
    public String userName;

    /**
     * 商品 category
     */
    public String category;


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
        this.category = listing.product.category.name;
        this.createAt = new Date();
        this.userName = account.username;
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
        List<Account> nonAddWishListAccs = new ArrayList<Account>();
        for(Account acc : opendAccs) {
            if(AmazonWishListRecord.count("asin=? and market=? and account=?", lidT2._1, lidT2._2.name(), acc) != 0)
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
     * @return T5<asin,market,category,addedNumber,totalNumb >
     */
    public static F.T5<String, String, String, Long, Long> wishList(String asin, M market) {
        SqlSelect sqlSelect = new SqlSelect();
        sqlSelect.select("a.category as cg", "count(a.category) as count")
                .from("(select category from AmazonWishListRecord where asin=? and market=?) as a");
        List<Map<String, Object>> rows = DBUtils.rows(sqlSelect.toString(), asin, market.name());
        long addedNumb = 0;
        String category = null;
        for(Map<String, Object> row : rows) {
            category = row.get("cg").toString();
            addedNumb += (Long) row.get("count");
        }
        long totalNumb = Account.count("type=?", market);
        return new F.T5<String, String, String, Long, Long>(asin, market.name(), category, addedNumb, totalNumb);
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
