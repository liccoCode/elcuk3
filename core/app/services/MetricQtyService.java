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
import play.Logger;

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
        List<Selling> sells = Selling.find("product.sku=? and market=?", this.sku, this.market).fetch();
        String cacheKey = SellingSaleAnalyzeJob.AnalyzeDTO_SID_CACHE;
        // 这个地方有缓存, 但还是需要一个全局锁, 控制并发, 如果需要写缓存则锁住
        List<AnalyzeDTO> dtos = JSON.parseArray(Caches.get(cacheKey), AnalyzeDTO.class);
        if(dtos != null) {
            long start = System.currentTimeMillis();
            for(Selling sell : sells) {
                for(AnalyzeDTO dto : dtos) {
                    if(dto.fid.trim().equals(sell.sellingId.trim())) {
                        profit.workingqty = dto.plan + dto.working + dto.worked;
                        profit.wayqty = dto.way;
                        profit.inboundqty = dto.inbound + dto.qty;
                        if(profit.workingqty != 0 || profit.wayqty != 0 || profit.inboundqty != 0) {
                            return profit;
                        }
                    }
                }
            }
            Logger.info("calProfit 耗时: " + (System.currentTimeMillis() - start) + " ms");
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
                case BOOKED:
                case DELIVERYING:
                case RECEIPTD:
                    way += si.qty;
                default:
                    way += 0;
            }
        }
        return way;
    }
}
