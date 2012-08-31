package controllers;

import models.product.Category;
import models.view.RewAndFdbkDTO;
import play.cache.CacheFor;
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

    @CacheFor(id = "TicketAnalyzes.index")
    public static void index() {
        List<Category> cates = Category.all().fetch();
        render(cates);
    }

    public static void reviews(Date from, Date to, String col) {
        List<RewAndFdbkDTO> reviews = RewAndFdbkDTO.reviews(from, to);
        RewAndFdbkDTO.sortByColumn(reviews, col);
        render(reviews);
    }

    public static void feedbacks(Date from, Date to, String col) {
        List<RewAndFdbkDTO> feedbacks = RewAndFdbkDTO.feedbacks(from, to);
        RewAndFdbkDTO.sortByColumn(feedbacks, col);
        render(feedbacks);
    }
}
