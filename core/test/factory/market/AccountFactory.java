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


    @Factory(name = "ide")
    public Account de() {
        Account acc = deNoId();
        acc.id = 2l;
        return acc;
    }

    @Factory(name = "de")
    public Account deNoId() {
        Account acc = new Account();
        acc.type = M.AMAZON_DE;
        acc.accessKey = "AKIAIAEPZK5CZEIVZFCQ";
        acc.merchantId = "A22H6OV6Q7XBYK";
        acc.username = "wyatt@easyacceu.com";
        acc.password = "amz13297472505de";
        acc.token = "fC57b00QsLKfRbYoY7fbUQmb2j2wonv81vWFWuNF";
        acc.closeable = false;
        acc.isSaleAcc = true;
        return acc;
    }

    @Factory(name = "ius")
    public Account us() {
        Account acc = noIdUS();
        acc.id = 131l;
        return acc;
    }

    @Factory(name = "us")
    public Account noIdUS() {
        Account acc = new Account();
        acc.type = M.AMAZON_US;
        acc.accessKey = "AKIAIAEPZK5CZEIVZFCQ";
        acc.merchantId = "A22H6OV6Q7XBYK";
        acc.username = "wyatt@easyacceu.com";
        acc.password = "amz13297472505us";
        acc.closeable = false;
        acc.token = "fC57b00QsLKfRbYoY7fbUQmb2j2wonv81vWFWuNF";
        acc.isSaleAcc = true;
        return acc;
    }

    @Factory(name = "iuk")
    public Account uk() {
        Account acc = noIdUK();
        acc.id = 1l;
        return acc;
    }

    @Factory(name = "uk")
    public Account noIdUK() {
        Account acc = new Account();
        acc.type = M.AMAZON_UK;
        acc.accessKey = "AKIAI6EBPJLG64HWDBGQ";
        acc.merchantId = "AJUR3R8UN71M4";
        acc.username = "wyatt@easyacceu.com";
        acc.password = "amz13297472505uk";
        acc.closeable = false;
        acc.token = "3e3TWsDOt6KBfubRzEIRWZuhSuxa+aRGWvnnjJuf";
        acc.isSaleAcc = true;
        return acc;
    }

    @Factory(name = "iit")
    public Account it() {
        Account acc = noIdIT();
        acc.id = 132l;
        return acc;
    }

    @Factory(name = "it")
    public Account noIdIT() {
        Account acc = new Account();
        acc.type = M.AMAZON_IT;
        acc.accessKey = "AKIAJNQ4WKPFHGGSQ76Q";
        acc.merchantId = "A2ESAGA2P1UPKN";
        acc.username = "wyatt@easyacceu.com";
        acc.password = "amz13297472505it";
        acc.closeable = false;
        acc.token = "sDEGd3btDuDjv1dpSjdUYaeeELCb36TAW0PUvBHe";
        acc.isSaleAcc = true;
        return acc;
    }

    @Factory(name = "ijp")
    public Account jp() {
        Account acc = noIdJP();
        acc.id = 133l;
        return acc;
    }

    @Factory(name = "jp")
    public Account noIdJP() {
        Account acc = new Account();
        acc.type = M.AMAZON_JP;
        acc.accessKey = "UNKNOW";
        acc.merchantId = "UNKNOW";
        acc.username = "wyatt@easyacceu.com";
        acc.password = "amz13297472505jp";
        acc.closeable = false;
        acc.token = "UNKNOW";
        acc.isSaleAcc = true;
        return acc;
    }
}
