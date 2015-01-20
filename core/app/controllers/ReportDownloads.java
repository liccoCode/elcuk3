package controllers;

import helper.Constant;
import helper.HTTP;
import helper.Webs;
import models.ReportRecord;
import models.view.Ret;
import models.view.post.ReportPost;
import play.mvc.Controller;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-1-16
 * Time: 下午4:40
 */
public class ReportDownloads extends Controller {

    public static void index(ReportPost p) {
        if(p == null) p = new ReportPost();
        List<ReportRecord> reports = p.query();
        render(p, reports);
    }

    public static void download(Long id) {
        ReportRecord record = ReportRecord.findById(id);
        record.downloadcount += 1;
        record.save();
        File file = new File(Constant.REPORT_PATH + "/" + record.filepath);
        file.deleteOnExit();
        renderBinary(file);
    }

    public static void repeatCalculate(Long id) {
        ReportRecord record = ReportRecord.findById(id);
        try {
            HTTP.get(record.calUrl());
            renderJSON(new Ret(true, String.valueOf("重新计算请求成功,请稍候!")));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }
}
