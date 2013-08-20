package models.view.post;

import jobs.analyze.SellingRecordCaculateJob;
import models.market.M;
import models.market.SellingRecord;
import models.product.Product;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.libs.F;
import play.utils.FastRuntimeException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 8/16/13
 * Time: 2:12 PM
 */
public class SellingRecordsPost extends Post<SellingRecord> {
    public SellingRecordsPost() {
        this.from = new DateTime(this.to).minusMonths(1).toDate();
        this.perSize = 20;
    }

    public Date dateTime = new Date();

    public String market;

    /**
     * Selling, SKU, Category 三个种类
     */
    public String type = "selling";

    @Override
    public F.T2<String, List<Object>> params() {
        return new F.T2<String, List<Object>>("", null);
    }

    @Override
    public Long getTotalCount() {
        return this.count;
    }

    @SuppressWarnings("unchecked")
    public List<SellingRecord> records() {
        List<SellingRecord> records = Cache.get("sellingRecordCaculateJob", List.class);
        if(records == null || records.size() == 0) {
            String running = Cache.get("sellingRecordCaculateJobRunning", String.class);
            if(StringUtils.isNotBlank(running)) throw new FastRuntimeException("正在计算中, 请等待 10 分钟后重试.");
            DateTime dateBegin = new DateTime(2013, 7, 3, 0, 0);
            new SellingRecordCaculateJob(dateBegin).now();
        }
        return records;
    }

    @Override
    public List<SellingRecord> query() {
        List<SellingRecord> records = records();
        if(StringUtils.isNotBlank(this.type)) {
            if("sku".equals(this.type)) {
                records = recordsToSKU(records);
            } else if("category".equals(this.type)) {
            }
        }

        if(StringUtils.isNotBlank(this.market)) {
            CollectionUtils.filter(records, new MarketPredicate(M.val(this.market)));
        }
        return this.programPager(records);
    }

    private static class MarketPredicate implements Predicate {
        private M market;

        public MarketPredicate(M market) {
            this.market = market;
        }

        @Override
        public boolean evaluate(Object o) {
            SellingRecord dto = (SellingRecord) o;
            return this.market.equals(dto.market);
        }

    }

    /**
     * 把 Records 中的内容统计为 SKU 级别
     *
     * @param records
     * @return
     */
    public List<SellingRecord> recordsToSKU(List<SellingRecord> records) {
        Map<String, SellingRecord> skuRecordsMap = new HashMap<String, SellingRecord>();
        M market = M.val(this.market);
        for(SellingRecord rcd : records) {
            if(market != null && rcd.market != market) continue;
            String sku = Product.merchantSKUtoSKU(rcd.selling.merchantSKU);
            SellingRecord record;
            if(skuRecordsMap.containsKey(sku)) {
                record = skuRecordsMap.get(sku);
            } else {
                record = new SellingRecord(rcd.selling, rcd.date);
                record.selling.sellingId = sku;
                skuRecordsMap.put(sku, record);
            }
            record.units += rcd.units;
            record.sales += rcd.sales;
            record.income += rcd.income;
            record.procureCost += rcd.procureCost;
            record.procureNumberSum += rcd.procureNumberSum;
            record.shipCost += rcd.shipCost;
            record.shipNumberSum += rcd.shipNumberSum;
            record.profit += rcd.profit;
        }
        for(SellingRecord record : skuRecordsMap.values()) {
            record.costProfitRatio = record.profit / (record.shipCost + record.procureCost);
            record.saleProfitRatio = record.profit / record.sales;
        }
        return new ArrayList<SellingRecord>(skuRecordsMap.values());
    }
}
