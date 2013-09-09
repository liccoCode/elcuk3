package models.product;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import helper.Webs;
import models.embedded.AmazonProps;
import models.market.M;
import models.market.Selling;
import org.apache.http.NameValuePair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.libs.F;
import play.test.UnitTest;

import java.io.IOException;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 9/6/13
 * Time: 6:07 PM
 */
public class ProductTest extends UnitTest {
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
    }

    public F.T2<Product, Selling> initData() throws IOException, ClassNotFoundException {
        Product p = FactoryBoy.create(Product.class);
        Selling sell = FactoryBoy.build(Selling.class, "de", new BuildCallback<Selling>() {
            @Override
            public void build(Selling target) {
                target.merchantSKU = "73SMS4MINI-BVMGL,881165105706";
                target.market = M.AMAZON_IT;
                AmazonProps aps = target.aps;
                aps.upc = "881165105706";
            }
        });
        p.category.settings.amazonITCategory = "consumer_electronics/consumer_electronics";

        Webs.dev_login(sell.account);
        return new F.T2<Product, Selling>(p, sell);
    }

    @Test
    public void testSaleAmazonStep3() throws IOException, ClassNotFoundException {
        F.T2<Product, Selling> t2 = initData();
        Product p = t2._1;
        Selling sell = t2._2;
        p.saleAmazon(sell);
    }

    @Ignore
    @Test
    public void testSaleAmazonStep2() throws IOException, ClassNotFoundException {
        F.T2<Product, Selling> t2 = initData();
        Product p = t2._1;
        Selling sell = t2._2;

        Set<NameValuePair> params = p.saleAmazonStep2(sell);
        assertThat(params.size(), is(5));
        for(NameValuePair pair : params) {
            String name = pair.getName();
            String val = pair.getValue();
            if("productType".equals(name))
                assertThat(val, is("CONSUMER_ELECTRONICS"));
            else if("itemType".equals(name))
                assertThat(val, is(""));
            else if("displayPath".equals(name))
                assertThat(val, is("All Product Categories/ConsumerElectronics/ConsumerElectronics"));
            else if("newCategory".equals(name))
                assertThat(val, is(p.category.settings.amazonITCategory));
            else if("category".equals(name))
                assertThat(val, is(""));
        }
    }

    @Ignore
    @Test
    public void testSaleAmazonStep1() throws IOException, ClassNotFoundException {
        F.T2<Product, Selling> t2 = initData();
        Product p = t2._1;
        Selling sell = t2._2;
        Set<NameValuePair> params = p.saleAmazonStep1(sell);
        assertThat(params.size(), is(2));
        for(NameValuePair pair : params) {
            String name = pair.getName();
            String val = pair.getValue();
            if("encoded_session_hidden_map".equals(name))
                assertThat(val, is(notNullValue()));
            else if("activeClientTimeOnTask".equals(name))
                assertThat(val, is(""));
        }
    }
}
