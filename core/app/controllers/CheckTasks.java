package controllers;

import models.ElcukRecord;
import models.embedded.ERecordBuilder;
import models.procure.Cooperator;
import models.product.Whouse;
import models.qc.CheckTask;
import models.view.Ret;
import models.view.post.CheckTaskPost;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.helper.StringUtil;
import play.data.binding.As;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.modules.pdf.PDF;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

import static play.modules.pdf.PDF.renderPDF;

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
        List<ElcukRecord> records = ElcukRecord.find("action like '%CheckTask%' ORDER BY createAt DESC").fetch();
        List<CheckTask> checks = p.check();
        List<CheckTask> checkeds = p.checked();
        List<CheckTask> checkRepeats = p.checkRepeat();

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

    public static void show(Long id) {
        CheckTask check = CheckTask.findById(id);
        render(check);
    }

    @Check("checktasks.update")
    public static void update(Long id, CheckTask check, @As("yyyy-MM-dd HH:mm") Date from,
                              @As("yyyy-MM-dd HH:mm") Date to) {
        CheckTask old = CheckTask.findById(id);
        check.startTime = from;
        check.endTime = to;
        check.validateRight();
        if(Validation.hasErrors()) render("CheckTasks/show.html", check);
        old.update(check);
        flash.success("更新成功");
        redirect("/CheckTasks/show/" + id);
    }

    @Check("checktasks.update")
    public static void fullUpdate(Long id, CheckTask check, @As("yyyy-MM-dd HH:mm") Date from,
                                  @As("yyyy-MM-dd HH:mm") Date to) {
        CheckTask old = CheckTask.findById(id);
        check.startTime = from;
        check.endTime = to;
        check.validateRequired();
        check.validateRight();
        if(Validation.hasErrors()) render("CheckTasks/show.html", check);
        old.fullUpdate(check);
        flash.success("更新成功");
        redirect("/CheckTasks/checkerlist");
    }

    public static void prints(Long id) {
        CheckTask check = CheckTask.findById(id);
        check.arryParamSetUP(CheckTask.FLAG.STR_TO_ARRAY);
        render(check);
    }

    public static void addRequireAndWay(String require, String way, Long id) {
        if(StringUtil.isBlank(require)) {
            require = " ";
        }
        if(StringUtil.isBlank(way)) {
            way = " ";
        }
        CheckTask check = CheckTask.findById(id);
        check.arryParamSetUP(CheckTask.FLAG.STR_TO_ARRAY);
        check.qcRequire.add(require);
        check.qcWay.add(way);
        check.arryParamSetUP(CheckTask.FLAG.ARRAY_TO_STR);
        check.save();
        flash.success("添加成功!");
        prints(id);
    }

    public static void doPrints(Long id) {
        CheckTask check = CheckTask.findById(id);
        check.arryParamSetUP(CheckTask.FLAG.STR_TO_ARRAY);
        check.printNumber++;
        check.save();
        //log
        new ERecordBuilder("checktask.doPrints").fid(id).msgArgs(id).save();
        renderArgs.put("check", check);
        final PDF.Options options = new PDF.Options();
        options.pageSize = IHtmlToPdfTransformer.A4P;
        renderPDF(options);
    }

    /**
     * 质检信息查看列表
     * <p/>
     * 当采购计划ID下存在1个以上的质检任务
     */
    public static void showList(Long id) {
        List<CheckTask> checks = CheckTask.find("units_id=?", id).fetch();
        render(checks);
    }
}

