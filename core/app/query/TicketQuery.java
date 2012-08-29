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
     * 某一个用户接手处理的但是还没有处理完的, 时间端内有回复的 Ticket 数量
     * @param from
     * @param to
     * @param user
     * @return
     */
    public static long userTakedButNotCloseTickets(Date from, Date to, User user) {
        return (Long) DBUtils.row("SELECT count(DISTINCT(tr.ticket_id)) as c FROM TicketResponse tr LEFT JOIN Ticket t on t.id=tr.ticket_id " +
                "WHERE t.resolver_id=? AND tr.created>=? AND tr.created<=?", user.id, Dates.morning(from), Dates.night(to)).get("c");
    }

    public static long noUserTakedButNotCloseTickets(Date from, Date to) {
        return (Long) DBUtils.row("SELECT count(DISTINCT(tr.ticket_id)) as c FROM TicketResponse tr LEFT JOIN Ticket t on t.id=tr.ticket_id " +
                "WHERE t.resolver_id IS NULL AND tr.created>=? AND tr.created<=?", Dates.morning(from), Dates.night(to)).get("c");
    }

    /**
     * 除开指定状态的 Ticket 数量; state 为 null 则是所有
     * @return
     */
    public static long unTakeAndNotCloseTickets(TicketState state) {
        if(state == null) {
            return (Long) DBUtils.row("SELECT count(*) as c FROM Ticket WHERE resolver_id IS NULL").get("c");
        } else {
            return (Long) DBUtils.row("SELECT count(*) as c FROM Ticket WHERE resolver_id IS NULL AND state!=?;", state).get("c");
        }
    }

    public static long unTakeAndNotCloseTickets(TicketState state, Date from, Date to) {
        if(state == null) {
            return (Long) DBUtils.row("SELECT count(*) as c FROM Ticket WHERE resolver_id IS NULL AND createAt>=? AND createAt<=?;", from, to).get("c");
        } else {
            return (Long) DBUtils.row("SELECT count(*) as c FROM Ticket WHERE resolver_id IS NULL AND state!=? AND createAt>=? AND createAt<=?;", state, from, to).get("c");
        }
    }
}
