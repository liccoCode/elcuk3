package models;

import helper.DBUtils;
import helper.Dates;
import models.market.M;
import org.apache.commons.lang3.tuple.ImmutablePair;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

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
     * Reserved数量
     */
    public Integer reservedQty;

    /**
     * 主键 ID
     */
    @Id
    public String id;

    /**
     * 日期(任务执行时月份的最后一天)
     */
    public Date date;

    /**
     * 报表显示冗余字段,不做数据库映射
     */
    @Transient
    public Float transportPriceTemp;
    public Float taxPriceTemp;


    public static List<Map<String, Object>> countByCategory(Date target) {
        String sql = "SELECT categoryId, `date`,"
                + " SUM(reservedQty) AS reservedQty,"
                + " SUM(productionQty) AS productionQty,"
                + " SUM(productionCost) AS productionCost,"
                + " SUM(transitQty) AS transitQty,"
                + " SUM(transitCost) AS transitCost,"
                + " SUM(stockQty) AS stockQty,"
                + " SUM(stockCost) AS stockCost "
                + " FROM("
                + "  SELECT `date`, categoryId,"
                + "  SUM(reservedQty) AS reservedQty,"
                + "  SUM(productionQty) AS productionQty,"
                + "  SUM(internalStockCost) AS productionCost,"
                + "  SUM(transitQty) AS transitQty,"
                + "  SUM(transitQty*procurementPrice+transitQty*transportPrice) AS transitCost,"
                + "  SUM(stockQty) AS stockQty,"
                + "  SUM(stockQty*procurementPrice+stockQty*transportPrice+stockQty*taxPrice) AS stockCost"
                + "  FROM InventoryCostUnit"
                + "  WHERE date BETWEEN ? AND ?"
                + "  GROUP BY sku"
                + ") AS TMP GROUP BY categoryId;";
        return DBUtils.rows(sql.toString(), Dates.morning(Dates.monthBegin(target)), Dates.monthEnd(target));
    }


    public static ImmutablePair<List<InventoryCostUnit>, List<Map<String, Object>>> countPrice(
            List<InventoryCostUnit> units, List<Map<String, Object>> summaries) {
        List<M> marketlist = Arrays.asList(M.AMAZON_DE, M.AMAZON_FR, M.AMAZON_UK, M.AMAZON_IT, M.AMAZON_ES);

        Map<String, BigDecimal> totalMap = new HashMap<>();
        //key: categoryId-sku-market  value: taxPrice
        Map<String, Float> priceMap = new HashMap<>();

        /** 1 循环明细集合,将 categoryId-sku-market : vat费用 存入map **/
        for(InventoryCostUnit unit : units) {
            if(unit.market != null) {
                priceMap.put(String.format("taxPrice-%s-%s-%s", unit.categoryId, unit.sku, unit.market.name()),
                        unit.taxPrice);
                priceMap.put(String.format("transportPrice-%s-%s-%s", unit.categoryId, unit.sku, unit.market.name()),
                        unit.transportPrice);
                unit.transportPriceTemp = unit.transportPrice;
                unit.taxPriceTemp = unit.taxPrice;
            }
        }

        /** 2 循环明细集合,处理 欧洲五国vat费用为0的数据并按 品线汇总 存入map **/
        for(InventoryCostUnit unit : units) {
            if(unit.market != null) {
                if(unit.taxPrice == 0 && marketlist.contains(unit.market)) {
                    unit.taxPriceTemp = getTaxPrice(priceMap, unit.categoryId, unit.sku, marketlist);
                }
                if(unit.transportPrice == 0 && marketlist.contains(unit.market)) {
                    unit.transportPriceTemp = getTransportPrice(priceMap, unit.categoryId, unit.sku, marketlist);
                }
                setTotalMap(unit, totalMap);
            }
        }

        /** 3 汇总结果 匹配计算好的totalMap**/
        for(Map<String, Object> map : summaries) {
            map.put("stockCost", totalMap.get(String.format("%s-%s", map.get("categoryId"), "stockCost")));
            map.put("reservedCost", totalMap.get(String.format("%s-%s", map.get("categoryId"), "reservedCost")));
            map.put("transitCost", totalMap.get(String.format("%s-%s", map.get("categoryId"), "transitCost")));
        }

        return new ImmutablePair<>(units, summaries);
    }


    public static Float getTaxPrice(Map<String, Float> priceMap, String categoryId, String sku, List<M> marketlist) {
        Float taxPrice = 0f;
        for(M market : marketlist) {
            taxPrice = priceMap.get(String.format("taxPrice-%s-%s-%s", categoryId, sku, market.name()));
            if(taxPrice != null && taxPrice > 0) {
                break;
            }
        }
        if(taxPrice == null) return 0F;
        return taxPrice;
    }

    public static Float getTransportPrice(Map<String, Float> priceMap, String categoryId, String sku,
                                          List<M> marketlist) {
        Float transportPrice = 0f;
        for(M market : marketlist) {
            transportPrice = priceMap.get(String.format("transportPrice-%s-%s-%s", categoryId, sku, market.name()));
            if(transportPrice != null && transportPrice > 0) {
                break;
            }
        }
        if(transportPrice == null) return 0F;
        return transportPrice;
    }

    /**
     * 匹配计算 制作中、已交货成本
     *
     * @param unit
     * @param totalMap
     */
    public static void setTotalMap(InventoryCostUnit unit, Map<String, BigDecimal> totalMap) {
        BigDecimal price = new BigDecimal(unit.procurementPrice).add(new BigDecimal(unit.transportPriceTemp)).add(new
                BigDecimal(unit.taxPriceTemp));
        BigDecimal taxPrice = price.multiply(new BigDecimal(unit.stockQty));
        BigDecimal reservedPrice = price.multiply(new BigDecimal(unit.reservedQty));

        BigDecimal transitCost = new BigDecimal(unit.procurementPrice).add(new BigDecimal(unit.transportPriceTemp))
                .multiply(new BigDecimal(unit.transitQty));

        String taxPriceKey = String.format("%s-%s", unit.categoryId, "stockCost");
        String reservedPriceKey = String.format("%s-%s", unit.categoryId, "reservedCost");
        String transitCostKey = String.format("%s-%s", unit.categoryId, "transitCost");

        if(totalMap.containsKey(taxPriceKey) && totalMap.containsKey(reservedPriceKey)) {
            totalMap.put(taxPriceKey, totalMap.get(taxPriceKey).add(taxPrice));
            totalMap.put(reservedPriceKey, totalMap.get(reservedPriceKey).add(reservedPrice));
            totalMap.put(transitCostKey, totalMap.get(transitCostKey).add(transitCost));
        } else {
            totalMap.put(taxPriceKey, taxPrice);
            totalMap.put(reservedPriceKey, reservedPrice);
            totalMap.put(transitCostKey, transitCost);
        }
    }


}
