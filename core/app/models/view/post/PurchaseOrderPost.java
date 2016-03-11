package models.view.post;

import helper.Dates;
import models.procure.ProcureUnit;
import org.apache.commons.lang.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by licco on 16/3/7.
 */
public class PurchaseOrderPost extends Post<ProcureUnit> {

    public Long cooperatorId;

    public ProcureUnit.STAGE stage;

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sql = new StringBuilder(" 1 = 1");
        List<Object> params = new ArrayList<Object>();
        if(from != null) {
            sql.append(" AND createDate>=? ");
            params.add(Dates.morning(this.from));
        }
        if(to != null) {
            sql.append(" AND createDate<=? ");
            params.add(Dates.night(this.to));
        }

        if(stage != null) {
            sql.append(" AND stage = ? ");
            params.add(stage);
        }
        if(cooperatorId != null) {
            sql.append(" AND cooperator.id = ? ");
            params.add(cooperatorId);
        }
        if(StringUtils.isNotEmpty(this.search)) {
            sql.append(" AND (sku = ? or deliveryment.id = ? )");
            params.add(this.search);
            params.add(this.search);
        }
        return new F.T2<String, List<Object>>(sql.toString(), params);
    }

    public List<ProcureUnit> query() {
        F.T2<String, List<Object>> params = params();
        return ProcureUnit.find(params._1, params._2.toArray()).fetch();
    }


}
