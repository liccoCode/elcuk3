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
        String html = IO.readContentAsString(Play.getFile("test/html/jobs/028-7358669-9705905.html"));
        Orderr orderr = FactoryBoy.build(Orderr.class);
        OrderInfoFetchJob.orderDetailUserIdAndEmailAndPhone(orderr, html);

        assertThat(orderr.email, is("x0k1mslx7rcvm72@marketplace.amazon.de"));
        assertThat(orderr.userid, is("A2CVVXOBORYLX8"));
        assertThat(orderr.phone, is("0177/3193037"));
    }
}
