package controllers;

import models.view.RewAndFdbk;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * 对 Ticket 的分页页面的控制器
 * User: wyattpan
 * Date: 8/9/12
 * Time: 4:20 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class TicketAnalyzes extends Controller {
    public static void index() {
        render();
    }

    public static void reviews(Date from, Date to, String col) {
        List<RewAndFdbk> reviews = RewAndFdbk.reviews(from, to);
        RewAndFdbk.sortByColumn(reviews, col);
        render(reviews);
    }

    public static void feedbacks(Date from, Date to, String col) {
        List<RewAndFdbk> feedbacks = RewAndFdbk.feedbacks(from, to);
        RewAndFdbk.sortByColumn(feedbacks, col);
        render(feedbacks);
    }
}
