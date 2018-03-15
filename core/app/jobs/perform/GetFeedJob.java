package jobs.perform;

import helper.LogUtils;
import jobs.driver.BaseJob;
import jobs.driver.GJob;
import models.market.Account;
import models.market.Feed;
import mws.v2.MWSFeeds;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import play.utils.FastRuntimeException;

import java.io.File;
import java.util.List;

/**
 * 用来获取 Feed 的处理结果, 并且进行获得 Feed 后的处理
 * User: wyatt
 * Date: 12/17/13
 * Time: 4:01 PM
 *
 * @deprecated
 */
public class GetFeedJob extends BaseJob {
    @SuppressWarnings("unchecked")
    @Override
    public void doit() {
        long begin = System.currentTimeMillis();

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
        } catch(Exception e) {
            /**
             * 如果发生异常( Amazon 未处理好数据就会返回一个错误 ),重新发起请求(两分钟之后)
             */
            if((e.getMessage()).contains("Feed Submission Result is not ready for Feed")) {
                GJob.perform(GetFeedJob.class.getName(), getContext(), DateTime.now().plusMinutes(3).toDate());
            } else if(e.getMessage().contains("Content-MD5 HTTP header transmitted by MWS")) {
                /**
                 * 临时性质代码，上架时如果出现 Amazon 报告 MD5 校验出错，则直接进行 Asin 抓取,并保存异常信息到该 getFeedJob 的msg 字段内。
                 */
                GJob.perform(GetAsinJob.class.getName(), getContext(), DateTime.now().plusMinutes(1).toDate());
                throw new FastRuntimeException(e);
            }
        } finally {
            if(file != null) FileUtils.deleteQuietly(file);
        }

        if(LogUtils.isslow(System.currentTimeMillis() - begin, "GetFeedJob")) {
            LogUtils.JOBLOG.info(String.format("GetFeedJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }
}
