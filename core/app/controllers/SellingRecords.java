package controllers;

import jobs.analyze.SellingRecordCaculateJob;
import models.market.SellingRecord;
import models.view.post.SellingRecordsPost;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.With;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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

    public static void index() {
        SellingRecordsPost p = new SellingRecordsPost();
        render(p);
    }

    public static void sid(SellingRecordsPost p) {
        if(p == null) p = new SellingRecordsPost();
        try {
            List<SellingRecord> records = p.query();
            render(records, p);
        } catch(Exception e) {
            renderArgs.put("msg", e.getMessage());
            render(p);
        }
    }

    public static void job(@As("yyyy-MM-dd") Date date) {
        new SellingRecordCaculateJob(new DateTime(date)).now();
        renderHtml("<h3>SellingRecordCaculateJob 开始执行</h3>");
    }

    public static void ttt() throws IOException {
        List<SellingRecord> records = Cache.get("sellingRecordCaculateJob", List.class);
        FileOutputStream fileout = new FileOutputStream(
                "/Users/wyatt/Programer/repo/elcuk2/core/sellings");
        ObjectOutputStream out = new ObjectOutputStream(fileout);
        out.writeObject(records);
        out.close();
        renderText("写入成功.");
    }
}
