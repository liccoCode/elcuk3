package models.view.post;

import helper.Dates;
import models.procure.Shipment;
import models.whouse.ShipPlan;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 16/6/27
 * Time: 3:42 PM
 */
public class ShipPlanPost extends Post<ShipPlan> {
    public static final List<F.T2<String, String>> DATE_TYPES;

    static {
        DATE_TYPES = new ArrayList<>();
        DATE_TYPES.add(new F.T2<>("sp.createDate", "创建时间"));
        DATE_TYPES.add(new F.T2<>("sp.planShipDate", "预计运输时间"));
        DATE_TYPES.add(new F.T2<>("sp.planArrivDate", "预计到达时间"));
    }

    public Date from;
    public Date to;
    public String dateType = "sp.planShipDate";
    public ShipPlan.S state;
    public long whouseId;
    public Shipment.T shipType;
    public String search;
    public boolean isHaveNoShipment = false;
    public String centerId;

    public ShipPlanPost() {
        this.from = DateTime.now().minusDays(25).toDate();
        this.to = new Date();
        this.dateType = "sp.planShipDate";
        this.perSize = 50;
    }

    public List<ShipPlan> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count();
        return ShipPlan.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    public Long getTotalCount() {
        return this.count();
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) ShipPlan.find(params._1, params._2.toArray()).fetch().size();
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT sp FROM ShipPlan sp")
                .append(" LEFT JOIN sp.unit u")
                .append(" LEFT JOIN sp.selling s")
                .append(" LEFT JOIN sp.product pd")
                .append(" LEFT JOIN sp.fba f")
                .append(" LEFT JOIN sp.shipItems si ")
                .append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND(");
            if(NumberUtils.isNumber(this.search)) {
                sbd.append(" sp.id=? OR u.id=? OR ");
                long seed = NumberUtils.toLong(this.search);
                for(int i = 0; i < 2; i++) params.add(seed);
            }
            sbd.append(" s.sellingId LIKE ? OR pd.sku LIKE ? OR f.shipmentId LIKE ?")
                    .append(" OR pd.abbreviation LIKE ?)");
            String word = this.word();
            for(int i = 0; i <= 3; i++) params.add(word);
        }

        if(StringUtils.isNotBlank(this.dateType)) {
            sbd.append(" AND ").append(this.dateType).append(">=?").append(" AND ").append(this.dateType)
                    .append("<=?");
            params.add(Dates.morning(this.from));
            params.add(Dates.night(this.to));
        }

        if(this.state != null) {
            sbd.append(" AND sp.state=?");
            params.add(this.state);
        }

        if(this.whouseId > 0) {
            sbd.append(" AND sp.whouse.id=?");
            params.add(this.whouseId);
        }

        if(this.shipType != null) {
            sbd.append(" AND sp.shipType=? ");
            params.add(this.shipType);
        }
        if(isHaveNoShipment) {
            sbd.append(" AND si.shipment IS NULL ");
        }

        sbd.append(" ORDER BY sp.createDate DESC");
        return new F.T2<>(sbd.toString(), params);
    }

    public List<ShipPlan> queryForExcel() {
        F.T2<String, List<Object>> params = params();
        return ShipPlan.find(params._1, params._2.toArray()).fetch();
    }
}