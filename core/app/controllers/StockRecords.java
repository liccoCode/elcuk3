package controllers;

import controllers.api.SystemOperation;
import models.procure.ProcureUnit;
import models.view.post.StockPost;
import models.view.post.StockRecordPost;
import models.whouse.StockRecord;
import models.whouse.Whouse;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
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
    @Before(only = {"index", "stockIndex"})
    public static void setWhouses() {
        renderArgs.put("whouses", Whouse.selfWhouses());
    }

    @Check("stockrecords.index")
    public static void index(StockRecordPost p) {
        if(p == null) p = new StockRecordPost();
        List<StockRecord> records = p.query();
        render(p, records);
    }

    public static void stockIndex(StockPost p) {
        if(p == null) p = new StockPost();
        List<ProcureUnit> units = p.query();
        render(p, units);
    }

    public static void adjustStock(Long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        StockRecord record = new StockRecord();
        render(unit, record);
    }

    public static void saveRecord(StockRecord record) {
        record.type = StockRecord.T.Stocktaking;
        record.createDate = new Date();
        record.save();
        ProcureUnit unit = ProcureUnit.findById(record.unit.id);
        unit.availableQty += record.qty;
        unit.save();
        flash.success("调整库存成功");
        index(new StockRecordPost());
    }

    public static void changeRecords(Long id) {
          index(new StockRecordPost(id));
    }

}
