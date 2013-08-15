package models.market;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import helper.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.cache.Cache;
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

    private static final long serialVersionUID = 897305328219999830L;

    public SellingRecord(Selling sell, Date date) {
        this.id = SellingRecord.id(sell.sellingId, date);
        this.selling = sell;
        this.account = sell.account;
        this.market = this.selling.market;
        this.date = date;
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
     * 当天产生的 Cancel 的订单数量(计算系统数据)
     */
    @Expose
    public Integer orderCanceld = 0;

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
     * 实际收入 = 销售额(包括 shipping fee) - amazon 扣费 (包括 shipping refuned)
     */
    public float income = 0;

    /**
     * 采购成本
     */
    public float procureCost = 0;

    /**
     * 采购到目前为止, 总共采购的数量
     */
    public int procureNumberSum = 0;

    /**
     * 运输成本
     */
    public float shipCost = 0;

    /**
     * 运输到目前为之, 总共运输的数量
     */
    public int shipNumberSum = 0;

    /**
     * 利润 = 销售额 - 采购成本 - 运输成本
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
    @Temporal(TemporalType.DATE)
    @Expose
    public Date date = new Date();

    public void updateAttr(SellingRecord nsrd) {
        if(!StringUtils.equals(this.id, nsrd.id))
            throw new FastRuntimeException("不相同的 SellingReocrd 不能进行更新!");
        if(nsrd.units != null && nsrd.units > 0) this.units = nsrd.units;
        if(nsrd.orders != null && nsrd.orders > 0) this.orders = nsrd.orders;
        if(nsrd.orderCanceld != null && nsrd.orderCanceld > 0)
            this.orderCanceld = nsrd.orderCanceld;
        if(nsrd.sales > 0) this.sales = nsrd.sales;
        if(nsrd.salePrice != null && nsrd.salePrice > 0) this.salePrice = nsrd.salePrice;
        if(nsrd.pageViews != null && nsrd.pageViews > 0) this.pageViews = nsrd.pageViews;
        if(nsrd.sessions != null && nsrd.sessions > 0) this.sessions = nsrd.sessions;
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
                        // 无论数据库中存在不存在都需要更新下面数据
                        record.sessions = Webs.amazonPriceNumber(M.AMAZON_UK, rowArr.get(4).getAsString()).intValue();
                        record.pageViews = Webs.amazonPriceNumber(M.AMAZON_UK, rowArr.get(6).getAsString()).intValue();

                        records.add(record);
                    } catch(Exception e) {
                        Logger.warn("SellingRecord.newRecordFromAmazonBusinessReports (%s)", Webs.E(e));
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
                    groupByDate.get(key).orderCanceld += rcd.orderCanceld;
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
     *
     * @param msku
     * @param from
     * @param to
     * @return
     */
    public static Map<String, ArrayList<F.T2<Long, Float>>> ajaxHighChartPVAndSS(String msku, Account acc, Date from,
                                                                                 Date to) {
        /**
         * 格式 map[lineName, datas]
         * datas -> [
         * [1262304000000, 29.9],
         * [1282304000000, 99.9]
         * ]
         */
        Map<String, ArrayList<F.T2<Long, Float>>> highCharLines = GTs.MapBuilder
                .map("pv_uk", new ArrayList<F.T2<Long, Float>>())
                .put("ss_uk", new ArrayList<F.T2<Long, Float>>())
                .put("pv_de", new ArrayList<F.T2<Long, Float>>())
                .put("ss_de", new ArrayList<F.T2<Long, Float>>())
                .put("pv_fr", new ArrayList<F.T2<Long, Float>>())
                .put("ss_fr", new ArrayList<F.T2<Long, Float>>())
                .put("pv_us", new ArrayList<F.T2<Long, Float>>())
                .put("ss_us", new ArrayList<F.T2<Long, Float>>())
                .build();

        List<SellingRecord> records = SellingRecord.accountMskuRelateRecords(acc, msku, from, to);
        for(SellingRecord rcd : records) {
            if(rcd.market == M.AMAZON_UK) {
                highCharLines.get("pv_uk").add(new F.T2<Long, Float>(rcd.date.getTime(), rcd.pageViews.floatValue()));
                highCharLines.get("ss_uk").add(new F.T2<Long, Float>(rcd.date.getTime(), rcd.sessions.floatValue()));
            } else if(rcd.market == M.AMAZON_DE) {
                highCharLines.get("pv_de").add(new F.T2<Long, Float>(rcd.date.getTime(), rcd.pageViews.floatValue()));
                highCharLines.get("ss_de").add(new F.T2<Long, Float>(rcd.date.getTime(), rcd.sessions.floatValue()));
            } else if(rcd.market == M.AMAZON_FR) {
                highCharLines.get("pv_fr").add(new F.T2<Long, Float>(rcd.date.getTime(), rcd.pageViews.floatValue()));
                highCharLines.get("ss_fr").add(new F.T2<Long, Float>(rcd.date.getTime(), rcd.sessions.floatValue()));
            } else if(rcd.market == M.AMAZON_US) {
                highCharLines.get("pv_us").add(new F.T2<Long, Float>(rcd.date.getTime(), rcd.pageViews.floatValue()));
                highCharLines.get("ss_us").add(new F.T2<Long, Float>(rcd.date.getTime(), rcd.sessions.floatValue()));
            } else {
                Logger.info("Skip one Market %s.", rcd.market);
            }
        }
        return highCharLines;
    }


    /**
     * 加载一段时间内指定 Selling 的转换率曲线数据, 给前台的 HighChar 使用
     *
     * @return
     */
    public static Map<String, ArrayList<F.T2<Long, Float>>> ajaxHighChartTurnRatio(String msku, Account acc, Date from,
                                                                                   Date to) {
        Map<String, ArrayList<F.T2<Long, Float>>> highCharLines = GTs.MapBuilder
                .map("tn_uk", new ArrayList<F.T2<Long, Float>>())
                .put("tn_de", new ArrayList<F.T2<Long, Float>>())
                .put("tn_fr", new ArrayList<F.T2<Long, Float>>())
                .put("tn_us", new ArrayList<F.T2<Long, Float>>())
                .build();
        List<SellingRecord> records = SellingRecord.accountMskuRelateRecords(acc, msku, from, to);
        for(SellingRecord rcd : records) {
            float turnRatio = Webs
                    .scalePointUp(3, (float) rcd.orders / (rcd.sessions == 0 ? 1 : rcd.sessions));
            if(rcd.sessions <= 0) turnRatio = 0f;
            if(rcd.market == M.AMAZON_UK)
                highCharLines.get("tn_uk").add(new F.T2<Long, Float>(rcd.date.getTime(), turnRatio));
            else if(rcd.market == M.AMAZON_DE)
                highCharLines.get("tn_de").add(new F.T2<Long, Float>(rcd.date.getTime(), turnRatio));
            else if(rcd.market == M.AMAZON_FR)
                highCharLines.get("tn_fr").add(new F.T2<Long, Float>(rcd.date.getTime(), turnRatio));
            else if(rcd.market == M.AMAZON_US)
                highCharLines.get("tn_us").add(new F.T2<Long, Float>(rcd.date.getTime(), turnRatio));
            else
                Logger.info("Skip One Makret %s.", rcd.market);
        }
        return highCharLines;
    }

    /**
     * 产生当天的 Id
     *
     * @param sid
     * @return
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
            record.orderCanceld = (int) Orderr.count("state=? AND createDate=?", Orderr.S.CANCEL, record.date);
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
}
