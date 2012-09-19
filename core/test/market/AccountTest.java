package market;

import helper.HTTP;
import helper.Webs;
import models.market.Account;
import models.market.M;
import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午6:31
 */
public class AccountTest extends UnitTest {
    Account acc;

    @Before
    public void setAcc() {
        acc = Account.findById(131l);
    }

    @Test
    public void readAccount() throws ClassNotFoundException, IOException {
        try {
            // 登陆并且缓存登陆
            //testSellerCentralLogIn
            Webs.dev_login(acc);
        } catch(Exception e) {
            e.printStackTrace();
            //ignore
        }
    }

    //    @Test
    public void testBriefFlatFinance() {
        acc.briefFlatFinance(M.AMAZON_US);
    }

    //    @Test
    public void testsellerCentralHomePage() throws IOException {
        String html = HTTP.get(acc.cookieStore(acc.type), acc.type.sellerCentralHomePage());
        assertTrue(Account.isLoginEnd(Jsoup.parse(html)));
    }

    @Test
    public void testamazonSiteLogin() {
        assertTrue(acc.loginAmazonSize(M.AMAZON_US));
    }
}
