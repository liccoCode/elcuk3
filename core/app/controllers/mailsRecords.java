package controllers;

import models.MailsRecord;
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
           render();
    }


    public static void ajaxRecord(Date from,Date to,String type,boolean success,String group) {
        MailsRecord.ajaxRecordBy(from, to, type, success, group);
    }


}
