package models.procure;

import helper.DBUtils;
import models.product.Product;
import org.apache.commons.lang3.tuple.ImmutablePair;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import java.util.*;

/**
 * 供销存model
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2018/6/4
 * Time: 上午10:03
 */
@Entity
public class ProcureUnitAnalyze extends Model {

    private static final long serialVersionUID = -3979047647306162456L;

    @OneToOne(fetch = FetchType.LAZY)
    public Product product;

    public String categoryId;

    public int year;

    public int month;

    public int planQty;

    /**
     * 收货数
     */
    public int qty;

    /**
     * 入库数
     */
    public int inboundQty;

    /**
     * 不良品数
     */
    public int unqualifiedQty;

    /**
     * 深圳出库数
     */
    public int outQty;

    /**
     * 其它出库数
     */
    public int stockQty;

    /**
     * 仓库退货数
     */
    public int refundQty;

    /**
     * 销量
     */
    public int units;
    /**
     * 退货量
     */
    public int returnQty;

    public String market;

    /**
     * 采购成本
     */
    public Float averageProcurePrice;
    /**
     * 运输费用、成本
     */
    public Float averageShipPrice;
    /**
     * VAT费用、成本
     */
    public Float averageVATPrice;

    /**
     * 创建日期
     */
    public Date createDate;

    public static List<Map<String, Object>> getTotalPerMonth(int year) {
        List<String> columns = Arrays.asList("采购数", "收货数", "入库数", "不良品数", "深圳出库数", "其它出库数", "仓库退货数",
                "销量", "退货量", "采购成本", "运输费用、成本", "VAT费用、成本");
        ImmutablePair<String, List<Object>> immutablePair = sql(year);
        List<Map<String, Object>> totals = DBUtils.rows(immutablePair.getLeft(), immutablePair.getRight().toArray());
        for(int i = 0; i < totals.size(); i++) {
            Map<String, Object> totalMap = totals.get(i);
            totalMap.put("column", columns.get(i));
        }
        return totals;
    }

    public static ImmutablePair<String, List<Object>> sql(Integer year) {
        List<String> columns = Arrays.asList("planQty", "qty", "inboundQty", "unqualifiedQty", "outQty", "stockQty",
                "refundQty", "units", "returnQty", "averageProcurePrice", "averageShipPrice", "averageVATPrice");
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        for(int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            sql.append(monthSql(column));
            params.add(year);
            if(i + 1 < columns.size()) {
                sql.append(" UNION ALL ");
            }
        }
        return new ImmutablePair<>(sql.toString(), params);
    }

    private static String monthSql(String column) {
        StringBuilder sql = new StringBuilder(" select ");
        sql.append(" MAX( CASE month  WHEN 1 THEN data ELSE  0 END ) AS 'm1' ,");
        sql.append(" MAX( CASE month  WHEN 2 THEN data ELSE  0 END ) AS 'm2' ,");
        sql.append(" MAX( CASE month  WHEN 3 THEN data ELSE  0 END ) AS 'm3' ,");
        sql.append(" MAX( CASE month  WHEN 4 THEN data ELSE  0 END ) AS 'm4' ,");
        sql.append(" MAX( CASE month  WHEN 5 THEN data ELSE  0 END ) AS 'm5' ,");
        sql.append(" MAX( CASE month  WHEN 6 THEN data ELSE  0 END ) AS 'm6' ,");
        sql.append(" MAX( CASE month  WHEN 7 THEN data ELSE  0 END ) AS 'm7' ,");
        sql.append(" MAX( CASE month  WHEN 8 THEN data ELSE  0 END ) AS 'm8' ,");
        sql.append(" MAX( CASE month  WHEN 9 THEN data ELSE  0 END ) AS 'm9' ,");
        sql.append(" MAX( CASE month  WHEN 10 THEN data ELSE  0 END ) AS 'm10' ,");
        sql.append(" MAX( CASE month  WHEN 11 THEN data ELSE  0 END ) AS 'm11' ,");
        sql.append(" MAX( CASE month  WHEN 12 THEN data ELSE  0 END ) AS 'm12' ");
        sql.append(" from ( select ");
        if(Arrays.asList("averageProcurePrice", "averageShipPrice", "averageVATPrice").contains(column)) {
            sql.append(" avg");
        } else {
            sql.append(" sum");
        }
        sql.append("(");
        sql.append(column);
        sql.append(") data, month from ProcureUnitAnalyze t where t.year = ? group by t.month order by t.month) t ");
        return sql.toString();
    }

}
