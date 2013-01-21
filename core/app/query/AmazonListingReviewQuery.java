package query;

import helper.DBUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 10/8/12
 * Time: 7:10 PM
 */
public class AmazonListingReviewQuery {

    /**
     * 查询 Selling 关联的 Review
     *
     * @return ._1: review 数量; ._2: reivew 分数; ._3: reviewId
     */
    public F.T3<Integer, Float, List<String>> sidRelateReviews(String sid) {
        SqlSelect sql = new SqlSelect()
                .select("count(r.alrId) as c", "avg(r.rating) as rating",
                        "group_concat(r.reviewId) as reviewIds")
                .from("AmazonListingReview r")
                .leftJoin("Selling s USING(listing_listingId)")
                .where("s.sellingId=?").param(sid);
        Map<String, Object> row = DBUtils.row(sql.toString(), sql.getParams().toArray());
        if(NumberUtils.toInt(row.get("c").toString()) <= 0)
            return new F.T3<Integer, Float, List<String>>(0, 0f, Arrays.asList(""));

        return new F.T3<Integer, Float, List<String>>(
                NumberUtils.toInt(row.get("c").toString()),
                NumberUtils.toFloat(row.get("rating").toString()),
                Arrays.asList(StringUtils.split(row.get("reviewIds").toString(), ","))
        );
    }

    /**
     * 查询 SKU 关联的 Review
     *
     * @return ._1: review 数量; ._2: reivew 分数; ._3: reviewId
     */
    public F.T3<Integer, Float, List<String>> skuRelateReviews(String sku) {
        SqlSelect sql = new SqlSelect()
                .select("count(r.alrId) as c", "avg(r.rating) as rating",
                        "group_concat(r.reviewId) as reviewIds")
                .from("AmazonListingReview r")
                .leftJoin("Listing l ON r.listing_listingId=l.listingId")
                .where("l.product_sku=?").param(sku);
        Map<String, Object> row = DBUtils.row(sql.toString(), sql.getParams().toArray());
        if(NumberUtils.toInt(row.get("c").toString()) <= 0)
            return new F.T3<Integer, Float, List<String>>(0, 0f, Arrays.asList(""));
        return new F.T3<Integer, Float, List<String>>(
                NumberUtils.toInt(row.get("c").toString()),
                NumberUtils.toFloat(row.get("rating").toString()),
                Arrays.asList(StringUtils.split(row.get("reviewIds").toString(), ","))
        );
    }

}
