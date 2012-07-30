package models.market;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import helper.*;
import helper.Currency;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;
import play.Logger;
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
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SellingRecord extends GenericModel {

    /**
     * 默认构造函数, 初始化数值
     */
    public SellingRecord() {
        this.pageViews = 0;
        this.sessions = 0;
        this.sales = 0f;
        this.units = 0;
        this.orders = 0;
        this.orderCanceld = 0;
        this.rating = 0f;
        this.reviewSize = 0;
        this.salePrice = 0f;
        this.usdSales = 0f;
        this.date = new Date();
    }

    public SellingRecord(String id, Selling sell, Date date) {
        this();
        this.id = id;
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
    public Account.M market;

    /**
     * hash(sellingId_account.id_market_date) 值, 为 ID
     */
    @Id
    @Column(length = 40)
    public String id;

    /**
     * 每天访问的 Session 数量
     */
    @Expose
    public Integer sessions;

    /**
     * 每天的页面访问数量
     */
    @Expose
    public Integer pageViews;


    /**
     * 当天产生的销量产品数量
     */
    @Expose
    public Integer units;

    /**
     * 当天产生的订单数量
     */
    @Expose
    public Integer orders;

    /**
     * 当天产生的 Cancel 的订单数量
     */
    @Expose
    public Integer orderCanceld;

    /**
     * 当天的销售价格
     */
    @Expose
    public Float salePrice;

    /**
     * 当天的 Review 评分
     */
    @Expose
    public Float rating;

    /**
     * 当天的 Review 的个数
     */
    @Expose
    public Integer reviewSize;

    /**
     * 销售额
     */
    public Float sales;
    @Enumerated(EnumType.STRING)
    public Currency currency;

    /**
     * 换算成美元的销售
     */
    public Float usdSales;

    /**
     * 记录的时间
     */
    @Temporal(TemporalType.DATE)
    @Expose
    public Date date;

    public void updateAttr(SellingRecord nsrd) {
        if(!StringUtils.equals(this.id, nsrd.id)) throw new FastRuntimeException("不相同的 SellingReocrd 不能进行更新!");
        if(nsrd.units != null && nsrd.units > 0) this.units = nsrd.units;
        if(nsrd.orders != null && nsrd.orders > 0) this.orders = nsrd.orders;
        if(nsrd.orderCanceld != null && nsrd.orderCanceld > 0) this.orderCanceld = nsrd.orderCanceld;
        if(nsrd.sales != null && nsrd.sales > 0) this.sales = nsrd.sales;
        if(nsrd.currency != null) this.currency = nsrd.currency;
        if(nsrd.usdSales != null && nsrd.usdSales > 0) this.usdSales = nsrd.usdSales;
        if(nsrd.rating != null && nsrd.rating > 0) this.rating = nsrd.rating;
        if(nsrd.salePrice != null && nsrd.salePrice > 0) this.salePrice = nsrd.salePrice;
        if(nsrd.pageViews != null && nsrd.pageViews > 0) this.pageViews = nsrd.pageViews;
        if(nsrd.sessions != null && nsrd.sessions > 0) this.sessions = nsrd.sessions;
    }

    /**
     * 通过 Amazon 的 BusinessReports 产生一组 SellingRecord, 以便更新或者是记录
     *
     * @return
     */
    public static Set<SellingRecord> newRecordFromAmazonBusinessReports(Account acc, Account.M market, Date oneDay) {
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
                JsonObject data = null;
                try {
                    data = new JsonParser().parse(rtJson).getAsJsonObject().get("data").getAsJsonObject();
                    hasNext = data.get("hasNextPage").getAsInt() > 0;
                } catch(Exception e) {
                    FLog.fileLog(String.format("%s.%s.%s.json", acc.prettyName(), market, Dates.date2Date(oneDay)), rtJson, FLog.T.SELLINGRECORD);
                }
                rows = data.get("rows").getAsJsonArray();
                if(rows.size() == 0)
                    FLog.fileLog(String.format("%s.%s.%s.noresult.json", acc.prettyName(), market, Dates.date2Date(oneDay)), rtJson, FLog.T.SELLINGRECORD);
                for(JsonElement row : rows) {
                    try {
                        JsonArray rowArr = row.getAsJsonArray();
                        String msku = StringUtils.splitByWholeSeparator(rowArr.get(3).getAsString(), "\">")[1];
                        msku = msku.substring(0, msku.length() - 4).toUpperCase(); /*前面截取了 "> 后最后的 </a> 过滤掉*/
                        String sid = Selling.sid(msku, market, acc);
                        String srid = SellingRecord.id(sid, oneDay);

                        SellingRecord record = SellingRecord.findById(srid);
                        if(record == null) { // 数据库中不存在的时候, 进行如下数据更新
                            record = new SellingRecord();
                            record.account = acc;
                            record.market = market;
                            record.selling = Selling.findById(sid);
                            if(record.selling == null) {
                                Logger.error("SellingRecord has no selling (%s) !", sid);
                                continue;
                            }

                            // Amazon 的订单数据也抓取回来, 但还是会重新计算
                            record.units = rowArr.get(9).getAsInt();
                            /**
                             * 1. de: €2,699.37
                             * 2. uk: £2,121.30
                             * 3. fr: €44.99
                             * fr, de 都是使用的 xx.xx 的格式, 而没有 ,
                             */
                            record.sales = Webs.amazonPriceNumber(Account.M.AMAZON_UK/*格式固定*/, rowArr.get(11).getAsString().substring(1));
                            switch(market) {
                                case AMAZON_UK:
                                    record.currency = Currency.GBP;
                                    break;
                                case AMAZON_DE:
                                case AMAZON_FR:
                                case AMAZON_IT:
                                case AMAZON_ES:
                                    record.currency = Currency.EUR;
                                    break;
                                case AMAZON_US:
                                    record.currency = Currency.USD;
                                    break;
                                default:
                                    record.currency = Currency.GBP;
                            }
                            record.usdSales = record.currency.toUSD(record.sales);
                            if(record.usdSales == null) record.usdSales = 0f;
                            record.orders = rowArr.get(12).getAsInt();
                            record.date = oneDay;
                            record.id = srid;
                        }
                        // 无论数据库中存在不存在都需要更新下面数据
                        record.sessions = Webs.amazonPriceNumber(Account.M.AMAZON_UK, rowArr.get(4).getAsString()).intValue();
                        record.pageViews = Webs.amazonPriceNumber(Account.M.AMAZON_UK, rowArr.get(6).getAsString()).intValue();

                        records.add(record);
                    } catch(Exception e) {
                        Logger.warn("SellingRecord.newRecordFromAmazonBusinessReports (%s)", Webs.E(e));
                    }
                }


            } while(hasNext);
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
    @Cached("5mn") // 具体的缓存统一到页面上,这里的缓存 5mn 用来防止多次加载
    public static List<SellingRecord> accountMskuRelateRecords(Account acc, String msku, Date from, Date to) {
        String cacheKey = Caches.Q.cacheKey(acc, msku, from, to);
        List<SellingRecord> cacheElement = Cache.get(cacheKey, List.class);
        synchronized(SellingRecord.class) {
            cacheElement = Cache.get(cacheKey, List.class);
            if(cacheElement != null) return cacheElement;

            cacheElement = SellingRecord.find("selling.merchantSKU=? AND account=? AND date>=? AND date<=? ORDER BY date", msku, acc, from, to).fetch();
            Cache.add(cacheKey, cacheElement, "1h");
        }
        return Cache.get(cacheKey, List.class);
    }

    /**
     * 加载出一段时间内指定 Selling 的 PageView 与 Session 数据, 给前台的 HighChar 使用
     *
     * @param msku
     * @param from
     * @param to
     * @return
     */
    public static Map<String, ArrayList<F.T2<Long, Float>>> ajaxHighChartPVAndSS(String msku, Account acc, Date from, Date to) {
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
                .build();

        List<SellingRecord> records = accountMskuRelateRecords(acc, msku, from, to);
        for(SellingRecord rcd : records) {
            switch(rcd.market) {
                case AMAZON_UK:
                    highCharLines.get("pv_uk").add(new F.T2<Long, Float>(rcd.date.getTime(), rcd.pageViews.floatValue()));
                    highCharLines.get("ss_uk").add(new F.T2<Long, Float>(rcd.date.getTime(), rcd.sessions.floatValue()));
                    break;
                case AMAZON_DE:
                    highCharLines.get("pv_de").add(new F.T2<Long, Float>(rcd.date.getTime(), rcd.pageViews.floatValue()));
                    highCharLines.get("ss_de").add(new F.T2<Long, Float>(rcd.date.getTime(), rcd.sessions.floatValue()));
                    break;
                case AMAZON_FR:
                    highCharLines.get("pv_fr").add(new F.T2<Long, Float>(rcd.date.getTime(), rcd.pageViews.floatValue()));
                    highCharLines.get("ss_fr").add(new F.T2<Long, Float>(rcd.date.getTime(), rcd.sessions.floatValue()));
                    break;
                default:
                    // ignore
            }
        }
        return highCharLines;
    }


    /**
     * 加载一段时间内指定 Selling 的转换率曲线数据, 给前台的 HighChar 使用
     *
     * @return
     */
    public static Map<String, ArrayList<F.T2<Long, Float>>> ajaxHighChartTurnRatio(String msku, Account acc, Date from, Date to) {
        Map<String, ArrayList<F.T2<Long, Float>>> highCharLines = GTs.MapBuilder
                .map("tn_uk", new ArrayList<F.T2<Long, Float>>())
                .put("tn_de", new ArrayList<F.T2<Long, Float>>())
                .put("tn_fr", new ArrayList<F.T2<Long, Float>>())
                .build();
        List<SellingRecord> records = SellingRecord.accountMskuRelateRecords(acc, msku, from, to);
        for(SellingRecord rcd : records) {
            float turnRatio = Webs.scalePointUp(3, (float) rcd.orders / (rcd.sessions == 0 ? 1 : rcd.sessions));
            if(rcd.sessions <= 0) turnRatio = 0f;
            switch(rcd.market) {
                case AMAZON_UK:
                    highCharLines.get("tn_uk").add(new F.T2<Long, Float>(rcd.date.getTime(), turnRatio));
                    break;
                case AMAZON_DE:
                    highCharLines.get("tn_de").add(new F.T2<Long, Float>(rcd.date.getTime(), turnRatio));
                    break;
                case AMAZON_FR:
                    highCharLines.get("tn_fr").add(new F.T2<Long, Float>(rcd.date.getTime(), turnRatio));
                    break;
                default:
                    //ignore
            }
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
