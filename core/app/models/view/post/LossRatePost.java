package models.view.post;

import helper.Currency;
import helper.DBUtils;
import helper.Dates;
import models.data.AverageData;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.view.report.LossRate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.libs.F;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 12/4/14
 * Time: 4:32 PM
 */
public class LossRatePost extends Post<LossRate> {

    private static final long serialVersionUID = -4400684666002811569L;
    public String compenType;
    public Date from;
    public Date to;

    public LossRatePost() {
        this.from = Dates.morning(Dates.monthBegin(DateTime.now().minusMonths(1).toDate()));
        this.to = Dates.night(Dates.monthEnd(DateTime.now().minusMonths(1).toDate()));
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sql = new StringBuilder("");
        List<Object> params = new ArrayList<>();
        sql.append("select f.shipmentid,p.sku,(s.qty-ifnull(p.purchaseSample,0)) as qty, s.lossqty,"
                + " s.compenusdamt, p.currency, p.price, l.market, s.compentype, p.id as unitId, l.sellingId"
                + " From ShipItem s "
                + " LEFT JOIN ProcureUnit p ON s.unit_id=p.id "
                + " LEFT JOIN Selling l ON l.sellingId = p.sid "
                + " LEFT JOIN Shipment m ON s.shipment_id=m.id "
                + " LEFT JOIN FBAShipment f ON p.fba_id=f.id "
                + " WHERE m.arriveDate >= ? AND m.arriveDate <= ? "
                + " AND s.lossqty!=0 and s.compenamt != 0 "
                + " GROUP BY p.fba_id,p.sku ORDER BY l.sellingId desc ");
        if(StringUtils.isNotBlank(this.compenType)) {
            sql.append(" AND s.compenType= '").append(this.compenType).append("' ");
        }
        return new F.T2<>(sql.toString(), params);
    }

    public Map queryDate() {
        F.T2<String, List<Object>> params = params();
        F.T2<String, List<Object>> shipParams = shipParams();
        Map<String, Object> map = lossRateMap(params, shipParams);
        List<LossRate> lossRates = (List<LossRate>) map.get("lossRateList");
        this.count = lossRates.size();
        return map;
    }

    public F.T2<String, List<Object>> shipParams() {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT s FROM ShipItem s LEFT JOIN s.shipment m "
                + " WHERE m.state IN ('RECEIVING', 'DONE') "
                + " AND m.dates.inbondDate >= ? "
                + " AND s.qty <> s.adjustQty "
                + " ORDER BY s.unit.sid DESC ";
        return new F.T2<>(sql, params);
    }

    public F.T2<String, List<Object>> totalParams() {
        StringBuilder sql = new StringBuilder("");
        List<Object> params = new ArrayList<>();
        sql.append("select sum(s.qty-ifnull(p.purchaseSample,0)) shipqty,"
                + " sum(s.lossqty) totalqty,sum(round(s.compenusdamt,3)) totalamt,p.currency, p.price From ShipItem s "
                + " left join ProcureUnit p on s.unit_id=p.id "
                + " left join Shipment m on s.shipment_id=m.id "
                + " left join FBAShipment f on p.fba_id=f.id "
                + " where m.arriveDate >= ? AND m.arriveDate <= ? "
                + " and s.lossqty!=0 ");
        if(StringUtils.isNotBlank(this.compenType)) {
            sql.append(" AND s.compenType= '").append(this.compenType).append("' ");
        }
        return new F.T2<>(sql.toString(), params);
    }

    public LossRate querytotal() {
        F.T2<String, List<Object>> params = totalParams();
        List<Map<String, Object>> rows = DBUtils.rows(params._1, Dates.morning(this.from), Dates.night(this.to));
        LossRate losstotal = new LossRate();

        for(Map<String, Object> row : rows) {
            losstotal.shipqty = (BigDecimal) row.get("shipqty");
            losstotal.totalqty = (BigDecimal) row.get("totalqty");

            Float totalamt = Float.parseFloat(row.get("totalamt").toString());
            if(totalamt != null)
                losstotal.totalamt = new BigDecimal(totalamt).setScale(2, 4).floatValue();
            if(losstotal.shipqty != null && losstotal.shipqty.compareTo(new BigDecimal(0)) != 0) {
                losstotal.lossrate = (losstotal.totalqty).divide(losstotal.shipqty, 4,
                        4).multiply(new BigDecimal(100));
            }
        }
        return losstotal;
    }

