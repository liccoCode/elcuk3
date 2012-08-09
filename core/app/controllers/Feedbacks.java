package controllers;

import models.market.Feedback;
import models.product.Category;
import models.support.Ticket;
import models.support.TicketReason;
import models.support.TicketState;
import models.view.Ret;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;
import java.util.Set;

/**
 * 管理 Feedbacks 的功能
 * User: wyattpan
 * Date: 3/15/12
 * Time: 1:41 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Feedbacks extends Controller {
    public static void index() {
        F.T2<List<Ticket>, List<Ticket>> newFdbk = Ticket.tickets(Ticket.T.FEEDBACK, TicketState.NEW, true);
        F.T2<List<Ticket>, List<Ticket>> needTwoFdbk = Ticket.tickets(Ticket.T.FEEDBACK, TicketState.TWO_MAIL, true);
        List<Ticket> noRespFeedbacks = Ticket.tickets(Ticket.T.FEEDBACK, TicketState.NO_RESP, false)._1;
        List<Ticket> newMsgFeedbacks = Ticket.tickets(Ticket.T.FEEDBACK, TicketState.NEW_MSG, false)._1;

        renderArgs.put("newFeedbacks", newFdbk._1);
        renderArgs.put("newOverdueFeedbacks", newFdbk._2);
        renderArgs.put("twoMailFeedbacks", needTwoFdbk._1);
        renderArgs.put("twoMailOverdueFeedbacks", needTwoFdbk._2);
        render(noRespFeedbacks, newMsgFeedbacks);
    }

    public static void show(String oid) {
        Feedback feedback = Feedback.findById(oid);
        List<Category> cats = feedback.relateCats();
        F.T2<Set<TicketReason>, Set<TicketReason>> unTagAndAll = feedback.untagAndAllTags();
        render(feedback, cats, unTagAndAll);
    }

    public static void iFeedback() {
        Ticket.initFeedbackFix();
        renderJSON(new Ret());
    }

}
