package models.view.post;

import helper.*;
import jobs.LossRateJob;
import models.procure.ShipItem;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import models.view.report.LossRate;
import play.cache.Cache;
import play.libs.F;
import play.utils.FastRuntimeException;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 12/4/14
 * Time: 4:32 PM
 */
public class LossRatePost extends Post<LossRate> {


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
        List<Object> params = new ArrayList<Object>();
        sql.append(
                "select f.shipmentid,p.sku,s.qty,s.lossqty,s.compenusdamt,p.currency, p.price, l.market From ShipItem s "
                        + " left join ProcureUnit p on s.unit_id=p.id "
                        + " LEFT JOIN Selling l ON l.sellingId = p.sid "
                        + " left join Shipment m on s.shipment_id=m.id "
                        + " left join FBAShipment f on p.fba_id=f.id "
                        + " where m.arriveDate >= ? AND m.arriveDate <= ? "
                        + " and s.lossqty!=0 "
                        + " group by p.fba_id,p.sku ");
        if(StringUtils.isNotBlank(this.compenType)) {
            sql.append(" AND s.compenType= '" + this.compenType + "' ");
        }
        return new F.T2<String, List<Object>>(sql.toString(), params);
    }

    public Map queryDate() {
        String key = Caches.Q.cacheKey(this.from, this.to, "LossRateJob");
        Map<String, Object> map = Cache.get(key, Map.class);
        if(map == null || map.get("lossrate") == null || map.get("shipItems") == null) {
            if(!LossRateJob.isRunning(key)) {
                F.T2<String, List<Object>> params = params();
                F.T2<String, List<Object>> shipParams = params();
                new LossRateJob(this.from, this.to, params, shipParams()).now();
            }
            throw new FastRuntimeException("赔偿统计明细已经在后台计算中，请于 10min 后再来查看结果~");
        }
        List<LossRate> lossRates = (List<LossRate>) map.get("lossrate");
        List<ShipItem> shipItems = (List<ShipItem>) map.get("shipItems");
        this.count = lossRates.size();
        return map;
    }

    public F.T2<String, List<Object>> shipParams() {
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder("SELECT s FROM ShipItem s LEFT JOIN s.shipment m ")
                .append(" WHERE m.state = 'DONE' ")
                .append("AND m.dates.arriveDate >= ? AND m.dates.arriveDate <= ? ")
                .append(" AND s.qty <> s.recivedQty ");
        return new F.T2<String, List<Object>>(sql.toString(), params);
    }

    public F.T2<String, List<Object>> totalparams() {
        StringBuffer sql = new StringBuffer("");
        List<Object> params = new ArrayList<Object>();
        sql.append(
                "select sum(s.qty) shipqty,sum(s.lossqty) totalqty,sum(round(s.compenusdamt,3)) totalamt,p.currency, p.price From ShipItem s "
                        + " left join ProcureUnit p on s.unit_id=p.id "
                        + " left join Shipment m on s.shipment_id=m.id "
                        + " left join FBAShipment f on p.fba_id=f.id "
                        + " where m.arriveDate >= ? AND m.arriveDate <= ? "
                        + " and s.lossqty!=0 ");
        if(StringUtils.isNotBlank(this.compenType)) {
            sql.append(" AND s.compenType= '" + this.compenType + "' ");
        }
        return new F.T2<String, List<Object>>(sql.toString(), params);
    }

    public LossRate querytotal() {
        F.T2<String, List<Object>> params = totalparams();
        List<Map<String, Object>> rows = DBUtils.rows(params._1, Dates.morning(this.from), Dates.night(this.to));
        LossRate losstotal = new LossRate();

        for(Map<String, Object> row : rows) {
            losstotal.shipqty = (BigDecimal) row.get("shipqty");
            losstotal.totalqty = (BigDecimal) row.get("totalqty");

            Object totalamt = row.get("totalamt");
            if(totalamt != null)
                losstotal.totalamt = new BigDecimal((Float)totalamt).setScale(2, 4).floatValue();
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

    private BigDecimal ifBlank(BigDecimal b) {
        return b == null ? new BigDecimal(0) : b;
    }


    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return 0L;
    }

    @Override
    public Long getTotalCount() {
        return 0L;
    }

}
