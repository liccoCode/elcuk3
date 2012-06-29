package jobs;

import helper.Currency;
import helper.Dates;
import helper.Webs;
import models.market.*;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Job;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 在检查所有 Selling 的 SellingRecord, 如果没有进行计算;
 * 每天的 00:30 计算昨天的数据
 * User: wyattpan
 * Date: 5/29/12
 * Time: 4:03 PM
 */
public class SellingRecordCheckJob extends Job {

    public DateTime fixTime;

    @Override
    public void doJob() {
        /**
         * 0. 一 fixTime 为基准时间
         * 1. 检查 7 天内的 SellingRecord, 没有的创建, 有的进行更新
         * 2. 下载 2 天前的 SellingRecord 数据.
         */

        List<Selling> sellings = Selling.all().fetch();

        if(fixTime == null) fixTime = DateTime.now();
        for(int i = -7; i < 0; i++)
            checkOneDaySellingRecord(sellings, fixTime.plusDays(i).toDate());

        //------- 抓取 Amazon 的数据 (由于抓取 Amazon 的数据是一个整体, 所以最后处理) --------
        /**
         * 找到所有 Amazon 市场的 SellingRecord 数据
         * PS: 只能抓取到两天前的 PageView 数据
         */
        amazonNewestRecords();
    }

    private void amazonNewestRecords() {
        List<Account> accs = Account.all().fetch();
        Set<SellingRecord> records = null;
        // 现在写死, 只有 2 个账户, UK 需要抓取 uk, de; DE 只需要抓取 de
        for(Account acc : accs) {
            if("AJUR3R8UN71M4".equals(acc.merchantId)) { // UK 账号, uk,de 两个市场的数据都需要
                records = SellingRecord.newRecordFromAmazonBusinessReports(acc, Account.M.AMAZON_UK, fixTime.plusDays(-2).toDate());
                records.addAll(SellingRecord.newRecordFromAmazonBusinessReports(acc, Account.M.AMAZON_DE, fixTime.plusDays(-2).toDate()));
                Logger.info("Account(%s) Fetch UK & DE  %s records.", acc.prettyName(), records.size());
            } else if("A22H6OV6Q7XBYK".equals(acc.merchantId)) {
                records = SellingRecord.newRecordFromAmazonBusinessReports(acc, Account.M.AMAZON_DE, fixTime.plusDays(-2).toDate());
                Logger.info("Account(%s) Fetch DE %s records", acc.prettyName(), records.size());
            }
            if(records == null || records.size() <= 0) continue;
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
    public void checkOneDaySellingRecord(List<Selling> sellings, Date checkDate) {
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
                Logger.warn("SellingRecordCheckJob %s", Webs.E(e));
            }
        }
    }
}
