package controllers.api;

import models.ElcukRecord;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 记录附件的日志
 * User: mac
 * Date: 14-3-27
 * Time: 上午10:12
 */
@With({APIChecker.class})
public class AttachLog extends Controller {
    
    public static void attachAction() {
        String action = request.params.get("action");
        String filename = request.params.get("filename");
        String materialcode = request.params.get("materialcode");
        String username = request.params.get("username");
        new ElcukRecord("attach.log",
                String.format("%s %s", filename, action), username, materialcode).save();
    }
}
