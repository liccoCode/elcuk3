package models.view.post;

import models.procure.ProcureUnit;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/7/13
 * Time: 10:10 AM
 */
public class ProcureUnitShipPost extends Post<ProcureUnit> {
    public ProcureUnitShipPost() {
        this.to = new Date();
        this.from = new DateTime(this.to).minusMonths(1).toDate();
    }

    @Override
    public F.T2<String, List<Object>> params() {
        SqlSelect jpql = new SqlSelect();
        jpql.select("p").from("ProcureUnit p");

        jpql.where("p.createDate>=?").param(this.from)
                .where("p.createDate<=?").params(this.to);

        return new F.T2<String, List<Object>>(jpql.toString(), jpql.getParams());
    }

    @Override
    public List<ProcureUnit> query() {
        F.T2<String, List<Object>> params = this.params();
        return ProcureUnit.find(params._1, params._2.toArray()).fetch();
    }
}
