package models.view.post;

import helper.DBUtils;
import helper.Dates;
import models.market.M;
import models.procure.Shipment;
import models.view.report.ArrivalRate;
import org.joda.time.DateTime;
import play.libs.F;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 15/6/11
 * Time: 下午2:43
 */
public class ArrivalRatePost extends Post<ArrivalRate> {

    private static final long serialVersionUID = 1993384893755767775L;

    public ArrivalRatePost() {
        this.from = Dates.morning(Dates.monthBegin(DateTime.now().minusMonths(1).toDate()));
        this.to = Dates.night(Dates.monthEnd(DateTime.now().minusMonths(1).toDate()));
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT w.market, m.type, count(1) AS totalShipNum, ");
        sql.append(" (SELECT COUNT(1)  FROM Shipment m1 WHERE m1.state IN ");
        sql.append(" ('RECEIPTD','RECEIVING','DONE')  AND m1.receiptDate >= ? AND m1.receiptDate <= ? ");
        sql.append(" AND DATE_FORMAT(m1.receiptDate,'%Y-%m-%d')<=DATE_FORMAT(m1.planArrivDateForCountRate,'%Y-%m-%d')");
        sql.append(" AND m1.whouse_id = w.id AND m1.type = m.type) AS onTimeShipNum,");
        sql.append(" (SELECT COUNT(1) FROM Shipment m2 WHERE m2.state IN ");
        sql.append(" ('RECEIPTD','RECEIVING','DONE')  AND m2.receiptDate >= ? AND m2.receiptDate <= ? ");
        sql.append(" AND DATE_FORMAT(m2.receiptDate,'%Y-%m-%d')>DATE_FORMAT(m2.planArrivDateForCountRate,'%Y-%m-%d')");
        sql.append(" AND m2.whouse_id = w.id AND m2.type = m.type) AS overTimeShipNum,");
        sql.append(" (SELECT COUNT(1) FROM Shipment m3 WHERE m3.state IN ");
        sql.append(" ('RECEIPTD','RECEIVING','DONE') AND m3.receiptDate >= ? AND m3.receiptDate <=? ");
        sql.append(" AND DATE_FORMAT(m3.receiptDate,'%Y-%m-%d')<DATE_FORMAT(m3.planArrivDateForCountRate,'%Y-%m-%d')");
        sql.append(" AND m3.whouse_id = w.id AND m3.type = m.type) AS 'earlyTimeShipNum'");
        sql.append(" FROM Shipment m LEFT JOIN Whouse w ON m.whouse_id = w.id ");
        sql.append(" WHERE m.state IN ('RECEIPTD','RECEIVING','DONE') AND m.receiptDate >=? AND m.receiptDate <=? ");
        sql.append(" GROUP BY w.market, m.type ");
        List<Object> param = new ArrayList<>();
        for(int i = 0; i < 4; i++) {
            param.add(Dates.morning(this.from));
            param.add(Dates.night(this.to));
        }
        return new F.T2(sql.toString(), param);
    }

    public List<ArrivalRate> query() {
        F.T2<String, List<Object>> params = params();
        List<Map<String, Object>> rows = DBUtils.rows(params._1, params._2.toArray());
        List<ArrivalRate> list = new ArrayList<>();
        ArrivalRate average = new ArrivalRate();   //平均
        average.shipType = "总计";
        Map<String, Object> map = new HashMap<>();
        for(Map<String, Object> row : rows) {
            ArrivalRate rate = new ArrivalRate();
            rate.market = M.valueOf(row.get("market").toString());
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
        return Shipment.find("FROM Shipment s WHERE DATE_FORMAT(s.dates.receiptDate, '%Y-%m-%d') > DATE_FORMAT" +
                        "(s.dates.planArrivDateForCountRate, '%Y-%m-%d') AND s.dates.receiptDate >= ? AND s.dates.receiptDate <= ?" +
                        " AND s.state IN (?,?,?) ", Dates.morning(this.from), Dates.night(this.to), Shipment.S.RECEIPTD,
                Shipment.S.RECEIVING, Shipment.S.DONE).fetch();
    }

    public List<Shipment> queryMonthlyShipment() {
        return Shipment.find("FROM Shipment s WHERE s.dates.receiptDate >= ? AND s.dates.receiptDate <= ?" +
                        " AND s.state IN (?,?,?) ",
                Dates.morning(this.from),
                Dates.night(this.to),
                Shipment.S.RECEIPTD,
                Shipment.S.RECEIVING,
                Shipment.S.DONE).fetch();
    }

    public Long isZero(Long num) {
        return num == 0 ? 1 : num;
    }

    public Map<String, F.T3<String, String, Double>> calAverageTime(List<Shipment> list) {
        Map<String, List<Float>> map = new HashMap<>();
        for(Shipment shipment : list) {
            String key = String.format("%s:%s", shipment.type.name(), shipment.whouse.market.name());
            if(map.containsKey(key)) {
                List<Float> times = map.get(key);
                times.add(shipment.calPrescription());
            } else {
                List<Float> floats = new ArrayList<>();
                floats.add(shipment.calPrescription());
                map.put(key, floats);
            }
        }
        Map<String, F.T3<String, String, Double>> time = new HashMap<>();
        map.keySet().forEach(key -> {
            List<Float> temp = map.get(key);
            double total = temp.stream().mapToDouble(num -> num).sum();
            time.put(key, new F.T3(key.split(":")[0], key.split(":")[1], total / map.get(key).size()));
        });
        return time;
    }

}