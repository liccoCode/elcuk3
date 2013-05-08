package models.view.post;

import helper.Dates;
import models.procure.ProcureUnit;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.libs.F;

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

    public boolean isHaveShipItems = false;

    public String dateType = "createDate";

    @Override
    public F.T2<String, List<Object>> params() {
        SqlSelect jpql = new SqlSelect();
        jpql.select("p").from("ProcureUnit p");

        jpql.where("p." + this.dateType + ">=?").param(Dates.morning(this.from))
                .where("p." + this.dateType + "<=?").params(Dates.night(this.to));

        if(!this.isHaveShipItems)
            jpql.where("SIZE(p.shipItems)=0");

        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            jpql.where("p.fba.shipmentId LIKE ?").param(word)
                    .orWhere("p.sku LIKE ?").param(word);
        }

        return new F.T2<String, List<Object>>(jpql.toString(), jpql.getParams());
    }

    @Override
    public List<ProcureUnit> query() {
        F.T2<String, List<Object>> params = this.params();
        return ProcureUnit.find(params._1, params._2.toArray()).fetch();
    }
}
