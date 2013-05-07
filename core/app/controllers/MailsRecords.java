package controllers;

import helper.J;
import helper.Webs;
import models.MailsRecord;
import models.view.Ret;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rose
 * Date: 13-4-3
 * Time: 下午1:48
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class MailsRecords extends Controller {

    @Check("mailsRecords.index")
    public static void index() {
        render();
    }

    /**
     * 查询邮件日志
     *
     * @param from
     * @param to
     * @param type
     * @param templates
     * @param success
     * @param group
     */
    public static void ajaxRecord(Date from, Date to, MailsRecord.T type, List<String> templates, boolean success, String group) {
        try {
            renderJSON(J.json(MailsRecord.ajaxRecordBy(from, to, type, templates, success, group)));
        } catch(Exception e) {
            e.printStackTrace();
            renderJSON(new Ret(Webs.S(e)));
        }

    }


}
