package mws;

import com.amazonservices.mws.reports.MarketplaceWebService;
import com.amazonservices.mws.reports.MarketplaceWebServiceClient;
import com.amazonservices.mws.reports.MarketplaceWebServiceConfig;
import com.amazonservices.mws.reports.MarketplaceWebServiceException;
import com.amazonservices.mws.reports.model.*;
import helper.Constant;
import models.market.Account;
import models.market.JobRequest;
import models.market.M;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.utils.FastRuntimeException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <pre>
 * 对 Amazon MWSReports 的 report 的请求 API
 * 1. 发出 Report Request 请求, 获得 ReportRequestId
 * 2. 根据 ReportRequestId 请求, 查询状态, 直到这个请求被处理结束;
 * 3. 当 ReportRequestId 的任务执行完成后, 获取 ReportId , 用来下载 report 文件;
 * 4. 根据获取好的 ReportId 下载 Amazon 处理完的 report 文件
 *
 * </pre>
 * User: wyattpan
 * Date: 1/23/12
 * Time: 6:10 PM
 */
public class MWSReports {

    private MWSReports() {
    }

    /**
     * 年/月/日/文件名
     */
    public static final String REPORT_BASE_PATH = Constant.E_DATE + "/%s/%s/%s/%s";

    public static void requestReportStep1(JobRequest job) {
        if(job.state != JobRequest.S.NEW) {
            Logger.info("The JobRequest State is wrong! It`s must NEW State.");
            throw new FastRuntimeException("The JobRequest State is wrong! It`s must NEW State.");
        }
        switch(job.type) {
            case ALL_FBA_ORDER_FETCH:
            case ALL_FBA_ORDER_SHIPPED:
            case MANAGE_FBA_INVENTORY_ARCHIVED:
            case GET_FBA_FULFILLMENT_INVENTORY_RECEIPTS_DATA:
            case ACTIVE_LISTINGS:
                MarketplaceWebService service = client(job);

                RequestReportRequest res = new RequestReportRequest()
                        .withMerchant(job.account.merchantId)
                        .withMarketplaceIdList(new IdList(Arrays.asList(
                                job.marketplaceId.name()))) // only have the uk MarketplaceId.
                        .withReportType(job.type.toString())
                        .withReportOptions("ShowSalesChannel=true");
                try {
                    DateTime time = DateTime.now();
                    if(job.type == JobRequest.T.GET_FBA_FULFILLMENT_INVENTORY_RECEIPTS_DATA) {
                        time = time.minusMonths(3); // 最近 3 个月
                    } else {
                        // TODO 思考如何让数据同步更准确
                        time = time.minusDays(7); // 新订单的获取, 捕捉最近 7 天内的
                    }
                    DatatypeFactory df = DatatypeFactory.newInstance();
                    res.setStartDate(df.newXMLGregorianCalendar(
                            new GregorianCalendar(time.getYear(),
                                    time.getMonthOfYear() - 1/*0~11*/,
                                    time.getDayOfMonth())));
                    RequestReportResponse resp = service.requestReport(res);
                    ReportRequestInfo info = resp.getRequestReportResult().getReportRequestInfo();

                    job.startDate = time.toDate();
                    job.endDate = new Date();
                    job.requestId = info.getReportRequestId();
                    job.procressState = info.getReportProcessingStatus();
                    job.state = JobRequest.S.REQUEST;
                    job.lastUpdateDate = new Date();
                    job.save();
                } catch(MarketplaceWebServiceException e) {
                    Logger.warn("requestReportStep1 has error:{" + e.getMessage() + "}");
                } catch(DatatypeConfigurationException e) {
                    // can not be happed.
                    Logger.warn(
                            "DatatypeConfigurationException Can not be happed in MWSReports.requestReportStep1!");
                }
                return;
            default:
                Logger.info("Job Type is not valid, it is not a amazon request report Job.");
        }
    }


