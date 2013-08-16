package controllers;

import jobs.analyze.SellingRecordCaculateJob;
import models.market.SellingRecord;
import models.view.post.SellingRecordsPost;
import org.joda.time.DateTime;
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * 销售财务分析
 * User: wyatt
 * Date: 8/16/13
 * Time: 2:03 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class SellingRecords extends Controller {

    public static void index(SellingRecordsPost post) {
        if(post == null) post = new SellingRecordsPost();
        try {
            List<SellingRecord> records = post.query();
            render(records, post);
        } catch(Exception e) {
            renderArgs.put("msg", e.getMessage());
            render(post);
        }
    }

    public static void job(@As("yyyy-MM-dd") Date date) {
        new SellingRecordCaculateJob(new DateTime(date)).now();
        renderHtml("<h3>SellingRecordCaculateJob 开始执行</h3>");
    }
}
