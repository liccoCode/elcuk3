package controllers;

import com.alibaba.fastjson.JSONObject;
import controllers.api.SystemOperation;
import helper.AmazonSQS;
import helper.Webs;
import models.ElcukRecord;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Arrays;
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
    public static void run(String jobName) {
        Map map = new HashMap();
        map.put("jobName", jobName);  //  任务ID
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
