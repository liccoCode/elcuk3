package models.view.dto;

import helper.DBUtils;
import models.market.M;
import play.db.helper.SqlSelect;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by licco on 16/3/18.
 */
public class OrderReportDTO {

    public String orderId;

    public M market;

    public String paymentDate;

    public String sku;

    public float positivePrice;

    public float negativePrice;

    public float percent;

    public static List<OrderReportDTO> query(Set<String> orderIds) {
        StringBuffer sql = new StringBuffer("SELECT r.orderId,r.market,group_concat(DISTINCT f.product_sku) AS sku,");
        sql.append("DATE_FORMAT(r.paymentDate,'%Y-%m-%d %H:%i:%s') AS paymentDate,");
        sql.append("ROUND(sum(IF(f.usdCost>0, f.usdCost, 0)), 2) AS positivePrice, ");
        sql.append("ROUND(sum(IF(f.usdCost<0, f.usdCost, 0)), 2)  AS negativePrice ");
        sql.append("FROM Orderr r LEFT JOIN SaleFee f ON f.order_orderId = r.orderId ");
        sql.append("WHERE r.orderId IN " + SqlSelect.inlineParam(orderIds));
        sql.append("GROUP BY r.orderId");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString());
        List<OrderReportDTO> dtos = new ArrayList<OrderReportDTO>();
        for(Map<String, Object> row : rows) {
            OrderReportDTO dto = new OrderReportDTO();
            dto.orderId = row.get("orderId").toString();
            dto.sku = row.get("sku").toString();
            dto.market = M.valueOf(row.get("market").toString());
            dto.paymentDate = row.get("paymentDate").toString();
            dto.positivePrice = Float.parseFloat(row.get("positivePrice").toString());
            dto.negativePrice = -Float.parseFloat(row.get("negativePrice").toString());
            if(dto.positivePrice == 0) {
                dto.percent = 0;
            } else {
                dto.percent = new BigDecimal(dto.negativePrice).multiply(new BigDecimal(100)).divide(
                        new BigDecimal(dto.positivePrice), 2).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            }
            dtos.add(dto);
        }
        return dtos;
    }


}
