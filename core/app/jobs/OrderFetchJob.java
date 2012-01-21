package jobs;

import models.market.Account;
import play.jobs.Job;

import java.util.List;

/**
 * 每隔一段时间到 Amazon 上进行订单的抓取
 * //TODO 在处理好 Product, Listing, Selling 的数据以后再编写
 * User: Wyatt
 * Date: 12-1-8
 * Time: 上午5:59
 */
public class OrderFetchJob extends Job {
    @Override
    public void doJob() throws Exception {
        List<Account> accs = Account.all().fetch();
    }
}
