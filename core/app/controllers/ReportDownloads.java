package controllers;

import controllers.api.SystemOperation;
import helper.Constant;
import helper.Webs;
import models.ReportRecord;
import models.procure.Cooperator;
import models.view.Ret;
import models.view.post.ReportPost;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.util.List;

/**
 * 各类报表控制器
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-1-16
 * Time: 下午4:40
 */
@With({SystemOperation.class})
public class ReportDownloads extends Controller {

    public static void index(ReportPost p) {
        if(p == null) p = new ReportPost();
        p.reportTypes = ReportPost.saleReportTypes();
        List<ReportRecord> reports = p.query();
        render(p, reports);
    }

    /**
     * 报表下载
     *
     * @param id
     */
    public static void download(Long id) {
        ReportRecord record = ReportRecord.findById(id);
        record.downloadcount += 1;
        record.save();
        File file = new File(Constant.REPORT_PATH + "/" + record.filepath);
        renderBinary(file);
    }

    /**
     * 重新计算
     *
     * @param id
     */
    public static void repeatCalculate(Long id) {
        ReportRecord record = ReportRecord.findById(id);
        notFoundIfNull(record);
        try {
            record.recalculate();
            renderJSON(new Ret(true, String.valueOf("重新计算请求成功,请稍候!")));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    /**
     * 财务报表
     */
    @Check("reportdownloads.applyreportsindex")
    public static void applyReportsIndex(ReportPost p) {
        if(p == null) p = new ReportPost();
        p.reportTypes = ReportPost.applyReportTypes();
        List<ReportRecord> reports = p.query();
        List<Cooperator> cooperators = Cooperator.suppliers();
        render(p, reports, cooperators);
    }

    /**
     * 采购报表
     *
     * @deprecated
     */
    @Check("report.procurereports")
    public static void procureReportsIndex(ReportPost p) {
        if(p == null) p = new ReportPost();
        p.reportTypes = ReportPost.procureReportTypes();
        List<ReportRecord> reports = p.query();
        render(p, reports);
    }
}
