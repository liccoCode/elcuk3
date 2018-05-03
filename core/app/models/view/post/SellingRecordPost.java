package models.view.post;

import helper.DBUtils;
import helper.Dates;
import models.market.M;
import models.market.SellingRecord;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/3/12
 * Time: 4:32 PM
 */
public class SellingRecordPost extends Post<SellingRecord> {

    private static final long serialVersionUID = -2081286181455788781L;

    public Date from;
    public Date to;
    public int perSize = 50;

    /**
     * 市场
     */
    public M market;

    public String sku;
    public String categoryId;


    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT s FROM SellingRecord s WHERE  1=1 AND ");
        List<Object> params = new ArrayList<>();
        /** 时间参数 **/
        sbd.append(" s.date>=? AND s.date<=?");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

        if(this.search != null && !"".equals(this.search.trim())) {
            String word = String.format("%%%s%%", StringUtils.replace(search.trim(), "'", "''"));
            sbd.append(" AND s.product_sku like ? ");
            params.add(word);
        }
        if(this.market != null) {
            sbd.append(" AND s.market=? ");
            params.add(this.market);
        }
        sbd.append(" ORDER BY s.id");
        return new F.T2<>(sbd.toString(), params);
    }


    public SellingRecordPost() {
        this.from = DateTime.now().minusDays(3).toDate();
        this.to = new Date();
    }


    public List<SellingRecord> query() {
        F.T2<String, List<Object>> params = params();
        this.count = SellingRecord.find(params._1, params._2.toArray()).fetch().size();
        String sql = params._1 + " ";
        return SellingRecord.find(sql, params._2.toArray()).fetch(this.page, this.perSize);
    }

    public List<SellingRecord> queryForExcel() {
        F.T2<String, List<Object>> params = params();
        String sql = params._1 + " ";
        return SellingRecord.find(sql, params._2.toArray()).fetch();
    }

    public Long getTotalCount() {
        return this.count;
    }

    public List<Map<String, Object>> queryProfits() {
        StringBuffer sql = new StringBuffer();
        List<Object> params = new ArrayList<>();
        sql.append(" select t.product_sku sku,t.market,units quantity,cast(sales as decimal(18,2)) totalfee, ");
        sql.append(" cast(amzFee as decimal(18,2)) amazonfee,cast(fbaFee as decimal(18,2)) fbafee,  ");
        sql.append(" cast(averageProcurePrice as decimal(18,2))  procureprice,  ");
        sql.append(" cast(averageShipPrice as decimal(18,2)) shipprice, ");
        sql.append(" cast(averageVATPrice as decimal(18,2)) vatprice, ");
        sql.append(" cast((sales+ amzFee+fbaFee-(averageProcurePrice+averageShipPrice+averageVATPrice)*units) ");   
        sql.append(" as decimal (18, 2)) totalprofit , ");
        sql.append(" ifnull(cast((sales+ amzFee+fbaFee-(averageProcurePrice+averageShipPrice+averageVATPrice)*units) ");
        sql.append(" /sales as decimal(18,4)),0)*100 profitrate ");
        sql.append(" from ( ");
        sql.append(" select  t.product_sku,t.market,sum(t.units) units,");
        sql.append(" sum(t.sales) sales,sum(t.amzFee) amzFee, sum(t.fbaFee) fbaFee,  ");
        sql.append(" ifnull(a.averageProcurePrice,0) averageProcurePrice, ");
        sql.append(" ifnull(a.averageShipPrice,0) averageShipPrice,  ifnull(a.averageVATPrice,0) averageVATPrice ");
        sql.append(" from SellingRecord t  left join  AverageData a on  a.selling_sellingId = t.selling_sellingId ");
        sql.append(" left join Product p on p.sku = t.product_sku ");
        sql.append(" where 1=1  ");

        if(from != null) {
            sql.append("  AND t.date >= ?");
            params.add(new SimpleDateFormat("yyyy-MM-dd").format(from));
        }
        if(to != null) {
            sql.append("  AND t.date <=?  ");
            params.add(new SimpleDateFormat("yyyy-MM-dd").format(to));
        }
        if(market != null) {
            sql.append(" AND t.market = ? ");
            params.add(market.name());
        }
        if(sku != null && !"".equals(sku)) {
            sql.append(" AND t.product_sku = ? ");
            params.add(sku.trim());
        }
        if(categoryId != null && !"".equals(categoryId)) {
            sql.append(" AND p.category_categoryId = ? ");
            params.add(categoryId);
        }
        sql.append(" group by t.product_sku,t.market  ");
        sql.append(" ) t ");
        return DBUtils.rows(sql.toString(), params.toArray());
    }

}
