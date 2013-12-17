package jobs.perform;

import jobs.driver.BaseJob;
import models.market.Account;
import mws.v2.MWSFeeds;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 用来提交 MWS 的 Feed 的任务
 * User: wyatt
 * Date: 12/17/13
 * Time: 3:50 PM
 */
public class SubmitFeedJob extends BaseJob {

    @Override
    public void doit() {
        Account acc = Account.findById(NumberUtils.toLong(getContext().get("accountId").toString()));

        MWSFeeds feedRequest = new MWSFeeds(acc);
        String feedId = feedRequest.submitFeed(null, MWSFeeds.T.UPLOAD_PRODUCT);
        if(StringUtils.isNotBlank(feedId)) {
        }
    }
}
