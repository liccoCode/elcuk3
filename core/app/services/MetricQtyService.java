package services;

import com.alibaba.fastjson.JSON;
import helper.Caches;
import jobs.analyze.SellingSaleAnalyzeJob;
import models.market.M;
import models.market.Selling;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.view.dto.AnalyzeDTO;
import models.view.report.Profit;
import play.cache.Cache;

import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 14-6-13
 * Time: 下午3:20
 */
public class MetricQtyService {


    public M market;
    public String sku;


    public MetricQtyService(M market,
                            String sku) {

        this.market = market;
        this.sku = sku;
    }


    /**
     * 计算利润对象
     */
    public Profit calProfit(Profit profit) {
        Selling sell = Selling.find("listing.product.sku=? and market=?", this.sku, this.market).first();
        if(sell != null) {
            String AnalyzeDTO_SID_MAP_CACHE = "analyze_post_sid_map";
            String cacke_key = SellingSaleAnalyzeJob.AnalyzeDTO_SID_CACHE;
            // 这个地方有缓存, 但还是需要一个全局锁, 控制并发, 如果需要写缓存则锁住
            List<AnalyzeDTO> dtos = JSON.parseArray(Caches.get(cacke_key), AnalyzeDTO.class);
            if(dtos != null) {
                java.util.Map<String, AnalyzeDTO> dtomap = Cache.get(AnalyzeDTO_SID_MAP_CACHE, java.util.Map.class);
                if(dtomap == null) {
                    dtomap = new HashMap<String, AnalyzeDTO>();
                    if(dtos != null) {
                        for(AnalyzeDTO dto : dtos) {
                            dtomap.put(dto.fid, dto);
                        }
                    }
                    Cache.add(AnalyzeDTO_SID_MAP_CACHE, dtomap, "4h");
                }
                AnalyzeDTO dto = dtomap.get(sell.sellingId);
                if(dto != null) {
                    profit.workingqty = dto.plan + dto.working + dto.worked;
                    profit.wayqty = dto.way;
                    profit.inboundqty = dto.inbound + dto.qty;
                }
            }
        }
        return profit;
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


}
