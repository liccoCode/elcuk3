package mws.v2;

import com.amazonaws.mws.MarketplaceWebService;
import com.amazonaws.mws.model.GetFeedSubmissionResultRequest;
import com.amazonaws.mws.model.GetFeedSubmissionResultResponse;
import com.amazonaws.mws.model.SubmitFeedRequest;
import com.amazonaws.mws.model.SubmitFeedResponse;
import helper.Constant;
import models.market.JobRequest;
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
                return "_POST_PRODUCT_DATA_";
            }
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
