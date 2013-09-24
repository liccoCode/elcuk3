package models.view.post;

import jobs.analyze.SellingRecordCaculateJob;
import models.market.M;
import models.market.SellingRecord;
import models.product.Category;
import models.product.Product;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.libs.F;
import play.utils.FastRuntimeException;

import java.lang.reflect.Field;
import java.util.*;

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

    @SuppressWarnings("unchecked")
    public List<SellingRecord> records() {
        List<SellingRecord> records = Cache.get("sellingRecordCaculateJob", List.class);
        if(records == null || records.size() == 0) {
            if(!SellingRecordCaculateJob.isRunning())
                new SellingRecordCaculateJob(new DateTime(this.dateTime)).now();
            throw new FastRuntimeException("正在计算中, 请等待 10 分钟后重试.");
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
                records = recordToCategory(records);
            }
        }

        if(StringUtils.isNotBlank(this.market)) {
            CollectionUtils.filter(records, new MarketPredicate(M.val(this.market)));
        }
        if(StringUtils.isNotBlank(this.categoryId))
            CollectionUtils.filter(records, new SearchPredicate("^" + this.categoryId));

        if(this.filterDot2)
            CollectionUtils.filter(records, new UnContainsPredicate(","));
        if(StringUtils.isNotBlank(this.search))
            CollectionUtils.filter(records, new SearchPredicate(this.search));
        if(StringUtils.isNotBlank(this.orderBy))
            Collections.sort(records, new FieldComparator(this.orderBy, this.desc));
        return this.programPager(records);
    }

    private static class FieldComparator implements Comparator<SellingRecord> {
        private Field field;
        private boolean desc = true;

        private FieldComparator(String fieldName, Boolean desc) {
            try {
                this.field = SellingRecord.class.getField(fieldName);
            } catch(Exception e) {
                try {
                    this.field = SellingRecord.class.getField("sales");
                } catch(Exception e1) {
                    //ignore
                }
            }
            this.desc = desc == null ? true : desc;
        }

        @Override
        public int compare(SellingRecord o1, SellingRecord o2) {
            try {
                Float differ = 0f;
                if(desc) differ = (this.field.getFloat(o2) - this.field.getFloat(o1));
                else differ = (this.field.getFloat(o1) - this.field.getFloat(o2));
                // 避免太小无法正确比较大小
                if(differ < 1) differ *= 100;
                else if(differ < 0.1) differ *= 1000;
                else if(differ < 0.01) differ *= 10000;
                return differ.intValue();
            } catch(Exception e) {
                // 错误,没有这个字段,无法排序
                return 0;
            }
        }
    }

    private static class SearchPredicate implements Predicate {
        private String str;

        private SearchPredicate(String str) {
            this.str = str;
        }

        @Override
        public boolean evaluate(Object o) {
            SellingRecord dto = (SellingRecord) o;
            if(str.startsWith("^"))
                return dto.selling.sellingId.toLowerCase()
                        .startsWith(StringUtils.replace(str.toLowerCase(), "^", ""));
            else
                return dto.selling.sellingId.toLowerCase().contains(str.toLowerCase());
        }
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

    private static class UnContainsPredicate implements Predicate {
        private String str;

        private UnContainsPredicate(String str) {
            this.str = str;
        }

        @Override
        public boolean evaluate(Object o) {
            SellingRecord dto = (SellingRecord) o;
            return !dto.selling.sellingId.contains(this.str);
        }
    }

    /**
     * 把 Records 中的内容统计为 SKU 级别
     *
     * @param records
     * @return
     */
    public List<SellingRecord> recordsToSKU(List<SellingRecord> records) {
        return recordToWithCallback(records, new Callback() {
            @Override
            public String key(SellingRecord rcd) {
                return Product.merchantSKUtoSKU(rcd.selling.merchantSKU);
            }
        });
    }

    public List<SellingRecord> recordToCategory(List<SellingRecord> records) {
        return recordToWithCallback(records, new Callback() {
            @Override
            public String key(SellingRecord rcd) {
                return Category.skuToCategoryId(Product.merchantSKUtoSKU(rcd.selling.merchantSKU));
            }
        });
    }

    public List<SellingRecord> recordToWithCallback(List<SellingRecord> records,
                                                    Callback callback) {
        Map<String, SellingRecord> skuRecordsMap = new HashMap<String, SellingRecord>();
        M market = M.val(this.market);
        for(SellingRecord rcd : records) {
            if(market != null && rcd.market != market) continue;
            String key = callback.key(rcd);
            SellingRecord record;
            if(skuRecordsMap.containsKey(key)) {
                record = skuRecordsMap.get(key);
            } else {
                record = new SellingRecord(rcd.selling, rcd.date);
                record.selling.sellingId = key;
                skuRecordsMap.put(key, record);
            }
            record.units += rcd.units;
            record.sales += rcd.sales;
            record.income += rcd.income;
            record.procureCost += rcd.procureCost;
            record.procureNumberSum += rcd.procureNumberSum;
            record.profit += rcd.profit;
        }
        for(SellingRecord record : skuRecordsMap.values()) {
            // TODO 需要重新计算
            record.costProfitRatio = record.profit / (/*record.expressCost + */record.procureCost);
            record.saleProfitRatio = record.profit / record.sales;
        }
        return new ArrayList<SellingRecord>(skuRecordsMap.values());
    }

    /**
     * 计算返回的 key
     */
    public interface Callback {
        public String key(SellingRecord rcd);
    }
}
