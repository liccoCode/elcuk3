package models.view.post;

import controllers.Login;
import models.User;
import models.procure.Cooperator;
import org.apache.commons.lang.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/5/19
 * Time: 下午2:10
 */
public class CooperatorPost extends Post<Cooperator> {

    private static final long serialVersionUID = 4613786316373015576L;

    public Cooperator.T type;

    @Override
    public F.T2<String, List<Object>> params() {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT c FROM Cooperator c LEFT JOIN c.cooperItems i ");
        sql.append("WHERE  1 = 1 ");

        if(StringUtils.isNotEmpty(search)) {
            sql.append(" AND (i.product.sku like ? OR c.fullName like ? OR c.name like ? )");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }

        User user = Login.current();

        if(!user.isHaveCrossCop()) {
            if(user.isShipmentRole()) {
                sql.append(" AND c.type = ? ");
                params.add(Cooperator.T.SHIPPER);
            } else {
                sql.append(" AND c.type = ? ");
                params.add(Cooperator.T.SUPPLIER);
            }
            sql.append(" AND c.projectName = ? ");
            params.add(user.projectName);
        } 

        sql.append(" ORDER BY c.name ASC ");
        return new F.T2<>(sql.toString(), params);
    }


    public List<Cooperator> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);
        return Cooperator.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }


    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) Cooperator.find(params._1, params._2.toArray()).fetch().size();
    }

}
