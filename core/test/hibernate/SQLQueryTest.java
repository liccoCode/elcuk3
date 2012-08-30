package hibernate;

import helper.DBUtils;
import models.User;
import models.market.Orderr;
import models.support.Ticket;
import models.support.TicketState;
import models.view.TicketPost;
import org.joda.time.DateTime;
import org.junit.Test;
import play.db.DB;
import play.db.helper.SqlSelect;
import play.test.UnitTest;
import play.utils.FastRuntimeException;
import query.TicketQuery;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/11/12
 * Time: 3:51 PM
 */
public class SQLQueryTest extends UnitTest {
    @Test
    public void testSQLQuery() {
        System.out.println(new SqlSelect().select("createDate, price, quantity, currency, usdCost").from("OrderItem").limit(20).toString());
    }

    @Test
    public void testDBUtils() {
        String orderId = "026-0051051-3786714";
        Map<String, Object> row = DBUtils.row("select * from Orderr where orderId=?", orderId);
        assertEquals(orderId, row.get("orderId"));
        assertEquals("cl64np52w7hk170@marketplace.amazon.co.uk", row.get("email"));
        assertEquals("AMAZON_UK", row.get("market"));
        assertEquals("A2CSHTENJAB293", row.get("userid"));


        try {
            DBUtils.row("select * from Orderr");
        } catch(FastRuntimeException e) {
            assertEquals("play.utils.FastRuntimeException: Only Deal one Row!", e.getMessage());
        }
    }

    @Test
    public void testDBUtilsRows() {
        List<Map<String, Object>> rows = DBUtils.rows("select * from Orderr limit 3");
        assertEquals(3, rows.size());
        System.out.println(rows);
    }

    @Test
    public void testOrderItemCount() {
        Orderr order = Orderr.findById("026-0210035-5030756");
        assertEquals(2l, order.itemCount().longValue());
    }

    @Test
    public void testFont() {
        System.out.println(Ticket.frontPageTable(DateTime.now().minusMonths(3).toDate(), DateTime.now().toDate()));
    }

}
