package controllers;

import play.mvc.Controller;

import java.util.Date;

/**
 * 对 Ticket 的分页页面的控制器
 * User: wyattpan
 * Date: 8/9/12
 * Time: 4:20 PM
 */
public class TicketAnalyzes extends Controller {
    public static void index() {
        render();
    }

    public static void reviews(Date from, Date to) {
        render();
    }
}
