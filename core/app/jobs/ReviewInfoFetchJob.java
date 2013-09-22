package jobs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import helper.Crawl;
import helper.Dates;
import models.Jobex;
import models.market.AmazonListingReview;
import models.market.Listing;
import models.market.M;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.jobs.Job;
import play.libs.F;

/**
 * <pre>
 * 针对每一个有问题的 Review 进行检查, 查看 Review 的处理状况.
 * 周期:
 * - 轮询周期: 1mn
 * - Duration: 1h
 * </pre>
 * User: wyattpan
 * Date: 8/24/12
 * Time: 5:56 PM
 */
public class ReviewInfoFetchJob extends Job {
    @Override
    public void doJob() {
        if(!Jobex.findByClassName(ReviewInfoFetchJob.class.getName()).isExcute()) return;
        int size = 30;
        if(Play.mode.isDev()) size = 10;
        //TODO 对 Review 的跟踪检查, 检查 Reivew 是否删除, 评分是否变化.
    }


    /**
     * 更新
     * 1. 检查 Review 的内容是否有改动
     * 2. 检查 Review 分值是否改变(通过 updateAttr 来处理)
     *
     * @param review
     * @return Review 的 JSON Element
     */
    public static JsonElement syncSingleReview(AmazonListingReview review) {
        F.T2<String, M> splitListingId = Listing.unLid(review.listingId);
        JsonElement reviewElement = Crawl
                .crawlReview(splitListingId._2.toString(), review.reviewId);

        JsonObject reviewObj = reviewElement.getAsJsonObject();
        if(!reviewObj.get("isRemove").getAsBoolean()) {
            AmazonListingReview newReview = AmazonListingReview
                    .parseAmazonReviewJson(reviewElement);

            // 1
            if(StringUtils.isNotBlank(StringUtils.difference(review.review, newReview.review)))
                review.comment(
                        String.format("[%s] - Review At %s", review.review, Dates.date2Date()));

            // 2
            review.updateAttr(newReview);
        }
        return reviewElement;
    }

}
