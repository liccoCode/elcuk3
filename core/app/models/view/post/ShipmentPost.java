package models.view.post;

import helper.Dates;
import models.procure.Shipment;
import models.procure.iExpress;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/8/12
 * Time: 11:06 AM
 */
public class ShipmentPost extends Post {
    public static final List<F.T2<String, String>> DATE_TYPES;
    private static final Pattern ID = Pattern.compile("^(\\w{2}\\|\\d{6}\\|\\d{2})$");

    public ShipmentPost() {
        DateTime now = DateTime.now(Dates.timeZone(null));
        this.from = now.minusDays(7).toDate();
        this.to = now.plusDays(7).toDate();
        this.state = "NOCANCEL";
    }

    static {
        DATE_TYPES = new ArrayList<F.T2<String, String>>();
        DATE_TYPES.add(new F.T2<String, String>("dates.planBeginDate", "预计开始运输时间"));
        DATE_TYPES.add(new F.T2<String, String>("dates.beginDate", "开始运输时间"));
        DATE_TYPES.add(new F.T2<String, String>("createDate", "创建时间"));
        DATE_TYPES.add(new F.T2<String, String>("dates.planArrivDate", "预计 [到库] 时间"));
        DATE_TYPES.add(new F.T2<String, String>("dates.arriveDate", "实际 [到库] 时间"));
    }

    // 默认的搜索排序时间
    public String dateType = "dates.planBeginDate";

    public Shipment.P pype;

    public Shipment.T type;

    public String state;

    public iExpress iExpress;

    public Long cooperId;

    public long whouseId;

    @Override
    public List<Shipment> query() {
        F.T2<String, List<Object>> params = this.params();
        return Shipment.find(params._1, params._2.toArray()).fetch();
    }

    @Override
    public F.T2<String, List<Object>> params() {
        F.T3<Boolean, String, List<Object>> specialSearch = deliverymentId();

        if(specialSearch._1)
            return new F.T2<String, List<Object>>(specialSearch._2, specialSearch._3);

        StringBuilder sbd = new StringBuilder(
                // 几个表使用 left join 级联...
                String.format("SELECT DISTINCT s FROM Shipment s LEFT JOIN s.items i" +
                        " LEFT JOIN i.unit u" +
                        " LEFT JOIN s.items it" +
                        " WHERE s.%s>=? AND s.%s<=?",
                        this.dateType, this.dateType));
        List<Object> params = new ArrayList<Object>();
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

        if(this.pype != null) {
            sbd.append(" AND s.pype=?");
            params.add(this.pype);
        }

        if(this.type != null) {
            sbd.append(" AND s.type=?");
            params.add(this.type);
        }

        if(StringUtils.isNotBlank(this.state)) {
            if(StringUtils.equals(this.state, "NOCANCEL")) {
                sbd.append(" AND s.state!=?");
                params.add(Shipment.S.CANCEL);
            } else {
                sbd.append(" AND s.state=?");
                params.add(Shipment.S.valueOf(this.state));
            }
        }

        if(this.iExpress != null) {
            sbd.append(" AND s.internationExpress=?");
            params.add(this.iExpress);
        }

        if(this.whouseId > 0) {
            sbd.append(" AND s.whouse.id=?");
            params.add(this.whouseId);
        }

        if(this.cooperId != null) {
            sbd.append(" AND s.cooper.id=?");
            params.add(this.cooperId);
        }

        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sbd.append(" AND (")
                    .append("s.trackNo LIKE ?")
                    .append(" OR it.unit.fba.shipmentId LIKE ?")
                    .append(" OR u.sid LIKE ?")
                    .append(")");
            for(int i = 0; i < 3; i++) params.add(word);
        }

        // 因为需要使用 deliverymentId() 方法, 不能够在 param 的地方添加 fba.centerId 路径
        sbd.append(" ORDER BY s.createDate DESC");
        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }

    /**
     * 通过 Id 搜索 Deliveryment
     *
     * @return
     */
    private F.T3<Boolean, String, List<Object>> deliverymentId() {
        if(StringUtils.isNotBlank(this.search)) {
            this.search = this.search.trim();
            Matcher matcher = ID.matcher(this.search);
            if(matcher.find()) {
                String deliverymentId = matcher.group(1);
                return new F.T3<Boolean, String, List<Object>>(true,
                        "SELECT s FROM Shipment s WHERE s.id=?",
                        new ArrayList<Object>(Arrays.asList(deliverymentId)));
            }
        }
        return new F.T3<Boolean, String, List<Object>>(false, null, null);
    }
}
