package models.view.post;

import helper.Dates;
import models.procure.ProcureUnit;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/7/13
 * Time: 10:10 AM
 */
public class ProcureUnitShipPost extends Post<ProcureUnit> {
    public ProcureUnitShipPost() {
        DateTime now = new DateTime();
        this.to = now.plusDays(14).toDate();
        this.from = now.minusDays(45).toDate();
    }

    public List<F.T2<String, String>> dateTypes = Arrays.asList(
            new F.T2<String, String>("createDate", "创建时间"),
            new F.T2<String, String>("attrs.planShipDate", "预计 [发货] 时间"),
            new F.T2<String, String>("attrs.planDeliveryDate", "预计 [交货] 时间"),
            new F.T2<String, String>("attrs.planArrivDate", "预计 [到库] 时间")
    );

    public boolean isHaveShipment = false;

    public String dateType = "attrs.planShipDate";

    public String whouse;

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sql = new StringBuilder("SELECT p FROM ProcureUnit p")
                .append(" LEFT JOIN p.fba f")
                .append(" LEFT JOIN p.shipItems si")
                .append(" WHERE ");
        List<Object> params = new ArrayList<Object>();

        sql.append("p.").append(this.dateType).append(">=?").append(" AND ")
                .append("p.").append(this.dateType).append("<=?");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));


        if(!this.isHaveShipment)
            sql.append(" AND si.shipment IS NULL");

        if(StringUtils.isNotBlank(this.whouse)) {
            sql.append(" AND p.whouse.name=?");
            params.add(this.whouse);
        }

        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sql.append(" AND (f.shipmentId LIKE ?")
                    .append(" OR p.sku LIKE ?")
                    .append(" OR p.sid LIKE ?")
                    .append(" OR f.centerId LIKE ?")
                    .append(")");

            for(int i = 0; i < 4; i++) {
                params.add(word);
            }
        }

        return new F.T2<String, List<Object>>(sql.toString(), params);
    }

    @Override
    public List<ProcureUnit> query() {
        F.T2<String, List<Object>> params = this.params();
        return ProcureUnit.find(params._1, params._2.toArray()).fetch();
    }
}
