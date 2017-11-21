package controllers;

import com.alibaba.fastjson.JSONObject;
import controllers.api.SystemOperation;
import helper.AmazonSQS;
import helper.Webs;
import models.ElcukRecord;
import org.apache.commons.lang3.StringUtils;
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


    @Check("jobs.index")
    public static void run(String jobName, Date from, Date to, String market) {
        Map<String, Object> jobMap = new HashMap<>();
        //任务ID
        jobMap.put("jobName", jobName);
        Map<String, Object> jobParameters = new HashMap<>();
        if(from != null && to != null) {
            jobParameters.put("beginDate", new SimpleDateFormat("yyyy-MM-dd").format(from));
            jobParameters.put("endDate", new SimpleDateFormat("yyyy-MM-dd").format(to));
        }
        if(StringUtils.isNotBlank(market)) {
            jobParameters.put("marketErp", market);
        }
        jobMap.put("args", jobParameters);
        String message = JSONObject.toJSONString(jobMap);
        try {
            AmazonSQS.sendMessage(message);
        } catch(Exception e) {
            flash.error("Job [%s] 因 [%s] 调用失败.", jobName, Webs.e(e));
        }
        flash.success("Job [%s] 调用成功", jobName);
        new ElcukRecord(Messages.get("jrockends.run"), Messages.get("jrockends.run.msg", jobName), jobName).save();
        redirect("/Jrockends/index");
    }
}
