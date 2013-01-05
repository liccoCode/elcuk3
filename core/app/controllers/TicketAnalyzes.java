package controllers;

import models.product.Category;
import models.support.Ticket;
import models.view.dto.RewAndFdbkDTO;
import play.cache.CacheFor;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;
import query.TicketQuery;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 对 Ticket 的分页页面的控制器
 * User: wyattpan
 * Date: 8/9/12
 * Time: 4:20 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class TicketAnalyzes extends Controller {

    @CacheFor(id = "TicketAnalyzes.index")
    @Check("ticketanalyzes.index")
    public static void index() {
        List<Category> cates = Category.all().fetch();
        render(cates);
    }

    /**
     * 查看 Ticket 的总体解决情况
     * 1. 当天还需要处理的 Ticket
     * 2. 30 天内的 Ticket 处理情况
     * - a: 成功处理
     * - b: 悬而未决
     * - c: 处理失败
     */
    @CacheFor(id = "ticket_overview", value = "10min")
    public static void overview(Boolean full) {
        if(full == null) full = true;
        long ticketsWaitForReply = TicketQuery.waitForReply(Ticket.T.TICKET, null);
        long feedbacksWaitForReply = TicketQuery.waitForReply(Ticket.T.FEEDBACK, null);
        long reviewsWaitForReply = TicketQuery.waitForReply(Ticket.T.REVIEW, null);
        Map<String, F.T3<Long, Long, Float>> day90SuccInfo = TicketQuery.dealSuccess(90);
        Map<String, F.T3<Long, Long, Float>> day90FailInfo = TicketQuery.dealFailed(90);
        Map<String, F.T3<Long, Long, Float>> day90HangupInfo = TicketQuery.dealHangup(90);
        Map<String, F.T3<Long, Long, Float>> day90DealingInfo = TicketQuery.dealing(90);

        render(full, ticketsWaitForReply, feedbacksWaitForReply, reviewsWaitForReply,
                day90SuccInfo, day90FailInfo, day90HangupInfo, day90DealingInfo);
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
