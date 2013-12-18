package jobs.perform;

import jobs.driver.BaseJob;
import models.market.Account;
import mws.v2.MWSFeeds;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;

/**
 * 用来获取 Feed 的处理结果, 并且进行获得 Feed 后的处理
 * User: wyatt
 * Date: 12/17/13
 * Time: 4:01 PM
 */
public class GetFeedJob extends BaseJob {
    @Override
    public void doit() {
        Account acc = Account.findById(NumberUtils.toLong(getContext().get("accountId").toString()));
        MWSFeeds feedRequest = new MWSFeeds(acc);
        File feedResult = feedRequest.getFeedResult(getContext().get("feedId").toString());

    }
}
