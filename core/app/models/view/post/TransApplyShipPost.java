package models.view.post;

import models.procure.Shipment;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * TransportApply Shipment Post
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 11/3/16
 * Time: 2:18 PM
 */
public class TransApplyShipPost extends Post<Shipment> {
    public Long applyId;

    public TransApplyShipPost() {
        this.perSize = 20;
    }

    public TransApplyShipPost(long applyId) {
        this.perSize = 20;
        this.applyId = applyId;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT s FROM Shipment s "
                + "LEFT JOIN s.items i "
                + "LEFT JOIN s.fees f WHERE 1 = 1");
        List<Object> params = new ArrayList<>();

        if(this.applyId != null) {
            sql.append(" AND s.apply.id=?");
            params.add(applyId);
        }
        if(StringUtils.isNotBlank(this.search)) {
            sql.append(" AND (")
                    .append(" s.id LIKE ?")
                    .append(" OR s.trackNo LIKE ?")
                    .append(" OR s.whouse.name LIKE ?")
                    .append(" OR i.unit.fba.shipmentId LIKE ?")
                    .append(" OR f.memo LIKE ?");
            for(int i = 0; i <= 4; i++) params.add("%" + this.search + "%");
            if(NumberUtils.isNumber(this.search)) {
                sql.append(" OR f.id=?");
                params.add(NumberUtils.toLong(this.search));
            }
            sql.append(")");
        }
        return new F.T2(sql.toString(), params);
    }

    public List<Shipment> query() {
        F.T2<String, List<Object>> params = params();
        return Shipment.find(params._1, params._2.toArray()).fetch(this.page(), this.perSize);
    }

    public Long getTotalCount() {
        return this.count();
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) Shipment.find(params._1, params._2.toArray()).fetch().size();
    }
}
