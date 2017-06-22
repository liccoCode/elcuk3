package models.view.post;

import helper.DBUtils;
import helper.Dates;
import models.market.M;
import models.procure.Shipment;
import models.view.dto.MonthlyShipmentDTO;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/6/21
 * Time: 下午3:11
 */
public class MonthlyShipmentPost extends Post<MonthlyShipmentDTO> {

    private static final long serialVersionUID = -1753986320549964929L;

    @Override
    public F.T2<String, List<Object>> params() {
        return null;
    }

    public Map<String, MonthlyShipmentDTO> queryBySku() {
        Map<String, MonthlyShipmentDTO> map = new HashMap<>();
        SqlSelect sql = buildSql();
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        for(Map<String, Object> row : rows) {
            String sku = row.get("product_sku").toString();
            if(map.containsKey(sku)) {
                MonthlyShipmentDTO dto = map.get(sku);
                this.buildDto(dto, row);
            } else {
                MonthlyShipmentDTO dto = new MonthlyShipmentDTO();
                dto.sku = sku;
                this.buildDto(dto, row);
                map.put(sku, dto);
            }
        }
        return map;
    }

    private SqlSelect buildSql() {
        SqlSelect sql = new SqlSelect()
                .select("u.product_sku, i.qty, d.category_categoryId as categoryId, " +
                        "round(ifnull(sum(i.qty * d.weight),0),2) as totalWeight, " +
                        "s.type, sum(i.qty) as totalQty, " +
                        "round(ifnull(sum(d.lengths*d.width*d.heigh*i.qty/1000000),0),2) as totalCbm," +
                        "w.market, f.centerId ")
                .from("ShipItem i ")
                .leftJoin(" Shipment s ON s.id = i.shipment_id ")
                .leftJoin(" ProcureUnit u ON i.unit_id= u.id ")
                .leftJoin(" Whouse w ON w.id = u.whouse_id ")
                .leftJoin(" FBAShipment f ON u.fba_id = f.id ")
                .leftJoin(" Product d ON d.sku = u.product_sku ")
                .leftJoin(" PaymentUnit p ON p.shipment_id= s.id ")
                .where("s.planBeginDate >=?").params(Dates.morning(this.from))
                .andWhere("s.planBeginDate <=?").params(Dates.night(this.to));
        sql.groupBy("u.product_sku, s.type ");
        sql.orderBy("d.category_categoryId ASC");
        return sql;
    }

    private void buildDto(MonthlyShipmentDTO dto, Map<String, Object> row) {
        Shipment.T type = Shipment.T.valueOf(row.get("type").toString());
        dto.categoryId = row.get("categoryId").toString();
        dto.market = row.get("market") == null ? null : M.valueOf(row.get("market").toString());
        dto.centerId = row.get("centerId") == null ? "" : row.get("centerId").toString();
        if(Objects.equals(Shipment.T.AIR, type)) {
            dto.airQty = Integer.parseInt(row.get("totalQty").toString());
            dto.airWeight = Float.parseFloat(row.get("totalWeight").toString());
            dto.airCbm = Float.parseFloat(row.get("totalCbm").toString());
        }
        if(Objects.equals(Shipment.T.SEA, type)) {
            dto.seaQty = Integer.parseInt(row.get("totalQty").toString());
            dto.seaWeight = Float.parseFloat(row.get("totalWeight").toString());
            dto.seaCbm = Float.parseFloat(row.get("totalCbm").toString());
        }
        if(Objects.equals(Shipment.T.EXPRESS, type)) {
            dto.expressQty = Integer.parseInt(row.get("totalQty").toString());
            dto.expressWeight = Float.parseFloat(row.get("totalWeight").toString());
            dto.expressCbm = Float.parseFloat(row.get("totalCbm").toString());
        }
    }


}
