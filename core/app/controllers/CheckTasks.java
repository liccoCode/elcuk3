package controllers;

import models.ElcukRecord;
import models.procure.Cooperator;
import models.product.Whouse;
import models.qc.CheckTask;
import models.view.Ret;
import models.view.post.CheckTaskPost;
import org.joda.time.DateTime;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * sku_check列表
 * User: cary
 * Date: 5/8/14
 * Time: 3:53 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class CheckTasks extends Controller {

    @Before(only = {"checklist", "checkerList"})
    public static void beforIndex() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        renderArgs.put("whouses", Whouse.<Whouse>find("type !=?", Whouse.T.FORWARD).fetch());
        renderArgs.put("shipwhouses", Whouse.<Whouse>find("type =?", Whouse.T.FORWARD).fetch());
        renderArgs.put("cooperators", cooperators);
    }


    @Check("checktasks.checklist")
    public static void checklist(CheckTaskPost p, int day) {
        CheckTask.generateTask();
        List<CheckTask> tasklist = null;
        if(p == null) p = new CheckTaskPost();
        if(day == 3) {
            p.from = DateTime.now().minusDays(3).toDate();
            p.to = new Date();
        } else if(day == 2) {
            p.from = DateTime.now().minusDays(2).toDate();
            p.to = new Date();
        } else if(day == 1) {
            p.from = DateTime.now().minusDays(1).toDate();
            p.to = new Date();
        }
        tasklist = p.query();
        render(tasklist, p);
    }

    /**
     * 质检员任务列表
     */
    @Check("checktasks.checkerList")
    public static void checkerList(CheckTaskPost p, int day) {
        String username = Secure.Security.connected();
        if(p == null) p = new CheckTaskPost(username);
        if(day == 3) {
            p.from = DateTime.now().minusDays(3).toDate();
            p.to = new Date();
        } else if(day == 2) {
            p.from = DateTime.now().minusDays(2).toDate();
            p.to = new Date();
        } else if(day == 1) {
            p.from = DateTime.now().minusDays(1).toDate();
            p.to = new Date();
        }
        List<CheckTask> checks = p.check();
        List<CheckTask> checkeds = p.checked();
        List<CheckTask> checkRepeats = p.checkRepeat();
        List<ElcukRecord> records = ElcukRecord.records("qcCheckRecords");

        render(p, checks, checkeds, checkRepeats, records);
    }

    /**
     * 更新质检方式
     */
    public static void updateQcType(long id, CheckTask.T qcType) {
        CheckTask check = CheckTask.findById(id);
        check.qcType = qcType;
        check.save();
        renderJSON(new Ret());
    }
}

