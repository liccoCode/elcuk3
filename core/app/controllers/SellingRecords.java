package controllers;

import jobs.analyze.SellingRecordCaculateJob;
import models.market.SellingRecord;
import models.view.dto.HighChart;
import models.view.post.SellingRecordChartsPost;
import models.view.post.SellingRecordsPost;
import org.joda.time.DateTime;
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 销售财务分析
 * User: wyatt
 * Date: 8/16/13
 * Time: 2:03 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class SellingRecords extends Controller {

    @Check("sellingrecords.index")
    public static void index() {
        SellingRecordsPost p = new SellingRecordsPost();
        render(p);
    }

    /**
     * 页面下方的表格
     *
     * @param p
     */
    @Check("sellingrecords.table")
    public static void table(SellingRecordsPost p) {
        if(p == null) p = new SellingRecordsPost();
        try {
            List<SellingRecord> records = p.query();
            render(records, p);
        } catch(Exception e) {
            renderArgs.put("msg", e.getMessage());
            render(p);
        }
    }

    /**
     * 页面左上访的曲线
     *
     * @param p
     */
    @Check("sellingrecords.lines")
    public static void lines(SellingRecordChartsPost p) {
        if(p == null) p = new SellingRecordChartsPost("line");
        HighChart chart = p.query().get(0);
        renderJSON(chart);
    }

    @Check("sellingrecords.columns")
    public static void columns(SellingRecordChartsPost p) {
        if(p == null) p = new SellingRecordChartsPost("column");
        p.lineType = "column";
        HighChart chart = p.query().get(0);
        renderJSON(chart);
    }

    public static void job(@As("yyyy-MM-dd") Date date) throws ExecutionException, InterruptedException {
        new SellingRecordCaculateJob(new DateTime(date)).now().get();
        renderHtml("<h3>SellingRecordCaculateJob 开始执行</h3>");
    }

}
