package jobs;

import helper.Webs;
import models.Jobex;
import models.procure.Shipment;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.jobs.Job;

import java.util.List;

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

    @Override
    public void doJob() {
        if(!Jobex.findByClassName(ShipmentSyncJob.class.getName()).isExcute()) return;
        List<Shipment> shipments = Shipment.findByTypeAndStates(Shipment.T.EXPRESS,
                Shipment.S.SHIPPING, Shipment.S.CLEARANCE, Shipment.S.PACKAGE,
                Shipment.S.BOOKED, Shipment.S.DELIVERYING);

        Logger.info("Fetch Shipments: %s", shipments.size());
        for(Shipment ship : shipments) {
            // 没有 trackNo 的, 直接略过
            if(StringUtils.isBlank(ship.trackNo)) continue;
            try {
                ship.trackWebSite();
                ship.monitor();
            } catch(Exception e) {
                Logger.warn(Webs.E(e));
            }

            // dev 只测试一个
            if(Play.mode.isDev()) break;
        }
    }
}
