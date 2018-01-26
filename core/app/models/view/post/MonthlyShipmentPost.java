package models.view.post;

import com.alibaba.fastjson.JSON;
import helper.DBUtils;
import helper.Dates;
import models.market.M;
import models.procure.Shipment;
import models.qc.CheckTaskDTO;
import models.view.dto.MonthlyShipmentDTO;
import org.apache.commons.lang3.StringUtils;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.math.BigDecimal;
import java.util.*;

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

    public List<MonthlyShipmentDTO> queryBySku() {
        List<MonthlyShipmentDTO> list = new ArrayList<>();
        Map<String, MonthlyShipmentDTO> map = new HashMap<>();
        SqlSelect sql = buildSql();
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());

        SqlSelect unitSql = buildProcureUnitSql();
        List<Map<String, Object>> unitRows = DBUtils.rows(unitSql.toString(), unitSql.getParams().toArray());
        Map<String, BigDecimal> skuMap = new HashMap<>();
        for(Map<String, Object> unitMap : unitRows) {
            calculation(skuMap, unitMap);
        }

        for(Map<String, Object> row : rows) {
            String sku = row.get("product_sku").toString();
            if(map.containsKey(sku)) {
                MonthlyShipmentDTO dto = map.get(sku);
                this.buildDto(dto, row, sku, skuMap);
            } else {
                MonthlyShipmentDTO dto = new MonthlyShipmentDTO();
                dto.sku = sku;
                this.buildDto(dto, row, sku, skuMap);
                map.put(sku, dto);
                list.add(dto);
            }
        }
        return list;
    }

    private SqlSelect buildSql() {
        SqlSelect sql = new SqlSelect()
                .select(" u.product_sku, i.qty, d.category_categoryId as categoryId, "
                        + "s.type, sum(i.qty) as totalQty, "
                        + "round(ifnull(sum(d.lengths*d.width*d.heigh*i.qty/1000000),0),2) as totalCbm,"
                        + "w.market, f.centerId ")
                .from("ShipItem i ")
                .leftJoin(" Shipment s ON s.id = i.shipment_id ")
                .leftJoin(" ProcureUnit u ON i.unit_id= u.id ")
                .leftJoin(" Whouse w ON w.id = u.whouse_id ")
                .leftJoin(" FBAShipment f ON u.fba_id = f.id ")
                .leftJoin(" Product d ON d.sku = u.product_sku ")
                .where("s.planBeginDate >=?").params(Dates.morning(this.from))
                .andWhere("s.planBeginDate <=?").params(Dates.night(this.to));
        sql.groupBy("u.product_sku, s.type ");
        sql.orderBy("d.category_categoryId ASC");
        return sql;
    }

    private SqlSelect buildProcureUnitSql() {
        SqlSelect sql = new SqlSelect()
                .select(" i.qty,u.mainBoxInfo,u.product_sku as sku,s.type ")
                .from("ShipItem i ")
                .leftJoin(" Shipment s ON s.id = i.shipment_id ")
                .leftJoin(" ProcureUnit u ON i.unit_id= u.id ")
                .where("s.planBeginDate >=?").params(Dates.morning(this.from))
                .andWhere("s.planBeginDate <=?").params(Dates.night(this.to));
        sql.orderBy("u.id ASC");
        return sql;
    }


    private void buildDto(MonthlyShipmentDTO dto, Map<String, Object> row, String sku, Map<String, BigDecimal> skuMap) {
        Shipment.T type = Shipment.T.valueOf(row.get("type").toString());
        dto.categoryId = row.get("categoryId").toString();
        dto.market = row.get("market") == null ? null : M.valueOf(row.get("market").toString());
        dto.centerId = row.get("centerId") == null ? "" : row.get("centerId").toString();
        String kgsKey = String.format("kgs_%s_%s", sku, row.get("type").toString());
        String cbmKey = String.format("cbm_%s_%s", sku, row.get("type").toString());

        if(Objects.equals(Shipment.T.AIR, type)) {
            dto.airQty = Integer.parseInt(row.get("totalQty").toString());
            dto.airWeight = skuMap.get(kgsKey) == null ? 0f : skuMap.get(kgsKey).floatValue();
            dto.airCbm = skuMap.get(cbmKey) == null ? 0f : skuMap.get(cbmKey).floatValue();
        }
        if(Objects.equals(Shipment.T.SEA, type)) {
            dto.seaQty = Integer.parseInt(row.get("totalQty").toString());
            dto.seaWeight = skuMap.get(kgsKey) == null ? 0f : skuMap.get(kgsKey).floatValue();
            dto.seaCbm = skuMap.get(cbmKey) == null ? 0f : skuMap.get(cbmKey).floatValue();
        }
        if(Objects.equals(Shipment.T.EXPRESS, type)) {
            dto.expressQty = Integer.parseInt(row.get("totalQty").toString());
            dto.expressWeight = skuMap.get(kgsKey) == null ? 0f : skuMap.get(kgsKey).floatValue();
            dto.expressCbm = skuMap.get(cbmKey) == null ? 0f : skuMap.get(cbmKey).floatValue();
        }
        if(Objects.equals(Shipment.T.DEDICATED, type)) {
            dto.dedicatedQty = Integer.parseInt(row.get("totalQty").toString());
            dto.dedicatedWeight = skuMap.get(kgsKey) == null ? 0f : skuMap.get(kgsKey).floatValue();
            dto.dedicatedCbm = skuMap.get(cbmKey) == null ? 0f : skuMap.get(cbmKey).floatValue();
        }
        if(Objects.equals(Shipment.T.RAILWAY, type)) {
            dto.railwayQty = Integer.parseInt(row.get("totalQty").toString());
            dto.railwayWeight = skuMap.get(kgsKey) == null ? 0f : skuMap.get(kgsKey).floatValue();
            dto.railwayCbm = skuMap.get(cbmKey) == null ? 0f : skuMap.get(cbmKey).floatValue();
        }
    }

    /**
     * 计算采购计划对应的sku重量
     *
     * @param skuMap
     * @param unitMap
     */
    public void calculation(Map<String, BigDecimal> skuMap, Map<String, Object> unitMap) {
        String sku = (String) unitMap.get("sku");
        String type = (String) unitMap.get("type");
        int qty = (int) unitMap.get("qty");
        String kgsKey = String.format("kgs_%s_%s", sku, type);
        String cbmKey = String.format("cbm_%s_%s", sku, type);

        if(sku.equals("11CAR2U1C68A-B")) {
            System.out.println(11111);
        }

        if(unitMap.get("mainBoxInfo") != null && StringUtils.isNotBlank(unitMap.get("mainBoxInfo").toString())) {
            CheckTaskDTO mainBox = JSON.parseObject(unitMap.get("mainBoxInfo").toString(), CheckTaskDTO.class);
            /** 1 计算kgs**/
            if(mainBox != null && mainBox.boxNum > 0 && mainBox.singleBoxWeight > 0) {
                BigDecimal kg = new BigDecimal(mainBox.boxNum).
                        multiply(new BigDecimal(mainBox.singleBoxWeight)).setScale(4, 4);
                if(skuMap.containsKey(kgsKey)) skuMap.put(kgsKey, skuMap.get(kgsKey).add(kg));
                else skuMap.put(kgsKey, kg);
            }

            /** 2 计算cbm**/
            if(mainBox != null && mainBox.length > 0 && mainBox.width > 0 && mainBox.height > 0 && mainBox.boxNum > 0) {


                BigDecimal cmb = new BigDecimal(mainBox.length).
                        multiply(new BigDecimal(mainBox.width)).
                        multiply(new BigDecimal(mainBox.height)).
                        multiply(new BigDecimal(mainBox.boxNum)).
                        divide(new BigDecimal(1000)).
                        setScale(4, 4);
                if(skuMap.containsKey(cbmKey)) skuMap.put(cbmKey, skuMap.get(cbmKey).add(cmb));
                else skuMap.put(cbmKey, cmb);
            }

        }
    }

}
