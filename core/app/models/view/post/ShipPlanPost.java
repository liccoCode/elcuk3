package models.view.post;

import helper.Dates;
import models.procure.Shipment;
import models.whouse.ShipPlan;
import org.apache.commons.lang.StringUtils;
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
        DATE_TYPES.add(new F.T2<>("createDate", "创建时间"));
        DATE_TYPES.add(new F.T2<>("planShipDate", "预计运输时间"));
        DATE_TYPES.add(new F.T2<>("planArrivDate", "预计到达时间"));
    }

    public Date from;
    public Date to;
    public String dateType = "planShipDate";
    public ShipPlan.S state;
    public long whouseId;
    public Shipment.T shipType;
    public String search;

    public ShipPlanPost() {
        this.from = DateTime.now().minusDays(25).toDate();
        this.to = new Date();
        this.dateType = "planShipDate";
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
        return ShipPlan.count(params._1, params._2.toArray());
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();

        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND (id LIKE ? OR selling.sellingId LIKE ? OR product.sku LIKE ? OR fba.shipmentId LIKE ? OR " +
                    "product.abbreviation LIKE ?)");
            String word = this.word();
            for(int i = 0; i <= 4; i++) params.add(word);
        }

        if(StringUtils.isNotBlank(this.dateType)) {
            sbd.append(" AND ").append(this.dateType).append(">=?").append(" AND ").append(this.dateType)
                    .append("<=?");
            params.add(Dates.morning(this.from));
            params.add(Dates.night(this.to));
        }

        if(this.state != null) {
            sbd.append(" AND state=?");
            params.add(this.state.name());
        }

        if(this.whouseId > 0) {
            sbd.append(" AND whouse.id=?");
            params.add(this.whouseId);
        }

        if(this.shipType != null) {
            sbd.append(" AND shipType=? ");
            params.add(this.shipType.name());
        }

        sbd.append(" ORDER BY createDate DESC");
        return new F.T2<>(sbd.toString(), params);
    }
}
