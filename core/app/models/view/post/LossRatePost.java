package models.view.post;

import com.alibaba.fastjson.JSON;
import helper.*;
import helper.Currency;
import models.market.M;
import models.procure.ShipItem;
import models.view.dto.ProfitDto;
import models.view.report.LossRate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.libs.F;
import play.utils.FastRuntimeException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
        StringBuffer sql = new StringBuffer("");
        List<Object> params = new ArrayList<>();
        sql.append(
                "select f.shipmentid,p.sku,(s.qty-ifnull(p.purchaseSample,0)-ifnull(c.qcSample,0)) as qty,"
                        + " s.lossqty, s.compenusdamt, p.currency, p.price, l.market, s.compentype "
                        + " From ShipItem s "
                        + " left join ProcureUnit p on s.unit_id=p.id "
                        + " LEFT JOIN CheckTask c ON c.units_id = p.id"
                        + " LEFT JOIN Selling l ON l.sellingId = p.sid "
                        + " left join Shipment m on s.shipment_id=m.id "
                        + " left join FBAShipment f on p.fba_id=f.id "
                        + " where m.arriveDate >= ? AND m.arriveDate <= ? "
                        + " and s.lossqty!=0 and s.compenamt != 0 "
                        + " group by p.fba_id,p.sku order by l.sellingId desc ");
        if(StringUtils.isNotBlank(this.compenType)) {
            sql.append(" AND s.compenType= '" + this.compenType + "' ");
        }
        return new F.T2<>(sql.toString(), params);
    }

    public Map queryDate() {
        String key = "losspost_" + new SimpleDateFormat("yyyyMMdd").format(this.from)
                + "_" + new SimpleDateFormat("yyyyMMdd").format(this.to);
        String postvalue = Caches.get(key);
        if(StringUtils.isBlank(postvalue)) {
            HTTP.get(System.getenv(Constant.ROCKEND_HOST) + "/loss_rate_job?from="
                    + new SimpleDateFormat("yyyy-MM-dd").format(this.from)
                    + "&to="
                    + new SimpleDateFormat("yyyy-MM-dd").format(this.to));
            throw new FastRuntimeException("赔偿统计明细已经在后台计算中，请于 10min 后再来查看结果~");
        }

        F.T2<String, List<Object>> params = params();
        F.T2<String, List<Object>> shipParams = shipParams();
        Map<String, Object> map = lossRateMap(params, shipParams);
        List<LossRate> lossRates = (List<LossRate>) map.get("lossrate");
        List<ShipItem> shipItems = (List<ShipItem>) map.get("shipItems");
        this.count = lossRates.size();
        return map;
    }

    public F.T2<String, List<Object>> shipParams() {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT s FROM ShipItem s LEFT JOIN s.shipment m ")
                .append(" WHERE m.state = 'DONE' ")
                .append(" AND m.dates.arriveDate >= ? AND m.dates.arriveDate <= ? ")
                .append(" AND s.qty <> s.recivedQty ")
                .append(" ORDER BY s.unit.sid DESC ");
        return new F.T2<>(sql.toString(), params);
    }

    public F.T2<String, List<Object>> totalparams() {
        StringBuffer sql = new StringBuffer("");
        List<Object> params = new ArrayList<>();
        sql.append("select sum(s.qty-ifnull(p.purchaseSample,0)-ifnull(c.qcSample,0)) shipqty," +
                " sum(s.lossqty) totalqty,sum(round(s.compenusdamt,3)) totalamt,p.currency, p.price From ShipItem s " +
                " left join ProcureUnit p on s.unit_id=p.id " +
                " left join CheckTask c ON c.units_id = p.id" +
                " left join Shipment m on s.shipment_id=m.id " +
                " left join FBAShipment f on p.fba_id=f.id " +
                " where m.arriveDate >= ? AND m.arriveDate <= ? " +
                " and s.lossqty!=0 ");
        if(StringUtils.isNotBlank(this.compenType)) {
            sql.append(" AND s.compenType= '" + this.compenType + "' ");
        }
        return new F.T2<>(sql.toString(), params);
    }

    public LossRate querytotal() {
        F.T2<String, List<Object>> params = totalparams();
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
        //TODO: 这里的日志 Logger.info 需要集中清理.
        List<Map<String, Object>> rows = DBUtils.rows(params._1, Dates.morning(this.from), Dates.night(this.to));
        List<LossRate> lossrate = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.00");

        Map<String, ProfitDto> existMap = new HashMap<>();
        List<ProfitDto> dtos = null;
        M[] marray = models.market.M.values();
        for(M m : marray) {
            String cacke_key = "lossrate_" + m.name() + "_" +
                    new SimpleDateFormat("yyyyMMdd").format(this.from)
                    + "_" + new SimpleDateFormat("yyyyMMdd").format(this.to);
            Logger.info("::::::xx:::::key:::" + cacke_key);
            String cache_str = Caches.get(cacke_key);
            if(!StringUtils.isBlank(cache_str)) {
                dtos = JSON.parseArray(cache_str, ProfitDto.class);
                if(dtos != null) {
                    for(ProfitDto dto : dtos) {
                        Logger.info("::::::0:::::key:::" + dto.sku + "_" + m.name());
                        existMap.put(dto.sku + "_" + m.name(), dto);
                    }
                }
            }
        }


        for(Map<String, Object> row : rows) {
            LossRate loss = new LossRate();
            loss.sku = (String) row.get("sku");
            loss.fba = (String) row.get("shipmentid");
            loss.qty = Integer.parseInt(row.get("qty").toString());
            loss.lossqty = Integer.parseInt(row.get("lossqty").toString());
            loss.compentype = (String) row.get("compentype");
            if(row.get("currency") != null) {
                loss.currency = Currency.valueOf(row.get("currency").toString());
                loss.price = (Float) row.get("price");
                loss.totallossprice = Float.parseFloat(df.format(loss.currency.toUSD(loss.price) * loss.lossqty));
            }
            loss.market = M.valueOf(row.get("market").toString());

            String key = loss.sku + "_" + loss.market.name();
            Logger.info("::::::1:::::key:::" + key);
            ProfitDto dto = existMap.get(key);
            Logger.info("::::::1:::::key:::xxx:::" + dto);
            if(dto != null) {
                loss.totalShipmentprice = Float
                        .parseFloat(df.format((dto.ship_price + dto.vat_price) * loss.lossqty));
            } else
                loss.totalShipmentprice = 0;

            Object compenusdamt = row.get("compenusdamt");
            if(compenusdamt != null)
                loss.compenusdamt = new BigDecimal(Float.parseFloat(compenusdamt.toString())).setScale(2,
                        BigDecimal.ROUND_HALF_UP).floatValue();
            else
                loss.compenusdamt = 0f;

            loss.payrate = new BigDecimal(loss.compenusdamt).divide(new BigDecimal(loss.totallossprice +
                    loss.totalShipmentprice), 4, 4).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            lossrate.add(loss);
        }

        List<ShipItem> shipItems = ShipItem.find(shipParams._1, Dates.morning(this.from), Dates.night(this.to)).fetch();
        for(ShipItem ship : shipItems) {
            if(ship.recivedLogs().size() == 0) {
                ship.adjustQty = ship.recivedQty;
                ship.save();
            }

            Integer lossNum = ship.qty - (ship.adjustQty == null ? 0 : ship.adjustQty);
            ship.purchaseCost = new BigDecimal(ship.unit.attrs.currency.toUSD(ship.unit.attrs.price) * lossNum)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            String key = ship.unit.sku + "_" + ship.unit.selling.market.name();
            Logger.info("::::::2:::::key:::" + key);
            ProfitDto dto = existMap.get(key);
            Logger.info("::::::2:::::key:::xxx:::" + dto);
            if(dto != null) {
                ship.shipmentCost = new BigDecimal((dto.ship_price + dto.vat_price) * lossNum)
                        .setScale(2, BigDecimal.ROUND_HALF_UP);
            } else
                ship.shipmentCost = new BigDecimal(0);
            ship.lossCost = ship.purchaseCost.add(ship.shipmentCost);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("shipItems", shipItems);
        map.put("lossrate", lossrate);
        return map;
    }

}
