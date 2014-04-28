package jobs.analyze;

import helper.Dates;
import helper.Promises;
import models.market.M;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.view.dto.AnalyzeDTO;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.cache.Cache;
import play.jobs.Job;
import play.jobs.On;
import play.libs.F;
import query.*;

import java.util.*;

/**
 * 周期:
 * 轮询: 0:20, 7:20, 15:20 三个时间点执行三次
 * 为系统后台任务
 * <p/>
 * User: wyatt
 * Date: 8/13/13
 * Time: 3:06 PM
 */
@On("0 20 0,7,15 * * ?")
public class SellingSaleAnalyzeJob extends Job {
    public static final String RUNNING = "analyze_running";
    public static final String AnalyzeDTO_SID_CACHE = "analyze_post_sid";
    public static final String AnalyzeDTO_SKU_CACHE = "analyze_post_sku";

    @Override
    public void doJob() {
        if(isRnning()) return;
        /**
         * 1. 北京时间最近 1 周
         * 2. 两种类型 sid/sku
         */
        // 清理掉原来的
        Cache.delete(AnalyzeDTO_SID_CACHE);
        Cache.delete(AnalyzeDTO_SKU_CACHE);

        long begin = System.currentTimeMillis();
        analyzes("sid");
        Logger.info("SellingSaleAnalyzeJob calculate Sellings.... [%sms]", System.currentTimeMillis() - begin);

        begin = System.currentTimeMillis();
        analyzes("sku");
        Logger.info("SellingSaleAnalyzeJob calculate SKU.... [%sms]", System.currentTimeMillis() - begin);
    }

    /**
     * 确保同一时间只有一个 analyzes 正在计算
     *
     * @return
     */
    public static boolean isRnning() {
        return StringUtils.isNotBlank(Cache.get(RUNNING, String.class));
    }

    /**
     * 根据 type(sku/msku(sid)) 进行数据的分析计算
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<AnalyzeDTO> analyzes(String type) {
        if(isRnning()) return null;

        String cacke_key = "sid".equals(type) ? AnalyzeDTO_SID_CACHE : AnalyzeDTO_SKU_CACHE;
        // 这个地方有缓存, 但还是需要一个全局锁, 控制并发, 如果需要写缓存则锁住
        List<AnalyzeDTO> dtos = Cache.get(cacke_key, List.class);
        if(dtos != null) return dtos;

        synchronized(AnalyzeDTO.class) {
            try {
                Cache.add(RUNNING, RUNNING);
                dtos = Cache.get(cacke_key, List.class);
                if(dtos != null) return dtos;

                dtos = new ArrayList<AnalyzeDTO>();
                boolean isSku = StringUtils.equalsIgnoreCase("sku", type);

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
                        else if(unit.stage == ProcureUnit.STAGE.SHIPPING) dto.way += countWay(unit);
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
            } finally {
                Cache.delete(RUNNING);
            }
        }

        return dtos;
    }

    /**
     * 统计在途的数量
     * 公式为：(运输中 + 清关中 + 提货中 + 已预约 + 派送中 + 已签收)
     */
    public int countWay(ProcureUnit unit) {
        int way = 0;
        //采购计划的 shipItems
        for(ShipItem si : unit.shipItems) {
            switch(si.shipment.state) {
                case SHIPPING:
                case CLEARANCE:
                case PACKAGE:
                case BOOKED:
                case DELIVERYING:
                case RECEIPTD:
                    way += si.qty;
            }
        }
        return way;
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

    /**
     * 获取下一次缓存刷新的时间
     *
     * @param type
     * @return
     */
    public static Date cachedDate(String type) {
        String cacke_key = "sid".equals(type) ? AnalyzeDTO_SID_CACHE : AnalyzeDTO_SKU_CACHE;
        return Cache.get(cacke_key + ".time", Date.class);
    }
}
