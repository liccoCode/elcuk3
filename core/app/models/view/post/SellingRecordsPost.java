package models.view.post;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import helper.Caches;
import helper.DBUtils;
import helper.Dates;
import models.market.M;
import models.market.SellingRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 8/16/13
 * Time: 2:12 PM
 */
public class SellingRecordsPost extends Post<SellingRecord> {
    private static final long serialVersionUID = 7039370941066949195L;

    public SellingRecordsPost() {
        this.from = new DateTime(this.to).minusMonths(1).toDate();
        this.perSize = 20;
    }

    public Date dateTime = new Date();

    public String market;

    public String categoryId;

    /**
     * Selling, SKU, Category 三个种类
     */
    public String type = "selling";

    // 是否过滤掉含有 ,2 的 sid/sku; 默认过滤
    public boolean filterDot2 = true;

    public String orderBy = "sales";
    public boolean desc = true;

    @Override
    public F.T2<String, List<Object>> params() {
        throw new UnsupportedOperationException("SellingRecordsPost 不需要 params()");
    }

    @Override
    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public List<SellingRecord> query() {
        if(!"selling".equals(this.type)) {
            throw new UnsupportedOperationException("还不支持查看" + this.type + "类型的数据");
        }

        SqlSelect sql = new SqlSelect()
                .select(
                        "sr.selling_sellingId",
                        // 费用
                        "avg(sr.salePrice) salePrice", "sum(sr.units) units", "sum(sr.sales) sales",
                        "sum(sr.income * sr.units) income",
                        // 利润
                        "sum(sr.profit * sr.units) profit", "avg(sr.costProfitRatio) costProfitRatio",
                        "avg(sr.saleProfitRatio) saleProfitRatio",
                        // 成本
                        "avg(sr.procureCost) procureCost", "avg(sr.airCost) airCost", "avg(sr.seaCost) seaCost",
                        "avg(sr.expressCost) expressCost"
                ).from("SellingRecord sr")
                .where("sr.date>=?").param(Dates.date2JDate(this.from))
                .where("sr.date<?").param(Dates.date2Date(new DateTime(this.to).plusDays(1).toDate()))
                .where("units>0").where("sales>0")
                .groupBy("sr.selling_sellingId")
                .orderBy("sales DESC");

        // 过滤 market
        if(StringUtils.isNotBlank(this.market)) {
            sql.where("sr.market=?").param(M.val(this.market).name());
        }
        List<SellingRecord> records = rowsToRecords(sql);

        // 过滤 category
        if(StringUtils.isNotBlank(this.categoryId)) {
            final String categoryId = this.categoryId;
            records = new ArrayList<>(Collections2.filter(records, new Predicate<SellingRecord>() {
                @Override
                public boolean apply(SellingRecord record) {
                    return record.selling.sellingId.startsWith(categoryId);
                }
            }));
        }


        return this.programPager(records);
    }

    @SuppressWarnings("unchecked")
    public List<SellingRecord> rowsToRecords(SqlSelect sql) {
        String cackeKey = Caches.Q.cacheKey(sql.toString(), sql.getParams());
        List<SellingRecord> records = Cache.get(cackeKey, List.class);
        if(records != null) return records;
        synchronized(cackeKey.intern()) {
            records = Cache.get(cackeKey, List.class);
            if(records != null) return records;
            records = new ArrayList<>();

            List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
            for(Map<String, Object> row : rows) {
                SellingRecord record = new SellingRecord(row.get("selling_sellingId").toString());
                // 费用
                record.salePrice = toFloat(row.get("salePrice"));
                record.units = toInt(row.get("units"));
                record.sales = toFloat(row.get("sales"));
                record.income = toFloat(row.get("income"));
                //利润
                record.profit = toFloat(row.get("profit"));
                record.costProfitRatio = toFloat(row.get("costProfitRatio"));
                record.saleProfitRatio = toFloat(row.get("saleProfitRatio"));
                //成本
                record.procureCost = toFloat(row.get("procureCost"));
                record.airCost = toFloat(row.get("airCost"));
                record.seaCost = toFloat(row.get("seaCost"));
                record.expressCost = toFloat(row.get("expressCost"));

                records.add(record);
            }
            Cache.add(cackeKey, records);
        }
        return records;
    }

    private float toFloat(Object obj) {
        if(obj == null) return 0;
        return NumberUtils.toFloat(obj.toString());
    }

    private int toInt(Object obj) {
        if(obj == null) return 0;
        return NumberUtils.toInt(obj.toString());
    }

}
