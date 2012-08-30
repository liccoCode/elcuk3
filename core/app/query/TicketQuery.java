package query;

import helper.DBUtils;
import helper.Dates;
import models.User;
import models.support.TicketState;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/29/12
 * Time: 6:29 PM
 */
public class TicketQuery {

    /**
     * 用户在时间段内回复的邮件数量; 用户为 null 则查询的为未知用户
     *
     * @param from
     * @param to
     * @param user
     * @return
     */
    public static long userEmailsCount(Date from, Date to, User user) {
        if(user == null) {
            return (Long) DBUtils.row("SELECT COUNT(*) AS c FROM TicketResponse tr " +
                    "LEFT JOIN Ticket t on t.id=tr.ticket_id " +
                    "WHERE t.resolver_id IS NULL AND tr.created>=? AND tr.created<=?",
                    from, to).get("c");
        } else {
            return (Long) DBUtils.row("SELECT COUNT(*) AS c FROM TicketResponse tr " +
                    "LEFT JOIN Ticket t on t.id=tr.ticket_id " +
                    "WHERE t.resolver_id=? AND tr.created>=? AND tr.created<=?",
                    user.id, from, to).get("c");
        }
    }

    /**
     * 用户在时间段内回复的 Tickets 数; 用户为 null 则查询的为未知用户
     *
     * @param from
     * @param to
     * @return
     */
    public static long userTakeAndMailedTicketsCount(Date from, Date to, User user) {
        if(user == null) {
            return (Long) DBUtils.row("SELECT COUNT(DISTINCT(tr.ticket_id)) AS c FROM TicketResponse tr " +
                    "LEFT JOIN Ticket t ON t.id=tr.ticket_id " +
                    "WHERE t.resolver_id IS NULL AND tr.created>=? AND tr.created<=?;",
                    from, to).get("c");
        } else {
            return (Long) DBUtils.row("SELECT COUNT(DISTINCT(tr.ticket_id)) AS c FROM TicketResponse tr " +
                    "LEFT JOIN Ticket t ON t.id=tr.ticket_id " +
                    "WHERE t.resolver_id=? AND tr.created>=? AND tr.created<=?;",
                    user.id, from, to).get("c");
        }
    }

    /**
     * 用户总共解决的 Ticket
     * @param user
     * @param isClose
     * @return
     */
    public static long userSolvedTicketsCount(User user, boolean isClose) {
        if(isClose) {
            return (Long) DBUtils.row("SELECT COUNT(*) AS c FROM Ticket WHERE resolver_id" + (user != null ? "=" + user.id : " IS NULL") + " AND state='CLOSE'").get("c");
        } else {
            return (Long) DBUtils.row("SELECT COUNT(*) AS c FROM Ticket WHERE resolver_id" + (user != null ? "=" + user.id : " IS NULL") + " AND state!='CLOSE'").get("c");
        }
    }

    /**
     * 某一个用户接手处理的但是还没有处理完的
     *
     * @param user
     * @return
     */
    public static long userTakedButNotCloseTickets(User user) {
        if(user == null) {
            return (Long) DBUtils.row("SELECT count(DISTINCT(tr.ticket_id)) AS c FROM TicketResponse tr LEFT JOIN Ticket t on t.id=tr.ticket_id " +
                    "WHERE t.resolver_id IS NULL").get("c");
        } else {
            return (Long) DBUtils.row("SELECT count(DISTINCT(tr.ticket_id)) AS c FROM TicketResponse tr LEFT JOIN Ticket t on t.id=tr.ticket_id " +
                    "WHERE t.resolver_id=?", user.id).get("c");
        }
    }


    /**
     * 指定时间段内创建的 Tickets, 可选择是关闭了的还是未关闭的.
     *
     * @param from
     * @param to
     * @return
     */
    public static long periodCreateTicketsCount(Date from, Date to, boolean isClose) {
        if(isClose) {
            return (Long) DBUtils.row("SELECT COUNT(*) AS c FROM Ticket WHERE createAt>=? AND createAt<=? AND state='CLOSE'", from, to).get("c");
        } else {
            return (Long) DBUtils.row("SELECT COUNT(*) AS c FROM Ticket WHERE createAt>=? AND createAt<=? AND state!='CLOSE'", from, to).get("c");
        }
    }

    /**
     * 还没有解决完的 Ticket 数量;
     *
     * @return
     */
    public static long solvedTicketsCount(boolean isClose) {
        if(isClose) {
            return (Long) DBUtils.row("SELECT COUNT(*) AS c FROM Ticket WHERE state='CLOSE'").get("c");
        } else {
            return (Long) DBUtils.row("SELECT COUNT(*) AS c FROM Ticket WHERE state!='CLOSE'").get("c");
        }
    }

}
