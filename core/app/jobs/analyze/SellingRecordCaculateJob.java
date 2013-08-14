package jobs.analyze;

import helper.DBUtils;
import helper.Dates;
import models.finance.FeeType;
import models.market.Selling;
import models.market.SellingRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.jobs.Job;

import java.util.*;

/**
 * 周期:
 * 轮询: 7, 15, 23 三个时间点执行三次
 * 每天用于计算 SellingRecord 的后台任务.
 * <p/>
 * 每天需要计算三次, 但由于数据越到后面越准确, 所以一天内的后一次计算可以覆盖前一次计算的结果
 * User: wyatt
 * Date: 8/14/13
 * Time: 4:54 PM
 */
public class SellingRecordCaculateJob extends Job {
    @Override
    public void doJob() {
        DateTime now = DateTime.now();
        Map<String, Integer> sellingUnits = sellingUnits(now.toDate());
        Map<String, Float> sellingSales = sellingSales(now.toDate());
        Map<String, Float> sellingIncome = sellingIncome(now.toDate());

        List<Selling> sellings = Selling.findAll();
        for(Selling selling : sellings) {
            String sid = selling.sellingId;
            SellingRecord record = SellingRecord.today(sid);
            /**
             * 1. 销量
             * 2. 销售额
             * 3. 实际收入
             *
             * 4. 采购成本
             * 5. 运输成本
             * 6. 利润
             * 7. 销售利润率
             * 8. 历史总销售额
             * 9. 历史总利润
             */
            record.units = sellingUnits.get(sid) == null ? 0 : sellingUnits.get(sid);
            record.sales = sellingSales.get(sid) == null ? 0 : sellingSales.get(sid);
            record.income = sellingIncome.get(sid) == null ? 0 : sellingIncome.get(sid);

        }
    }

    // -------------------- 这里是根据业务, 将 SellingRecord 的计算方法全部组织到了当前这个类 ------------------

    /**
     * Selling 的销量数据
     *
     * @return
     */
    public Map<String, Integer> sellingUnits(Date date) {
        SqlSelect sql = new SqlSelect()
                .select("selling_sellingId as sellingId", "sum(quantity) as qty")
                .from("OrderItem")
                .where("createDate>=?").param(Dates.morning(date))
                .where("createDate<=?").param(Dates.night(date))
                .groupBy("sellingId");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        Map<String, Integer> sellingUnits = new HashMap<String, Integer>();
        for(Map<String, Object> row : rows) {
            String sellingId = row.get("sellingId").toString();
            if(StringUtils.isBlank(sellingId)) continue;
            sellingUnits.put(sellingId, NumberUtils.toInt(row.get("qty").toString()));
        }
        return sellingUnits;
    }

    /**
     * Selling 的销售额数据
     *
     * @return
     */
    public Map<String, Float> sellingSales(Date date) {
        /**
         * 1. 找到某天 OrderItem 中所有涉及的 Selling 与每个 Selling 涉及的 Order.id
         * 2. 根据每个 selling 所涉及的 id 与费用类型, 计算处每个 Selling 的销售额
         */
        return sellingFeeTypesCost(date, Arrays.asList("productcharges", "shipping"));
    }

    /**
     * Selling 的实际收入
     *
     * @param date
     * @return
     */
    public Map<String, Float> sellingIncome(Date date) {
        List<FeeType> fees = FeeType.amazon().children;
        List<String> feesTypeName = new ArrayList<String>();
        for(FeeType fee : fees) {
            if("shipping".equals(fee.name)) continue;
            feesTypeName.add(fee.name);
        }
        return sellingFeeTypesCost(date, feesTypeName);
    }

    /**
     * 指定 amazon 费用类型, 返回当天所有 Selling 这些费用类型的总费用
     *
     * @param date
     * @param feeTypes
     * @return
     */
    public Map<String, Float> sellingFeeTypesCost(Date date, List<String> feeTypes) {
        Map<String, List<String>> sellingOrders = oneDaySellingOrderIds(date);
        SqlSelect sellFeesTemplate = new SqlSelect()
                .select("sum(usdCost) as cost")
                .from("SaleFee")
                        // 需要统计 productcharges 销售价格, 和 shipping 加快快递(这个会在 amazon 中减去)
                .where(SqlSelect.whereIn("type_name", feeTypes));
        Map<String, Float> sellingSales = new HashMap<String, Float>();
        for(String sellingId : sellingOrders.keySet()) {
            SqlSelect sellFees = new SqlSelect(sellFeesTemplate)
                    .where(SqlSelect.whereIn("orderId", sellingOrders.get(sellingId)));
            Map<String, Object> row = DBUtils.row(sellFees.toString());
            sellingSales.put(sellingId, NumberUtils.toFloat(row.get("cost").toString()));
        }
        return sellingSales;
    }

    /**
     * 查询某天销售中, 每个 Selling 所涉及的 OrderId 是哪些
     *
     * @return
     */
    public Map<String, List<String>> oneDaySellingOrderIds(Date date) {
        // 设置 group_concat_max_len 最大为 20M
        DBUtils.row("set group_concat_max_len=20971520");
        SqlSelect sellingOdsSql = new SqlSelect()
                .select("selling_sellingId as sellingId", "group_concat(order_orderId) as orderIds")
                .from("OrderItem")
                .where("createDate>=?").param(Dates.morning(date))
                .where("createDate<=?").param(Dates.night(date))
                .groupBy("sellingId");

        Map<String, List<String>> sellingOrders = new HashMap<String, List<String>>();
        List<Map<String, Object>> rows = DBUtils.rows(sellingOdsSql.toString(), sellingOdsSql.getParams().toArray());
        for(Map<String, Object> row : rows) {
            String sellingId = row.get("sellingId").toString();
            if(StringUtils.isBlank(sellingId)) continue;
            sellingOrders.put(sellingId, Arrays.asList(StringUtils.split(row.get("orderIds").toString(), ",")));
        }
        return sellingOrders;
    }
}
