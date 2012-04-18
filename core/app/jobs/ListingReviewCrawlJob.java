package jobs;

import com.google.gson.JsonElement;
import helper.Webs;
import models.Server;
import models.market.Listing;
import play.Logger;
import play.db.DB;
import play.jobs.Job;
import play.libs.WS;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用来更新 Listing 的 Review 信息的线程, 这个线程并不需要执行得那么频繁, 基本上每 24 小时执行一次即可.
 * User: wyattpan
 * Date: 4/18/12
 * Time: 4:57 PM
 */
public class ListingReviewCrawlJob extends Job {

    @Override
    public void doJob() {
        ResultSet rs = DB.executeQuery("select listingId from Listing");
        List<String> listingIds = new ArrayList<String>();
        try {
            while(rs.next()) {
                listingIds.add(rs.getString("listingId"));
            }
        } catch(Exception e) {
            Logger.warn(Webs.E(e));
        }

        ExecutorService service = Executors.newFixedThreadPool(6); // 允许 6 个线程并发去抓取 Review

        for(String lid : listingIds) service.submit(new ReviewWorker(lid));

        service.shutdown(); // 线程执行完之后关闭
    }

    public static class ReviewWorker implements Runnable {
        private String listingId;

        public ReviewWorker(String listingId) {
            this.listingId = listingId;
        }

        @Override
        public void run() {
            Listing listing = Listing.findById(listingId);
            // host/reviews/{market}/{asin}
            JsonElement reviews = WS.url(String.format("%s/reviews/%s/%s",
                    Server.server(Server.T.CRAWLER).url, listing.market.name(), listing.asin)).get().getJson();
            /**
             * 解析出所有的 Reviews, 然后从数据库中加载出此 Listing 对应的所有 Reviews 然后进行判断这些 Reviews 是更新还是新添加?
             *
             * 新添加, 直接 Save
             *
             * 更新, 需要对某一些字段进行判断后更新并添加 Comment
             */
        }
    }
}
