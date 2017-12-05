package controllers;

import com.alibaba.fastjson.JSONObject;
import controllers.api.SystemOperation;
import helper.AmazonSQS;
import helper.Dates;
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

    public static void main(String[] args) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -30);

        Map<String, String> map = Dates.splitDayForDate(c.getTime(), new Date(), 2);
        System.out.print(map);
    }

    @Check("jobs.index")
    public static void run(String jobName, Date from, Date to, String market) {
        Map<String, Object> jobMap = new HashMap<>();
        Map<String, Object> jobParameters = new HashMap<>();
        //任务ID
        jobMap.put("jobName", jobName);
        if(Objects.equals(jobName, "multiCountryOrderrSync")) {
            if(from != null && to != null) {
                int day = (int) ((to.getTime() - from.getTime()) / (1000 * 3600 * 24));
                if(day > 31) {
                    flash.error("此Job调用时间差不能大于一个月");
                    render("/Jrockends/index.html", from, to, market);
                } else if(day > 4) {

                    int i = 1;
                    Map<String, String> map = Dates.splitDayForDate(from, to, 4);
                    for(String key : map.keySet()) {
                        jobParameters.put("beginDate", key);
                        jobParameters.put("endDate", map.get(key));
                        if(StringUtils.isNotBlank(market)) {
                            jobParameters.put("marketErp", market);
                        }
                        jobMap.put("args", jobParameters);
                        AmazonSQS.sendMessage(JSONObject.toJSONString(jobMap), i * 60);
                        i++;
                    }
                    render("/Jrockends/index.html", from, to, market);
                }
            }
        }

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
        render("/Jrockends/index.html", from, to, market);
    }

}
