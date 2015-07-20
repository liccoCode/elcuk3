package models.view.report;

import helper.*;
import helper.Currency;
import models.procure.FBACenter;
import models.procure.Shipment;
import models.qc.CheckTask;
import org.jsoup.helper.StringUtil;
import play.db.helper.SqlSelect;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by licco on 15/6/4.
 */
public class AreaGoodsAnalyze implements Serializable {

    public Date from;

    public Date to;

    public String countryCode;

    public String centerId;

    public float totalWeight;

    public float totalVolume;

    public float seaWeight;

    public float seaVolume;

    public float airWeight;

    public float airVolume;

    public float expressWeight;

    public float expressVolume;

    public float totalCost;

    public float seaCost;

    public float airCost;

    public float expressCost;

    public Currency currency;

    public int index;

    public int nocaluFeeNum;

    public int caluFeeNum;

    public List<AreaGoodsAnalyze> query() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Object> list = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder(
                "SELECT fc.countryCode, fc.centerId, IFNULL(sum(pu.fixValue+pu.amount), 0) as totalCost, ")
                .append(" IFNULL(SUM(case s.type when 'SEA' then pu.fixValue+pu.amount else 0 end), 0) as seaCost, ")
                .append(" IFNULL(SUM(case s.type when 'AIR' then pu.fixValue+pu.amount else 0 end), 0) as airCost, ")
                .append(" IFNULL(SUM(case s.type when 'EXPRESS' then pu.fixValue+pu.amount else 0 end), 0) as expressCost, ")
                .append(" pu.currency")
                .append(" FROM Shipment s LEFT JOIN ShipItem si on si.shipment_id = s.id ")
                .append(" AND s.beginDate >= ? AND s.beginDate <= ? ")
                .append(" LEFT JOIN ProcureUnit p on si.unit_id = p.id ")
                .append(" LEFT JOIN PaymentUnit pu ON pu.shipment_id = s.id ")
                .append(" LEFT JOIN CheckTask c on c.units_id = p.id ")
                .append(" LEFT JOIN FBAShipment fs on p.fba_id = fs.id ")
                .append(" LEFT JOIN FBACenter fc on fs.fbaCenter_id = fc.id ")
                .append(" WHERE fc.countryCode IS NOT NULL AND pu.currency IS NOT NULL ");
        list.add(Dates.morning(from));
        list.add(Dates.night(to));
        if(!StringUtil.isBlank(this.countryCode)) {
            sql.append(" AND fc.countryCode = ? ");
            list.add(this.countryCode);
        }
        if(!StringUtil.isBlank(this.centerId)) {
            sql.append(" AND fc.centerId = ? ");
            list.add(this.centerId);
        }

