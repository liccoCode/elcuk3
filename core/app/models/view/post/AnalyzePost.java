package models.view.post;

import helper.Dates;
import helper.Webs;
import models.market.M;
import models.market.Selling;
import models.market.SellingQTY;
import models.procure.ProcureUnit;
import models.product.Product;
import models.view.dto.AnalyzeDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.jobs.Job;
import play.libs.F;
import play.utils.FastRuntimeException;
import query.AmazonListingReviewQuery;
import query.OrderItemQuery;
import query.vo.AnalyzeVO;

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
    /**
     * 开启的, 会去搜索的市场
     */
    public static final M[] MARKETS = {M.AMAZON_DE, M.AMAZON_US, M.AMAZON_UK, M.AMAZON_FR};
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

                // 从北京时间?
                Date startOfDay = Dates.night(this.to);

                // 准备计算用的数据容器?
                Map<String, AnalyzeDTO> analyzeMap = new HashMap<String, AnalyzeDTO>();
                if(isSku) {
                    for(Product product : Product.<Product>findAll()) {
                        analyzeMap.put(product.sku, new AnalyzeDTO(product.sku));
                    }
                } else {
                    for(Selling selling : Selling.<Selling>findAll()) {
                        analyzeMap.put(selling.sellingId, new AnalyzeDTO(selling));
                    }
                }

                List<AnalyzeVO> vos = new ArrayList<AnalyzeVO>();
                // 通过 Job 异步 fork 加载不同时段的数据
                /**
                 * FIXME 这类型的代码在下面, 这些需要进行重构到一起
                 * 1. 此处
                 * 2. OrderItem.categoryPercent
                 * 3. OrderItem.skuOrMskuAccountRelateOrderItem
                 */
                List<F.Promise<List<AnalyzeVO>>> voPromises = new ArrayList<F.Promise<List<AnalyzeVO>>>();
                Logger.info("Start Fork to fetch Analyzes Sellings.");
                try {
                    for(final M m : MARKETS) {
                        voPromises.add(new Job<List<AnalyzeVO>>() {
                            @Override
                            public List<AnalyzeVO> doJobWithResult() throws Exception {
                                return new OrderItemQuery().analyzeVos(
                                        m.withTimeZone(Dates.morning(from)).toDate(),
                                        m.withTimeZone(Dates.night(to)).toDate(),
                                        m);
                            }
                        }.now());
                    }
                    for(F.Promise<List<AnalyzeVO>> voP : voPromises) {
                        vos.addAll(voP.get(1, TimeUnit.MINUTES));
                    }
                } catch(Exception e) {
                    throw new FastRuntimeException(
                            String.format("因为 %s 问题, 请然后重新尝试搜索.", Webs.E(e)));
                } finally {
                    Logger.info("End of Fork Fetch.");
                }

                // 销量 AnalyzeVO
                for(AnalyzeVO vo : vos) {
                    String key = isSku ? vo.sku : vo.sid;
                    AnalyzeDTO currentDto = analyzeMap.get(key);
                    if(currentDto == null) {
                        Logger.warn("AnalyzeVO: %s, DTO is not exist. Key[%s]", vo, key);
                        continue;
                    }

                    long differTime =
                            vo.market.withTimeZone(startOfDay).getMillis() - vo.date.getTime();
                    if(differTime <= TimeUnit.DAYS.toMillis(1) && differTime >= 0)
                        currentDto.day0 += vo.qty;
                    if(differTime <= TimeUnit.DAYS.toMillis(2) && differTime >= 0)
                        currentDto.day1 += vo.qty;
                    if(differTime <= TimeUnit.DAYS.toMillis(7) && differTime >= 0)
                        currentDto.day7 += vo.qty;
                    if(differTime <= TimeUnit.DAYS.toMillis(30) && differTime >= 0)
                        currentDto.day30 += vo.qty;
                }

                // ProcureUnit
                for(AnalyzeDTO dto : analyzeMap.values()) {
                    // 切换 ProcureUnit 的 sku/sid 的参数?
                    List<ProcureUnit> untis = ProcureUnit
                            .find((isSku ? "sku=?" : "sid=?") + " AND stage NOT IN (?,?)", dto.fid,
                                    ProcureUnit.STAGE.CLOSE, ProcureUnit.STAGE.SHIP_OVER).fetch();

                    // plan, working, worked, way
                    for(ProcureUnit unit : untis) {
                        if(unit.stage == ProcureUnit.STAGE.PLAN) dto.plan += unit.qty();
                        else if(unit.stage == ProcureUnit.STAGE.DELIVERY) dto.working += unit.qty();
                        else if(unit.stage == ProcureUnit.STAGE.DONE) dto.worked += unit.qty();
                        else if(unit.stage == ProcureUnit.STAGE.SHIPPING) dto.way += unit.qty();
                        else if(unit.stage == ProcureUnit.STAGE.INBOUND)
                            dto.inbound += unit.inboundingQty();
                    }

                    // ps cal
                    List<SellingQTY> qtys = SellingQTY
                            .find((isSku ? "product.sku=?" : "selling.sellingId=?"), dto.fid)
                            .fetch();
                    for(SellingQTY qty : qtys) dto.qty += qty.qty;

                    // review
                    F.T3<Integer, Float, List<String>> reviewT3;
                    if(isSku) reviewT3 = new AmazonListingReviewQuery().skuRelateReviews(dto.fid);
                    else reviewT3 = new AmazonListingReviewQuery().sidRelateReviews(dto.fid);
                    dto.reviews = reviewT3._1;
                    dto.rating = reviewT3._2;
                    if(dto.reviews > 0)
                        dto.reviewRatio =
                                dto.reviews / ((5 - dto.rating) == 0 ? 0.1f : (5 - dto.rating));
                    else dto.reviewRatio = 0;
                }

                Cache.set(cacke_key, new ArrayList<AnalyzeDTO>(analyzeMap.values()), "12h");
                Cache.set(cacke_key + ".time", new Date(), "12h");
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
        // 过滤 Category
        if(StringUtils.isNotBlank(this.categoryId))
            CollectionUtils.filter(dtos, new SearchPredicate(this.categoryId));
        if(StringUtils.isNotBlank(this.search))
            CollectionUtils.filter(dtos, new SearchPredicate(this.search));
        if(StringUtils.isNotBlank(this.orderBy))
            Collections.sort(dtos, new FieldComparator(this.orderBy, this.desc));
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
}
