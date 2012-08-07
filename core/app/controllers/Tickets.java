package controllers;

import com.google.gson.JsonObject;
import helper.J;
import jobs.TicketStateSyncJob;
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

import java.util.Arrays;
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
        F.T2<List<Ticket>, List<Ticket>> newT2 = Ticket.tickets(Ticket.T.REVIEW, TicketState.NEW, true);
        F.T2<List<Ticket>, List<Ticket>> needTwoT2 = Ticket.tickets(Ticket.T.REVIEW, TicketState.TWO_MAIL, true);
        List<Ticket> noRespTickets = Ticket.tickets(Ticket.T.REVIEW, TicketState.NO_RESP, false)._1;
        List<Ticket> newMsgTickts = Ticket.tickets(Ticket.T.REVIEW, TicketState.NEW_MSG, false)._1;


        renderArgs.put("newTickets", newT2._1);
        renderArgs.put("newOverdueTickets", newT2._2);
        renderArgs.put("twoMailTickets", needTwoT2._1);
        renderArgs.put("twoMailOverdueTickets", needTwoT2._2);
        int totalNeedDealReview = newT2._1.size() + newT2._2.size() + needTwoT2._1.size() + needTwoT2._2.size() + noRespTickets.size() + newMsgTickts.size();
        render(noRespTickets, newMsgTickts, totalNeedDealReview);
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
     * 关闭这个 Ticket 的原因.
     *
     * @param tid
     * @param reason
     */
    public static void close(long tid, String reason) {
        Ticket ticket = Ticket.findById(tid);
        renderJSON(J.G(ticket.close(reason)));
    }

    /**
     * 通过 ReviewId 向 OsTicket 进行更新
     *
     * @param rid
     */
    public static void sync(String rid) {
        AmazonListingReview review = AmazonListingReview.findByReviewId(rid);
        JsonObject syncsJsonDetails = TicketStateSyncJob.communicationWithOsTicket(Arrays.asList(review.ticket.osTicketId()));
        F.T2<List<Ticket>, List<Ticket>> ticketT2 = TicketStateSyncJob.syncOsTicketDetailsIntoSystem(syncsJsonDetails, Arrays.asList(review.ticket));
        if(ticketT2._1.size() != 0) {
            renderJSON(new Ret(true, ticketT2._1.get(0).osTicketId()));
        } else {
            renderJSON(new Ret(ticketT2._2.get(0).osTicketId()));
        }
    }

    @Check("manager")
    public static void syncAll() {
        TicketStateSyncJob job = new TicketStateSyncJob();
        job.now();
        renderJSON(new Ret(true, "已经提交, 正常情况下, 1 分钟后即可刷新生效."));
    }

    /**
     * 初始化代码, 需要删除的
     */
    public static void iReview() {
        Ticket.initReviewFix();
        renderJSON(new Ret());
    }
}
