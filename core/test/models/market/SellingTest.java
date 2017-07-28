package models.market;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import helper.Webs;
import jobs.driver.DriverJob;
import jobs.driver.GJob;
import models.embedded.AmazonProps;
import models.product.Attach;
import models.product.Product;
import org.junit.Before;
import org.junit.Ignore;
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
    //@Before
    public void setUp() {
        FactoryBoy.deleteAll();
    }

    //@Ignore
    //@Test
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
        Webs.devLogin(selling.account);
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

    //@Test
    public void testIsMSkuValid() {
        Selling s = FactoryBoy.build(Selling.class, new BuildCallback<Selling>() {
            @Override
            public void build(Selling target) {
                target.merchantSKU = "88HSSG1-B,885618738909";
                target.aps.upc = "885618738909";
            }
        });
        boolean flag = s.isMSkuValid();

        assertThat(flag, is(true));
    }

    //@Test
    public void testPatchSkuToListing() {
        final Product product = FactoryBoy.create(Product.class);
        FactoryBoy.create(Attach.class, new BuildCallback<Attach>() {
            @Override
            public void build(Attach target) {
                target.fid = product.sku;
                target.p = Attach.P.SKU;
            }
        });
        final Selling selling = FactoryBoy.build(Selling.class, "withListing", new BuildCallback<Selling>() {
            @Override
            public void build(Selling target) {
                target.merchantSKU = String.format("%s,%s", product.sku, target.aps.upc);
            }
        });
        selling.buildFromProduct();
        // 1. Selling/Listing 要保存
        // 2. 产生一个 Feed
        // 3. 产生一个 GJob
        assertThat(Selling.count(), is(1l));
        assertThat(Listing.count(), is(1l));
        assertThat(Feed.count(), is(1l));
        assertThat(GJob.count(), is(1l));
    }

    @Test
    public void testGetMappingSKU() {
        Selling s = new Selling();
        Selling.getMappingSKU("72FLMINI-BF,889280822799|A_US|131");
    }
}
