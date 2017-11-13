package controllers;

import com.alibaba.fastjson.JSONObject;
import controllers.api.SystemOperation;
import helper.AmazonSQS;
import helper.GTs;
import helper.Webs;
import models.ElcukRecord;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        renderArgs.put("logs", ElcukRecord.records(Arrays.asList("jrockends.run"), 50));
        render();
    }


    @Check("jobs.index")
    public static void run(String jobName, Date from, Date to) {
        Map map = new HashMap();
        map.put("jobName", jobName);  //  任务ID

        if(from != null && to != null) {
            String beginDate = new SimpleDateFormat("yyyy-MM-dd").format(from);
            String endDate = new SimpleDateFormat("yyyy-MM-dd").format(to);
            map.put("args", GTs.newMap("beginDate", beginDate).put("endDate", endDate).build());  //开始日期和结束日期
        }
        String message = JSONObject.toJSONString(map);
        try {
            AmazonSQS.sendMessage(message);
        } catch(Exception e) {
            flash.error("Job [%s] 因 [%s] 调用失败.", jobName, Webs.e(e));
        }
        flash.success("Job [%s] 调用成功", jobName);
        new ElcukRecord(Messages.get("jrockends.run"),
                Messages.get("jrockends.run.msg", jobName), jobName).save();
        redirect("/Jrockends/index");
    }
}
