package controllers;

import com.alibaba.fastjson.JSONObject;
import controllers.api.SystemOperation;
import helper.AmazonSQS;
import helper.Webs;
import models.ElcukRecord;
import models.market.Account;
import models.market.Jrockend;
import models.market.M;
import models.view.Ret;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.StringUtil;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/10/10
 * Time: AM11:10
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Jrockends extends Controller {


    @Check("jobs.index")
    public static void index() {
        renderArgs.put("logs", ElcukRecord.records(Collections.singletonList("jrockends.run"), 50));
        render();
    }


    /**
     * @param jobName   任务ID
     * @param from      开始时间
     * @param to        结束时间
     * @param splitDate 分割时间
     * @param market    执行市场
     */
    @Check("jobs.index")
    public static void run(String jobName,String esType, Date from, Date to, Integer splitDate, String market) {
        Map<String, Object> jobMap = new HashMap<>();
        jobMap.put("jobName", jobName);
        Map<String, Object> jobParameters = new HashMap<>();

        if (!Objects.equals(jobName, "multiCountryOrderrSync")) {
            /***************************  一般任务正常处理  ***************************/
            if (from != null && to != null) {
                jobParameters.put("beginDate", new SimpleDateFormat("yyyy-MM-dd").format(from));
                jobParameters.put("endDate", new SimpleDateFormat("yyyy-MM-dd").format(to));
            }
            if (StringUtils.isNotBlank(market)) {
                jobParameters.put("marketErp", market);
            }
            jobParameters.put("esType", esType);
            jobMap.put("args", jobParameters);
            String message = JSONObject.toJSONString(jobMap);
            try {
                AmazonSQS.sendMessage(message);
            } catch (Exception e) {
                flash.error("Job [%s] 因 [%s] 调用失败.", jobName, Webs.e(e));
                render("/Jrockends/index.html", from, to, splitDate, market);
            }
        } else {
            /***************************  订单xml同步任务特速处理  ***************************/
            if (from != null && to != null) {
                if (!StringUtil.isBlank(market)) {
                    //页面传递了market,那么只执行该市场的订单抓取
                    Jrockend.orderProcess(jobMap, jobParameters, from, to, splitDate, market);
                } else {
                    //页面未传递market,那么只执行所有市场的订单抓取
                    for (M m : M.values()) {
                        Jrockend.orderProcess(jobMap, jobParameters, from, to, splitDate, m.name());
                    }
                }
            } else {
                flash.error("Job [%s] 订单XML同步必须选择时间", jobName);
                render("/Jrockends/index.html", from, to, splitDate, market);
            }
        }
        flash.success("Job [%s] 调用成功", jobName);
        new ElcukRecord(Messages.get("jrockends.run"), Messages.get("jrockends.run.msg", jobName), jobName).save();
        render("/Jrockends/index.html", from, to, splitDate, market);
    }


    public static void syncOrderr(String reportId, String market) {
        if (StringUtils.isBlank(reportId) || StringUtils.isBlank(market)){
            renderJSON(new Ret("reportId/market为空"));
        }
        Account account = Account.find("type=?", M.val(market)).first();
        Map<String, Object> jobMap = new HashMap<>();
        jobMap.put("jobName", "amazonMwsJob");
        Map<String, Object> jobParameters = new HashMap<>();
        jobParameters.put("accountId", account.id);
        jobParameters.put("reportId", reportId);
        jobParameters.put("method", "GetReport");
        jobParameters.put("market", market);
        jobParameters.put("reportType","_GET_XML_ALL_ORDERS_DATA_BY_ORDER_DATE_");
        jobMap.put("args", jobParameters);
        String message = JSONObject.toJSONString(jobMap);
        try {
            AmazonSQS.sendMessage(message);
        } catch (Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
        renderJSON(new Ret());
    }

}
