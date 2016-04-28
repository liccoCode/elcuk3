package controllers;

import controllers.api.SystemOperation;
import models.view.post.StockRecordPost;
import models.whouse.StockRecord;
import models.whouse.Whouse;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 库存异动控制器
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/6/16
 * Time: 5:08 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class StockRecords extends Controller {
    @Before(only = {"index"})
    public static void setWhouses() {
        renderArgs.put("whouses", Whouse.selfWhouses());
    }

    @Check("stockrecords.index")
    public static void index(StockRecordPost p) {
        if(p == null) p = new StockRecordPost();
        List<StockRecord> records = p.query();
        render(p, records);
    }
}
