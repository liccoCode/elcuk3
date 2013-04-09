package controllers;

import helper.J;
import helper.Webs;
import models.MailsRecord;
import models.view.Ret;
import models.view.dto.HighChart;
import play.Logger;
import play.mvc.Controller;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rose
 * Date: 13-4-3
 * Time: 下午1:48
 */
public class MailsRecords extends Controller {

    public static void index() {
        render();
    }

    /**
     * 查询邮件日志
     * @param from
     * @param to
     * @param type
     * @param tmp
     * @param success
     * @param group
     */
    public static void ajaxRecord(Date from, Date to, MailsRecord.T type, List<String> tmp, boolean success, String group) {
        try {
            renderJSON(J.json(MailsRecord.ajaxRecordBy(from, to, type, tmp, success, group)));
        } catch(Exception e) {
            e.printStackTrace();
            renderJSON(new Ret(Webs.S(e)));
        }

    }


}
