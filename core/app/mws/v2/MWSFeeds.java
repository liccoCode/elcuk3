package mws.v2;

import com.amazonaws.mws.MarketplaceWebService;
import com.amazonaws.mws.MarketplaceWebServiceException;
import com.amazonaws.mws.model.*;
import com.google.common.collect.Lists;
import helper.Constant;
import models.market.Account;
import models.market.JobRequest;
import models.market.M;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 12/13/13
 * Time: 5:03 PM
 */
public class MWSFeeds {
    public enum T {
        UPLOAD_PRODUCT {
            @Override
            public String toString() {
                return "_POST_FLAT_FILE_LISTINGS_DATA_";
            }
        }
    }

    private Account account;

    public MWSFeeds() {
    }

    public MWSFeeds(Account account) {
        this.account = account;
    }

    /**
     * 直接通过 Account 来提交 Feed
     */
    public String submitFeed(File feed, T feedType) {
        return submitFeed(feed, feedType, null);
    }

    /**
     * 直接通过 Account 来提交 Feed, 指定哪一个市场
     */
    public String submitFeed(File feed, T feedType, M.MID marketId) {
        MarketplaceWebService service = mws.MWSReports.client(account);
        SubmitFeedRequest req = new SubmitFeedRequest()
                .withMerchant(account.merchantId)
                .withFeedType(feedType.toString());
        // 如果为空, 默认为账户注册的市场
        if(marketId != null) {
            req.withMarketplaceIdList(new IdList(Lists.newArrayList(marketId.name())));
        }

        try {
            req.setFeedContent(new FileInputStream(feed));
            SubmitFeedResponse resp = service.submitFeedFromFile(req);
            return resp.getSubmitFeedResult().getFeedSubmissionInfo().getFeedSubmissionId();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过 FeedId 获取下载好的 Feed 结果
     */
    public File getFeedResult(String feedId) throws MarketplaceWebServiceException {
        MarketplaceWebService service = mws.MWSReports.client(account);
        GetFeedSubmissionResultRequest req = new GetFeedSubmissionResultRequest()
                .withMerchant(account.merchantId)
                .withFeedSubmissionId(feedId);
        try {
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            req.setFeedSubmissionResultOutputStream(byteBuffer);
            GetFeedSubmissionResultResponse resp = service.getFeedSubmissionResult(req);

            String path = String.format(Constant.E_DATE + "/%s", feedId);
            File file = new File(path);
            FileUtils.writeByteArrayToFile(file, byteBuffer.toByteArray());
            return file;
        } catch(MarketplaceWebServiceException e) {
            throw e;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 提交 Feed
     */
    public JobRequest submitFeed(JobRequest request) {
        MarketplaceWebService service = mws.MWSReports.client(request.account);
        SubmitFeedRequest req = new SubmitFeedRequest()
                .withMerchant(request.account.merchantId)
                .withFeedType(request.feedType.toString());

        try {
            req.setFeedContent(new FileInputStream(request.path));
            SubmitFeedResponse resp = service.submitFeedFromFile(req);
            request.reportId = resp.getSubmitFeedResult().getFeedSubmissionInfo().getFeedSubmissionId();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return request.save();
    }

    /**
     * 下载 Report
     */
    public JobRequest getFeedResult(JobRequest request) {

        MarketplaceWebService service = mws.MWSReports.client(request.account);
        GetFeedSubmissionResultRequest req = new GetFeedSubmissionResultRequest()
                .withMerchant(request.account.merchantId)
                .withFeedSubmissionId(request.reportId);
        try {
            GetFeedSubmissionResultResponse resp = service.getFeedSubmissionResult(req);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            req.setFeedSubmissionResultOutputStream(byteBuffer);

            String path = String.format(Constant.E_DATE + "/%s", request.reportId);
            FileUtils.write(new File(path), byteBuffer.toString("UTF-8"));
            request.path = path;
            request.state = JobRequest.S.END;
            request.lastUpdateDate = new Date();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return request;
    }
}
