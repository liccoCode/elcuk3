package models.view.post;

import helper.Dates;
import jobs.promise.AnalyzePostForkPromise;
import models.market.M;
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
import play.utils.FastRuntimeException;
import query.AmazonListingReviewQuery;
import query.vo.AnalyzeVO;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

                DateTime nowWithMorning = new DateTime(Dates.morning(this.to));

                // 准备计算用的数据容器
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

                /**
                 * !!! 系统内使用的是 UTC 时间, 市场他们想查看数据的时候以 1.14 当天的订单为例,
                 * 他们想查看的是 uk/de/us 等等国家 1.14 的订单, 那么就需要将 1.14 转换为各个
                 * 市场自己的 UTC 时间, 加载出这些数据最后再汇总.
                 */
                List<AnalyzeVO> vos = new ArrayList<AnalyzeVO>();
                try {
                    vos.addAll(marketsAnalyzeVOs(nowWithMorning.minusDays(30),
                            nowWithMorning.plusDays(1),
                            M.AMAZON_DE, M.AMAZON_US, M.AMAZON_UK, M.AMAZON_FR));
                } catch(Exception e) {
                    throw new FastRuntimeException(
                            String.format("发生错误 %s, 请稍等片刻后重试", e.getMessage()));
                }


                // sku, sid, qty, date, acc.id

                // 销量
                for(AnalyzeVO vo : vos) {
                    String key = isSku ? vo.sku : vo.sid;
                    AnalyzeDTO currentDto = analyzeMap.get(key);
                    if(currentDto == null) {
                        Logger.warn("AnalyzeVO: %s, DTO is not exist. Key[%s]", vo, key);
                        continue;
                    }

                    long differTime = nowWithMorning.toDate().getTime() - vo.date.getTime();
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
                    // 切换 ProcureUnit 的 sku/sid 的参数
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
                    if(isSku) reviewT3 = AmazonListingReviewQuery.skuRelateReviews(dto.fid);
                    else reviewT3 = AmazonListingReviewQuery.sidRelateReviews(dto.fid);
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

    /**
     * 执行市场, 加载执行不同市场不同时间的日期
     *
     * @param from
     * @param to
     * @param markets
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    private List<AnalyzeVO> marketsAnalyzeVOs(DateTime from, DateTime to, M... markets)
            throws InterruptedException, ExecutionException, TimeoutException {
        List<F.Promise<List<AnalyzeVO>>> vos = new ArrayList<F.Promise<List<AnalyzeVO>>>();

        for(M m : markets) {
            vos.add(new AnalyzePostForkPromise(from, to, m).now());
        }
        // 结果汇总
        List<AnalyzeVO> marketsVos = new ArrayList<AnalyzeVO>();
        for(F.Promise<List<AnalyzeVO>> p : vos) {
            marketsVos.addAll(p.get(5, TimeUnit.SECONDS));
        }
        return marketsVos;
    }
}
