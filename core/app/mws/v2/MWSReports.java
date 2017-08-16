package mws.v2;

import com.amazonservices.mws.reports.MarketplaceWebService;
import com.amazonservices.mws.reports.model.*;
import helper.Constant;
import models.market.JobRequest;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import javax.xml.datatype.DatatypeFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 12/13/13
 * Time: 4:05 PM
 */
public class MWSReports {

    public JobRequest requestReport(JobRequest request) {
        MarketplaceWebService service = mws.MWSReports.client(request.account);
        try {
            RequestReportRequest reportRequest = new RequestReportRequest()
                    .withMerchant(request.account.merchantId)
                    .withReportType(request.type.toString());

            DatatypeFactory df = DatatypeFactory.newInstance();
            if(request.startDate != null) {
                reportRequest.withStartDate(df.newXMLGregorianCalendar(
                        new DateTime(request.startDate).toString(ISODateTimeFormat.dateHourMinuteSecond())
                ));
            }
            if(request.endDate != null) {
                reportRequest.withEndDate(df.newXMLGregorianCalendar(
                        new DateTime(request.startDate).toString(ISODateTimeFormat.dateHourMinuteSecond())
                ));
            }

            RequestReportResponse resp = service.requestReport(reportRequest);
            ReportRequestInfo info = resp.getRequestReportResult().getReportRequestInfo();
            request.requestId = info.getReportRequestId();
            request.requestDate = new Date();
            request.state = JobRequest.S.REQUEST;
            request.lastUpdateDate = new Date();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return request.save();
    }

    /**
     * 检查 ReportRequest 的状态
     *
     * @param request
     * @return
     */
    public JobRequest checkReportRequest(JobRequest request) {
        MarketplaceWebService service = mws.MWSReports.client(request.account);
        try {
            GetReportRequestListRequest req = new GetReportRequestListRequest()
                    .withMerchant(request.account.merchantId) //暂时只要其中一个
                    .withReportRequestIdList(new IdList(Arrays.asList(request.requestId)));

            ReportRequestInfo info = service.getReportRequestList(req)
                    .getGetReportRequestListResult().getReportRequestInfoList().get(0);
            if("_DONE_".equals(info.getReportProcessingStatus())) {
                request.state = JobRequest.S.DONE;
                request.reportId = getReportId(request);
            } else if("_DONE_NO_DATA_".equals(info.getReportProcessingStatus())) {
                request.state = JobRequest.S.CLOSE;
            } else if("_CANCELLED_".equals(info.getReportProcessingStatus())) {
                request.state = JobRequest.S.CANCEL;
            } else {
                request.state = JobRequest.S.PROCRESS;
            }
            request.lastUpdateDate = new Date();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return request.save();
    }

    /**
     * 获取 ReportRequest 的 ReportId
     */
    public String getReportId(JobRequest request) {
        MarketplaceWebService service = mws.MWSReports.client(request.account);

        GetReportListRequest req = new GetReportListRequest()
                .withMerchant(request.account.merchantId)
                .withReportRequestIdList(new IdList(Arrays.asList(request.requestId)));

        try {
            ReportInfo info = service.getReportList(req).getGetReportListResult().getReportInfoList().get(0);
            return info.getReportId();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过 ReportId 下载 Report
     */
    public JobRequest downloadReport(JobRequest request) {
        MarketplaceWebService service = mws.MWSReports.client(request.account);

        try {

            GetReportRequest req = new GetReportRequest()
                    .withMerchant(request.account.merchantId)
                    .withReportId(request.reportId);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            req.setReportOutputStream(byteBuffer);
            service.getReport(req);

            String path = String.format(Constant.E_DATE + "/%s", request.reportId);
            FileUtils.write(new File(path), byteBuffer.toString("UTF-8"));
            request.path = path;
            request.state = JobRequest.S.END;
            request.lastUpdateDate = new Date();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return request.save();
    }
}
