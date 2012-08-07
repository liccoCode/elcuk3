package market;

import models.support.Ticket;
import org.junit.Test;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/7/12
 * Time: 11:14 AM
 */
public class TicketStateTest extends UnitTest {
    @Test
    public void testLoadTicket() {
        List<Ticket> tickets = Ticket.checkStateTickets(100);
        List<String> ticketIds = new ArrayList<String>();
        for(Ticket t : tickets) ticketIds.add(t.osTicketId());
        System.out.println(ticketIds);
    }
}
