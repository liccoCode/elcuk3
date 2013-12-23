package jobs.perform;

import com.amazonaws.mws.MarketplaceWebServiceException;
import jobs.driver.BaseJob;
import jobs.driver.GJob;
import models.market.Account;
import models.market.Feed;
import mws.v2.MWSFeeds;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.common.joda.time.DateTime;
import play.utils.FastRuntimeException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用来获取 Feed 的处理结果, 并且进行获得 Feed 后的处理
 * User: wyatt
 * Date: 12/17/13
 * Time: 4:01 PM
 */
public class GetFeedJob extends BaseJob {
    @Override
    public void doit() {
        /**
         * 1. 获取账户和提交的 Feed
         * 2. 向MWS 请求查询处理结果
         */
        if(getContext().get("account.id") == null)
            throw new FastRuntimeException("没有提交 account.id 信息, 不知道是哪个销售账户.");
        if(getContext().get("feedId") == null)
            throw new FastRuntimeException("没有提交 feedId 信息");
        if(getContext().get("feed.id") == null)
            throw new FastRuntimeException("没有提交 feed.id 信息");

        String action = "create";
        if(getContext().get("action") != null)
            action = "update";

        Account account = Account.findById(NumberUtils.toLong(getContext().get("account.id").toString()));
        String feedId = getContext().get("feedId").toString();

        Map<String, Object> args = new HashMap<String, Object>();
        args.put("account.id", account.id);
        args.put("feedId", feedId);
        File file = null;
        try {
            MWSFeeds mwsFeedRequest = new MWSFeeds(account);
            file = mwsFeedRequest.getFeedResult(feedId);
            try {
                List<String> reportLines = FileUtils.readLines(file, "ISO8859-1");
                Feed feed = Feed.findById(NumberUtils.toLong(getContext().get("feed.id").toString()));
                feed.result = StringUtils.join(reportLines, "\r\n");
                feed.save();
                for(String line : reportLines) {
                    if(!StringUtils.containsIgnoreCase(line, "error")) {
                        // TODO Report 处理成功后需要处理.
                        // TODO 如果 action 为 update 则完成 Feed 获取就结束
                        GJob.perform(GetAsinJob.class.getName(), getContext(), DateTime.now().plusMinutes(1).toDate());
                    }
                }
            } catch(IOException e) {
                throw new FastRuntimeException(e);
            }
        } catch(MarketplaceWebServiceException e) {
            /**
             * 如果发生异常,则重新发起请求(两分钟之后)
             */
            if((e.getMessage()).contains("Feed Submission Result is not ready for Feed")) {
                GJob.perform(GetFeedJob.class.getName(), args, DateTime.now().plusMinutes(2).toDate());
            }
        } finally {
            if(file != null) FileUtils.deleteQuietly(file);
        }

    }
}
