package models.market;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import helper.Currency;
import helper.Dates;
import helper.HTTP;
import org.joda.time.DateTime;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 每个 Selling 需要每天记录的信息
 * User: wyattpan
 * Date: 5/27/12
 * Time: 6:40 PM
 */
@Entity
public class SellingRecord extends GenericModel {

    @ManyToOne(fetch = FetchType.LAZY)
    public Selling selling;

    @ManyToOne(fetch = FetchType.LAZY)
    public Account account;

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
    public static List<SellingRecord> newRecordFromAmazonBusinessReports(Account acc, Account.M market, Date oneDay) {
        List<SellingRecord> records = new ArrayList<SellingRecord>();
        JsonArray rows = null;
        do {
            synchronized(acc.cookieStore()) {
                String rtJson = HTTP.get(acc.cookieStore(), market.salesAndTrafficByAsinLink(oneDay, oneDay, 0));
                JsonObject data = new JsonParser().parse(rtJson).getAsJsonObject().get("data").getAsJsonObject();
                rows = data.get("rows").getAsJsonArray();
            }
            for(JsonElement row : rows) {
                JsonArray rowArr = row.getAsJsonArray();
                SellingRecord record = new SellingRecord();
                record.id = SellingRecord.id("", "", oneDay);
                record.account = acc;
                record.market = market;
                //TODO

                records.add(record);
            }
        } while(rows.size() > 0);

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
