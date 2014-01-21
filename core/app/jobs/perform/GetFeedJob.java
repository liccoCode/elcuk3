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
import java.util.List;

/**
 * 用来获取 Feed 的处理结果, 并且进行获得 Feed 后的处理
 * User: wyatt
 * Date: 12/17/13
 * Time: 4:01 PM
 */
public class GetFeedJob extends BaseJob {
    @SuppressWarnings("unchecked")
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

        String action = getContext().get("action") == null ? "create" : getContext().get("action").toString();

        Account account = Account.findById(NumberUtils.toLong(getContext().get("account.id").toString()));
        String feedId = getContext().get("feedId").toString();

        File file = null;
        try {
            MWSFeeds mwsFeedRequest = new MWSFeeds(account);
            file = mwsFeedRequest.getFeedResult(feedId);

            List<String> reportLines = FileUtils.readLines(file, "ISO8859-1");
            Feed feed = Feed.findById(NumberUtils.toLong(getContext().get("feed.id").toString()));
            feed.result = StringUtils.join(reportLines, "\r\n");
            feed.save();
            for(String line : reportLines) {
                String[] args = StringUtils.splitPreserveAllTokens(line, "\t");
                if(args.length == 5 && "Error".equals(args[3]))
                    throw new FastRuntimeException("提交的 Feed 文件有错误，请检查");
            }
            if("create".equals(action)) {
                GJob.perform(GetAsinJob.class.getName(), getContext(), DateTime.now().plusMinutes(1).toDate());
            }
        } catch(IOException e) {
            throw new FastRuntimeException(e);
        } catch(MarketplaceWebServiceException e) {
            /**
             * 如果发生异常( Amazon 未处理好数据就会返回一个错误 ),重新发起请求(两分钟之后)
             */
            if((e.getMessage()).contains("Feed Submission Result is not ready for Feed")) {
                GJob.perform(GetFeedJob.class.getName(), getContext(), DateTime.now().plusMinutes(3).toDate());
            } else {
                throw new FastRuntimeException(e);
            }
        } finally {
            if(file != null) FileUtils.deleteQuietly(file);
        }
    }
}
