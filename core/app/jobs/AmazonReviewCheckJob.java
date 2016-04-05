package jobs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import helper.Crawl;
import helper.Dates;
import helper.LogUtils;
import models.Jobex;
import models.market.AmazonListingReview;
import models.market.Listing;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
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
 * @deprecated
 */
public class AmazonReviewCheckJob extends Job {
    @Override
    public void doJob() {
        long begin = System.currentTimeMillis();
        if(!Jobex.findByClassName(AmazonReviewCheckJob.class.getName()).isExcute()) return;
        // 抓取没有删除, 并且按照最后更新时间的升序排列;
        // 自己或者其他让的 Listing 都检查.
        List<AmazonListingReview> reviews = AmazonListingReview.find(
                "isRemove=false AND createDate>=? ORDER BY updateAt ASC",
                DateTime.now().minusDays(70).toDate()).fetch(20);
        for(AmazonListingReview review : reviews) {

            AmazonListingReview newReview = null;
            try {
                //停止3000毫秒
                Thread.sleep(3000);

                JsonElement reviewElement = Crawl
                        .crawlReview(Listing.unLid(review.listingId)._2.toString(), review.reviewId);
                JsonObject reviewObj = reviewElement.getAsJsonObject();
                if(reviewObj.get("isRemove") == null)
                    continue;
                if(reviewObj.get("isRemove").getAsBoolean()) {
                    review.isRemove = true;
                } else {
                    newReview = AmazonListingReview.parseAmazonReviewJson(reviewElement);
                    // 1
                    if(StringUtils.isNotBlank(StringUtils.difference(review.review, newReview.review)))
                        review.comment(String.format("[%s] - Review At %s", review.review, Dates.date2Date()));

                    // 2
                    review.updateAttr(newReview);
                }
                review.save();
            } catch(Exception e) {
                if(newReview != null) Logger.warn("update attr: %s, %s", newReview.review, newReview.alrId);
                Logger.warn("review check: %s", e.getMessage());
            }
        }

        if(LogUtils.isslow(System.currentTimeMillis() - begin, "AmazonReviewCheckJob")) {
            LogUtils.JOBLOG.info(String
                    .format("AmazonReviewCheckJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
        // 如果对 Listing Review 还有其他检查, 可以在这里编写
    }
}
