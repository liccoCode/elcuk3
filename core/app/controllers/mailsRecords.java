package controllers;

import helper.J;
import helper.Webs;
import models.MailsRecord;
import models.view.Ret;
import models.view.dto.HighChart;
import play.Logger;
import play.mvc.Controller;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: rose
 * Date: 13-4-3
 * Time: 下午1:48
 */
public class MailsRecords extends Controller {

    public static void index() {
        Logger.debug("邮件 log   index --------------");
        render();
    }


    public static void ajaxRecord(Date from,Date to,String type,boolean success,String group) {

        Logger.info("开始获取 邮件日志....");
        Logger.info("from:%s  to %s    type=%s   success: %s   group: %s",from,to,type,success,group);
        try{
            HighChart  hc= MailsRecord.ajaxRecordBy(from, to, type, success, group);
            Logger.info("返回结果");
            renderJSON(J.json(hc));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.S(e)));
        }

    }


}
