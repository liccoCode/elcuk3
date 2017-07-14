package models.view.dto;

import helper.DBUtils;
import models.market.M;
import play.db.helper.SqlSelect;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 16/3/18
 * Time: 3:08 PM
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
        String sql = "SELECT r.orderId,r.market,group_concat(DISTINCT f.product_sku) AS sku,"
                + "DATE_FORMAT(r.paymentDate,'%Y-%m-%d %H:%i:%s') AS paymentDate,"
                + "ROUND(sum(IF(f.usdCost>0, f.usdCost, 0)), 2) AS positivePrice, "
                + "ROUND(sum(IF(f.usdCost<0, f.usdCost, 0)), 2)  AS negativePrice "
                + "FROM Orderr r LEFT JOIN SaleFee f ON f.order_orderId = r.orderId "
                + "WHERE r.orderId IN " + SqlSelect.inlineParam(orderIds)
                + "GROUP BY r.orderId";
        List<Map<String, Object>> rows = DBUtils.rows(sql);
        List<OrderReportDTO> dtos = new ArrayList<>();
        for(Map<String, Object> row : rows) {
            OrderReportDTO dto = new OrderReportDTO();
            dto.orderId = row.get("orderId").toString();
            dto.sku = getNullReturn(row.get("sku"));
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

    public static String getNullReturn(Object obj) {
        if(obj == null) {
            return "";
        } else {
            return obj.toString();
        }
    }


}
