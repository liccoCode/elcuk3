package jobs;

import helper.Webs;
import models.procure.Shipment;
import play.Logger;
import play.jobs.Job;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动加载所有 Shipment , 并与 DHL, Fedex 进行更新
 * User: wyattpan
 * Date: 8/15/12
 * Time: 6:04 PM
 */
public class ShipmentSyncJob extends Job {

    @Override
    public void doJob() {
        List<Shipment> clearance = Shipment.shipmentsByState(Shipment.S.CLEARANCE);
        List<Shipment> shipping = (Shipment.shipmentsByState(Shipment.S.SHIPPING));

        List<Shipment> all = new ArrayList<Shipment>(clearance);
        all.addAll(shipping);

        Logger.info("Fetch [CLEARANCE: %s], [SHIPPING: %s]", clearance.size(), shipping.size());
        for(Shipment ship : all) {
            try {
                ship.refreshIExpressHTML();
            } catch(Exception e) {
                Logger.warn(Webs.E(e));
            }
        }
    }
}
