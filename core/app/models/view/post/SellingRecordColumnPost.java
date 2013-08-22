package models.view.post;

import helper.Dates;
import models.market.M;
import models.view.dto.HighChart;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.List;

/**
 * 用来计算销售财务分析页面的柱状图
 * User: wyatt
 * Date: 8/22/13
 * Time: 4:56 PM
 */
public class SellingRecordColumnPost extends Post<HighChart> {
    private static final long serialVersionUID = -4416739812002024200L;

    public SellingRecordColumnPost() {
        // 12 个月内的柱状图
        this.from = new DateTime(this.to).minusMonths(12).toDate();
    }

    public String market;

    // selling, sku, category
    public String type;

    public String val;

    @Override
    public F.T2<String, List<Object>> params() {
        SqlSelect sql = new SqlSelect()
                .select("date_format(date, '%Y-%m') as m", "sum(sr.sales) as sales", "sum(sr.units) as units",
                        "sum(sr.procureCost) procureCost", "sum(sr.procureNumberSum) procureNumberSum",
                        "sum(sr.shipCost) shipCost", "sum(sr.shipNumberSum) shipNumberSum",
                        "sum(sr.income) income", "sum(sr.profit) profit")
                .from("SellingRecord sr")
                .where("sr.date>=?").param(Dates.morning(this.from))
                .where("sr.date<=?").params(Dates.night(this.to))
                .groupBy("m");
        if(StringUtils.isNotBlank(this.market)) {
            sql.where("sr.market=?").param(M.val(this.market).name());
        }
        this.whereType(sql);

        //"l.product_sku,
        return new F.T2<String, List<Object>>(sql.toString(), sql.getParams());
    }

    private void whereType(SqlSelect sql) {
        if("selling".equals(this.type)) {
            if(StringUtils.isNotBlank(this.val)) {
                sql.select("sr.selling_sellingId as sellingId")
                        .where("sr.selling_sellingId=?").param(this.val);
            }
        } else if("sku".equals(this.type)) {
            if(StringUtils.isNotBlank(this.val)) {
                sql.select("l.product_sku as sku")
                        .leftJoin("Selling s ON s.sellingId=sr.selling_sellingId")
                        .leftJoin("Listing l ON l.listingId=s.listing_listingId")
                        .where("l.product_sku=?").param(this.val);
            }
        } else if("category".equals(this.type)) {
            if(StringUtils.isNotBlank(this.val)) {
                sql.select("p.category_categoryId as categoryId")
                        .leftJoin("Selling s ON s.sellingId=sr.selling_sellingId")
                        .leftJoin("Listing l ON l.listingId=s.listing_listingId")
                        .leftJoin("Product p ON p.sku=l.product_sku")
                        .where("p.category_categoryId=?").param(this.val);
            }
        }
    }

    @Override
    public List<HighChart> query() {
        return super.query();
    }
}
