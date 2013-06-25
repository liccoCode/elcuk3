package jobs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import helper.Crawl;
import models.Jobex;
import models.market.AmazonListingReview;
import models.market.Listing;
import play.jobs.Job;

import java.util.List;

/**
 * 用来检查系统内的 Amazon Review, 检查是否被删除
 * * 周期:
 * - 轮询周期: 5s
 * - Duration: 3mn
 * User: wyattpan
 * Date: 11/6/12
 * Time: 11:45 AM
 */
public class AmazonReviewCheckJob extends Job {
    @Override
    public void doJob() {
        if(!Jobex.findByClassName(AmazonReviewCheckJob.class.getName()).isExcute()) return;
        // 抓取没有删除, 并且按照最后更新时间的升序排列;
        // 自己或者其他让的 Listing 都检查.
        List<AmazonListingReview> reviewsToBeCheck = AmazonListingReview
                .find("isRemove=false ORDER BY updateAt ASC").fetch(20);
        for(AmazonListingReview review : reviewsToBeCheck) {
            JsonElement rvObj = Crawl.crawlReview(Listing.unLid(review.listingId)._2.toString(), review.reviewId);
            JsonObject obj = rvObj.getAsJsonObject();
            if(obj.get("isRemove").getAsBoolean()) {
                review.isRemove = true;
                review.save();
            }
        }

        // TODO 思考对于 Review 还需要有哪些检查?
    }
}
