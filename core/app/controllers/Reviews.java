package controllers;

import com.google.gson.JsonElement;
import helper.J;
import jobs.ReviewInfoFetchJob;
import models.market.AmazonListingReview;
import models.market.Feedback;
import models.market.Orderr;
import models.support.Ticket;
import models.support.TicketReason;
import models.support.TicketState;
import models.view.Ret;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * 售后的 Review 与 Feedback 的处理
 * User: wyattpan
 * Date: 7/26/12
 * Time: 11:46 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Reviews extends Controller {

    @Check("reviews.index")
    public static void index() {
        F.T2<List<Ticket>, List<Ticket>> newT2 = Ticket.tickets(Ticket.T.REVIEW, TicketState.NEW, true);
        F.T2<List<Ticket>, List<Ticket>> needTwoT2 = Ticket.tickets(Ticket.T.REVIEW, TicketState.TWO_MAIL, true);
        List<Ticket> noRespReviews = Ticket.tickets(Ticket.T.REVIEW, TicketState.NO_RESP, false)._1;
        List<Ticket> newMsgReviews = Ticket.tickets(Ticket.T.REVIEW, TicketState.NEW_MSG, false)._1;
        List<Ticket> preCloseReviews = Ticket.tickets(Ticket.T.REVIEW, TicketState.PRE_CLOSE, false)._1;
        List<Ticket> closed = Ticket.tickets(Ticket.T.REVIEW, TicketState.CLOSE, false, 30)._1;


        renderArgs.put("newReviews", newT2._1);
        renderArgs.put("newOverdueReviews", newT2._2);
        renderArgs.put("twoMailReviews", needTwoT2._1);
        renderArgs.put("twoMailOverdueReviews", needTwoT2._2);
        int totalNeedDealReview = newT2._1.size() + newT2._2.size() + needTwoT2._1.size() + needTwoT2._2.size() +
                noRespReviews.size() + newMsgReviews.size() + preCloseReviews.size();
        render(noRespReviews, newMsgReviews, totalNeedDealReview, preCloseReviews, closed);
    }

    public static void show(String rid) {
        AmazonListingReview review = AmazonListingReview.findByReviewId(rid);
        if(review.orderr != null)
            renderArgs.put("f", Feedback.findById(review.orderr.orderId));

        F.T2<List<TicketReason>, List<String>> reasons = review.unTagedReasons();
        renderArgs.put("unTagReasons", reasons._1);
        render(review);
    }

    public static void tryOrder(String rid) {
        AmazonListingReview review = AmazonListingReview.findByReviewId(rid);
        Orderr ord = review.tryToRelateOrderByUserId();
        if(ord != null) {
            review.orderr = ord;
            review.save();
        }
        renderJSON(new Ret(ord != null));
    }

    public static void sync(String rid) {
        AmazonListingReview review = AmazonListingReview.findByReviewId(rid);
        JsonElement el = ReviewInfoFetchJob.syncSingleReview(review);
        if(review.ticket != null) {
            ReviewInfoFetchJob.checkReviewDealState(review.ticket, el);
            review.ticket.lastSyncTime = new Date();
            review.ticket.save();
        }
        renderJSON(new Ret());
    }


    /**
     * 初始化代码, 需要删除的
     */
    public static void iReview() {
        Ticket.initReviewFix();
        renderJSON(new Ret());
    }
}
