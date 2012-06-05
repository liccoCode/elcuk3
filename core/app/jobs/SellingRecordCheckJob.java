package jobs;

import helper.Currency;
import helper.Dates;
import helper.Webs;
import models.market.*;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
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
        List<Selling> sellings = Selling.all().fetch();

        if(fixTime == null) fixTime = DateTime.now();
        Date checkDate = DateTime.parse(fixTime.toString("yyyy-MM-dd")).plusDays(-1).toDate();
        List<OrderItem> orderitems = OrderItem.find("createDate>=? AND createDate<=?",
                checkDate,/*记录昨天 00:00*/
                fixTime.toDate()/*当前时间*/).fetch();

        Logger.info("Date (%s), check %s selling...", Dates.date2Date(checkDate), sellings.size());

        for(Selling sell : sellings) {
            try {
                String srid = SellingRecord.id(sell.sellingId, checkDate);
                SellingRecord record = SellingRecord.findById(srid);
                if(record == null) record = new SellingRecord(srid, sell, checkDate);
                Map<String, AtomicInteger> currentSellOrderCache = new HashMap<String, AtomicInteger>();
                /**
                 * 1. 计算昨天的订单数量 units, orders, orderCanceled, sales(currency), usdSales
                 * 2. 记录昨天的数据 rating, salePrice,
                 * 3. 抓取 Amazon 的数据 (由于抓取 Amazon 的数据是一个整体, 所以最后处理)
                 */

                // ------- 1 ----------
                /*
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
                */
                // TODO 由于现在 OrderItem 全部统计成了 GBP 计算所以现在全部使用 GBP 计算
                record.currency = Currency.GBP;
                try {

                    for(OrderItem oi : orderitems) {
                        if(!StringUtils.equals(oi.selling.sellingId, sell.sellingId)) continue;
                        if(currentSellOrderCache.containsKey(oi.order.orderId))
                            currentSellOrderCache.get(oi.order.orderId).incrementAndGet();
                        else {
                            currentSellOrderCache.put(oi.order.orderId, new AtomicInteger(1));
                            record.orders += 1; //每一个 Selling 每碰到一个新订单 ID 则增加 1
                        }
                        record.units += 1;
                        if(oi.order.state == Orderr.S.CANCEL) record.orderCanceld += 1;
                        // TODO sales 需要在修改了 OrderItem 记录的价格以后再重新计算
                        record.sales += oi.price;
                    }
                } catch(Exception e) {
                    Logger.warn(Webs.E(e));
                }
                record.usdSales = record.currency.toUSD(record.sales);

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

        // ---------- 3 -------------
        /**
         * 找到所有 Amazon 市场的 SellingRecord 数据
         * PS: 只能抓取到两天前的 PageView 数据
         */
        List<Account> accs = Account.all().fetch();
        Set<SellingRecord> records = new HashSet<SellingRecord>();
        Map<String, SellingRecord> sellingRecordCache = new HashMap<String, SellingRecord>();
        // 现在写死, 只有 2 个账户, UK 需要抓取 uk, de; DE 只需要抓取 de
        for(Account acc : accs) {
            SellingRecord tmp = null;
            if("AJUR3R8UN71M4".equals(acc.merchantId)) { // UK 账号, uk,de 两个市场的数据都需要
                records = SellingRecord.newRecordFromAmazonBusinessReports(acc, Account.M.AMAZON_UK, fixTime.plusDays(-2).toDate());
                records.addAll(SellingRecord.newRecordFromAmazonBusinessReports(acc, Account.M.AMAZON_DE, fixTime.plusDays(-2).toDate()));
                Logger.info("Account(%s) Fetch UK & DE  %s records.", acc.prettyName(), records.size());

                Set<String> rcdIds = new HashSet<String>();
                for(SellingRecord rcd : records) rcdIds.add(rcd.id);

                List<SellingRecord> managedRecords = SellingRecord.find("id IN ('" + StringUtils.join(rcdIds, "','") + "')").fetch();
                for(SellingRecord msrc : managedRecords) sellingRecordCache.put(msrc.id, msrc);

                for(SellingRecord rcd : records) { // 寻找已经在系统中的 SellingRecord 进行更新, 否在再创建
                    try {
                        if(sellingRecordCache.containsKey(rcd.id)) {
                            tmp = sellingRecordCache.get(rcd.id);
                            tmp.pageViews = rcd.pageViews;
                            tmp.sessions = rcd.sessions;
                            tmp.save();
                        } else {
                            sellingRecordCache.put(rcd.id, rcd.<SellingRecord>save());
                        }
                    } catch(Exception e) {
                        Logger.warn(Webs.E(e));
                    }
                }
                /* 暂时不开放这个账户
            } else if(acc.type == Account.M.AMAZON_DE) {
                records = SellingRecord.newRecordFromAmazonBusinessReports(acc, Account.M.AMAZON_DE, dt.plusDays(-2).toDate());
                for(SellingRecord rcd : records) {
                    if(recordCaches.containsKey(rcd.id)) {
                        tmp = recordCaches.get(rcd.id);
                        tmp.pageViews = rcd.pageViews;
                        tmp.sessions = rcd.sessions;
                        tmp.save();
                    } else
                        Logger.error("SellingRecord(%s)(%s)(%s) is not exist, Check it!", rcd.id, rcd.selling.sellingId, rcd.account.prettyName());
                }
                */
            }
        }

    }
}
