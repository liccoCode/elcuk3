package jobs;

import helper.Currency;
import helper.Dates;
import helper.Webs;
import models.Jobex;
import models.market.*;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Every;
import play.jobs.Job;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <pre>
 * 在检查所有 Selling 的 SellingRecord, 如果没有进行计算;
 * 每天的 00:30 计算昨天的数据
 * 周期:
 * - 轮询周期: 5mn
 * - Duration: 0 40 0,23 * * ?
 * </pre>
 * User: wyattpan
 * Date: 5/29/12
 * Time: 4:03 PM
 */
@Every("5mn")
public class SellingRecordCheckJob extends Job {

    public DateTime fixTime;

    @Override
    public void doJob() {
        if(!Jobex.findByClassName(SellingRecordCheckJob.class.getName()).isExcute()) return;
        /**
         * 0. 一 fixTime 为基准时间
         * 1. 检查 7 天内的 SellingRecord, 没有的创建, 有的进行更新
         * 2. 下载 7~2(5) 天前的 SellingRecord 数据.
         */
        List<Selling> sellings = Selling.all().fetch();

        if(fixTime == null) fixTime = DateTime.now();
        for(int i = -7; i < 0; i++)
            SellingRecordCheckJob.checkOneDaySellingRecord(sellings, fixTime.plusDays(i).toDate());

        //------- 抓取 Amazon 的数据 (由于抓取 Amazon 的数据是一个整体, 所以最后处理) --------
        /**
         * 找到所有 Amazon 市场的 SellingRecord 数据
         * PS: 只能抓取到两天前的 PageView 数据
         */
        for(int i = -7; i <= -2; i++)
            SellingRecordCheckJob.amazonNewestRecords(fixTime.plusDays(i));
    }

    /**
     * 抓取 Amazon 某一天的 Selling Record 数据
     *
     * @param fixTime
     */
    public static void amazonNewestRecords(DateTime fixTime) {
        List<Account> accs = Account.openedSaleAcc();
        Set<SellingRecord> records = null;
        // 现在写死, 只有 2 个账户, UK 需要抓取 uk, de; DE 只需要抓取 de
        for(Account acc : accs) {
            records = SellingRecord.newRecordFromAmazonBusinessReports(acc, acc.type, fixTime.toDate());
            Logger.info("Fetch Account(%s) %s records", acc.prettyName(), records.size());
            if(records.size() <= 0) continue;
            // 直接这样处理,因为通过 SellingRecord.newRecordFromAmazonBusinessReports 出来的方法已经存在与 Session 缓存中了.
            for(SellingRecord record : records) {
                if(JPA.em().contains(record)) // 防止异常情况, 只有存在与一级缓存(Transation)中的才可以保存
                    record.save();
                else
                    Logger.warn("SellingRecord (%s) is not in Hibernate Session Cache!", record.id);
            }
        }
    }

    /**
     * 检查并且计算某一天的 SellingRecord. 重新计算指定 Selling 在某一个天的 SellingRecord 记录, 存在则更新否则新创建.
     *
     * @param sellings
     * @param checkDate
     */
    public static void checkOneDaySellingRecord(List<Selling> sellings, Date checkDate) {
        List<OrderItem> orderitems = OrderItem.find("createDate>=? AND createDate<=?",
                Dates.morning(checkDate), Dates.night(checkDate)).fetch();

        Logger.info("Check Date (%s) %s selling...", Dates.date2Date(checkDate), sellings.size());

        for(Selling sell : sellings) {
            try {
                String srid = SellingRecord.id(sell.sellingId, checkDate);
                SellingRecord record = SellingRecord.findById(srid);
                if(record == null) record = new SellingRecord(srid, sell, checkDate);
                Map<String, AtomicInteger> currentSellOrderCache = new HashMap<String, AtomicInteger>();
                /**
                 * 1. 计算昨天的订单数量 units, orders, orderCanceled, sales(currency), usdSales
                 * 2. 记录昨天的数据 rating, salePrice,
                 */

                // ------- 1 ----------
                switch(sell.market) {
                    case AMAZON_UK:
                        record.currency = Currency.GBP;
                        break;
                    case AMAZON_DE:
                    case AMAZON_ES:
                    case AMAZON_FR:
                    case AMAZON_IT:
                        record.currency = Currency.EUR;
                        break;
                    case AMAZON_US:
                        record.currency = Currency.USD;
                        break;
                    default:
                        record.currency = Currency.GBP;

                }
                try {
                    int orders = 0;
                    float sales = 0;
                    int units = 0;
                    float usdSales = 0;
                    for(OrderItem oi : orderitems) {
                        if(!StringUtils.equals(oi.selling.sellingId, sell.sellingId)) continue;
                        if(currentSellOrderCache.containsKey(oi.order.orderId))
                            currentSellOrderCache.get(oi.order.orderId).incrementAndGet();
                        else {
                            currentSellOrderCache.put(oi.order.orderId, new AtomicInteger(1));
                            orders += 1; //每一个 Selling 每碰到一个新订单 ID 则增加 1
                        }
                        units += 1;
                        if(oi.order.state == Orderr.S.CANCEL) record.orderCanceld += 1;
                        sales += oi.price;
                        usdSales += oi.usdCost == null ? 0 : oi.usdCost;
                    }
                    record.orders = orders;
                    record.units = units;
                    record.sales = sales;
                    record.usdSales = usdSales;
                } catch(Exception e) {
                    Logger.warn(Webs.S(e) + "---- %s", record.id);
                }

                // ---------- 2 ----------
                record.salePrice = sell.aps.salePrice != null && sell.aps.salePrice > 0 ? sell.aps.salePrice : sell.aps.standerPrice;
                record.reviewSize = sell.listing.listingReviews.size();
                float ratingAll = 0f;
                for(AmazonListingReview review : sell.listing.listingReviews) ratingAll += review.rating;
                record.rating = record.reviewSize > 0 ? Webs.scalePointUp(1, ratingAll / record.reviewSize) : 0;
                record.save();
            } catch(Exception e) {
                Logger.warn("SellingRecordCheckJob %s, %s", sell.sellingId, Webs.E(e));
            }
        }
    }
}
