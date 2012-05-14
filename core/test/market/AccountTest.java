package market;

import models.market.Account;
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

    //    @Test
    public void createAccount() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("a.type", Account.M.AMAZON_UK.name());
        params.put("a.username", "easyacc.eu@gmail.com");
        params.put("a.password", "6XC$X5oY!jj");
        Http.Response response = POST("/market/accounts/c", params);
        System.out.println(response.out.toString());
    }

    //    @Test
    public void updateAccount() {
        Account acc = Account.find("uniqueName", "amazon.co.uk_easyacc.eu@gmail.com").first();
        Map<String, String> params = new HashMap<String, String>();
        params.put("a.id", acc.id.toString());
        params.put("a.type", acc.type.name());
        params.put("a.username", acc.username);
        params.put("a.password", acc.password);
        params.put("a.token", "token!!"); //update
        System.out.println(POST("/market/accounts/u", params).out.toString());
    }

}
