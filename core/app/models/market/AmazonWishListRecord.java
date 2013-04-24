package models.market;

import com.google.gson.annotations.Expose;
import helper.DBUtils;
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
    public String market;


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

    /**
     * 是否自动生成
     */
    @Expose
    public boolean auto;

    public AmazonWishListRecord(Listing listing, Account account, boolean auto) {
        this.asin = listing.asin;
        this.market = listing.market.toString();
        this.account = account;
        this.category = listing.product.category.name;
        this.createAt = new Date();
        this.userName=account.username;
        this.listingId=listing.listingId;
        this.auto = auto;
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
        for(Account acc : opendAccs)
            if(AmazonWishListRecord.count("asin=? and market=? and account=?", lidT2._1, lidT2._2.toString(), acc) == 0)
                nonAddWishListAccs.add(acc);

        return nonAddWishListAccs;
    }

    /**
     * 获得listing的wishlist统计情况
     *
     * @param asin
     * @param market
     * @return
     */
    public static F.T5<String, String, Long, Long, Long> WishList(String asin, M market) {
        String sql="select a.category as cg,a.auto as auto ,count(a.auto) as count from (select category,auto from amazonwishlistrecord where asin=? and market=?) a group by a.auto";

        List<Map<String, Object>> rows = DBUtils.rows(sql, asin, market.toString());
        long addedNumb = 0;
        long autoAddedNum = 0;
        String category = null;
        for(Map row : rows) {
            category = row.get("cg").toString();
            long count = (Long)row.get("count");
            addedNumb += count;
            if((Boolean) row.get("auto"))
                autoAddedNum += count;
        }
        long totalNumb = Account.count("type=?", market);
        return new F.T5<String, String, Long, Long, Long>(
                Listing.lid(asin, market), category, addedNumb, autoAddedNum, totalNumb
        );
    }

}
