package jobs;

import helper.Currency;
import models.market.Account;
import models.market.Feedback;
import models.market.Orderr;
import play.Logger;
import play.jobs.Job;

import java.util.List;

/**
 * 用来抓取 Feedback 的线程, 7 小时抓取一次
 * <p/>
 * User: wyattpan
 * Date: 3/15/12
 * Time: 9:34 AM
 */
public class FeedbackCrawlJob extends Job {
    /**
     * <pre>
     * 如果是 Europe 的 Account 则进入一个流程
     *   a. 首先抓取 uk 的 feedback
     *   b. 然后抓取 de 的 feedback
     *   c. 最后抓取 fr 的 feedback
     * 否则仅仅抓取自己一个的即可.
     * </pre>
     */
    @Override
    public void doJob() {
        Currency.updateCRY();// 附带在 FeedbackCrawlJob 的周期更新一次 Currency.

        List<Account> accs = Account.openedAcc();
        for(Account acc : accs) {
            switch(acc.type) {
                case AMAZON_DE:
                case AMAZON_UK:
                case AMAZON_FR:
                    fetchAccountFeedback(acc, Account.M.AMAZON_UK);
                    fetchAccountFeedback(acc, Account.M.AMAZON_DE);
                    fetchAccountFeedback(acc, Account.M.AMAZON_FR);
                    break;
                case AMAZON_ES:
                case AMAZON_IT:
                    Logger.warn("Not Support Right Now!");
                    break;
            }
        }
    }

    /**
     * 抓取某一个市场的 N(现在为 2) 页的 feedback
     *
     * @param acc
     * @param market
     */
    private void fetchAccountFeedback(Account acc, Account.M market) {
        try {
            if(market != Account.M.AMAZON_UK &&
                    market != Account.M.AMAZON_FR &&
                    market != Account.M.AMAZON_DE) return;
            acc.changeRegion(market);
            for(int i = 1; i <= 2; i++) {
                List<Feedback> feedbacks = acc.fetchFeedback(i);
                Logger.info(String.format("Fetch %s %s, page %s, total %s.", acc.username, market, i, feedbacks.size()));

                //这段代码在 Feedbacks 也使用了, 但不好将其抽取出来
                for(Feedback f : feedbacks) {
                    f.orderr = Orderr.findById(f.orderId);
                    f.account = acc;
                    f.<Feedback>merge().checkMailAndTicket(); // 系统中有则更新, 没有则创建
                }
            }
        } catch(Exception e) {
            Logger.warn(String.format("Account %s Market %s fetch feedback have some error![%s]", acc.username, market, e.getMessage()));
        }
    }

}
