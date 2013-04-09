package jobs;

import helper.Dates;
import helper.Webs;
import models.Jobex;
import models.Notification;
import models.procure.Shipment;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.jobs.Job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 自动加载所有 Shipment , 并与 DHL, Fedex, UPS 进行更新
 * 周期:
 * - 轮询周期: 5mn
 * - Duration:  4h
 * User: wyattpan
 * Date: 8/15/12
 * Time: 6:04 PM
 */
public class ShipmentSyncJob extends Job {
    public static final String NOTIFY_TITLE = "海运提醒";

    @Override
    public void doJob() {
        if(!Jobex.findByClassName(ShipmentSyncJob.class.getName()).isExcute()) return;
        List<Shipment> clearance = Shipment.shipmentsByState(Shipment.S.CLEARANCE);
        List<Shipment> shipping = Shipment.shipmentsByState(Shipment.S.SHIPPING);

        List<Shipment> all = new ArrayList<Shipment>(clearance);
        all.addAll(shipping);

        Logger.info("Fetch [CLEARANCE: %s], [SHIPPING: %s]", clearance.size(), shipping.size());
        for(Shipment ship : all) {
            // 没有 trackNo 的, 直接略过
            if(StringUtils.isBlank(ship.trackNo)) continue;
            try {
                if(ship.type == Shipment.T.SEA) {
                    // 海运的给与提示信息, 暂时无法进行跟踪
                    /**
                     * 1. 寻找当天的 Notification, 如果没有则提醒, 有则跳过
                     * 2. 检查海运超过 30 天, 开始提醒, 每天一次.
                     */
                    if(Notification.count("title=? AND createAt<=?", ShipmentSyncJob.NOTIFY_TITLE,
                            Dates.night(new Date())) <= 0) {
                        Date beginDate = ship.beginDate;
                        // 做一个兼容...
                        if(beginDate == null) beginDate = ship.planBeginDate;
                        long differTime = System.currentTimeMillis() - beginDate.getTime();
                        if(differTime > TimeUnit.DAYS.toMillis(30)) {
                            Notification.notifies(ShipmentSyncJob.NOTIFY_TITLE,
                                    String.format("海运运输单 %s 已经运输 %s 天, 记得进行跟踪处理,添加状态 Comment.",
                                            ship.id, differTime / Dates.DAY_MILLIS),
                                    Notification.SHIPPER);
                        }
                    }
                } else {
                    ship.trackWebSite();
                    ship.monitor();
                }
            } catch(Exception e) {
                Logger.warn(Webs.E(e));
            }
        }
    }
}
