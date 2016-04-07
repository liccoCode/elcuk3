package jobs.works;

import com.google.gson.JsonElement;
import helper.Crawl;
import helper.Webs;
import models.market.Listing;
import play.Logger;
import play.jobs.Job;

import java.util.concurrent.TimeUnit;

/**
 * 最后执行 Listing 更新的地方; 使用 Job 的原因是在这里使用了多线程, 但 Play! 在 Job 中开启的线程中使用
 * JPA 保存等的时候无法进行, 所以只能继承 Job 来让 Play! 自动开启 JPA 与事务.
 * User: wyattpan
 * Date: 9/24/12
 * Time: 10:42 AM
 *
 * @deprecated
 */
public class ListingWork extends Job<Listing> {
    private String listingId;
    private boolean fulloffers = false;

    public ListingWork(String listingId, boolean fulloffers) {
        this.listingId = listingId;
        this.fulloffers = fulloffers;
    }

    @Override
    public void doJob() {
        Listing listing = Listing.find("listingId=?", this.listingId).first();
        // Current Only Amazon
        if(listing == null) {
            Logger.error("Listing %s is not exist! Check it immediately.", this.listingId);
            return;
        }
        if(listing.market.name().contains("EBAY")) {
            Logger.warn("ListingDriverlJob Current not support Ebay Web Site Listing.");
            return;
        }
        try {
            JsonElement lst = Crawl.crawlListing(listing.market.name(), listing.asin);
            Listing needCheckListing = Listing.parseAndUpdateListingFromCrawl(lst, false);
            if(needCheckListing == null) {
                // TODO 需要对删除的 Listing 做处理
                // 如果为 null , 则 7 天内不在检查
                listing.lastUpdateTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7);
                listing.saleRank = -2;// 使用 saleRank 为 -2 来标记此 Listing 已经 CLOSE 了
                listing.save();
                return;
            }
            if(fulloffers) {
                Logger.info("Listing (%s) fetch offers...", this.listingId);
                new ListingOffersWork(needCheckListing).now().get(20, TimeUnit.SECONDS); // 等待 20 s
            }
            // 保存 Offers
            needCheckListing.save();
            needCheckListing.checkAndSaveOffers();
        } catch(Exception e) {
            Logger.warn("ListingDriverlJob[%s]: %s", this.listingId, Webs.E(e));
        }
    }
}
