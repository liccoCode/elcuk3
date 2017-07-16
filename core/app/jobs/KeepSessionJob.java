package jobs;

import helper.LogUtils;
import jobs.driver.BaseJob;
import models.market.Account;
import play.Logger;

import java.util.List;

/**
 * 账户开启二步验证后已经无法正确拿到 cookie
 * 用来维持不同 Account 登陆网站的 Session;
 * <p/>
 * PS: 这个网站 Session 维护每 29mn 一次
 * 周期:
 * - 轮询周期: 5mn
 * - Duration: 15mn
 * User: wyattpan
 * Date: 3/14/12
 * Time: 4:38 PM
 *
 * @deprecated
 */
//@Every("20mn")
public class KeepSessionJob extends BaseJob {

    @Override
    public void doit() {
        long begin = System.currentTimeMillis();
        List<Account> accs = Account.openedSaleAcc();
        for(Account ac : accs) {
            Logger.info(String.format("Login %s with account %s.", ac.type, ac.username));
            ac.loginAmazonSellerCenter();
        }
        if(LogUtils.isslow(System.currentTimeMillis() - begin, "KeepSessionJob")) {
            LogUtils.JOBLOG
                    .info(String.format("KeepSessionJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }
}
