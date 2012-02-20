package jobs;

import com.google.gson.JsonElement;
import models.Server;
import models.market.Listing;
import play.Logger;
import play.jobs.Job;
import play.libs.WS;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 与 ListingSchedulJob 配合使用
 * User: wyattpan
 * Date: 2/9/12
 * Time: 7:04 PM
 */
public class ListingCrawlJob extends Job {
    /**
     * 进行更新使用的队列
     */
    public static LinkedBlockingQueue<String> QUEUE = new LinkedBlockingQueue<String>();

    /**
     * 用来控制线程此线程是否可以继续运行的, 如果任务全部结束, 那么则可以运行, 否则将被跳过这任务
     */
    public static final AtomicBoolean flag = new AtomicBoolean(true);

    private static final Integer CRAW_LISTING_NUM = 3;//一次任务抓取几个 Listing


    @Override
    public void doJob() throws Exception {
        /**
         * 1. 检查是否可以运行这次任务
         * 2. 从队列中依次拿出任务并根据参数进行速度调节
         * 3. 将任务分发到不同的子 Job 中去
         */
        if(!flag.get()) {
            Logger.debug("Last Time Job is not DONE yet...");
            return; // FLAG 为 false 则跳过这次任务
        }


        flag.set(false);
        for(int i = 0; i < CRAW_LISTING_NUM; i++) {
            String listingId = QUEUE.poll();
            if(listingId == null) {
                Logger.debug("Wow! Queue is empty!");
                continue;
            }
            new Worker(listingId).now().get(10, TimeUnit.SECONDS); // 不需要结果, 但需要为每个结果等待 10s
        }
        flag.set(true);
        Logger.debug("ListingCrawlJob.Queue left " + QUEUE.size());
    }

    /**
     * 最后执行 Listing 更新的地方; 使用 Job 的原因是在这里使用了多线程, 但 Play! 在 Job 中开启的线程中使用
     * JPA 保存等的时候无法进行, 所以只能继承 Job 来让 Play! 自动开启 JPA 与事务.
     */
    public class Worker extends Job<Listing> {
        private String listingId;

        public Worker(String listingId) {
            this.listingId = listingId;
        }

        @Override
        public void doJob() throws Exception {
            Listing listing = Listing.find("listingId=?", listingId).first();
            // Current Only Amazon
            if(listing == null) {
                Logger.error("The Listing Queue have error! Please check it immediately.");
                return;
            }
            if(listing.market.name().contains("EBAY")) {
                Logger.warn("ListingCrawlJob Current not support Ebay Web Site Listing.");
                return;
            }
            try {
                JsonElement lst = WS.url(String.format("%s/listings/%s/%s",
                        Server.server(Server.T.CRAWLER).url,
                        listing.market.name().split("_")[1],
                        listing.asin)).get().getJson();
                Listing needCheckListing = Listing.parseAndUpdateListingFromCrawl(lst);
                needCheckListing.check();
                needCheckListing.save();
            } catch(Exception e) {
                Logger.warn("ListingCrawlJob:" + e.getMessage());
            }
        }

    }
}
