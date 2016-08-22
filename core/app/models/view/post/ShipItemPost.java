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

    public static final List<F.T2<String, String>> DATE_TYPES;

    static {
            DATE_TYPES = new ArrayList<>();
            DATE_TYPES.add(new F.T2<>("createDate", "创建时间"));
            DATE_TYPES.add(new F.T2<>("planShipDate", "预计运输时间"));
            DATE_TYPES.add(new F.T2<>("planArrivDate", "预计到达时间"));
        }

    private static Pattern SHIPITEMS_ALL_NUM_PATTERN = Pattern.compile("^-?[1-9]\\d*$");

    public ShipItemPost() {
        DateTime now = new DateTime();
        this.to = now.plusDays(14).toDate();
        this.from = now.minusDays(45).toDate();
    }

    public boolean isHaveShipment = false;

    public String dateType = "createDate";

    public Long whouseId;

    public String centerId;


    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sql = new StringBuilder("SELECT si FROM ShipItem si LEFT JOIN si.plan p  ");
        sql.append(" LEFT JOIN si.unit u WHERE ");
        List<Object> params = new ArrayList<>();
        sql.append("p.").append(this.dateType).append(">=?").append(" AND ").append("p.").append(this.dateType)
                .append("<=?");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

        if(whouseId != null) {


        }

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
