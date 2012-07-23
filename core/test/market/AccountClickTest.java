package market;

import helper.J;
import models.market.Account;
import models.market.Listing;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 7/23/12
 * Time: 5:19 PM
 */
public class AccountClickTest extends UnitTest {

    @Before
    public void login() {
        Account acc = Account.findById(3l);
        acc.loginAmazonSize();
    }

    @Test
    public void clickLike() {
        Account acc = Account.findById(3l);
        Listing lst = Listing.findById("B004MKNBJG_amazon.de");
        System.out.println(J.json(acc.clickLike(lst)));
    }
}
