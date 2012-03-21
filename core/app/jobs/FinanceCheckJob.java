package jobs;

import models.finance.SaleFee;
import models.market.Account;
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
        List<Account> accs = Account.find("closeable=?", false).fetch();
        for(Account acc : accs) {
            for(Account.M m : Account.M.values()) {
                if(m == Account.M.EBAY_UK || m == Account.M.AMAZON_US) continue;
                File file = acc.briefFlatFinance(m);
                List<SaleFee> fees = SaleFee.flagFinanceParse(file, acc, m);
                SaleFee.clearOldSaleFee(fees);
                SaleFee.batchSaveWithJDBC(fees);
            }
        }
    }
}
