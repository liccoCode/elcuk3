package models.view;

import helper.Dates;
import models.procure.ProcureUnit;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/3/12
 * Time: 4:32 PM
 */
public class ProcurePost {

    public Date from;
    public Date to;

    public long whouseId;

    public long cooperatorId;

    public ProcureUnit.STAGE stage;

    public String search;

    public ProcurePost() {
        this.from = DateTime.now().minusMonths(2).toDate();
        this.to = new Date();
    }

    public List<ProcureUnit> search() {
        F.T2<String, List<Object>> params = params();
        return ProcureUnit.find(params._1, params._2.toArray()).fetch();
    }

    private F.T2<String, List<Object>> params() {
        //TODO createDate 修改
        StringBuilder sbd = new StringBuilder("planDeliveryDate>=? AND planDeliveryDate<=?");
        List<Object> params = new ArrayList<Object>();
        params.add(this.from);
        params.add(this.to);

        if(this.whouseId > 0) {
            sbd.append(" AND whouse.id=?");
            params.add(this.whouseId);
        }

        if(this.cooperatorId > 0) {
            sbd.append(" AND cooperator.id=? ");
            params.add(this.cooperatorId);
        }

        if(this.stage != null) {
            sbd.append(" AND stage=? ");
            params.add(this.stage);
        }

        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }
}
