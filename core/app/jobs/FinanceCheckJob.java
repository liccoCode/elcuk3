package jobs;

import helper.Webs;
import models.finance.SaleFee;
import models.market.Account;
import play.Logger;
import play.jobs.Job;

import java.io.File;
import java.util.List;

/**
 * 用来抓取各个市场的 Finance 信息的任务;
 * 每隔 3h 抓取一次, 因为通过这种方式抓取的 Finance 信息一次文件只能拥有 600 个 Transaction, 所以需要增加更新频率来获取新的
 * Transaction 的数据
 * User: wyattpan
 * Date: 3/19/12
 * Time: 12:01 PM
 */
public class FinanceCheckJob extends Job {

    @Override
    public void doJob() {
        List<Account> accs = Account.openedSaleAcc();
        for(Account acc : accs) {
            if("AJUR3R8UN71M4".equals(acc.merchantId)) {
                doFetch(acc, Account.M.AMAZON_UK);
                doFetch(acc, Account.M.AMAZON_DE);
            } else if("A22H6OV6Q7XBYK".equals(acc.merchantId)) {
                doFetch(acc, Account.M.AMAZON_DE);
            }
        }
    }

    private void doFetch(Account acc, Account.M m) {
        Logger.info("FinanceCheckJob Check Account[%s] Market[%s] Begin", acc.uniqueName, m.name());
        try {
            File file = acc.briefFlatFinance(m);
            List<SaleFee> fees = SaleFee.flagFinanceParse(file, acc, m);
            SaleFee.clearOldSaleFee(fees);
            SaleFee.batchSaveWithJDBC(fees);
            Logger.info("FinanceCheckJob Check Account[%s] Market[%s] Done", acc.uniqueName, m.name());
        } catch(Exception e) {
            Logger.warn("FinanceCheckJob %s", Webs.E(e));
        }
    }

}
