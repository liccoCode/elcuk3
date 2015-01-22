package controllers;

import helper.Constant;
import helper.HTTP;
import helper.Webs;
import models.ReportRecord;
import models.market.OperatorConfig;
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

    /**
     * 报表相关参数设置
     */
    @Check("reportdownloads.config")
    public static void config() {
        List<OperatorConfig> configurations = OperatorConfig.findAll();
        render(configurations);
    }

    /**
     * 报表相关参数设置
     */
    @Check("reportdownloads.config")
    public static void editConfig(Long id) {
        OperatorConfig config = OperatorConfig.findById(id);
        if(config.fullName().equalsIgnoreCase("SHIPMENT_运输天数")) {
            render("ReportDownloads/marketShipDay.html", config);
        } else {
            render(config);
        }
    }

    /**
     * 报表相关参数设置
     */
    @Check("reportdownloads.config")
    public static void updateConfig(Long id, String val) {
        OperatorConfig config = OperatorConfig.findById(id);
        config.val = val;
        config.save();
        flash.success("参数 %s 设置成功!", config.name);
        config();
    }
}
