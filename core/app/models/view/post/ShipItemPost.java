package models.view.post;

import helper.Dates;
import models.procure.ShipItem;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by licco on 16/8/12.
 */
public class ShipItemPost extends Post<ShipItem> {

    private static Pattern SHIPITEMS_ALL_NUM_PATTERN = Pattern.compile("^-?[1-9]\\d*$");

    public ShipItemPost() {
        DateTime now = new DateTime();
        this.to = now.plusDays(14).toDate();
        this.from = now.minusDays(45).toDate();
    }

    public List<F.T2<String, String>> dateTypes = Arrays.asList(
            new F.T2<>("createDate", "创建时间"),
            new F.T2<>("dates.planBeginDate", "预计 [发货] 时间"),
            new F.T2<>("dates.planDeliveryDate", "预计 [交货] 时间"),
            new F.T2<>("dates.planArrivDate", "预计 [到库] 时间")
    );

    public boolean isHaveShipment = false;

    public String dateType = "dates.planBeginDate";

    public Long whouseId;

    public String centerId;


    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sql = new StringBuilder("SELECT si FROM ShipItem si LEFT JOIN si.plan p WHERE ");

        List<Object> params = new ArrayList<>();
        sql.append("p.").append(this.dateType).append(">=?").append(" AND ").append("p.").append(this.dateType)
                .append("<=?");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

        if(!this.isHaveShipment)
            sql.append(" AND si.shipment IS NULL");


        return new F.T2<>(sql.toString(), params);
    }

    @Override
    public List<ShipItem> query() {
        F.T2<String, List<Object>> params = this.params();
        return ShipItem.find(params._1, params._2.toArray()).fetch();
    }

}
