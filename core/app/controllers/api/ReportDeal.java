package controllers.api;

import helper.Constant;
import models.ReportRecord;
import models.view.Ret;
import org.joda.time.DateTime;
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
}
