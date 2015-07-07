package models.view.post;

import helper.DBUtils;
import helper.Dates;
import models.procure.Shipment;
import models.view.report.ArrivalRate;
import org.joda.time.DateTime;
import play.libs.F;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by licco on 15/6/11.
 */
public class ArrivalRatePost extends Post<ArrivalRate> {

    public ArrivalRatePost() {
        this.from = Dates.morning(Dates.monthBegin(DateTime.now().minusMonths(1).toDate()));
        this.to = Dates.night(Dates.monthEnd(DateTime.now().minusMonths(1).toDate()));
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t1.type,t1.totalShipNum, IFNULL(t2.onTimeShipNum, 0) AS onTimeShipNum, ")
                .append("IFNULL(t3.overTimeShipNum, 0) AS overTimeShipNum, IFNULL(t4.earlyTimeShipNum, 0) AS earlyTimeShipNum FROM ")
                .append("(SELECT s.type, count(1) AS 'totalShipNum' FROM Shipment s WHERE s.state = 'DONE' ")
                .append(" AND s.begindate >= ? AND s.begindate <= ? GROUP BY s.type ) t1 LEFT JOIN ")
                .append("(SELECT COUNT(1) AS 'onTimeShipNum', m.type FROM Shipment m WHERE m.state = 'DONE' ")
                .append(" AND m.begindate >= ? AND m.begindate <= ? AND DATE_FORMAT(m.inbondDate, '%m-%d-%Y') <= DATE_FORMAT(m.planArrivDate, '%m-%d-%Y') ")
                .append(" GROUP BY m.type) t2 ON t2.type = t1.type LEFT JOIN ")
                .append("(SELECT COUNT(1) AS 'overTimeShipNum', m.type FROM Shipment m WHERE m.state = 'DONE' ")
                .append(" AND m.begindate >= ? AND m.begindate <= ? AND DATE_FORMAT(m.inbondDate, '%m-%d-%Y') > DATE_FORMAT(m.planArrivDate, '%m-%d-%Y') ")
                .append(" GROUP BY m.type) t3 ON t3.type = t1.type LEFT JOIN ")
                .append("(SELECT COUNT(1) AS 'earlyTimeShipNum', m.type FROM Shipment m WHERE m.state = 'DONE' ")
                .append(" AND m.begindate >= ? AND m.begindate <= ? AND DATE_FORMAT(m.inbondDate, '%m-%d-%Y') < DATE_FORMAT(m.planArrivDate, '%m-%d-%Y') GROUP BY m.type) t4 ON t4.type = t1.type ");
        List<Object> param = new ArrayList<Object>();
        for(int i = 0; i < 4; i++) {
            param.add(this.from);
            param.add(this.to);
        }
        return new F.T2(sql.toString(), param);
    }

    public List<ArrivalRate> query() {
        F.T2<String, List<Object>> params = params();
        List<Map<String, Object>> rows = DBUtils.rows(params._1, params._2.toArray());
        List<ArrivalRate> list = new ArrayList<ArrivalRate>();
        DecimalFormat df = new DecimalFormat("##0.00");
        ArrivalRate average = new ArrivalRate();   //平均
        average.shipType = "总计";
        Map<String, Object> map = new HashMap<String, Object>();
        for(Map<String, Object> row : rows) {
            ArrivalRate rate = new ArrivalRate();
            rate.shipType = row.get("type").toString();
            rate.totalShipNum = Long.parseLong(row.get("totalShipNum").toString());
            average.totalShipNum += rate.totalShipNum;
            rate.onTimeShipNum = Long.parseLong(row.get("onTimeShipNum").toString());
            average.onTimeShipNum += rate.onTimeShipNum;
            rate.overTimeShipNum = Long.parseLong(row.get("overTimeShipNum").toString());
            average.overTimeShipNum += rate.overTimeShipNum;
            rate.earlyTimeShipNum = Long.parseLong(row.get("earlyTimeShipNum").toString());
            average.earlyTimeShipNum += rate.earlyTimeShipNum;
            rate.onTimeRate = showTwoFloat(rate.onTimeShipNum * 100f / isZero(rate.totalShipNum));
            rate.overTimeRate = showTwoFloat(rate.overTimeShipNum * 100f / isZero(rate.totalShipNum));
            rate.earlyTimeRate = showTwoFloat(rate.earlyTimeShipNum * 100f / isZero(rate.totalShipNum));
            map.put(rate.shipType, rate);
            list.add(rate);
        }
        for(int i = 0; i < Shipment.T.values().length; i++) {
            String type = Shipment.T.values()[i].name();
            if(!map.containsKey(type)) {
                ArrivalRate rate = new ArrivalRate();
                rate.shipType = Shipment.T.values()[i].name();
                list.add(rate);
            }
        }

        average.onTimeRate = showTwoFloat(average.onTimeShipNum * 100f / isZero(average.totalShipNum));
        average.overTimeRate = showTwoFloat(average.overTimeShipNum * 100.0f / isZero(average.totalShipNum));
        average.earlyTimeRate = showTwoFloat(average.earlyTimeShipNum * 100.0f / isZero(average.totalShipNum));
        list.add(average);
        return list;
    }

    public float showTwoFloat(float num) {
        BigDecimal n = new BigDecimal(num);
        n = n.setScale(2, BigDecimal.ROUND_HALF_UP);
        return n.floatValue();
    }

    public List<Shipment> queryOverTimeShipment() {
           return Shipment.find("FROM Shipment s WHERE DATE_FORMAT(s.dates.inbondDate, '%m-%d-%Y') > DATE_FORMAT" +
                           "(s.dates.planArrivDate, '%m-%d-%Y')" +
                           "AND s.dates.beginDate >= ? AND s.dates.beginDate <= ? ",
                   this.from, this.to).fetch();
       }

    public Long isZero(Long num) {
        return num == 0 ? 1 : num;
    }

}
