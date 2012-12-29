package controllers;

import com.google.gson.JsonObject;
import helper.J;
import helper.OsTicket;
import helper.Webs;
import jobs.TicketStateSyncJob;
import models.ElcukRecord;
import models.User;
import models.support.Ticket;
import models.support.TicketReason;
import models.view.Ret;
import models.view.post.TicketPost;
import play.data.validation.Validation;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Arrays;
import java.util.List;

/**
 * 与 Ticket 统一相关的操作
 * User: wyattpan
 * Date: 8/9/12
 * Time: 1:38 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Tickets extends Controller {

    @Check("tickets.index")
    public static void index(TicketPost p) {
        if(p == null) p = new TicketPost();
        List<Ticket> tickets = p.query();
        List<F.T2<String, Long>> userIds = User.userIds();
        render(tickets, p, userIds);
    }

    public static void show(long tid) {
        Ticket ticket = Ticket.findById(tid);
        List<TicketReason> reasons = TicketReason.find("ORDER BY category.categoryId").fetch();
        render(ticket, reasons);
    }


    /**
     * 给 Ticket 添加原因[Review, Feedback 通用]
     */
    public static void tagReason(String reason, long ticketId) {
        Ticket ticket = Ticket.findById(ticketId);
        renderJSON(J.G(ticket.tagReason(TicketReason.findByReason(reason))));
    }

    /**
     * 给 Ticket 接触原因[Review, Feedback 通用]
     *
     * @param reason
     * @param ticketId
     */
    public static void unTagReason(String reason, long ticketId) {
        Ticket ticket = Ticket.findById(ticketId);
        renderJSON(J.G(ticket.unTagReason(TicketReason.findByReason(reason))));
    }

    public static void iTakeIt(long tid) {
        Ticket ticket = Ticket.findById(tid);
        ticket.resolver = User.findByUserName(ElcukRecord.username());
        ticket.save();
        renderJSON(new Ret());
    }

    public static void comment(long tid, String comment) {
        validation.required(comment);
        if(Validation.hasErrors()) {
            renderJSON(new Ret(Webs.V(Validation.errors())));
        }
        Ticket ticket = Ticket.findById(tid);
        ticket.memo = comment;
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

    public static void toggleStar(long tid) {
        Ticket ticket = Ticket.findById(tid);
        boolean isStart = ticket.toggleStar();
        renderJSON(new Ret(true, isStart ? "1" : "0"));
    }

    /**
     * 通过 TicketId 向 osTicket 更新
     *
     * @param tid
     */
    public static void sync(long tid) {
        Ticket ticket = Ticket.findByOsTicketId(tid + "");
        // 首先对 OsTicketId 加载, 然后再对 TicketId 进行加载尝试
        if(ticket == null) ticket = Ticket.findById(tid);
        if(ticket == null) {
            renderJSON(new Ret("Ticket " + tid + " is not exist."));
        }
        JsonObject syncsJsonDetails = OsTicket
                .communicationWithOsTicket(Arrays.asList(ticket.osTicketId()));
        F.T2<List<Ticket>, List<Ticket>> ticketT2 = TicketStateSyncJob
                .syncOsTicketDetailsIntoSystem(syncsJsonDetails, Arrays.asList(ticket));
        if(ticketT2._1.size() != 0) {
            renderJSON(new Ret(true, ticketT2._1.get(0).osTicketId()));
        } else {
            renderJSON(new Ret(ticketT2._2.get(0).osTicketId()));
        }
    }

    public static void syncAll() {
        TicketStateSyncJob job = new TicketStateSyncJob();
        job.now();
        renderJSON(new Ret(true, "已经提交, 正常情况下, 1 分钟后即可刷新生效."));
    }
}
