package models.view.post;

import helper.Dates;
import models.procure.Shipment;
import models.procure.iExpress;
import org.apache.commons.lang.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/8/12
 * Time: 11:06 AM
 */
public class ShipmentPost extends Post {
    public static final List<F.T2<String, String>> DATE_TYPES;

    static {
        DATE_TYPES = new ArrayList<F.T2<String, String>>();
        DATE_TYPES.add(new F.T2<String, String>("beginDate", "开始运输时间"));
        DATE_TYPES.add(new F.T2<String, String>("createDate", "创建时间"));
        DATE_TYPES.add(new F.T2<String, String>("planArrivDate", "预计 [到库] 时间"));
        DATE_TYPES.add(new F.T2<String, String>("arriveDate", "实际 [到库] 时间"));
    }

    // 默认的搜索排序时间
    public String dateType = "beginDate";

    public Shipment.P pype;

    public Shipment.T type;

    public Shipment.S state;

    public iExpress iExpress;

    public long whouseId;

    @Override
    public List<Shipment> query() {
        F.T2<String, List<Object>> params = this.params();
        return Shipment.find(params._1, params._2.toArray()).fetch();
    }

    @Override
    public F.T2<String, List<Object>> params() {
//        StringBuilder sbd = new StringBuilder(String.format("%s>=? AND %s<=?", this.dateType, this.dateType));
        StringBuilder sbd = new StringBuilder(
                String.format("SELECT DISTINCT s FROM Shipment s LEFT JOIN s.items i WHERE s.%s>=? AND s.%s<=?", this.dateType, this.dateType));
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

        if(this.state != null) {
            sbd.append(" AND s.state=?");
            params.add(this.state);
        }

        if(this.iExpress != null) {
            sbd.append(" AND s.internationExpress=?");
            params.add(this.iExpress);
        }

        if(this.whouseId > 0) {
            sbd.append(" AND s.whouse.id=?");
            params.add(this.whouseId);
        }

        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sbd.append(" AND (")
                    .append("s.memo LIKE ?")
                    .append(" OR s.trackNo LIKE ?")
                    .append(" OR i.unit.sid LIKE ?")
                    .append(")");
            for(int i = 0; i < 3; i++) params.add(word);
        }


        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }
}
