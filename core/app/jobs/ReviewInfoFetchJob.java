package jobs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ext.LinkHelper;
import helper.Crawl;
import helper.Dates;
import helper.HTTP;
import models.market.AmazonListingReview;
import models.market.Listing;
import models.market.M;
import models.support.Ticket;
import models.support.TicketState;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.jobs.Job;
import play.libs.F;

import java.util.Date;
import java.util.List;

/**
 * 针对每一个有问题的 Review 进行检查, 查看 Review 的处理状况.
 * User: wyattpan
 * Date: 8/24/12
 * Time: 5:56 PM
 */
public class ReviewInfoFetchJob extends Job {
    @Override
    public void doJob() {
        int size = 30;
        if(Play.mode.isDev()) size = 10;
        List<Ticket> tickets = Ticket.find("type=? AND isSuccess=? AND state NOT IN (?,?) ORDER BY lastSyncTime",
                Ticket.T.REVIEW, false, TicketState.PRE_CLOSE, TicketState.CLOSE).fetch(size);
        Logger.info("ReviewInfoFetchJob to Amazon sync %s tickets.", tickets.size());
        for(Ticket ticket : tickets) {
            JsonElement el = ReviewInfoFetchJob.syncSingleReview(ticket.review);
            ReviewInfoFetchJob.checkReviewDealState(ticket, el);
            ticket.lastSyncTime = new Date();
            ticket.save();
        }
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
        JsonElement reviewElement = Crawl.crawlReview(splitListingId._2.toString(), review.reviewId);

        JsonObject reviewObj = reviewElement.getAsJsonObject();
        if(!reviewObj.get("isRemove").getAsBoolean()) {
            AmazonListingReview newReview = AmazonListingReview.parseAmazonReviewJson(reviewElement);

            // 1
            if(StringUtils.isNotBlank(StringUtils.difference(review.review, newReview.review)))
                review.comment(String.format("[%s] - Review At %s", review.review, Dates.date2Date()));

            // 2
            review.updateAttr(newReview);
        }
        return reviewElement;
    }

    /**
     * 对 Review Ticket 进行状态检查处理
     * 1. 检查是否被删除, 如果被删除, 那么 Ticket State 进入 PRE_CLOSE
     *
     * @param ticket
     */
    public static void checkReviewDealState(Ticket ticket, JsonElement reviewElement) {
        if(ticket.review == null) {
            Logger.warn("ReviewInfoFetchJob deal an no Review Ticket(id|fid) [%s|%s]", ticket.id, ticket.fid);
            return;
        }

        JsonObject reviewObj = reviewElement.getAsJsonObject();

        if(reviewObj.get("isRemove").getAsBoolean()) {
            ticket.review.isRemove = true;
            ticket.isSuccess = ticket.review.isRemove;
            ticket.state = TicketState.PRE_CLOSE;
            ticket.review.comment(String.format("Review 已经被买家自行删除(%s).", Dates.date2Date()));
        }
    }


}
