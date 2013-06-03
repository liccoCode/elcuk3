package models.market;

import factory.FactoryBoy;
import org.junit.Test;
import play.test.UnitTest;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 6/3/13
 * Time: 9:59 AM
 */
public class AccountTest extends UnitTest {

    @Test
    public void testLoginAmazonSellerCenter() {
        Account acc = FactoryBoy.build(Account.class, "de");
        acc.cookieStore().clear();
        acc.loginAmazonSellerCenter();

        assertThat(acc.cookie("at-acbde"), is(notNullValue()));
    }
}
