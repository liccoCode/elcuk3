package query;

import helper.DBUtils;
import models.procure.ProcureUnit;
import org.apache.commons.lang.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/26/12
 * Time: 5:38 PM
 */
public class ProcureUnitQuery {
    /**
     * 查找每一个 ProcureUnit 运输相关联的 Ids(shipmentId, shipItemIds)
     *
     * @param unitId
     * @return
     */
    public static F.T2<List<String>, List<String>> procureRelateShippingRelateIds(long unitId) {
        List<Map<String, Object>> shipmentIdRows = DBUtils.rows("select s.id as shipmentId, si.id as shipItemId, u.id as uid from ProcureUnit u " +
                "left join ShipItem si on si.unit_id=u.id " +
                "left join Shipment s on si.shipment_id=s.id " +
                "where u.id=?", unitId);
        F.T2<List<String>, List<String>> ids = new F.T2<List<String>, List<String>>(new ArrayList<String>(), new ArrayList<String>());

        for(Map<String, Object> row : shipmentIdRows) {
            if(row.get("shipItemId") == null) continue;
            ids._1.add(row.get("shipmentId").toString());
            ids._2.add(row.get("shipItemId").toString());
        }
        return ids;
    }
}
