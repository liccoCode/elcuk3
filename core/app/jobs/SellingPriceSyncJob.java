package jobs;

import helper.Crawl;
import helper.Currency;
import helper.Webs;
import models.market.Listing;
import models.market.PriceStrategy;
import models.market.Selling;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.jobs.Job;

import java.util.List;

/**
 * 针对 Selling 身上设置的 priceMatchAsin 中的 ASIN 进行调价跟踪
 * User: wyattpan
 * Date: 5/12/12
 * Time: 3:23 PM
 */
public class SellingPriceSyncJob extends Job {

    @Override
    public void doJob() {
        /**
         * 1. 查询所有 Selling 中的 priceMatchAsin 不为 null 的 Selling,进行价格跟踪
         * 2. 数据库中查询 priceMatchAsin 中涉及的 Listing 的所有 Offers 中最低价格
         * 2.1. 如果这个 Listing 在数据库中不存在, 那么抓取一次
         *
         * 3. 根据 Selling 与最低价格进行上架的价格计算
         * 4. 对 Selling 进行价格调整
         */

        List<Selling> sellings = Selling.find("priceMatchAsin is not null AND priceMatchAsin!=''").fetch();
        for(Selling sell : sellings) {
            String[] asins = StringUtils.split(sell.priceMatchAsin, ",");

            //2
            float lowestPrice = Float.MAX_VALUE;
            for(String asin : asins) {
                Listing listing = Listing.findById(String.format("%s_%s", asin, sell.market.toString()));
                if(listing == null)
                    listing = Listing.parseAndUpdateListingFromCrawl(Crawl.crawlListing(sell.market.name(), asin)).save();
                //TODO 考虑抓取回来的竞争对手没有价格的话如何处理?
                float listingLowPrice = listing.lowestPrice();
                if(listingLowPrice <= 0) continue;
                if(listingLowPrice < lowestPrice) lowestPrice = listingLowPrice;
            }

            //3
            float before = (sell.salePrice == null || sell.salePrice <= 0) ? sell.price : sell.salePrice;
            if(sell.priceStrategy.type == PriceStrategy.T.LowestPrice) {
                switch(sell.market) {
                    case AMAZON_UK:
                        sell.salePrice = Currency.GBP.toGBP(lowestPrice) + sell.priceStrategy.differ;
                        break;
                    case AMAZON_DE:
                    case AMAZON_ES:
                    case AMAZON_IT:
                    case AMAZON_FR:
                        sell.salePrice = Currency.GBP.toEUR(lowestPrice) + sell.priceStrategy.differ;
                        break;
                    case AMAZON_US:
                        sell.salePrice = Currency.GBP.toUSD(lowestPrice) + sell.priceStrategy.differ;
                        break;
                }
                if(sell.salePrice < sell.priceStrategy.lowest)
                    sell.salePrice = sell.priceStrategy.lowest;

                if(sell.startDate == null || sell.endDate == null) {
                    sell.startDate = DateTime.now().plusMonths(-6).toDate();
                    sell.endDate = DateTime.now().plusMonths(18).toDate();
                }

                // 最后对价格进行 1. 向上取整, 精确到 2 位. 2. 0.49/0.99 上下调整
                sell.salePrice = Currency.upDown(Webs.scale2PointUp(sell.salePrice));
            }

            //4
            sell.deploy();
            Logger.info("Selling[%s] price from %s to %s, change: %s",
                    sell.sellingId, before, sell.salePrice, Webs.scale2PointUp(sell.salePrice - before));
        }
    }
}
