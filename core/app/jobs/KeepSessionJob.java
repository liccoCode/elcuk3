package jobs;

import models.Jobex;
import models.market.Account;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

import java.util.List;

/**
 * 用来维持不同 Account 登陆网站的 Session;
 * <p/>
 * PS: 这个网站 Session 维护每 29mn 一次
 * 周期:
 * - 轮询周期: 5mn
 * - Duration: 15mn
 * User: wyattpan
 * Date: 3/14/12
 * Time: 4:38 PM
 */
@Every("5mn")
public class KeepSessionJob extends Job {

    @Override
    public void doJob() {
        if(!Jobex.findByClassName(AmazonFBAQtySyncJob.class.getName()).isExcute()) return;
        List<Account> accs = Account.openedSaleAcc();
        for(Account ac : accs) {
            Logger.info(String.format("Login %s with account %s.", ac.type, ac.username));
            ac.loginAmazonSellerCenter();
        }
    }
}
