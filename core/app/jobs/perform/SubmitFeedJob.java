package jobs.perform;

import helper.Constant;
import helper.LogUtils;
import jobs.driver.BaseJob;
import jobs.driver.GJob;
import models.market.Account;
import models.market.Feed;
import models.market.M;
import mws.v2.MWSFeeds;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import play.utils.FastRuntimeException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * 用来提交 MWS 的 Feed 的任务
 * <pre>
 * 1. account.id 哪个销售账户
 * 2. feed.id 提交哪一个 Feed
 * 3. marketId 提交给哪一个 Amazon 市场
 * 4. action 代表是 update 还是 create (传递给 GetFeedJob 使用)
 * </pre>
 * User: wyatt
 * Date: 12/17/13
 * Time: 3:50 PM
 * @deprecated
 */
public class SubmitFeedJob extends BaseJob {

    @SuppressWarnings("unchecked")
    @Override
    public void doit() {
        long begin = System.currentTimeMillis();
        /**
         * 1. 获取账户和提交的 Feed
         * 2. 向 MWS 提交 Feed
         */
        if(getContext().get("account.id") == null)
            throw new FastRuntimeException("没有提交 account.id 信息, 不知道是哪个销售账户上架.");
        if(getContext().get("feed.id") == null)
            throw new FastRuntimeException("没有提交 feed.id 信息, 没有提交的 Feed 数据");
        if(getContext().get("marketId") == null)
            throw new FastRuntimeException("没有提交 marketId 信息，无法确认目标市场");

        M.MID marketId = M.MID.valueOf(getContext().get("marketId").toString());
        Account account = Account.findById(NumberUtils.toLong(getContext().get("account.id").toString()));
        Feed feed = Feed.findById(NumberUtils.toLong(getContext().get("feed.id").toString()));
        File file = new File(String.format("%s/%s", Constant.TMP, "feed_" + feed.id));
        try {
            try {
                FileUtils.write(file, feed.content, chatSet(marketId));
            } catch(IOException e) {
                throw new FastRuntimeException(e.getMessage());
            }
            MWSFeeds mwsFeedRequest = new MWSFeeds(account);
            feed.feedId = mwsFeedRequest.submitFeed(file, MWSFeeds.T.UPLOAD_PRODUCT, marketId);
            feed.save();

            Map nextContext = getContext();
            nextContext.put("feedId", feed.feedId);
            GJob.perform(GetFeedJob.class, nextContext);
        } finally {
            FileUtils.deleteQuietly(file);
        }
        if(LogUtils.isslow(System.currentTimeMillis() - begin,"SubmitFeedJob")) {
            LogUtils.JOBLOG
                    .info(String.format("SubmitFeedJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }

    /**
     * 不同市场编码设置
     *
     * @return
     */
    public static String chatSet(M.MID marketId) {
        switch(marketId.market()) {
            case AMAZON_CA:
            case AMAZON_FR:
            case AMAZON_UK:
            case AMAZON_ES:
            case AMAZON_IT:
            case AMAZON_DE:
            case AMAZON_US:
                return "ISO8859-1";
            case AMAZON_JP:
                return "SJIS";
            default:
                return "ISO8859-1";
        }
    }
}
