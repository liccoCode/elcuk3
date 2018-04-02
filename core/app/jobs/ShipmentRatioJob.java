package jobs;

import jobs.driver.BaseJob;
import models.procure.Shipment;
import play.Logger;
import play.jobs.Every;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/7/22
 * Time: 下午4:27
 */
@Every("5mn")
public class ShipmentRatioJob extends BaseJob {

    public void doit() {
        List<Shipment> shipments = Shipment.find("SELECT DISTINCT s FROM Shipment s LEFT JOIN s.items i "
                        + " LEFT JOIN i.unit u LEFT JOIN u.product p "
                        + " WHERE i.weightRatio IS NULL AND s.state NOT IN (? , ? , ?) AND p.weight > 0 AND i.qty > 0 ",
                Shipment.S.PLAN, Shipment.S.CONFIRM, Shipment.S.CANCEL).fetch(50);
        shipments.forEach(shipment -> {
            shipment.calculationRatio();
            Logger.info(shipment.id + "完成计算占比！");
        });
    }
}
