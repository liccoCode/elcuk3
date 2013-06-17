package query;

import helper.DBUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 10/8/12
 * Time: 7:10 PM
 */
public class AmazonListingReviewQuery {

    public Map<String, F.T2<Integer, Float>> sidRelateReviews(Collection<String> sids) {
        SqlSelect sql = new SqlSelect()
                .select("count(r.alrId) as c", "avg(r.rating) as rating",
                        "s.sellingId as sellingId")
                .from("AmazonListingReview r")
                .leftJoin("Selling s USING(listing_listingId)")
                .where("s.sellingId IN " + SqlSelect.inlineParam(sids));
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        Map<String, F.T2<Integer, Float>> reviewT2Map = new HashMap<String, F.T2<Integer, Float>>();
        for(Map<String, Object> row : rows) {
            reviewT2Map.put(row.get("sellingId").toString(),
                    new F.T2<Integer, Float>(
                            NumberUtils.toInt(row.get("c").toString()),
                            NumberUtils.toFloat(row.get("rating").toString()))
            );
        }
        return reviewT2Map;
    }

    public Map<String, F.T2<Integer, Float>> skuRelateReviews(Collection<String> skus) {
        SqlSelect sql = new SqlSelect()
                .select("count(r.alrId) as c", "avg(r.rating) as rating",
                        "l.product_sku as sku")
                .from("AmazonListingReview r")
                .leftJoin("Listing l ON r.listing_listingId=l.listingId")
                .where("l.product_sku IN " + SqlSelect.inlineParam(skus));
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        Map<String, F.T2<Integer, Float>> reviewT2Map = new HashMap<String, F.T2<Integer, Float>>();
        for(Map<String, Object> row : rows) {
            reviewT2Map.put(row.get("sku").toString(),
                    new F.T2<Integer, Float>(
                            NumberUtils.toInt(row.get("c").toString()),
                            NumberUtils.toFloat(row.get("rating").toString()))
            );
        }
        return reviewT2Map;
    }

    /**
     * 查询 SKU 最新的rating
     *
     * @param sku
     * @return
     */
    public F.T2<Float, Date> skuLastRating(String sku) {
        SqlSelect sql = new SqlSelect()
                .select("r.rating as rating, r.createDate as dt")
                .from("AmazonListingReview r")
                .leftJoin("Listing l on r.listingId=l.listingId")
                .where("l.product_sku=?").param(sku)
                .orderBy("r.createDate desc").limit(1);
        Map<String, Object> row = DBUtils.row(sql.toString(), sku);
        if(row.get("rating") != null) {
            return new F.T2<Float, Date>(NumberUtils.toFloat(row.get("rating").toString()), (Date) row.get("dt"));
        }
        return new F.T2<Float, Date>(-1f, null);
    }

}
