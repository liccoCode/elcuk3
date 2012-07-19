package jobs;

import models.market.Account;
import play.Logger;
import play.jobs.Job;

import java.util.List;

/**
 * 用来维持不同 Account 登陆网站的 Session;
 * <p/>
 * PS: 这个网站 Session 维护每 29mn 一次
 * User: wyattpan
 * Date: 3/14/12
 * Time: 4:38 PM
 */
public class KeepSessionJob extends Job {

    @Override
    public void doJob() {
        List<Account> accs = Account.openedSaleAcc();
        for(Account ac : accs) {
            Logger.info(String.format("Login %s with account %s.", ac.type, ac.username));
            ac.loginWebSite();
        }
    }
}
