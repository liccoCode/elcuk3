package models.view.post;

import helper.DBUtils;
import helper.Dates;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import java.util.*;
import models.view.report.LossRate;
import play.libs.F;
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
        this.from = DateTime.now().minusDays(3).toDate();
        this.to = new Date();
    }


    @Override
    public F.T2<String, List<Object>> params() {
        StringBuffer sql = new StringBuffer("");
        List<Object> params = new ArrayList<Object>();
        sql.append("select f.shipmentid,p.sku,s.qty,s.lossqty,s.compenusdamt From ShipItem s "
                + " left join ProcureUnit p on s.unit_id=p.id "
                + " left join Shipment m on s.shipment_id=m.id "
                + " left join FBAShipment f on p.fba_id=f.id "
                + " where m.planArrivDate >= ? AND m.planArrivDate <= ? "
                + " and s.lossqty!=0 "
                + " group by  p.fba_id,p.sku ");
        if(StringUtils.isNotBlank(this.compenType)) {
            sql.append(" AND s.compenType= '" + this.compenType + "' ");
        }
        return new F.T2<String, List<Object>>(sql.toString(), params);
    }

    public List<LossRate> query() {
        F.T2<String, List<Object>> params = params();
        List<Map<String, Object>> rows = DBUtils
                .rows(params._1, Dates.morning(this.from), Dates.night(this.to));
        List<LossRate> lossrate = new ArrayList<LossRate>();
        for(Map<String, Object> row : rows) {
            LossRate loss = new LossRate();
            loss.sku = (String) row.get("sku");
            loss.fba = (String) row.get("shipmentid");
            loss.qty = (Integer) row.get("qty");
            loss.lossqty = (Integer) row.get("lossqty");
            Object compenusdamt = row.get("compenusdamt");
            if(compenusdamt != null)
                loss.compenusdamt = (Float) row.get("compenusdamt");
            else
                loss.compenusdamt = 0f;

            lossrate.add(loss);
        }
        this.count = lossrate.size();
        return lossrate;
    }


    public F.T2<String, List<Object>> totalparams() {
        StringBuffer sql = new StringBuffer("");
        List<Object> params = new ArrayList<Object>();
        sql.append("select sum(s.qty) shipqty,sum(s.lossqty) totalqty,sum(s.compenusdamt) totalamt From ShipItem s "
                + " left join ProcureUnit p on s.unit_id=p.id "
                + " left join Shipment m on s.shipment_id=m.id "
                + " left join FBAShipment f on p.fba_id=f.id "
                + " where m.planArrivDate >= ? AND m.planArrivDate <= ? "
                + " and s.lossqty!=0 ");
        if(StringUtils.isNotBlank(this.compenType)) {
            sql.append(" AND s.compenType= '" + this.compenType + "' ");
        }
        return new F.T2<String, List<Object>>(sql.toString(), params);
    }

    public LossRate querytotal() {
        F.T2<String, List<Object>> params = totalparams();
        List<Map<String, Object>> rows = DBUtils
                .rows(params._1, Dates.morning(this.from), Dates.night(this.to));
        LossRate losstotal = new LossRate();

        for(Map<String, Object> row : rows) {
            losstotal.shipqty = (BigDecimal) row.get("shipqty");
            losstotal.totalqty = (BigDecimal) row.get("totalqty");
            Object totalamt = row.get("totalamt");
            if (totalamt != null)
                losstotal.totalamt = new BigDecimal((Double) totalamt).setScale(4,4).doubleValue();
            if(losstotal.shipqty!=null && losstotal.shipqty.compareTo(new BigDecimal(0))!=0) {
                losstotal.lossrate = (losstotal.totalqty).divide(losstotal.shipqty, 4,
                        4).multiply(new BigDecimal(100));
            }
        }
        return losstotal;
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