    public static void requestStateStep2(JobRequest job) {
        if(job.state != JobRequest.S.REQUEST && job.state != JobRequest.S.PROCRESS) {
            Logger.info("The JobRequest State is wrong! It`s must REQUEST State.");
            throw new FastRuntimeException(
                    "The JobRequest State is wrong! It`s must REQUEST State.");
        }

        MarketplaceWebService client = client(job);

        // 一个一个 Report 的处理, 不再是一批处理, 因为 Service 会不同.
        GetReportRequestListRequest req = new GetReportRequestListRequest()
                .withMerchant(job.account.merchantId) //暂时只要其中一个
                .withReportRequestIdList(new IdList(Arrays.asList(job.requestId)));

        try {
            ReportRequestInfo info = client.getReportRequestList(req)
                    .getGetReportRequestListResult().getReportRequestInfoList().get(0);
            job.procressState = info.getReportProcessingStatus();
            if("_DONE_".equals(job.procressState)) {
                job.state = JobRequest.S.DONE;
            } else if("_DONE_NO_DATA_".equals(job.procressState)) {
                job.state = JobRequest.S.CLOSE;
            } else if("_CANCELLED_".equals(job.procressState)) {
                job.state = JobRequest.S.CANCEL;
            } else {
                job.state = JobRequest.S.PROCRESS;
            }
            job.lastUpdateDate = new Date();
            job.save();
        } catch(MarketplaceWebServiceException e) {
            Logger.warn("requestStateStep2 has error:{" + e.getMessage() + "}");
        }

    }

    public static void requestReportIdStep3(JobRequest job) {
        if(job.state != JobRequest.S.DONE) {
            Logger.info("The JobRequest State is wrong! It`s must be DONE State.");
            throw new FastRuntimeException("The JobRequest State is wrong! It`s must DONE State.");
        }
        MarketplaceWebService client = client(job);

        GetReportListRequest req = new GetReportListRequest()
                .withMerchant(job.account.merchantId)
                .withReportRequestIdList(new IdList(Arrays.asList(job.requestId)));

        try {
            ReportInfo info = client.getReportList(req).getGetReportListResult().getReportInfoList().get(0);
            job.reportId = info.getReportId();
            job.state = JobRequest.S.DOWN;
            job.lastUpdateDate = new Date();
            job.procressState = "_END_";
            job.save();
        } catch(MarketplaceWebServiceException e) {
            Logger.warn("requestReportIdStep3 has error:{" + e.getMessage() + "}");
        }
    }

    public static void requestReportDownStep4(JobRequest job) {
        if(job.state != JobRequest.S.DOWN) {
            Logger.info("The JobRequest State is wrong! It`s must be DOWN State.");
            throw new FastRuntimeException(
                    "The JobRequest State is wrong! It`s must be DOWN State.");
        }
        MarketplaceWebService client = client(job);

        GetReportRequest req = new GetReportRequest()
                .withMerchant(job.account.merchantId)
                .withReportId(job.reportId);

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        req.setReportOutputStream(byteBuffer);

        try {
            GetReportResponse rep = client.getReport(req);
            Calendar cal = Calendar.getInstance();
            String filename = String.format(REPORT_BASE_PATH,
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH),
                    job.reportId);
            switch(job.type) {
                case ALL_FBA_ORDER_FETCH:
                    filename += ".xml";
                    break;
                case ACTIVE_LISTINGS:
                case GET_FBA_FULFILLMENT_INVENTORY_RECEIPTS_DATA:
                    filename += ".txt";
                    break;
                case ALL_FBA_ORDER_SHIPPED:
                case MANAGE_FBA_INVENTORY_ARCHIVED:
                    filename += ".csv";
                    break;
                default:
                    break;
            }
            FileUtils.write(new File(filename), byteBuffer.toString("UTF-8"));
            job.path = filename;
            job.state = JobRequest.S.END;
            job.lastUpdateDate = new Date();
            job.save();
        } catch(MarketplaceWebServiceException e) {
            Logger.warn("requestReportDownStep4 has error:{" + e.getMessage() + "}");
        } catch(IOException e) {
            Logger.warn("requestReportDownStep4 has error:{" + e.getMessage() + "}");
        }
    }


    private static final Map<String, MarketplaceWebService> cached = new HashMap<>();

    /**
     * 通过 JobRequest 获取缓存了的 MarketplaceWebService 对象
     *
     * @param job
     * @return
     */
    public static MarketplaceWebService client(JobRequest job) {
        return client(job.account);
    }

    public static MarketplaceWebService client(Account account) {
        return client(account, account.type);
    }

    public static MarketplaceWebService client(Account account, M market) {
        String key = String.format("WebServiceClient_%s_%s", account.id, market.name());
        MarketplaceWebService client;
        if(cached.containsKey(key)) return cached.get(key);
        else {
            synchronized(cached) {
                if(cached.containsKey(key)) return cached.get(key);
                MarketplaceWebServiceConfig config = new MarketplaceWebServiceConfig();
                config.setServiceURL(MWSProducts.getMwsUrl(market));
                client = new MarketplaceWebServiceClient(account.accessKey, account.token, "elcuk2", "1.0", config);
                cached.put(key, client);
            }
        }
        return client;
    }
}
