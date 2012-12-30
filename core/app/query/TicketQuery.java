package query;

import helper.DBUtils;
import models.support.Ticket;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/29/12
 * Time: 6:29 PM
 */
public class TicketQuery {
    /**
     * 等待回信的 Ticket/Feedback/Review
     *
     * @param type
     * @return
     */
    public static long waitForReply(Ticket.T type, Long userid) {
        StringBuilder sql = new StringBuilder("SELECT count(*) as c FROM Ticket WHERE ")
                .append("type=? AND responseTimes<=messageTimes AND state NOT IN ('PRE_CLOSE', 'CLOSE')");
        if(userid != null && userid > 0)
            sql.append(" AND resolver_id=").append(userid);
        return (Long) DBUtils.row(sql.toString(), type.name()).get("c");
    }

}
