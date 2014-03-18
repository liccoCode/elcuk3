package models.view.post;

import models.SaleTarget;
import models.market.M;
import models.procure.Shipment;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-3-18
 * Time: PM2:44
 */
public class SaleTargetPost extends Post<SaleTarget> {

    /**
     * 市场
     */
    public M market;

    /**
     * 货运类型
     */
    public Shipment.T shipmentType;

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder(
                "SELECT DISTINCT s FROM SaleTarget s WHERE 1=1 AND");
        List<Object> params = new ArrayList<Object>();

        if(this.market != null) {
            sbd.append("market=?");
            params.add(this.market);
        }
        if(this.shipmentType != null) {
            sbd.append("shipmentType=?");
            params.add(this.shipmentType);
        }
        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }

    public List<SaleTarget> query() {
        F.T2<String, List<Object>> params = params();
        return SaleTarget.find(params._1 + " ORDER BY s.createDate DESC", params._2.toArray())
                .fetch();
    }
}
