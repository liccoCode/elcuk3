package controllers;

import jobs.FeedbackInfoFetchJob;
import models.market.Feedback;
import models.product.Category;
import models.support.Ticket;
import models.support.TicketReason;
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
@With({GlobalExceptionHandler.class, Secure.class})
public class Feedbacks extends Controller {

    public static void show(String id) {
        Feedback feedback = Feedback.findById(id);
        if(feedback == null) {
            redirect("/Orders/show/" + id);
        } else {
            List<Category> cats = feedback.relateCats();
            F.T2<Set<TicketReason>, Set<TicketReason>> unTagAndAll = feedback.untagAndAllTags();
            render(feedback, cats, unTagAndAll);
        }
    }

    public static void check(String id) {
        Feedback feedback = Feedback.findById(id);
        String html = FeedbackInfoFetchJob
                .fetchAmazonFeedbackHtml(feedback.account, feedback.orderId);
        feedback.isRemove = FeedbackInfoFetchJob.isFeedbackRemove(html);
        if(FeedbackInfoFetchJob.isRequestSuccess(html))
            flash.success("刷新成功. [%s -> %s]", feedback.orderId, feedback.isRemove);
        else
            flash.error("刷新成功. [%s -> %s]", feedback.orderId, feedback.isRemove);
        redirect("/feedbacks/show/" + feedback.orderId);
    }

    public static void iFeedback() {
        Ticket.initFeedbackFix();
        renderJSON(new Ret());
    }

}
