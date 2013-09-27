package controllers;

import jobs.FeedbackCheckJob;
import jobs.FeedbackCrawlJob;
import models.market.Account;
import models.market.Feedback;
import models.market.M;
import models.product.Category;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

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
            render(feedback, cats);
        }
    }

    public static void check(String id) {
        Feedback feedback = Feedback.findById(id);
        String html = FeedbackCheckJob.ajaxLoadFeedbackOnOrderDetailPage(feedback.account, feedback.orderId);
        feedback.isRemove = FeedbackCheckJob.isFeedbackRemove(html);
        if(FeedbackCheckJob.isRequestSuccess(html))
            flash.success("刷新成功. [%s -> %s]", feedback.orderId, feedback.isRemove);
        else
            flash.error("刷新失败. [%s -> %s]", feedback.orderId, feedback.isRemove);
        redirect("/feedbacks/show/" + feedback.orderId);
    }

    public static void fixOnePage(long aid, M m, int p) {
        long begin = System.currentTimeMillis();
        Account account = Account.findById(aid);
        List<Feedback> feedbacks = FeedbackCrawlJob.fetchAccountFeedbackOnePage(account, m, p);
        renderText("Success: %s, passed: %s ms", feedbacks.size(), System.currentTimeMillis() - begin);
    }
}
