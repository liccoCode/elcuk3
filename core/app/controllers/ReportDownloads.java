package controllers;

import controllers.api.SystemOperation;
import helper.Constant;
import helper.HTTP;
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

    /**
     * 销售报表
     *
     * @param p
     */
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
        try {
            HTTP.get(String.format("%s/sku_month_profit_repeat?year=%s&month=%s",
                    System.getenv(Constant.ROCKEND_HOST), record.year, record.month));
            renderJSON(new Ret(true, String.valueOf("重新计算请求成功,请稍候!")));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
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
