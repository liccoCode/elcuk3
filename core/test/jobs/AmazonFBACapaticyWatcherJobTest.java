package jobs;

import helper.HTTP;
import helper.Webs;
import models.market.Account;
import models.product.Whouse;
import org.apache.commons.lang.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.Play;
import play.libs.IO;
import play.test.UnitTest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-1-15
 * Time: PM12:14
 */
public class AmazonFBACapaticyWatcherJobTest extends UnitTest {
    Document amazon;

    @Before
    public void initPage() {
        amazon = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/amazon.html")));
    }

    @Test
    public void AmazonFBACapaticyWatcherJob() throws ExecutionException, InterruptedException, TimeoutException, IOException, ClassNotFoundException {
        Account acc = Account.findById(NumberUtils.toLong("1"));
        Webs.dev_login(acc);
        AmazonFBACapaticyWatcherJob worker = new AmazonFBACapaticyWatcherJob();
        List<Whouse> whouses = Whouse.find("type=?", Whouse.T.FBA).fetch();
        for(Whouse whouse : whouses) {
            if(whouse.account == null) {
                Logger.warn("Whouse %s[%s] is FBA but do not have an relative accout!",
                        whouse.name(), whouse.id);
                continue;
            }
            if(!Account.isLoginEnd(amazon)) {
                Logger.warn("Account %s is not login, skip this one.", whouse.account.prettyName());
                continue;
            }
            whouse.capaticyContent = worker.fbaCapacityWidgetDiv(amazon, whouse);
            whouse.save();
        }
    }

}
