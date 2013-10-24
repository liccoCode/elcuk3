package http;

import com.alibaba.fastjson.JSONObject;
import helper.OsTicket;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/28/12
 * Time: 11:29 AM
 */
public class OsTicketTest extends UnitTest {
    @Test
    public void testPostSyncTicket() {
        JSONObject obj = OsTicket.communicationWithOsTicket(Arrays.asList("436978", "731229"));
        System.out.println(obj);
        assertEquals(true, obj.getBoolean("flag"));
        assertEquals(2, obj.getJSONArray("msgs").size());
    }

    @Test
    public void testCloseTicket() {
        boolean flag = OsTicket.closeOsTicket("678763", "Wyatt", "This is the reason why me close the ticket.(test)");
        assertEquals(true, flag);
    }
}
