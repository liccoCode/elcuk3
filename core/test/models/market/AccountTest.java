package models.market;

import factory.FactoryBoy;
import org.apache.http.NameValuePair;
import org.junit.Test;
import play.libs.F;
import play.test.UnitTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 6/3/13
 * Time: 9:59 AM
 */
public class AccountTest extends UnitTest {

    //    @Ignore
    @Test
    public void testLoginAmazonSellerCenter() {
        Account acc = FactoryBoy.build(Account.class, "de");
        acc.cookieStore().clear();
        acc.loginAmazonSellerCenter();

        assertThat(acc.cookie("at-acbde"), is(notNullValue()));
    }

    @Test
    public void testLoginAmazonSellerCenterStep1() throws IOException {
        Account acc = FactoryBoy.build(Account.class, "de");
        F.T2<List<NameValuePair>, String> params = acc.loginAmazonSellerCenterStep1();
        Map<String, String> pairs = new HashMap<String, String>();
        for(NameValuePair pair : params._1) {
            pairs.put(pair.getName(), pair.getValue());
        }

        assertThat(params._2, is(containsString("https://sellercentral.amazon.de/ap/signin")));
        assertThat(pairs.get("appActionToken"), is(notNullValue()));
        assertThat(pairs.get("appAction"), is("SIGNIN"));
        assertThat(pairs.get("openid.pape.max_auth_age"), is("ape:MA=="));
        assertThat(pairs.get("openid.ns"), is("ape:aHR0cDovL3NwZWNzLm9wZW5pZC5uZXQvYXV0aC8yLjA="));
        assertThat(pairs.get("openid.ns.pape"), is("ape:aHR0cDovL3NwZWNzLm9wZW5pZC5uZXQvZXh0ZW5zaW9ucy9wYXBlLzEuMA=="));
        assertThat(pairs.get("pageId"), is("ape:c2NfZXVfYW1hem9u"));
        assertThat(pairs.get("openid.identity"),
                is("ape:aHR0cDovL3NwZWNzLm9wZW5pZC5uZXQvYXV0aC8yLjAvaWRlbnRpZmllcl9zZWxlY3Q="));
        assertThat(pairs.get("openid.claimed_id"),
                is("ape:aHR0cDovL3NwZWNzLm9wZW5pZC5uZXQvYXV0aC8yLjAvaWRlbnRpZmllcl9zZWxlY3Q="));
        assertThat(pairs.get("openid.mode"), is("ape:Y2hlY2tpZF9zZXR1cA=="));
        assertThat(pairs.get("openid.return_to"),
                // 应该是属于 url 加密, 不过链接前缀一样
                is(containsString("ape:aHR0cHM6Ly9zZWxsZXJjZW50cmFsLmFtYXpvbi5kZS9ncC9ob21lcGFnZS5odG1")));
        assertThat(pairs.get("email"), is(acc.username));
        assertThat(pairs.get("password"), is(acc.password));
        assertThat(pairs.get("x"), is(nullValue()));
        assertThat(pairs.get("y"), is(nullValue()));
        // metadata1 被 map 给重复了
        assertThat(pairs.size(), is(15));
    }
}
