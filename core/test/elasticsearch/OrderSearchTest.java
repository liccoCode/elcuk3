package elasticsearch;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 10/24/13
 * Time: 4:00 PM
 */
public class OrderSearchTest extends UnitTest {
    @Test
    public void testClient() {
        Client client = new TransportClient().addTransportAddress(
                new InetSocketTransportAddress("gengar.easya.cc", 9300));
        GetResponse resp = client.prepareGet("elcuk2", "order", "203-8671889-8524331").execute().actionGet();
        Map<String, Object> source = resp.getSource();
        assertThat(source.get("orderId").toString(), is("203-8671889-8524331"));

        // utc -> +8
        assertThat(new DateTime(source.get("createDate").toString()).toString("yyyy-MM-dd HH:mm:ss"),
                // 2013-09-24 22:55:55
                is("2013-09-25 06:55:55"));
    }
}
