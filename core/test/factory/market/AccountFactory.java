package factory.market;

import factory.ModelFactory;
import factory.annotation.Factory;
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
        acc.accessKey = "accessKey";
        acc.merchantId = "merchantId";
        acc.username = "username";
        acc.password = "password";
        acc.token = "token";
        return acc;
    }


    @Factory(name = "de")
    public Account de() {
        Account acc = new Account();
        acc.type = M.AMAZON_DE;
        acc.id = 2l;
        acc.accessKey = "AKIAIAEPZK5CZEIVZFCQ";
        acc.merchantId = "A22H6OV6Q7XBYK";
        acc.username = "wyatt@easyacceu.com";
        acc.password = "pw5165688104";
        acc.closeable = false;
        acc.token = "fC57b00QsLKfRbYoY7fbUQmb2j2wonv81vWFWuNF";
        acc.isSaleAcc = true;
        return acc;
    }

    @Factory(name = "ide")
    public Account deNoId() {
        Account acc = new Account();
        acc.type = M.AMAZON_DE;
        acc.accessKey = "AKIAIAEPZK5CZEIVZFCQ";
        acc.merchantId = "A22H6OV6Q7XBYK";
        acc.username = "wyatt@easyacceu.com";
        acc.password = "pw5165688104";
        acc.token = "fC57b00QsLKfRbYoY7fbUQmb2j2wonv81vWFWuNF";
        acc.closeable = false;
        acc.isSaleAcc = true;
        return acc;
    }

    @Factory(name = "us")
    public Account us() {
        Account acc = new Account();
        acc.type = M.AMAZON_US;
        acc.id = 131l;
        acc.accessKey = "AKIAIAEPZK5CZEIVZFCQ";
        acc.merchantId = "A22H6OV6Q7XBYK";
        acc.username = "wyatt@easyacceu.com";
        acc.password = "pw5165688104";
        acc.closeable = false;
        acc.token = "fC57b00QsLKfRbYoY7fbUQmb2j2wonv81vWFWuNF";
        acc.isSaleAcc = true;
        return acc;
    }
}
