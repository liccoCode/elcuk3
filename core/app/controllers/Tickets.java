package controllers;

import helper.J;
import models.ElcukRecord;
import models.User;
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

import java.util.List;

/**
 * 售后的 Review 与 Feedback 的处理
 * User: wyattpan
 * Date: 7/26/12
 * Time: 11:46 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Tickets extends Controller {
    public static void index() {
        List<Ticket> tickets = Ticket.reviews(TicketState.NEW);
        render(tickets);
    }

    public static void show(String rid) {
        AmazonListingReview review = AmazonListingReview.findByReviewId(rid);
        if(review.orderr != null)
            renderArgs.put("f", Feedback.findById(review.orderr.orderId));

        F.T2<List<TicketReason>, List<String>> reasons = review.unTagedReasons();
        renderArgs.put("reasons_json", J.json(reasons._2));
        renderArgs.put("cat", review.listing.product.category);
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

    /**
     * 给 Listing 添加原因
     */
    public static void tagReason(String reason, String reviewId) {
        AmazonListingReview review = AmazonListingReview.findByReviewId(reviewId);
        TicketReason lr = TicketReason.findByReason(reason);
        renderJSON(J.G(review.addWhyNegtive(lr)));
    }

    public static void unTagReason(String reason, String reviewId) {
        AmazonListingReview review = AmazonListingReview.findByReviewId(reviewId);
        TicketReason lr = TicketReason.findByReason(reason);
        review.ticket.reasons.remove(lr);
        review.ticket.save();
        renderJSON(J.G(lr));
    }

    public static void iTakeIt(long tid) {
        Ticket ticket = Ticket.findById(tid);
        ticket.resolver = User.findByUserName(ElcukRecord.username());
        ticket.save();
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
