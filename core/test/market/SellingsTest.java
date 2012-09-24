package market;

import helper.GTs;
import helper.J;
import models.market.Account;
import models.market.OrderItem;
import models.market.Selling;
import org.apache.http.NameValuePair;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;
import play.Play;
import play.libs.F;
import play.template2.IO;
import play.test.FunctionalTest;
import play.test.UnitTest;

import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:59
 */
public class SellingsTest extends UnitTest {
    String html = IO.readContentAsString(Play.getFile("test/html/de.selling.B008CML318.html"));

    @Test
    public void testSellingAPS() {
        Selling selling = Selling.findById("71SMP5100-BHSPU,666346129906|A_DE|2");
        F.T2<Collection<NameValuePair>, Document> t2 = selling.aps.generateDeployAmazonProps(html, selling);
        System.out.println(t2._1);
    }
}
