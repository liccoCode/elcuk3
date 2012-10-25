package helper;

import com.amazonaws.mws.MarketplaceWebService;
import com.amazonaws.mws.MarketplaceWebServiceClient;
import com.amazonaws.mws.MarketplaceWebServiceConfig;
import com.amazonaws.mws.MarketplaceWebServiceException;
import com.amazonaws.mws.model.*;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.MWSEndpoint;
import com.jamonapi.Monitor;
import models.market.JobRequest;
import models.market.M;
import org.apache.commons.io.FileUtils;
import play.Logger;
import play.utils.FastRuntimeException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * 对 Amazon AWS 的 report 的请求 API
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
public class AWS {

    /**
     * 年/月/日/文件名
     */
    public static final String REPORT_BASE_PATH = Constant.E_DATE + "/%s/%s/%s/%s";

    public static void requestReport_step1(JobRequest job) {
        if(job.state != JobRequest.S.NEW) {
            Logger.info("The JobRequest State is wrong! It`s must NEW State.");
            throw new FastRuntimeException("The JobRequest State is wrong! It`s must NEW State.");
        }
        switch(job.type) {
            case ALL_FBA_ORDER_FETCH:
            case ALL_FBA_ORDER_SHIPPED:
            case MANAGE_FBA_INVENTORY_ARCHIVED:
            case ACTIVE_LISTINGS:
                //TODO 需要使用 Monitor 来监控到底有多少次的请求
                MarketplaceWebService service = client(job);

                RequestReportRequest res = new RequestReportRequest()
                        .withMerchant(job.account.merchantId)
                        .withMarketplaceIdList(new IdList(Arrays.asList(job.marketplaceId.name()))) // only have the uk MarketplaceId.
                        .withReportType(job.type.toString())
                        .withReportOptions("ShowSalesChannel=true");
                try {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7))); // 新订单的获取, 捕捉最近 7 天内的
                    DatatypeFactory df = DatatypeFactory.newInstance();
                    res.setStartDate(df.newXMLGregorianCalendar(new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))));
                    RequestReportResponse resp = service.requestReport(res);
                    ReportRequestInfo info = resp.getRequestReportResult().getReportRequestInfo();

                    job.startDate = cal.getTime();
                    job.endDate = new Date();
                    job.requestId = info.getReportRequestId();
                    job.procressState = info.getReportProcessingStatus();
                    job.state = JobRequest.S.REQUEST;
                    job.lastUpdateDate = new Date();
                    job.save();
                } catch(MarketplaceWebServiceException e) {
                    Logger.warn("requestReport_step1 has error:{" + e.getMessage() + "}");
                } catch(DatatypeConfigurationException e) {
                    // can not be happed.
                    Logger.warn("DatatypeConfigurationException Can not be happed in AWS.requestReport_step1!");
                }
                return;
            default:
                Logger.info("Job Type is not valid, it is not a amazon request report Job.");
        }
    }


    public static void requestState_step2(JobRequest job) {
        if(job.state != JobRequest.S.REQUEST && job.state != JobRequest.S.PROCRESS) {
            Logger.info("The JobRequest State is wrong! It`s must REQUEST State.");
            throw new FastRuntimeException("The JobRequest State is wrong! It`s must REQUEST State.");
        }

        MarketplaceWebService client = client(job);

        // 一个一个 Report 的处理, 不再是一批处理, 因为 Service 会不同.
        GetReportRequestListRequest req = new GetReportRequestListRequest()
                .withMerchant(job.account.merchantId) //暂时只要其中一个
                .withReportRequestIdList(new IdList(Arrays.asList(job.requestId)));

        try {
            ReportRequestInfo info = client.getReportRequestList(req).getGetReportRequestListResult().getReportRequestInfoList().get(0);
            job.procressState = info.getReportProcessingStatus();
            if("_DONE_".equals(job.procressState)) {
                job.state = JobRequest.S.DONE;
            } else if("_DONE_NO_DATA_".equals(job.procressState)) {
                job.state = JobRequest.S.CLOSE;
            } else {
                job.state = JobRequest.S.PROCRESS;
            }
            job.lastUpdateDate = new Date();
            job.save();
        } catch(MarketplaceWebServiceException e) {
            Logger.warn("requestState_step2 has error:{" + e.getMessage() + "}");
        }

    }

    public static void requestReportId_step3(JobRequest job) {
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
            Logger.warn("requestReportId_step3 has error:{" + e.getMessage() + "}");
        }
    }

    public static void requestReportDown_step4(JobRequest job) {
        if(job.state != JobRequest.S.DOWN) {
            Logger.info("The JobRequest State is wrong! It`s must be DOWN State.");
            throw new FastRuntimeException("The JobRequest State is wrong! It`s must be DOWN State.");
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
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
                    job.reportId);
            switch(job.type) {
                case ALL_FBA_ORDER_FETCH:
                    filename += ".xml";
                    break;
                case ACTIVE_LISTINGS:
                    filename += ".txt";
                    break;
                case ALL_FBA_ORDER_SHIPPED:
                case MANAGE_FBA_INVENTORY_ARCHIVED:
                    filename += ".csv";
                    break;
            }
            FileUtils.write(new File(filename), byteBuffer.toString("UTF-8"));
            job.path = filename;
            job.state = JobRequest.S.END;
            job.lastUpdateDate = new Date();
            job.save();
        } catch(MarketplaceWebServiceException e) {
            Logger.warn("requestReportDown_step4 has error:{" + e.getMessage() + "}");
        } catch(IOException e) {
            Logger.warn("requestReportDown_step4 has error:{" + e.getMessage() + "}");
        }
    }


    private static final Map<String, MarketplaceWebService> cached = new HashMap<String, MarketplaceWebService>();

    /**
     * 通过 JobRequest 获取缓存了的 MarketplaceWebService 对象
     *
     * @param job
     * @return
     */
    private static MarketplaceWebService client(JobRequest job) {
        String key = String.format("client_%s_%s", job.account.id, job.account.type.name());
        MarketplaceWebService client;
        if(cached.containsKey(key)) return cached.get(key);
        else {
            synchronized(cached) {
                if(cached.containsKey(key)) return cached.get(key);

                MarketplaceWebServiceConfig config = new MarketplaceWebServiceConfig();
                switch(job.account.type) {
                    case AMAZON_US:
                        config.setServiceURL("https://mws.amazonservices.com");
                        break;
                    case AMAZON_UK:
                        // 无法使用这个地址, 因为 toString() 使用了 FulfillmentInventory
//                        config.setServiceURL(MWSEndpoint.UK.toString());
                        config.setServiceURL("https://mws.amazonservices.co.uk");
                        break;
                    case AMAZON_DE:
                        config.setServiceURL("https://mws.amazonservices.de");
                        break;
//                    case AMAZON_ES: // not right now..
//                        break;
                    case AMAZON_FR:
                        config.setServiceURL("https://mws.amazonservices.fr");
                        break;
                    case AMAZON_IT:
                        config.setServiceURL("https://mws.amazonservices.it");
                        break;
                }

                client = new MarketplaceWebServiceClient(
                        job.account.accessKey, job.account.token, "elcuk2", "1.0", config);
                cached.put(key, client);
            }
        }
        return client;
    }
}
