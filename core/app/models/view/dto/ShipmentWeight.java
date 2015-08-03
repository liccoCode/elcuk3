package models.view.dto;

import helper.DBUtils;
import models.finance.PaymentUnit;
import models.market.M;
import models.procure.Shipment;
import models.product.Category;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;

import java.util.*;

/**
 * 用来生成 物流运输重量 Excel 文档的 Model
 * User: mac
 * Date: 14-9-22
 * Time: PM5:29
 */
public class ShipmentWeight {
    public Date from;
    public Date to;
    public M market;
    public Shipment.T shipType;
    public String categoryId;
    public String sku;
    public String m;

    public HashMap<String, Float> weights = new HashMap<String, Float>();

    public ShipmentWeight() {
        DateTime now = DateTime.now();
        this.from = now.minusMonths(1).toDate();
        this.to = now.toDate();
    }

    public SqlSelect buildSql() {
        List<String> skus = Category.getSKUs(this.categoryId);
        if(this.sku != null && StringUtils.isNotBlank(this.sku)) skus.add(this.sku);
        SqlSelect sql = new SqlSelect()
                .select("s.type as shipType, " +
                        "pro.sku as sku, " +
                        "pro.category_categoryId as categoryId, " +
                        "w.name as name, " +
                        "SUM(CASE WHEN pro.weight IS NULL THEN 0 * si.qty WHEN pro.weight >= 0 THEN pro.weight * si.qty END) as weight ")
                .from("ShipItem si")
                .leftJoin("Shipment s ON si.shipment_id=s.id")
                .leftJoin("ProcureUnit pu ON si.unit_id=pu.id")
                .leftJoin("Product pro on pu.product_sku = pro.sku")
                .leftJoin("Whouse w ON w.id=s.whouse_id")
                .where("s.planBeginDate>=?").param(this.from)
                .andWhere("s.planBeginDate<=?").param(this.to);
        if(this.market != null) sql.andWhere("w.name=?").param(this.market.marketAndWhouseMapping());
        if(this.shipType != null) sql.andWhere("s.type=?").param(this.shipType.toString());
        if(skus.size() > 0) sql.andWhere("pro.sku IN " + SqlSelect.inlineParam(skus));
        sql.groupBy("pro.sku, s.type, w.name");
        sql.orderBy("pro.category_categoryId ASC");
        return sql;
    }

    public Map<String, ShipmentWeight> query() {
        Map<String, ShipmentWeight> sws = new HashMap<String, ShipmentWeight>();
        SqlSelect sql = buildSql();
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());

        for(Map<String, Object> row : rows) {
            String row_sku = row.get("sku").toString();
            String row_name = row.get("name").toString().split("_")[1];
            String key = String.format("%s-%s", row_sku, row_name);
            if(key != null && StringUtils.isNotBlank(key)) {
                if(sws.containsKey(key)) {
                    ShipmentWeight sp = sws.get(key);
                    sp.weights.put(row.get("shipType").toString(),
                            NumberUtils.toFloat(row.get("weight").toString()));
                } else {
                    ShipmentWeight sp = new ShipmentWeight();
                    sp.categoryId = row.get("categoryId").toString();
                    sp.sku = row_sku;
                    sp.m = row_name;
                    sp.weights.put(row.get("shipType").toString(),
                            NumberUtils.toFloat(row.get("weight").toString()));
                    sws.put(key, sp);
                }
            }
        }
        return sortByKeys(sws);
    }

    public Map<String, ShipmentWeight> sortByKeys(Map<String, ShipmentWeight> map) {
        List<String> keys = new LinkedList<String>(map.keySet());
        Collections.sort(keys);
        Map<String, ShipmentWeight> sortedMap = new LinkedHashMap<String, ShipmentWeight>();
        for(String key : keys) {
            sortedMap.put(key, map.get(key));
        }
        return sortedMap;
    }

    public List<Shipment> queryShipmentCostAndWeight() {
        List<String> skus = Category.getSKUs(this.categoryId);
        if(this.sku != null && StringUtils.isNotBlank(this.sku)) skus.add(this.sku);
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT s FROM Shipment s LEFT JOIN s.whouse w " +
                " LEFT JOIN s.fees f" +
                " LEFT JOIN s.items i" +
                " LEFT JOIN i.unit u" +
                " LEFT JOIN u.product p " +
                " WHERE s.dates.beginDate>=? AND s.dates.beginDate<=? ");
        params.add(this.from);
        params.add(this.to);
        if(this.market != null) {
            sql.append(" AND w.name=?");
            params.add(this.market.marketAndWhouseMapping());
        }
        if(this.shipType != null) {
            sql.append(" AND s.type=?");
            params.add(this.shipType.toString());
        }
        if(skus.size() > 0) {
            sql.append(" AND p.sku IN " + SqlSelect.inlineParam(skus));
        }
        List<Shipment> list = Shipment.find(sql.toString(), params.toArray()).fetch();
        for(Shipment shipment : list) {
            for(PaymentUnit paymentUnit : shipment.fees) {
                paymentUnit.unitPrice = paymentUnit.currency.toUSD(paymentUnit.unitPrice);

            }
        }
        return list;
    }
}
