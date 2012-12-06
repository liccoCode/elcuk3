package models.view.post;

import helper.Dates;
import models.market.Selling;
import models.market.SellingQTY;
import models.procure.ProcureUnit;
import models.product.Product;
import models.view.dto.AnalyzeDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.cache.Cache;
import play.libs.F;
import query.AmazonListingReviewQuery;
import query.OrderItemQuery;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 分析页面的 Post 请求
 * User: wyattpan
 * Date: 10/8/12
 * Time: 10:16 AM
 */
public class AnalyzePost extends Post<AnalyzeDTO> {
    public static final String AnalyzeDTO_SID_CACHE = "analyze_post_sid";
    public static final String AnalyzeDTO_SKU_CACHE = "analyze_post_sku";

    public AnalyzePost() {
        this.perSize = 20;
    }

    public String type = "sid";
    public String aid;

    public String orderBy = "day7";
    public Boolean desc = true;

    // 是否过滤掉含有 ,2 的 sid/sku; 默认过滤
    public boolean filterDot2 = true;

    /**
     * 根据 type 指定是 msku 还是 sku
     */
    public String val;

    /**
     * 根据 type(sku/msku(sid)) 进行数据的分析计算
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<AnalyzeDTO> analyzes() {
        if(StringUtils.isNotBlank(this.type)) this.type = this.type.toLowerCase();
        String cacke_key = "sid".equals(this.type) ? AnalyzeDTO_SID_CACHE : AnalyzeDTO_SKU_CACHE;
        List<AnalyzeDTO> dtos = Cache.get(cacke_key, List.class);
        if(dtos == null) {
            synchronized(AnalyzePost.class) {
                dtos = Cache.get(cacke_key, List.class);
                if(dtos != null) return dtos;

                boolean isSku = StringUtils.equalsIgnoreCase("sku", this.type);

                DateTime nowWithMorning = new DateTime(Dates.morning(new Date()));

                // 准备计算用的数据容器
                Map<String, AnalyzeDTO> analyzeMap = new HashMap<String, AnalyzeDTO>();
                if(isSku) {
                    for(Product product : Product.<Product>findAll())
                        analyzeMap.put(product.sku, new AnalyzeDTO(product.sku));
                } else {
                    for(Selling selling : Selling.<Selling>findAll())
                        analyzeMap.put(selling.sellingId, new AnalyzeDTO(selling));
                }


                // sku, sid, qty, date, acc.id
                List<F.T5<String, F.T2<String, String>, Integer, Date, String>> t5s = OrderItemQuery.sku_sid_asin_qty_date_aId(nowWithMorning.minusDays(30).toDate(), Dates.night(nowWithMorning.toDate()), 0);

                // 销量
                for(F.T5<String, F.T2<String, String>, Integer, Date, String> t5 : t5s) {
                    // 切换 sku/sid 的 key
                    String key = isSku ? t5._1 : t5._2._1;
                    AnalyzeDTO currentDto = analyzeMap.get(key);
                    if(currentDto == null) {
                        Logger.warn("T4: %s, DTO is not exist. Key[%s]", t5, key);
                        continue;
                    }

                    long differTime = nowWithMorning.toDate().getTime() - t5._4.getTime();
                    if(differTime <= TimeUnit.DAYS.toMillis(1) && differTime >= 0)
                        currentDto.day0 += t5._3;
                    if(differTime <= TimeUnit.DAYS.toMillis(2) && differTime >= 0)
                        currentDto.day1 += t5._3;
                    if(differTime <= TimeUnit.DAYS.toMillis(7) && differTime >= 0)
                        currentDto.day7 += t5._3;
                    if(differTime <= TimeUnit.DAYS.toMillis(30) && differTime >= 0)
                        currentDto.day30 += t5._3;
                }

                // ProcureUnit
                for(AnalyzeDTO dto : analyzeMap.values()) {
                    // 切换 ProcureUnit 的 sku/sid 的参数
                    List<ProcureUnit> untis = ProcureUnit.find((isSku ? "sku=?" : "sid=?") + " AND stage NOT IN (?,?)", dto.fid, ProcureUnit.STAGE.CLOSE, ProcureUnit.STAGE.SHIP_OVER).fetch();

                    // plan, working, worked, way
                    for(ProcureUnit unit : untis) {
                        if(unit.stage == ProcureUnit.STAGE.PLAN) dto.plan += unit.qty();
                        else if(unit.stage == ProcureUnit.STAGE.DELIVERY) dto.working += unit.qty();
                        else if(unit.stage == ProcureUnit.STAGE.DONE) dto.worked += unit.qty();
                        else if(unit.stage == ProcureUnit.STAGE.SHIPPING) dto.way += unit.qty();
                        else if(unit.stage == ProcureUnit.STAGE.INBOUND) dto.inbound += unit.inboundingQty();
                    }

                    // ps cal
                    List<SellingQTY> qtys = SellingQTY.find((isSku ? "product.sku=?" : "selling.sellingId=?"), dto.fid).fetch();
                    for(SellingQTY qty : qtys) dto.qty += qty.qty;

                    // review
                    F.T3<Integer, Float, List<String>> reviewT3;
                    if(isSku) reviewT3 = AmazonListingReviewQuery.skuRelateReviews(dto.fid);
                    else reviewT3 = AmazonListingReviewQuery.sidRelateReviews(dto.fid);
                    dto.reviews = reviewT3._1;
                    dto.rating = reviewT3._2;
                    if(dto.reviews > 0)
                        dto.reviewRatio = dto.reviews / ((5 - dto.rating) == 0 ? 0.1f : (5 - dto.rating));
                    else dto.reviewRatio = 0;
                }

                // TODO 这部分缓存需要处理成为有生命周期的, 因为时间跨度越长, 缓存的数据越大, 而查看的几率越小, 所以需要对这缓存添加生命周期; 同时需要为系统中添加统一的访问这部分缓存的接口.
                Cache.add(cacke_key, new ArrayList<AnalyzeDTO>(analyzeMap.values()));
            }
        }

        return Cache.get(cacke_key, List.class);
    }

    @Override
    public F.T2<String, List<Object>> params() {
        // no use
        return new F.T2<String, List<Object>>("", new ArrayList<Object>());
    }

    @Override
    public List<AnalyzeDTO> query() {
        List<AnalyzeDTO> dtos = new ArrayList<AnalyzeDTO>(this.analyzes());
        if(StringUtils.isNotBlank(this.search)) CollectionUtils.filter(dtos, new SearchPredicate(this.search));
        if(StringUtils.isNotBlank(this.orderBy)) Collections.sort(dtos, new FieldComparator(this.orderBy, this.desc));
        if(StringUtils.isNotBlank(this.aid) && "sid".equalsIgnoreCase(this.type))
            CollectionUtils.filter(dtos, new AccountIdPredicate(this.aid));
        if(this.filterDot2) CollectionUtils.filter(dtos, new UnContainsPredicate(",2"));

        this.count = dtos.size();
        List<AnalyzeDTO> afterPager = new ArrayList<AnalyzeDTO>();
        int index = (this.page - 1) * this.perSize;
        int end = index + this.perSize;
        for(; index < end; index++) {
            if(index >= this.count) break;
            afterPager.add(dtos.get(index));
        }
        return afterPager;
    }

    private static class FieldComparator implements Comparator<AnalyzeDTO> {
        private Field field;
        private boolean desc = true;

        private FieldComparator(String fieldName, Boolean desc) {
            try {
                this.field = AnalyzeDTO.class.getField(fieldName);
            } catch(Exception e) {
                try {
                    this.field = AnalyzeDTO.class.getField("day7");
                } catch(Exception e1) {
                    //ignore
                }
            }
            this.desc = desc == null ? true : desc;
        }

        @Override
        public int compare(AnalyzeDTO o1, AnalyzeDTO o2) {
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

        private SearchPredicate(String containsString) {
            this.str = containsString;
        }

        @Override
        public boolean evaluate(Object o) {
            AnalyzeDTO dto = (AnalyzeDTO) o;
            if(str.startsWith("^"))
                return dto.fid.toLowerCase().startsWith(StringUtils.replace(str.toLowerCase(), "^", ""));
            else
                return dto.fid.toLowerCase().contains(str.toLowerCase());
        }
    }

    private static class UnContainsPredicate implements Predicate {
        private String str;

        private UnContainsPredicate(String str) {
            this.str = str;
        }

        @Override
        public boolean evaluate(Object o) {
            AnalyzeDTO dto = (AnalyzeDTO) o;
            return !dto.fid.toLowerCase().contains(str.toLowerCase());
        }
    }

    private static class AccountIdPredicate implements Predicate {
        private String aid;

        public AccountIdPredicate(String aid) {
            this.aid = aid;
        }

        @Override
        public boolean evaluate(Object o) {
            AnalyzeDTO dto = (AnalyzeDTO) o;
            return this.aid.equals(dto.aid);
        }
    }
}
