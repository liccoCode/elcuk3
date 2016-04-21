package controllers.api;

import helper.Constant;
import helper.J;
import jobs.analyze.SellingProfitJob;
import jobs.analyze.SellingProfitSearch;
import jobs.analyze.SkuSaleProfitJob;
import models.ReportRecord;
import models.view.Ret;
import models.view.post.ProfitPost;
import models.view.post.SkuProfitPost;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.util.List;

/**
 * 销量分析执行后需要清理缓存，保证数据及时
 * User: mac
 * Date: 14-3-27
 * Time: 上午10:12
 */
@With({APIChecker.class})
public class ReportDeal extends Controller {
    /**
     * 销量分析执行完后清理缓存
     */
    public static void reportClear() {
        List<ReportRecord> records = ReportRecord.find("reporttype=? and createAt<=?",
                ReportRecord.RT.ANALYZEREPORT, DateTime.now().plusDays(-14).toDate()).fetch();
        for(ReportRecord record : records) {
            File file = new File(Constant.REPORT_PATH + "/" + record.filepath);
            file.delete();
            record.delete();
        }
        renderJSON(new Ret(true, "清理销售分析文件成功!"));
    }


    public static void profitJob() {
        ProfitPost p = new ProfitPost();
        p.sku = request.params.get("sku");
        p.pmarket = request.params.get("pmarket");
        p.category = request.params.get("category");
        String begin = request.params.get("begin");
        String end = request.params.get("end");
        p.begin = DateTime.parse(begin, DateTimeFormat.forPattern("yyyy-MM-dd")).toDate();
        p.end = DateTime.parse(end, DateTimeFormat.forPattern("yyyy-MM-dd")).toDate();
        Logger.info("ProfitPost json: %s", J.json(p));

        //利润查询
        new SellingProfitSearch(p).now();
        //生成excel
        new SellingProfitJob(p).now();
        renderJSON(new Ret(true, "调用利润job成功!"));
    }

    public static void skuSaleProfitJob() {
        Logger.info("开始执行skuSaleProfitJob......");
        SkuProfitPost p = new SkuProfitPost();
        p.sku = request.params.get("sku");
        p.pmarket = request.params.get("pmarket");
        p.categories = request.params.get("categories");
        String begin = request.params.get("begin");
        String end = request.params.get("end");
        p.begin = DateTime.parse(begin, DateTimeFormat.forPattern("yyyy-MM-dd")).toDate();
        p.end = DateTime.parse(end, DateTimeFormat.forPattern("yyyy-MM-dd")).toDate();
        Logger.debug("ProfitPost json: %s", J.json(p));

        new SkuSaleProfitJob(p).now();

        renderJSON(new Ret(true, "调用利润job成功!"));
    }
}
