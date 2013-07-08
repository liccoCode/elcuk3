package models.view.post;

import helper.Dates;
import helper.Promises;
import models.market.M;
import models.procure.ProcureUnit;
import models.view.dto.AnalyzeDTO;
import models.view.dto.TimelineEventSource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.libs.F;
import play.utils.FastRuntimeException;
import query.*;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 分析页面的 Post 请求
 * User: wyattpan
 * Date: 10/8/12
 * Time: 10:16 AM
 */
public class AnalyzePost extends Post<AnalyzeDTO> {
    public static final String AnalyzeDTO_SID_CACHE = "analyze_post_sid";
    public static final String AnalyzeDTO_SKU_CACHE = "analyze_post_sku";
    public Date from = super.from;
    public Date to = super.to;

    public AnalyzePost() {
        this.perSize = 20;
    }

    public AnalyzePost(String type) {
        this.type = type;
    }

    public String type = "sid";
    public String aid;

    public String orderBy = "day7";
    public Boolean desc = true;

    // 是否过滤掉含有 ,2 的 sid/sku; 默认过滤
    public boolean filterDot2 = true;
    public String categoryId;

    /**
     * 根据 type 指定是 msku 还是 sku
     */
    public String val;

    public String market;

    /**
     * 根据 type(sku/msku(sid)) 进行数据的分析计算
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<AnalyzeDTO> analyzes() {
        if(StringUtils.isNotBlank(this.type)) this.type = this.type.toLowerCase();
        String cacke_key = "sid".equals(this.type) ? AnalyzeDTO_SID_CACHE : AnalyzeDTO_SKU_CACHE;
        // 这个地方有缓存, 但还是需要一个全局锁, 控制并发, 如果需要写缓存则锁住
        List<AnalyzeDTO> dtos = Cache.get(cacke_key, List.class);
        if(dtos != null) return dtos;

        synchronized(AnalyzeDTO.class) {
            dtos = Cache.get(cacke_key, List.class);
            if(dtos != null) return dtos;

            dtos = new ArrayList<AnalyzeDTO>();
            boolean isSku = StringUtils.equalsIgnoreCase("sku", this.type);

            // 准备计算用的数据容器
            Map<String, AnalyzeDTO> analyzeMap = new HashMap<String, AnalyzeDTO>();
            if(isSku) {
                for(String sku : new ProductQuery().skus()) {
                    analyzeMap.put(sku, new AnalyzeDTO(sku));
                }
            } else {
                for(AnalyzeDTO dto : new SellingQuery().analyzePostDTO()) {
                    analyzeMap.put(dto.fid, dto);
                }
            }

            // 销量 AnalyzeVO
            pullDaySales(isSku, analyzeMap);

            // ProcureUnit
            for(AnalyzeDTO dto : analyzeMap.values()) {
                // 切换 ProcureUnit 的 sku/sid 的参数?
                // todo:需要添加时间限制, 减少需要计算的 ProcureUnit 吗?
                List<ProcureUnit> untis = ProcureUnit.find(
                        (isSku ? "product.sku=?" : "selling.sellingId=?") + " AND stage NOT IN (?,?)",
                        dto.fid, ProcureUnit.STAGE.CLOSE, ProcureUnit.STAGE.SHIP_OVER)
                        .fetch();

                // plan, working, worked, way
                for(ProcureUnit unit : untis) {
                    if(unit.stage == ProcureUnit.STAGE.PLAN) dto.plan += unit.qty();
                    else if(unit.stage == ProcureUnit.STAGE.DELIVERY) dto.working += unit.qty();
                    else if(unit.stage == ProcureUnit.STAGE.DONE) dto.worked += unit.qty();
                    else if(unit.stage == ProcureUnit.STAGE.SHIPPING) dto.way += unit.qty();
                    else if(unit.stage == ProcureUnit.STAGE.INBOUND)
                        dto.inbound += (unit.qty() - unit.inboundingQty());
                }
                dto.difference = dto.day1 - dto.day7 / 7;
                dtos.add(dto);
            }

            // qty cal
            pullQtyToDTO(isSku, analyzeMap);

            // review
            pullReviewToDTO(isSku, analyzeMap);

            Cache.add(cacke_key, dtos, "8h");
            Cache.set(cacke_key + ".time", DateTime.now().plusHours(8).toDate(), "8h");
        }

        return dtos;
    }

    private void pullDaySales(boolean isSku, Map<String, AnalyzeDTO> analyzeMap) {
        DateTime now = DateTime.now();
        OrderItemQuery query = new OrderItemQuery();
        pullDay1(isSku, analyzeMap, now, query);
        pullDay7(isSku, analyzeMap, now, query);
        pullDay30(isSku, analyzeMap, now, query);
    }

    /**
     * 昨天
     *
     * @param isSku
     * @param analyzeMap
     * @param now
     * @param query
     */
    private void pullDay1(final boolean isSku, Map<String, AnalyzeDTO> analyzeMap, final DateTime now,
                          final OrderItemQuery query) {
        List<Map<String, Integer>> results = Promises.forkJoin(new Promises.DBCallback<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> doJobWithResult(M m) {
                return query.analyzeDaySale(
                        Dates.morning(now.minusDays(1).toDate()),
                        Dates.night(now.minusDays(1).toDate()),
                        m,
                        isSku,
                        getConnection());
            }

            @Override
            public String id() {
                return "AnalyzePost.pullDay1";
            }
        });
        for(Map<String, Integer> result : results) {
            for(String key : result.keySet()) {
                try {
                    analyzeMap.get(key).day1 += result.get(key) == null ? 0 : result.get(key);
                } catch(NullPointerException e) {
                    //ignore 如果不存在就不需要计算了
                }
            }
        }
    }

    /**
     * Day7 不包括今天的前 7 天
     *
     * @param isSku
     * @param analyzeMap
     * @param now
     * @param query
     */
    private void pullDay7(final boolean isSku, Map<String, AnalyzeDTO> analyzeMap, final DateTime now,
                          final OrderItemQuery query) {
        List<Map<String, Integer>> results = Promises.forkJoin(new Promises.DBCallback<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> doJobWithResult(M m) {
                return query.analyzeDaySale(
                        Dates.morning(now.minusDays(8).toDate()),
                        Dates.night(now.minusDays(1).toDate()),
                        m,
                        isSku,
                        getConnection());
            }

            @Override
            public String id() {
                return "AnalyzePost.pullDay7";
            }
        });
        for(Map<String, Integer> result : results) {
            for(String key : result.keySet()) {
                try {
                    analyzeMap.get(key).day7 += result.get(key) == null ? 0 : result.get(key);
                } catch(NullPointerException e) {
                    //ignore 如果不存在就不需要计算了
                }
            }
        }
    }

    /**
     * Day30 不包括今天的前 30 天
     *
     * @param isSku
     * @param analyzeMap
     * @param now
     * @param query
     */
    private void pullDay30(final boolean isSku, Map<String, AnalyzeDTO> analyzeMap, final DateTime now,
                           final OrderItemQuery query) {
        List<Map<String, Integer>> results = Promises.forkJoin(new Promises.DBCallback<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> doJobWithResult(M m) {
                return query.analyzeDaySale(
                        Dates.morning(now.minusDays(31).toDate()),
                        Dates.night(now.minusDays(1).toDate()),
                        m,
                        isSku,
                        getConnection());
            }

            @Override
            public String id() {
                return "AnalyzePost.pullDay30";
            }
        });
        for(Map<String, Integer> result : results) {
            for(String key : result.keySet()) {
                try {
                    analyzeMap.get(key).day30 += result.get(key) == null ? 0 : result.get(key);
                } catch(NullPointerException e) {
                    //ignore 如果不存在就不需要计算了
                }
            }
        }
    }

    /**
     * 拽出 qty 给 Dto
     *
     * @param sku
     * @param analyzeMap
     */
    private void pullQtyToDTO(boolean sku, Map<String, AnalyzeDTO> analyzeMap) {
        SellingQTYQuery sellingQTYQuery = new SellingQTYQuery();
        Map<String, Integer> qtyMap;
        if(sku) {
            qtyMap = sellingQTYQuery.sumQtyWithSKU(analyzeMap.keySet());
        } else {
            qtyMap = sellingQTYQuery.sumQtyWithSellingId(analyzeMap.keySet());
        }

        for(AnalyzeDTO dto : analyzeMap.values()) {
            dto.qty = qtyMap.get(dto.fid) == null ? 0 : qtyMap.get(dto.fid);
        }
    }

    /**
     * 拽出 Review 的信息给 DTO
     *
     * @param sku
     * @param analyzeMap
     */
    private void pullReviewToDTO(boolean sku, Map<String, AnalyzeDTO> analyzeMap) {
        AmazonListingReviewQuery amazonQuery = new AmazonListingReviewQuery();
        Map<String, F.T2<Integer, Float>> reviewMap;
        Map<String, F.T2<Float, Date>> latestReviewMap = new HashMap<String, F.T2<Float, Date>>();
        if(sku) {
            reviewMap = amazonQuery.skuRelateReviews(analyzeMap.keySet());
            latestReviewMap = amazonQuery.skusLastRating(analyzeMap.keySet());
        } else {
            reviewMap = amazonQuery.sidRelateReviews(analyzeMap.keySet());
        }
        for(AnalyzeDTO dto : analyzeMap.values()) {
            F.T2<Integer, Float> reviewT2 = reviewMap.get(dto.fid);
            if(reviewT2 == null) {
                dto.reviews = 0;
                dto.rating = 0;
            } else {
                dto.reviews = reviewT2._1;
                dto.rating = reviewT2._2;
            }
            if(dto.reviews > 0) {
                dto.reviewRatio = dto.reviews / ((5 - dto.rating) == 0 ? 0.1f : (5 - dto.rating));
            } else {
                dto.reviewRatio = 0;
            }

            //最新的评分
            if(sku) {
                F.T2<Float, Date> t2 = latestReviewMap.get(dto.fid);
                if(t2 != null) {
                    dto.lastRating = t2._1;
                    dto.lastRatingDate = t2._2;
                }
            }
        }
    }

    @Override
    public F.T2<String, List<Object>> params() {
        // no use
        return new F.T2<String, List<Object>>("", new ArrayList<Object>());
    }

    @Override
    public List<AnalyzeDTO> query() {
        List<AnalyzeDTO> dtos = new ArrayList<AnalyzeDTO>(this.analyzes());
        // 过滤 Category
        if(StringUtils.isNotBlank(this.categoryId))
            CollectionUtils.filter(dtos, new SearchPredicate("^" + this.categoryId));
        if(StringUtils.isNotBlank(this.search))
            CollectionUtils.filter(dtos, new SearchPredicate(this.search));
        if(StringUtils.isNotBlank(this.orderBy))
            Collections.sort(dtos, new FieldComparator(this.orderBy, this.desc));
        if(StringUtils.isNotBlank(this.aid) && "sid".equalsIgnoreCase(this.type))
            CollectionUtils.filter(dtos, new AccountIdPredicate(this.aid));
        if(this.filterDot2) CollectionUtils.filter(dtos, new UnContainsPredicate(",2"));
        if(StringUtils.isNotBlank(this.market))
            CollectionUtils.filter(dtos, new MarketPredicate(M.val(this.market)));

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

    public static Date cachedDate(String type) {
        String cacke_key = "sid".equals(type) ? AnalyzeDTO_SID_CACHE : AnalyzeDTO_SKU_CACHE;
        return Cache.get(cacke_key + ".time", Date.class);
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
                return dto.fid.toLowerCase()
                        .startsWith(StringUtils.replace(str.toLowerCase(), "^", ""));
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

    private static class MarketPredicate implements Predicate {
        private M market;

        public MarketPredicate(M market) {
            this.market = market;
        }

        @Override
        public boolean evaluate(Object o) {
            AnalyzeDTO dto = (AnalyzeDTO) o;
            return this.market.equals(dto.market);
        }

    }

    // ---------------- TimeLine ------------------------

    /**
     * 加载并且返回 Simile Timeline 的 Events
     * type 只允许为 sku, sid 两种类型; 如果 type 为空,默认为 sid
     */
    public static TimelineEventSource timelineEvents(String type, String val) {
        if(StringUtils.isBlank(type)) type = "sid";
        if("msku".equals(type)) type = "sid"; // 兼容
        if(!"sku".equals(type) && !"sid".equals(type))
            throw new FastRuntimeException("查看的数据类型(" + type + ")错误! 只允许 sku 与 sid.");

        DateTime dt = DateTime.now();
        List<ProcureUnit> units = ProcureUnit
                .find("createDate>=? AND createDate<=? AND " + type/*sid/sku*/ + "=?",
                        Dates.morning(dt.minusMonths(12).toDate()), Dates.night(dt.toDate()), val)
                .fetch();


        // 将所有与此 SKU/SELLING 关联的 ProcureUnit 展示出来.(前 9 个月~后3个月)
        TimelineEventSource eventSource = new TimelineEventSource();
        AnalyzeDTO analyzeDTO = AnalyzeDTO.findByValAndType(type, val);
        for(ProcureUnit unit : units) {
            TimelineEventSource.Event event = new TimelineEventSource.Event(analyzeDTO, unit);
            event.startAndEndDate(type)
                    .titleAndDesc()
                    .color(unit.stage);

            eventSource.events.add(event);
        }


        // 将当前 Selling 的销售情况展现出来
        eventSource.events.add(TimelineEventSource.currentQtyEvent(analyzeDTO, type));

        return eventSource;
    }

    public void setVal(String val) {
        if(StringUtils.isNotBlank(val))
            val = StringUtils.split(val, "|")[0];
        this.val = val;
    }

    @Override
    public AnalyzePost clone() throws CloneNotSupportedException {
        return (AnalyzePost) super.clone();
    }
}
