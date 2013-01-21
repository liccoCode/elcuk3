package query;

import helper.DBUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/28/12
 * Time: 6:51 PM
 */
public class FBAShipmentQuery {

    public List<String> uncloseFBAShipmentIds() {
        //TODO 还有条件需要添加的...
        List<Map<String, Object>> rows = DBUtils.rows("SELECT id, shipmentId FROM FBAShipment");
        List<String> ids = new ArrayList<String>();
        for(Map<String, Object> row : rows) {
            ids.add(row.get("shipmentId").toString());
        }
        return ids;
    }
}
