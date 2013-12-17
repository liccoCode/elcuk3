package models.market;

import factory.FactoryBoy;
import org.apache.http.NameValuePair;
import org.junit.Before;
import org.junit.Test;
import play.libs.F;
import play.test.UnitTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 6/3/13
 * Time: 9:59 AM
 */
public class AccountTest extends UnitTest {
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testLoginAmazonSellerCenterStep1DE() throws IOException {
        Account acc = FactoryBoy.create(Account.class, "de");
        acc.cookieStore().clear();
        F.T2<List<NameValuePair>, String> params = acc.loginAmazonSellerCenterStep1();
        Map<String, String> pairs = new HashMap<String, String>();
        for(NameValuePair pair : params._1) {
            pairs.put(pair.getName(), pair.getValue());
        }

        // 所必须的
        assertThat(params._2, is(containsString("https://sellercentral.amazon.de/ap/widget")));
        assertThat(pairs.get("widgetToken"), is(notNullValue()));
        assertThat(pairs.get("rememberMe"), is("false"));
        assertThat(pairs.get("username"), is(acc.username));
        assertThat(pairs.get("password"), is(acc.password));
    }

    @Test
    public void testLoginAmazonSellerCenter() {
        Account acc = FactoryBoy.create(Account.class, "de");
        acc.cookieStore().clear();
        acc.loginAmazonSellerCenter();

        assertThat(acc.cookie("at-acbde"), is(notNullValue()));
    }


    @Test
    public void testLoginAmazonSellerCenterUS() {
        Account acc = FactoryBoy.create(Account.class, "us");
        acc.cookieStore().clear();
        acc.loginAmazonSellerCenter();

        assertThat(acc.cookie("at-main"), is(notNullValue()));
    }

    @Test
    public void testLoginAmazonSellerCenterUK() {
        Account acc = FactoryBoy.create(Account.class, "uk");
        acc.cookieStore().clear();
        acc.loginAmazonSellerCenter();

        assertThat(acc.cookie("at-acbuk"), is(notNullValue()));
    }
}
