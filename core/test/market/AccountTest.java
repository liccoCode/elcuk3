package market;

import models.market.Account;
import org.junit.Test;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午6:31
 */
public class AccountTest extends FunctionalTest {
    //    @Before
    public void setup() {
        Fixtures.delete(Account.class);
    }

    @Test
    public void createAccount() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("acc.type", Account.M.AMAZON_UK.name());
        params.put("acc.username", "easyacc.eu@gmail.com");
        params.put("acc.password", "6XC$X5oY!jj");
        Http.Response response = POST("/market/accounts/c", params);
        System.out.println(response.out.toString());
    }

    @Test
    public void updateAccount() {
        Account acc = Account.find("uniqueName", "amazon.co.uk_easyacc.eu@gmail.com").first();
        Map<String, String> params = new HashMap<String, String>();
        params.put("acc.id", acc.id.toString());
        params.put("acc.type", acc.type.name());
        params.put("acc.username", acc.username);
        params.put("acc.password", acc.password);
        params.put("acc.token", "token!!"); //update
        System.out.println(POST("/market/accounts/u", params).out.toString());
    }
}
