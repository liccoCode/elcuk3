package models.market;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import helper.Webs;
import models.embedded.AmazonProps;
import org.junit.Test;
import play.test.UnitTest;

import java.io.IOException;

import static org.hamcrest.core.Is.is;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 9/11/13
 * Time: 8:23 PM
 */
public class SellingTest extends UnitTest {
    @Test
    public void testSyncFromAmazon() throws IOException, ClassNotFoundException {
        Selling selling = FactoryBoy.build(Selling.class, "de", new BuildCallback<Selling>() {
            @Override
            public void build(Selling target) {
                target.merchantSKU = "71GGN72013-BPU,882999934562";
                target.asin = "B00EXE7AC0";
                target.market = M.AMAZON_IT;
                target.aps.upc = "882999934562";
            }
        });
        Webs.dev_login(selling.account);
        selling.syncFromAmazon();

        AmazonProps aps = selling.aps;
        assertThat(aps.standerPrice, is(25.99f));
        assertThat(aps.salePrice, is(18.99f));

        assertThat(aps.title,
                is("EasyAcc custodia in pelle per Google Nexus 7 2013 Smart Cover Case con funzione di sostegno e presentazione - Google Nexus 7 2013 Accessori Custodia Protettiva(nero)"));
        assertThat(aps.manufacturer, is("EasyAcc"));

        assertThat(aps.keyFeturess.size(), is(5));
        assertThat(aps.keyFeturess.get(0),
                is("Exterior: Synthetic PU leather, with good wear resistant and touch comfortable; Interior: Soft lint, with good touch feeling, safeguards and cleans the screen"));
        assertThat(aps.keyFeturess.get(4),
                is("Built-in handy stylus holder, never lose your stylus;  Magnetic closure"));

        assertThat(aps.searchTermss.size(), is(5));
        assertThat(aps.searchTermss.get(0), is("Gecko custodia protettiva cover per Google Nexus 7"));
        assertThat(aps.searchTermss.get(4), is("cover protezione e supporto rotazione Nexus 7 2013"));
    }
}
