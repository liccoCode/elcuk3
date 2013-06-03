package factory.market;

import factory.ModelFactory;
import models.market.Account;
import models.market.M;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 6/3/13
 * Time: 10:01 AM
 */
public class AccountFactory extends ModelFactory<Account> {
    @Override
    public Account define() {
        Account acc = new Account();
        acc.type = M.AMAZON_DE;
        acc.id = 2l;

        acc.accessKey = "AKIAIAEPZK5CZEIVZFCQ";
        acc.merchantId = "A22H6OV6Q7XBYK";
        acc.username = "wyatt@easyacceu.com";
        acc.password = "pw5165688104";
        acc.token = "fC57b00QsLKfRbYoY7fbUQmb2j2wonv81vWFWuNF";
        return acc;
    }
}
