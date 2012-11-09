package query;

import helper.DBUtils;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/13/12
 * Time: 9:58 AM
 */
public class ShipmentQuery {
    public static long shipemntItemCountWithSameWhouse(String shipmentId, long whouseId) {
        return (Long) DBUtils.row("select count(*) as c from ShipItem si left join ProcureUnit u on si.unit_id=u.id where si.shipment_id=? and u.whouse_id=?", shipmentId, whouseId).get("c");
    }
}
