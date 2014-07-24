package models.market;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import helper.*;
import models.view.highchart.HighChart;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.*;

/**
 * 每个 Selling 需要每天记录的信息
 * User: wyattpan
 * Date: 5/27/12
 * Time: 6:40 PM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class SellingRecord extends GenericModel {

    @Transient
    private static final long serialVersionUID = 897305328219999830L;

    public SellingRecord(Selling sell, Date date) {
        this.id = SellingRecord.id(sell.sellingId, date);
        this.selling = sell;
        this.account = sell.account;
        this.market = this.selling.market;
        this.date = date;
    }

    /**
     * 仅仅用于 SellingRecordPost
     */
    public SellingRecord(String sellingId) {
        this.selling = new Selling();
        this.selling.sellingId = sellingId;
    }

    /**
     * 用于封装 快递/海运/空运 费用统计成为运输成本用.
     */
    public SellingRecord(float expressCost, float seaCost, float airCost) {
        this.seaCost = seaCost;
        this.expressCost = expressCost;
        this.airCost = airCost;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Selling selling;

    @ManyToOne(fetch = FetchType.LAZY)
    public Account account;

    @Enumerated(EnumType.STRING)
    @Expose
    public M market;

    /**
     * hash(sid_date) 值, 为 ID
     */
    @Id
    @Column(length = 40)
    public String id;

    /**
     * 每天访问的 Session 数量(通过访问 Amazon 解析)
     */
    @Expose
    public Integer sessions = 0;

    /**
     * 每天的页面访问数量(通过访问 Amazon 解析)
     */
    @Expose
    public Integer pageViews = 0;


    /**
     * 当天产生的订单数量(计算系统数据)
     */
    @Expose
    public Integer orders = 0;

    /**
     * 当天的销售价格(通过 Amazon 解析)
     */
    @Expose
    public Float salePrice = 0f;

    /**
     * 当天产生的销量产品数量(计算系统数据)
     */
    @Expose
    public Integer units = 0;

    // TODO: 所有与金钱相关的, 币种统一为 USD (按照当天计算时的 google 财经汇率计算)

    /**
     * 销售额(通过系统计算)
     */
    public float sales = 0;

    /**
     * 实际收入 = 单个销售额(包括 shipping fee) - amazon 扣费 (包括 shipping refuned)
     */
    public float income = 0;

    /**
     * Amazon 所有收取的费用
     */
    public float amzFee = 0;

    /**
     * Amazon FBA 收取的费用
     */
    public float fbaFee = 0;

    /**
     * (单个产品)采购成本
     */
    public float procureCost = 0;

    /**
     * 采购到目前为止, 总共采购的数量
     */
    public int procureNumberSum = 0;

    /**
     * 快递运输成本($ N/kg)
     */
    public float expressCost = 0;

    /**
     * 运输到目前为之, 总共的快递运输重量(kg)
     */
    @Deprecated
    public float expressKilogram = 0;

    /**
     * 空运的运输成本($ N/kg)
     */
    public float airCost = 0;

    /**
     * 到目前为之, 总共的空运运输重量(kg)
     */
    @Deprecated
    public float airKilogram = 0;

    /**
     * 海运运输成本($ N/m3)
     */
    public float seaCost = 0;

    /**
     * 到目前为止, 总共的海运运输体积(立方米)
     * //
     */
    @Deprecated
    public float seaCubicMeter = 0;

    /**
     * 产品的关税和VAT费用
     */
    public float dutyAndVAT = 0;

    /**
     * 单个利润 = 销售额 - 采购成本 - 运输成本
     */
    public float profit = 0;

    /**
     * 成本利润率 = 利润 / (采购成本 + 运输成本)
     */
    public float costProfitRatio = 0;

    /**
     * 销售利润率 = 利润 / 销售额
     */
    public float saleProfitRatio = 0;

    /**
     * 历史总销售额
     */
    public float totalSales = 0;

    /**
     * 历史总利润
     */
    public float totalProfit = 0;

    /**
     * 记录的时间
     */
    @Expose
    public Date date = new Date();

    /**
     * 使用平均值的方式对成本等进行均等化处理;
     * ps: 为什么不和采购成本计算一样, 使用长时间的平均值?
     * 答: 因为运输等成本的计算无法获取最细节的运输量, 都是根据体重,重量均等下来的.
     * 同时, 按照现在的算法进行如此的计算, 难度相当大.
     */
    public float mergeWithLatest(Float currentValue, String name) {
        Logger.info("%s - %s", this.selling.sellingId, name);
        if(currentValue == null) currentValue = 0f;
        SqlSelect lastValueSql = new SqlSelect()
                .select(name + " lastValue").from("SellingRecord")
                .where("selling_sellingId=?").param(this.selling.sellingId)
                .where("date<?").param(this.date)
                .where(name + ">0")
                .orderBy("date DESC")
                .limit(1);
        Map<String, Object> row = DBUtils.row(lastValueSql.toString(), lastValueSql.getParams().toArray());
        Object lastValueObj = row.get("lastValue");
        float finalValue = 0;
        float lastValue = lastValueObj == null ? 0 : NumberUtils.toFloat(lastValueObj.toString());
        if(currentValue <= 0.01 && lastValue <= 0.01) finalValue = 0; // 当前值为 0, 最后值也为 0 ,那么没得玩就是 0
        else if(currentValue <= 0.01 && lastValue > 0.01) finalValue = lastValue; // 当前值非常小, 最后值不为0 使用最后值
        else if(currentValue > 0.01 && lastValue <= 0.01) finalValue = currentValue; // 如果最后一个值非常小, 当前值大于 0 则使用当前值
        else finalValue = (currentValue + lastValue) / 2;// 正常使用 (当前值 + 最后值) / 2
        if(this.selling.sellingId.contains("80")) {
            Logger.info("[%s - Name: %s, current: %s, last: %s, final: %s]",
                    this.selling.sellingId, name, currentValue, lastValue, finalValue);
        }
        return finalValue;
    }

    /**
     * 将计算出的费用根据 units 数量换算成单个的费用
     */
    public float totalToSingle(Float totalFee) {
        if(this.units == null || this.units < 0) throw new FastRuntimeException("请先计算 units 的值");
        if(totalFee == null) return 0;
        if(this.units == 0) return 0;
        return totalFee / this.units;
    }

    /**
     * 根据已经计算好的 快递/空运/海运 运费, 用于估计计算物流成本
     *
     * @return
     */
    public float mergeToShipCost() {
        return (this.expressCost * 0.333f) + (this.seaCost * 0.333f) + (this.airCost * 0.333f);
    }

    /**
     * 统计采购物流的单个成本(包括 VAT)
     *
     * @return
     */
    public float procureAndShipCost() {
        // 物流 + VAT + 采购
        if(this.units == 0) return 0;
        return this.mergeToShipCost() + this.procureCost /*+this.dutyAndVAT dutyAndVAT 调整正确以后再进行统计*/;
    }

    /**
     * 计算单个成本利润率 = 利润 / (采购成本 + 运输成本 + VAT)
     */
    public float costProfitRatio() {
        if(this.procureAndShipCost() == 0) return 0;
        if(this.units == 0) return 0;
        return this.profit / this.procureAndShipCost();
    }

    /**
     * 单个销售利润率 = 利润 / 销售额
     */
    public float saleProfitRatio() {
        if(this.sales == 0) return 0;
        return this.profit / (this.sales / this.units);
    }


    /**
     * 通过 Amazon 的 BusinessReports 产生一组 SellingRecord, 以便更新或者是记录; 返回的 Selling 如果数据库中有则是持久化的, 数据库中没有则是新的
     * 记录 Selling 的 Price, Session, PageView
     * <p/>
     * 拥有自己的 market 参数是因为为了兼容原来的 Amazon UK 账号在 DE 销售, 而 UK/DE 的数据是分开的
     *
     * @return
     */
    public static Set<SellingRecord> newRecordFromAmazonBusinessReports(Account acc, M market, Date oneDay) {
        DateTime dt = new DateTime(oneDay).plusDays(1);
        F.T2<DateTime, DateTime> actualDatePair = market
                .withTimeZone(Dates.morning(oneDay), Dates.morning(dt.toDate()));

        Set<SellingRecord> records = new HashSet<SellingRecord>();
        JsonArray rows = null;
        int curentPage = 0;
        synchronized(acc.cookieStore()) {
            acc.changeRegion(market); // 在循环外面, 只改变一次, 并将 Cookie 同步住
            boolean hasNext = true;
            do {
                String url = acc.type.salesAndTrafficByAsinLink(oneDay, oneDay, curentPage++);/*需要根据 Account 的所在国家进行访问*/
                Logger.info("Fetch SellingRecord [%s]", url);
                String rtJson = HTTP.get(acc.cookieStore(), url);
                if(Play.mode.isDev())
                    FLog.fileLog(String.format("%s.%s.%s.raw.json", acc.prettyName(), market,
                            Dates.date2Date(oneDay)), rtJson, FLog.T.SELLINGRECORD);
                JsonObject data = null;
                try {
                    data = new JsonParser().parse(rtJson).getAsJsonObject().get("data")
                            .getAsJsonObject();
                    hasNext = data.get("hasNextPage").getAsInt() > 0;
                } catch(Exception e) {

                    LogUtils.JOBLOG.info("SellingRecordCheckJob:" + market.toString() + e.getMessage());

                    FLog.fileLog(String.format("%s.%s.%s.json", acc.prettyName(), market,
                            Dates.date2Date(oneDay)), rtJson, FLog.T.SELLINGRECORD);
                    return records;
                }
                rows = data.get("rows").getAsJsonArray();
                if(rows.size() == 0)
                    FLog.fileLog(String.format("%s.%s.%s.noresult.json", acc.prettyName(), market,
                            Dates.date2Date(oneDay)), rtJson, FLog.T.SELLINGRECORD);
                for(JsonElement row : rows) {
                    try {
                        JsonArray rowArr = row.getAsJsonArray();
                        String msku = StringUtils
                                .substringBetween(rowArr.get(3).getAsString(), "\">", "</")
                                .toUpperCase();
                        if(StringUtils.contains(msku, ",2")) continue;
                        String sid = Selling.sid(msku, market, acc);

                        SellingRecord record = SellingRecord.oneDay(sid, oneDay);


                        SqlSelect sql = new SqlSelect()
                                .select("oi.selling_sellingId as sellingId", "count(o.orderId) as qty")
                                .from("Orderr o")
                                .leftJoin("OrderItem oi ON o.orderId=oi.order_orderId")
                                .where("selling_sellingId=?").param(record.selling.sellingId)
                                .where("o.createDate>=?").param(actualDatePair._1.toDate())
                                .where("o.createDate<?").param(actualDatePair._2.toDate());
                        List<Map<String, Object>> sellingrow = DBUtils.rows(sql.toString(),
                                sql.getParams().toArray());
                        for(Map<String, Object> srow : sellingrow) {
                            Object sellingId = srow.get("sellingId");
                            if(sellingId != null && !StringUtils.isBlank(sellingId.toString())) {
                                record.orders = org.apache.commons.lang.math.NumberUtils
                                        .toInt(srow.get("qty").toString());
                            }
                        }

                        // 无论数据库中存在不存在都需要更新下面数据
                        record.sessions = Webs
                                .amazonPriceNumber(M.AMAZON_UK, rowArr.get(4).getAsString())
                                .intValue();
                        record.pageViews = Webs
                                .amazonPriceNumber(M.AMAZON_UK, rowArr.get(6).getAsString())
                                .intValue();

                        records.add(record);
                    } catch(Exception e) {

                        LogUtils.JOBLOG.info("SellingRecordCheckJob1:" + market.toString() + e.getMessage());

                        Logger.warn("SellingRecord.newRecordFromAmazonBusinessReports (%s)",
                                Webs.E(e));
                    }
                }
            } while(hasNext);
            acc.changeRegion(acc.type);
        }

        return records;
    }

    /**
     * 按照 Account, Msku 与时间来查询 SellingRecord
     *
     * @param acc
     * @param msku
     * @param from
     * @param to
     * @return
     */
    @SuppressWarnings("unchecked")
    @Cached("4h") // 具体的缓存统一到页面上,这里的缓存 5mn 用来防止多次加载
    public static List<SellingRecord> accountMskuRelateRecords(Account acc, String msku, Date from, Date to) {
        String cacheKey = Caches.Q.cacheKey(acc, msku, from, to);
        List<SellingRecord> cacheElement = Cache.get(cacheKey, List.class);
        if(cacheElement != null) return cacheElement;

        if(acc == null) {
            List<SellingRecord> dateMixRecords = SellingRecord
                    .find("selling.merchantSKU=? AND date>=? AND date<=? ORDER BY date", msku, from,
                            to).fetch();
            // 需要将相同 Date 不同 Market 的全部累计
            Map<String, SellingRecord> groupByDate = new LinkedHashMap<String, SellingRecord>();
            for(SellingRecord rcd : dateMixRecords) {
                String key = rcd.date.getTime() + "" + rcd.market;
                if(groupByDate.containsKey(key)) {
                    groupByDate.get(key).sessions += rcd.sessions;
                    groupByDate.get(key).pageViews += rcd.pageViews;
                    groupByDate.get(key).orders += rcd.orders;
                    groupByDate.get(key).sales += rcd.sales;
                    groupByDate.get(key).units += rcd.units;
                } else
                    groupByDate.put(key, rcd);
            }
            cacheElement = new ArrayList<SellingRecord>(groupByDate.values());
        } else {
            //因为对 Amazon 来说, 一个 Account 拥有相同 Msku 是不可能的, 所以没关系
            cacheElement = SellingRecord
                    .find("selling.merchantSKU=? AND account=? AND date>=? AND date<=? ORDER BY date",
                            msku, acc, from, to).fetch();
        }
        Cache.add(cacheKey, cacheElement, "4h");
        return cacheElement;
    }

    /**
     * 加载出一段时间内指定 Selling 的 PageView 与 Session 数据, 给前台的 HighChar 使用
     * // TODO 这个方 HighChar 是不是需要挪到 view package 中的 object 去?
     */
    public static HighChart ajaxHighChartPVAndSS(String msku, Account acc, Date from, Date to) {
        /**
         * 格式 map[lineName, datas]
         * datas -> [
         * [1262304000000, 29.9],
         * [1282304000000, 99.9]
         * ]
         */
        HighChart chart = new HighChart();

        List<SellingRecord> records = SellingRecord.accountMskuRelateRecords(acc, msku, from, to);
        for(SellingRecord rcd : records) {
            if(rcd.market == M.AMAZON_UK) {
                chart.series("PageView(uk)").add(rcd.date, rcd.pageViews.floatValue());
                chart.series("Session(uk)").add(rcd.date, rcd.sessions.floatValue());
            } else if(rcd.market == M.AMAZON_DE) {
                chart.series("PageView(de)").add(rcd.date, rcd.pageViews.floatValue());
                chart.series("Session(de)").add(rcd.date, rcd.sessions.floatValue());
            } else if(rcd.market == M.AMAZON_FR) {
                chart.series("PageView(fr)").add(rcd.date, rcd.pageViews.floatValue());
                chart.series("Session(fr)").add(rcd.date, rcd.sessions.floatValue());
            } else if(rcd.market == M.AMAZON_US) {
                chart.series("PageView(us)").add(rcd.date, rcd.pageViews.floatValue());
                chart.series("Session(us)").add(rcd.date, rcd.sessions.floatValue());
            } else if(rcd.market == M.AMAZON_JP) {
                chart.series("PageView(jp)").add(rcd.date, rcd.pageViews.floatValue());
                chart.series("Session(jp)").add(rcd.date, rcd.sessions.floatValue());
            } else if(rcd.market == M.AMAZON_IT) {
                chart.series("PageView(it)").add(rcd.date, rcd.pageViews.floatValue());
                chart.series("Session(it)").add(rcd.date, rcd.sessions.floatValue());
            } else if(rcd.market == M.AMAZON_ES) {
                chart.series("PageView(es)").add(rcd.date, rcd.pageViews.floatValue());
                chart.series("Session(es)").add(rcd.date, rcd.sessions.floatValue());
            } else {
                Logger.info("Skip one Market %s.", rcd.market);
            }
        }
        return chart;
    }


    /**
     * 加载一段时间内指定 Selling 的转换率曲线数据, 给前台的 HighChar 使用
     * // TODO 这个方 HighChar 是不是需要挪到 view package 中的 object 去?
     */
    public static HighChart ajaxHighChartTurnRatio(String msku, Account acc, Date from, Date to) {
        HighChart chart = new HighChart();
        List<SellingRecord> records = SellingRecord.accountMskuRelateRecords(acc, msku, from, to);
        for(SellingRecord rcd : records) {
            float turnRatio = Webs.scalePointUp(3, (float) rcd.orders / (rcd.sessions == 0 ? 1 : rcd.sessions));
            if(rcd.sessions <= 0) turnRatio = 0f;
            if(rcd.market == M.AMAZON_UK)
                chart.series("TurnRatio(uk)").add(rcd.date, turnRatio);
            else if(rcd.market == M.AMAZON_DE)
                chart.series("TurnRatio(de)").add(rcd.date, turnRatio);
            else if(rcd.market == M.AMAZON_FR)
                chart.series("TurnRatio(fr)").add(rcd.date, turnRatio);
            else if(rcd.market == M.AMAZON_US)
                chart.series("TurnRatio(us)").add(rcd.date, turnRatio);
            else if(rcd.market == M.AMAZON_IT)
                chart.series("TurnRatio(it)").add(rcd.date, turnRatio);
            else if(rcd.market == M.AMAZON_JP)
                chart.series("TurnRatio(jp)").add(rcd.date, turnRatio);
            else if(rcd.market == M.AMAZON_ES)
                chart.series("TurnRatio(es)").add(rcd.date, turnRatio);
            else
                Logger.info("Skip One Makret %s.", rcd.market);
        }
        return chart;
    }

    /**
     * 产生当天的 Id
     */
    public static String id(String sid) {
        return id(sid, DateTime.now().toDate());
    }

    /**
     * 产生指定天的 Id
     *
     * @param sid
     * @param time
     * @return
     */
    public static String id(String sid, Date time) {
        return DigestUtils.md5Hex(String.format("%s|%s", sid, Dates.date2Date(time)));
    }

    /**
     * 返回今天的 SellingRecord, 有则加载没有则创建
     *
     * @param sid
     * @return
     */
    public static SellingRecord today(String sid) {
        return oneDay(sid, new Date());
    }

    /**
     * 返回某一天的 SellingRecord, 有则加载, 没有则创建
     *
     * @param sid
     * @param oneDay
     * @return
     */
    public static SellingRecord oneDay(String sid, Date oneDay) {
        String srid = id(sid, oneDay);
        SellingRecord record = SellingRecord.findById(srid);
        if(record == null) {
            Selling selling = Selling.findById(sid);
            if(selling == null)
                throw new FastRuntimeException("系统中无 Selling: " + sid);
            record = new SellingRecord(selling, oneDay);
            record.selling = selling;
            record.account = selling.account;
            record.save();
        }
        return record;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        SellingRecord that = (SellingRecord) o;

        if(id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "SellingRecord{" +
                "selling=" + selling +
                ", account=" + account +
                ", market=" + market +
                ", id='" + id + '\'' +
                ", sessions=" + sessions +
                ", pageViews=" + pageViews +
                ", orders=" + orders +
                ", salePrice=" + salePrice +
                ", units=" + units +
                ", sales=" + sales +
                ", income=" + income +
                ", amzFee=" + amzFee +
                ", fbaFee=" + fbaFee +
                ", procureCost=" + procureCost +
                ", procureNumberSum=" + procureNumberSum +
                ", expressCost=" + expressCost +
                ", airCost=" + airCost +
                ", seaCost=" + seaCost +
                ", profit=" + profit +
                ", costProfitRatio=" + costProfitRatio +
                ", saleProfitRatio=" + saleProfitRatio +
                ", totalSales=" + totalSales +
                ", totalProfit=" + totalProfit +
                ", date=" + date +
                '}';
    }
}
