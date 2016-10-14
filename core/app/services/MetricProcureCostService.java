package services;

import helper.Currency;
import helper.DBUtils;
import helper.Dates;
import models.market.Selling;
import models.market.SellingRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 10/12/13
 * Time: 2:18 PM
 */
public class MetricProcureCostService {
    /**
     * 某一个 Selling 的采购成本(发货时间与指定日期相同); 币种统一为 USD
     */
    public F.T2<Float, Integer> sellingProcreCost(Selling selling, Date date) {
        /**
         * 1. 确定昨天的采购成本与到昨天为之的所有采购数量
         * 2. 从今天的所有采购单中寻找, 找出今天 selling 采购的所有数量与各自的单价(统一币种为 USD)
         * 3. 根据 1, 2 通过计算出 总价格 / 总数量 得出今天的平均单价
         */
        DateTime oneDay = new DateTime(date);
        SellingRecord record = SellingRecord.oneDay(selling.sellingId, oneDay.minusDays(1).toDate());

        SqlSelect sql = new SqlSelect()
                .select("currency", "sum(qty) as qty", "sum(price * qty) as cost")
                .from("ProcureUnit")
                .where("selling_sellingId=?").param(selling.sellingId)
                .where("deliveryDate>=?").param(Dates.morning(date))
                .where("deliveryDate<=?").param(Dates.night(date))
                .groupBy("currency");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());

        float toDayTotalProcureCost = 0;
        int toDayProcureNumberSum = 0;
        for(Map<String, Object> row : rows) {
            if(row.get("currency") == null) continue;
            Currency currency = Currency.valueOf(row.get("currency").toString());
            toDayTotalProcureCost += currency.toUSD(NumberUtils.toFloat(row.get("cost").toString()));
            toDayProcureNumberSum += NumberUtils.toInt(row.get("qty").toString());
        }

        float toDayProcureCost = 0;
        int procureNumberSum = record.procureNumberSum + toDayProcureNumberSum;
        if(procureNumberSum != 0) {
            toDayProcureCost = (record.procureCost * record.procureNumberSum + toDayTotalProcureCost) / procureNumberSum;
        }
        return new F.T2<>(toDayProcureCost, procureNumberSum);
    }
}
