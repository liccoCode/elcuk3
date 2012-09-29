package jobs;

import helper.Dates;
import models.Jobex;
import models.procure.Shipment;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import play.jobs.Every;
import play.jobs.Job;

import java.util.List;

/**
 * 每天进行检查一次,检查向后一周内的运输单
 * 每周的周二与周四创建各创建一份 "快递" 的运输单
 * User: wyattpan
 * Date: 9/27/12
 * Time: 1:02 PM
 */
@Every("4h")
public class ShipmentCycleJob extends Job {
    @Override
    public void doJob() {
        if(!Jobex.findByClassName(ShipmentCycleJob.class.getName()).isExcute()) return;
        DateTime now = DateTime.now(DateTimeZone.forID("Asia/Shanghai"));
        for(int i = 0; i < 30; i++) {
            DateTime dealDate = now.plusDays(i);
            List<Shipment> cyclingShipments = Shipment.find("cycle=? AND beginDate>=? AND beginDate<=?",
                    true, Dates.morning(dealDate.toDate()), Dates.night(dealDate.toDate())).fetch();
            if(cyclingShipments.size() > 0) continue;
            // 周一: 1, 周日: 7
            if(dealDate.getDayOfWeek() == 2 || dealDate.getDayOfWeek() == 4) {
                Shipment ship = new Shipment(Shipment.id());
                ship.cycle = true;
                ship.beginDate = dealDate.toDate();
                ship.planArrivDate = dealDate.plusDays(7).toDate();
                ship.save();
            }
        }
    }
}
