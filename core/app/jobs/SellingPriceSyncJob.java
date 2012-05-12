package jobs;

import helper.Crawl;
import models.market.Listing;
import models.market.PriceStrategy;
import models.market.Selling;
import org.apache.commons.lang.StringUtils;
import play.jobs.Job;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
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
                float listingLowPrice = listing.lowestPrice();
                if(listingLowPrice < lowestPrice) lowestPrice = listingLowPrice;
            }

            //3
            if(sell.priceStrategy.type == PriceStrategy.T.LowestPrice) {
                sell.price = lowestPrice + sell.priceStrategy.differ;
            }

            sell.deploy();
        }
    }
}
