package models.view.post;

import helper.Dates;
import models.User;
import models.procure.ProcureUnit;
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
 * User: wyatt
 * Date: 5/7/13
 * Time: 10:10 AM
 */
public class ProcureUnitShipPost extends Post<ProcureUnit> {
    private static final long serialVersionUID = -8171825936576265590L;
    private static Pattern SHIPITEMS_ALL_NUM_PATTERN = Pattern.compile("^-?[1-9]\\d*$");

    public ProcureUnitShipPost() {
        DateTime now = new DateTime();
        this.to = now.plusDays(14).toDate();
        this.from = now.minusDays(45).toDate();
    }

    public List<F.T2<String, String>> dateTypes = Arrays.asList(
            new F.T2<>("createDate", "创建时间"),
            new F.T2<>("attrs.planShipDate", "预计 [发货] 时间"),
            new F.T2<>("attrs.planDeliveryDate", "预计 [交货] 时间"),
            new F.T2<>("attrs.planArrivDate", "预计 [到库] 时间")
    );

    public boolean isHaveShipment = false;

    public String dateType = "attrs.planShipDate";

    public Long whouseId;

    public String centerId;

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sql = new StringBuilder("SELECT p FROM ProcureUnit p")
                .append(" LEFT JOIN p.fba f")
                .append(" LEFT JOIN p.shipItems si")
                .append(" WHERE ");
        List<Object> params = new ArrayList<>();

        sql.append("p.").append(this.dateType).append(">=?").append(" AND ")
                .append("p.").append(this.dateType).append("<=?");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));


        if(!this.isHaveShipment)
            sql.append(" AND si.shipment IS NULL");

        if(this.whouseId != null) {
            sql.append(" AND p.whouse.id=?");
            params.add(this.whouseId);
        }

        if(StringUtils.isNotBlank(this.centerId)) {
            sql.append(" AND f.centerId=?");
            params.add(this.centerId);
        }

        this.sameSearch(sql, params);

        return new F.T2<>(sql.toString(), params);
    }

    private void sameSearch(StringBuilder sql, List<Object> params) {
        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            Matcher matcher_all = SHIPITEMS_ALL_NUM_PATTERN.matcher(this.search);
            if(matcher_all.find()) {
                sql.append(" AND p.id=?");
                params.add(Long.parseLong(StringUtils.trim(this.search)));
            } else {
                sql.append(" AND (f.shipmentId LIKE ?")
                        .append(" OR p.sku LIKE ?")
                        .append(" OR p.sid LIKE ?")
                        .append(")");

                for(int i = 0; i < 3; i++) {
                    params.add(word);
                }
            }
        }
    }

    @Override
    public List<ProcureUnit> query() {
        F.T2<String, List<Object>> params = this.params();
        return ProcureUnit.find(params._1, params._2.toArray()).fetch();
    }

    public List<ProcureUnit> queryB2B() {
        StringBuilder sql = new StringBuilder("SELECT p FROM ProcureUnit p")
                .append(" LEFT JOIN p.fba f")
                .append(" LEFT JOIN p.shipItems si")
                .append(" WHERE ");
        List<Object> params = new ArrayList<>();
        sql.append("p.").append(this.dateType).append(">=?").append(" AND ");
        sql.append("p.").append(this.dateType).append("<=?");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

        sql.append(" AND p.projectName = ? ");
        params.add(User.COR.MengTop.name());

        this.sameSearch(sql, params);

        return ProcureUnit.find(sql.toString(), params.toArray()).fetch();
    }
}