        sql.append(" GROUP BY fc.countryCode, fc.centerId, pu.currency");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), list.toArray());
        List<AreaGoodsAnalyze> analyzes = new ArrayList<AreaGoodsAnalyze>();
        Map<String, AreaGoodsAnalyze> map = new HashMap<String, AreaGoodsAnalyze>();
        int sort = 0;
        for(Map<String, Object> row : rows) {
            AreaGoodsAnalyze analyze = new AreaGoodsAnalyze();
            analyze.countryCode = row.get("countryCode").toString();
            analyze.centerId = row.get("centerId").toString();
            Currency curr = Currency.valueOf(row.get("currency").toString());
            analyze.totalCost = curr.toUSD(Float.parseFloat(row.get("totalCost").toString()));
            analyze.seaCost = curr.toUSD(Float.parseFloat(row.get("seaCost").toString()));
            analyze.airCost = curr.toUSD(Float.parseFloat(row.get("airCost").toString()));
            analyze.expressCost = curr.toUSD(Float.parseFloat(row.get("expressCost").toString()));

            String key = analyze.countryCode + "_" + analyze.centerId;
            if(map.containsKey(key)) {
                analyze.index = sort;
                AreaGoodsAnalyze exist = map.get(key);
                exist.totalCost += analyze.totalCost;
                exist.airCost += analyze.airCost;
                exist.seaCost += analyze.seaCost;
                exist.expressCost += analyze.expressCost;
            } else {
                map.put(analyze.countryCode + "_" + analyze.centerId, analyze);
                analyze.index = sort++;
            }
        }
        Collection<AreaGoodsAnalyze> a = map.values();
        Iterator<AreaGoodsAnalyze> it = a.iterator();
        while(it.hasNext()) {
            analyzes.add(it.next());
        }
        Collections.sort(analyzes, new Comparator<AreaGoodsAnalyze>() {
            @Override
            public int compare(AreaGoodsAnalyze e1, AreaGoodsAnalyze e2) {
                return e1.index - e2.index;
            }
        });

        sql = new StringBuilder("SELECT fc.countryCode, fc.centerId,  c.standBoxQctInfo, c.tailBoxQctInfo, s.type ")
                .append(" FROM Shipment s LEFT JOIN ShipItem si on si.shipment_id = s.id LEFT JOIN ProcureUnit p ")
                .append(" ON si.unit_id = p.id LEFT JOIN CheckTask c on c.units_id = p.id ")
                .append(" LEFT JOIN FBAShipment fs on p.fba_id = fs.id ")
                .append(" LEFT JOIN FBACenter fc on fs.fbaCenter_id = fc.id ")
                .append(" WHERE c.standBoxQctInfo IS NOT NULL AND fc.countryCode IS NOT NULL AND fc.centerId IS NOT NULL");
        List<Map<String, Object>> checkRows = DBUtils.rows(sql.toString());
        for(Map<String, Object> row : checkRows) {
            CheckTask task = new CheckTask();
            task.standBoxQctInfo = row.get("standBoxQctInfo").toString();
            task.tailBoxQctInfo = row.get("tailBoxQctInfo").toString();
            task.arryParamSetUP(CheckTask.FLAG.STR_TO_ARRAY);
            String countryCodeAndCenterId = row.get("countryCode").toString() + "_" + row.get("centerId").toString();
            AreaGoodsAnalyze analyze = map.get(countryCodeAndCenterId);
            if(analyze != null) {
                Float totalV = Float.parseFloat(task.totalVolume().toString());
                Float totalW = Float.parseFloat(task.totalWeight().toString());
                if(row.get("type").toString().equals("SEA")) {
                    analyze.seaVolume += totalV;
                    analyze.seaWeight += totalW;
                }
                if(row.get("type").toString().equals("AIR")) {
                    analyze.airVolume += totalV;
                    analyze.airWeight += totalW;
                }
                if(row.get("type").toString().equals("EXPRESS")) {
                    analyze.expressVolume += totalV;
                    analyze.expressWeight += totalW;
                }
                analyze.totalVolume += totalV;
                analyze.totalWeight += totalW;
            }
        }
        DecimalFormat df = new DecimalFormat("##0.00");
        for(AreaGoodsAnalyze area : analyzes) {
            area.totalWeight = Float.parseFloat(df.format(area.totalWeight));
            area.totalVolume = Float.parseFloat(df.format(area.totalVolume));
            area.seaWeight = Float.parseFloat(df.format(area.seaWeight));
            area.seaVolume = Float.parseFloat(df.format(area.seaVolume));
            area.airWeight = Float.parseFloat(df.format(area.airWeight));
            area.airVolume = Float.parseFloat(df.format(area.airVolume));
            area.expressWeight = Float.parseFloat(df.format(area.expressWeight));
            area.expressVolume = Float.parseFloat(df.format(area.expressVolume));
            area.totalCost = Float.parseFloat(df.format(area.totalCost));
            area.seaCost = Float.parseFloat(df.format(area.seaCost));
            area.airCost = Float.parseFloat(df.format(area.airCost));
            area.expressCost = Float.parseFloat(df.format(area.expressCost));
        }
        return analyzes;
    }

    public List<String> queryCountryCode() {
        SqlSelect sql = new SqlSelect().select("countryCode").from("FBACenter").groupBy("countryCode");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString());
        List<String> countryCode = new ArrayList<String>();
        for(Map<String, Object> row : rows) {
            countryCode.add(row.get("countryCode").toString());
        }
        return countryCode;
    }

    public List<String> queryCenterIdByCountryCode(String countryCode) {
        if(StringUtil.isBlank(countryCode)) {
            return null;
        }
        SqlSelect sql = new SqlSelect().select("centerId").from("FBACenter").where("countryCode=?").groupBy("centerId");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), countryCode);
        List<String> centerId = new ArrayList<String>();
        for(Map<String, Object> row : rows) {
            centerId.add(row.get("centerId").toString());
        }
        return centerId;
    }

    public void queryTotalShipmentAnalyze() {
        this.nocaluFeeNum = Shipment.find("SELECT s.id FROM Shipment s LEFT JOIN s.fees f WHERE s.dates.beginDate >= ?" +
                " AND s.dates.beginDate <= ? GROUP BY s.id HAVING COUNT(f.id)=0 ", this.from, this.to).fetch().size();
        this.caluFeeNum = Shipment
                .find(" From Shipment s WHERE s.dates.beginDate >= ? AND s.dates.beginDate <= ? ", this.from, this.to)
                .fetch().size();
    }

}
