package controllers;

import models.market.SellingRecord;
import models.view.highchart.HighChart;
import models.view.post.SellingRecordChartsPost;
import models.view.post.SellingRecordsPost;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

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

}