    public LossRate buildTotalLossRate(List<LossRate> lossRates) {
        if(lossRates != null && lossRates.size() > 0) {
            LossRate lossRate = this.querytotal();
            for(LossRate loss : lossRates) {
                lossRate.totallossprice += loss.totallossprice;
                lossRate.totalShipmentprice += loss.totalShipmentprice;
            }
            DecimalFormat df = new DecimalFormat("##0.00");
            lossRate.totallossprice = Float.parseFloat(df.format(lossRate.totallossprice));
            lossRate.totalShipmentprice = Float.parseFloat(df.format(lossRate.totalShipmentprice));
            lossRate.totalamt = new BigDecimal(lossRate.totalamt).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            lossRate.lossrate = lossRate.lossrate.setScale(2, BigDecimal.ROUND_HALF_UP);
            lossRate.payrate = new BigDecimal(lossRate.totalamt).divide(new BigDecimal(lossRate.totallossprice + lossRate
                    .totalShipmentprice), 4, 4).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

            return lossRate;
        }
        return new LossRate(new BigDecimal(0));
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return 0L;
    }

    @Override
    public Long getTotalCount() {
        return 0L;
    }

    public Map<String, Object> lossRateMap(F.T2<String, List<Object>> params, F.T2<String, List<Object>> shipParams) {
        long start = System.currentTimeMillis();
        /*查询丢失集合**/
        List<Map<String, Object>> rows = DBUtils.rows(params._1, Dates.morning(this.from), Dates.night(this.to));
        List<LossRate> lossRateList = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.00");
        List<AverageData> dataList = AverageData.findAll();
        Map<String, AverageData> dataMap = AverageData.buildMap(dataList);
        for(Map<String, Object> row : rows) {
            LossRate loss = new LossRate();
            loss.sku = (String) row.get("sku");
            loss.fba = (String) row.get("shipmentid");
            loss.qty = Integer.parseInt(row.get("qty").toString());
            loss.lossqty = Integer.parseInt(row.get("lossqty").toString());
            loss.compentype = (String) row.get("compentype");
            if(row.get("unitId") != null) {
                loss.unit = ProcureUnit.findById(Long.parseLong(row.get("unitId").toString()));
            }
            if(row.get("currency") != null) {
                loss.currency = Currency.valueOf(row.get("currency").toString());
                loss.price = (Float) row.get("price");
                loss.totallossprice = Float.parseFloat(df.format(loss.currency.toUSD(loss.price) * loss.lossqty));
            }
            if(dataMap.get(row.get("sellingId").toString()) != null) {
                AverageData data = dataMap.get(row.get("sellingId").toString());
                loss.totalShipmentprice = data.averageShipPrice.add(data.averageVATPrice).multiply(new BigDecimal(loss
                        .lossqty)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            } else {
                loss.totalShipmentprice = 0;
            }
            Object compenusdamt = row.get("compenusdamt");
            if(compenusdamt != null)
                loss.compenusdamt = new BigDecimal(Float.parseFloat(compenusdamt.toString())).setScale(2,
                        BigDecimal.ROUND_HALF_UP).floatValue();
            else
                loss.compenusdamt = 0f;

            loss.payrate = new BigDecimal(loss.compenusdamt).divide(new BigDecimal(loss.totallossprice
                    + loss.totalShipmentprice), 4, 4).multiply(new BigDecimal(100))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            lossRateList.add(loss);
        }
        Logger.info("查询丢失集合耗时:" + (System.currentTimeMillis() - start) + " ms ");

        start = System.currentTimeMillis();
        List<ShipItem> shipItems = ShipItem.find(shipParams._1, Dates.beginOfYear()).fetch();
        for(ShipItem ship : shipItems) {
            if(ship.recivedLogs().size() == 0) {
                ship.adjustQty = ship.recivedQty;
                ship.save();
            }
            Integer lossNum = ship.qty - (ship.adjustQty == null ? 0 : ship.adjustQty);
            ship.purchaseCost = new BigDecimal(ship.unit.attrs.currency.toUSD(ship.unit.attrs.price) * lossNum)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            if(dataMap.get(ship.unit.selling.sellingId) != null) {
                AverageData data = dataMap.get(ship.unit.selling.sellingId);
                ship.shipmentCost = (data.averageShipPrice.add(data.averageVATPrice)).multiply(new BigDecimal(lossNum))
                        .setScale(2, BigDecimal.ROUND_HALF_UP);
            } else {
                ship.shipmentCost = new BigDecimal(0);
            }
            ship.lossCost = ship.purchaseCost.add(ship.shipmentCost);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("shipItems", shipItems);
        map.put("lossRateList", lossRateList);
        Logger.info("查询今年数据耗时:" + (System.currentTimeMillis() - start) + " ms ");
        return map;
    }

}
