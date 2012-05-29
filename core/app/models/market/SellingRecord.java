package models.market;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import helper.Currency;
import helper.Dates;
import helper.HTTP;
import helper.Webs;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 每个 Selling 需要每天记录的信息
 * User: wyattpan
 * Date: 5/27/12
 * Time: 6:40 PM
 */
@Entity
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
            do {
                String url = market.salesAndTrafficByAsinLink(oneDay, oneDay, curentPage++);
                Logger.info("Fetch SellingRecord [%s]", url);
                String rtJson = HTTP.get(acc.cookieStore(), url);
                JsonObject data = new JsonParser().parse(rtJson).getAsJsonObject().get("data").getAsJsonObject();
                rows = data.get("rows").getAsJsonArray();
                for(JsonElement row : rows) {
                    JsonArray rowArr = row.getAsJsonArray();
                    SellingRecord record = new SellingRecord();
                    record.account = acc;
                    record.market = market;

                    String msku = StringUtils.splitByWholeSeparator(rowArr.get(3).getAsString(), "\">")[1];
                    msku = msku.substring(0, msku.length() - 4); /*前面截取了 "> 后最后的 </a> 过滤掉*/
                    String sid = String.format("%s_%s", msku, market.toString());
                    record.selling = Selling.findById(sid);
                    record.sessions = rowArr.get(4).getAsInt();
                    record.pageViews = rowArr.get(6).getAsInt();

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
                    record.orders = rowArr.get(12).getAsInt();
                    record.date = oneDay;


                    record.id = SellingRecord.id(sid, acc.id + "", oneDay);
                    records.add(record);
                }


            } while(rows.size() > 0);
        }

        return records;
    }

    /**
     * 产生当天的 Id
     *
     * @param sid
     * @param aid
     * @return
     */
    public static String id(String sid, String aid) {
        return id(sid, aid, DateTime.now().toDate());
    }

    /**
     * 产生指定天的 Id
     *
     * @param sid
     * @param aid
     * @param time
     * @return
     */
    public static String id(String sid, String aid, Date time) {
        return (String.format("%s_%s_%s", sid, aid, Dates.date2Date(time))).hashCode() + "";
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
