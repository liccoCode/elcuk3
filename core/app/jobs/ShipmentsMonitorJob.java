package jobs;

import helper.LogUtils;
import models.ElcukConfig;
import models.procure.Shipment;
import notifiers.FBAMails;
import play.jobs.Job;

import java.util.List;

/**
 * 每天运行一次, 用来检查系数中的 Shipment 进行问题检测.
 * 周期:
 * - 轮询周期: 1h
 * - Duration: 24h
 * User: wyatt
 * Date: 5/16/13
 * Time: 10:18 AM
 * @deprecated
 */
public class ShipmentsMonitorJob extends Job {

    @Override
    public void doJob() throws Exception {
        long begin = System.currentTimeMillis();
        /**
         * 此任务每天只执行一次, 对检查符合的 Shipment 只进行一次判断, 按时时间的消耗速度,
         * 运输单总会抵达需要检测的那一天(如何满足要求)
         *
         */

        // 在路上漂泊的时间
        dayTypeCheck("atport", 3);

        // 入库的时间
        dayTypeCheck("clearance", 3);
        if(LogUtils.isslow(System.currentTimeMillis() - begin,"ShipmentsMonitorJob")) {
            LogUtils.JOBLOG
                    .info(String.format("ShipmentsMonitorJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }

    private void dayTypeCheck(String dayType, int beforeDays) throws InterruptedException {
        for(String market : ElcukConfig.MARKETS.keySet()) {
            for(String shipType : ElcukConfig.SHIP_TYPES.keySet()) {
                if(shipType.equalsIgnoreCase(Shipment.T.EXPRESS.name())) continue;

                String checkDate = "";
                Shipment.S state = null;
                if("clearance".equals(dayType)) {
                    checkDate = "dates.atPortDate";
                    state = Shipment.S.CLEARANCE;
                } else if("atport".equals(dayType)) {
                    checkDate = "dates.beginDate";
                    state = Shipment.S.SHIPPING;
                }

                ElcukConfig config = ElcukConfig.findByName(
                        String.format("%s_%s_%s", market, shipType, dayType));

                Shipment.T type = Shipment.T.valueOf(shipType.toUpperCase());
                List<Shipment> shipments = Shipment.find("whouse.country=? AND type=?"
                                + " AND state=? AND DATEDIFF(now(), " + checkDate + ")=?",
                        // (now - beginDate) = 平均时长 - 3(提前3天提醒)
                        market, type, state, (config.toInteger() - beforeDays)).fetch();

                if(shipments != null && shipments.size() > 0) {
                    FBAMails.shipmentsNotify(shipments, state, config);
                    Thread.sleep(500);
                }
            }
        }
    }
}
