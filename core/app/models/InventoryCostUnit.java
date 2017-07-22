package models;

import helper.DBUtils;
import helper.Dates;
import models.market.M;
import play.db.jpa.GenericModel;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 3/18/17
 * Time: 3:17 PM
 */
@Entity
public class InventoryCostUnit extends GenericModel {
    /**
     * 产品
     */
    public String sku;

    /**
     * 产品线
     */
    public String categoryId;

    /**
     * 市场
     */
    @Enumerated(EnumType.STRING)
    public M market;

    /**
     * 采购单价
     */
    public Float procurementPrice;

    /**
     * 运输单价
     */
    public Float transportPrice;

    /**
     * 缴税单价(VAT、关税)
     */
    public Float taxPrice;

    /**
     * 在途数量
     */
    public Integer transitQty;

    /**
     * 在库数量(入库中+在库)
     */
    public Integer stockQty;

    /**
     * 生产中的数量(制作中+已交货)
     */
    public Integer productionQty;

    /**
     * 主键 ID
     */
    @Id
    public String id;

    /**
     * 日期(任务执行时月份的最后一天)
     */
    public Date date;

    public static List<Map<String, Object>> countByCategory(Date target) {
        String sql = "SELECT categoryId, `date`,"
                + " SUM(productionQty) AS productionQty,"
                + " SUM(productionCost) AS productionCost,"
                + " SUM(transitQty) AS transitQty,"
                + " SUM(transitCost) AS transitCost,"
                + " SUM(stockQty) AS stockQty,"
                + " SUM(stockCost) AS stockCost "
                + " FROM("
                + "  SELECT `date`, categoryId,"
                + "  productionQty AS productionQty,"
                + "  productionQty*procurementPrice AS productionCost,"
                + "  SUM(transitQty) AS transitQty,"
                + "  SUM(transitQty*procurementPrice+transitQty*transportPrice) AS transitCost,"
                + "  SUM(stockQty) AS stockQty,"
                + "  SUM(stockQty*procurementPrice+stockQty*transportPrice+stockQty*taxPrice) AS stockCost"
                + "  FROM InventoryCostUnit"
                + "  WHERE date BETWEEN ? AND ?"
                + "  GROUP BY sku"
                + ") AS TMP GROUP BY categoryId;";
        return DBUtils.rows(sql, Dates.monthBegin(target), Dates.monthEnd(target));
    }
}
