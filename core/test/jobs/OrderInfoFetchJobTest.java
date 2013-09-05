package jobs;

import factory.FactoryBoy;
import models.market.Orderr;
import org.junit.Test;
import play.Play;
import play.libs.IO;
import play.test.UnitTest;

import static org.hamcrest.core.Is.is;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 7/9/13
 * Time: 9:40 AM
 */
public class OrderInfoFetchJobTest extends UnitTest {
    @Test
    public void testOrderDetailUserIdAndEmailAndPhone() {
        String html = IO.readContentAsString(Play.getFile("test/html/jobs/028-0149841-3067566.html"));
        Orderr orderr = FactoryBoy.build(Orderr.class);
        OrderInfoFetchJob.orderDetailUserIdAndEmailAndPhone(orderr, html);

        assertThat(orderr.email, is("jstb0j122m5hzj0@marketplace.amazon.de"));
        assertThat(orderr.userid, is("A1W54C8VWNGTYL"));
        assertThat(orderr.phone, is(""));
    }

    @Test
    public void testOrderDetailUserIdAndEmailAndPhoneUS() {
        String html = IO.readContentAsString(Play.getFile("test/html/jobs/002-0021022-2780229.html"));
        Orderr orderr = FactoryBoy.build(Orderr.class);
        OrderInfoFetchJob.orderDetailUserIdAndEmailAndPhone(orderr, html);

        assertThat(orderr.email, is("lxgg5hc9xntg81k@marketplace.amazon.com"));
        assertThat(orderr.userid, is("A22O0T26R4FF86"));
        assertThat(orderr.phone, is("2068496599"));
    }

    @Test
    public void testOrderDetailUserIdAndEmailAndPhoneDENewPage() {
        String html = IO.readContentAsString(Play.getFile("test/html/jobs/102-9624788-3525012.html"));
        Orderr orderr = FactoryBoy.build(Orderr.class);
        OrderInfoFetchJob.orderDetailUserIdAndEmailAndPhone(orderr, html);

        assertThat(orderr.email, is("ksyx1yry74z6hzy@marketplace.amazon.com"));
        assertThat(orderr.userid, is("AHFIQ2DYR6WSW"));
        assertThat(orderr.phone, is("610-704-5762"));
    }
}
