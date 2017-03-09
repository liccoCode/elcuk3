package controllers;

import controllers.api.SystemOperation;
import models.embedded.ERecordBuilder;
import models.procure.Cooperator;
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
        renderArgs.put("cooperators", Cooperator.suppliers());
        renderArgs.put("whouses", Whouse.exceptAMZWhoses());
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
        ProcureUnit unit = ProcureUnit.findById(record.unit.id);
        record.type = StockRecord.T.Stocktaking;
        record.currQty = unit.availableQty;
        record.createDate = new Date();
        record.save();
        unit.availableQty += record.qty;
        unit.save();
        record.recordId = record.id;
        record.whouse = unit.currWhouse;
        record.creator = Login.current();
        record.save();
        flash.success("调整库存成功");
        new ERecordBuilder("procureunit.adjuststock")
                .msgArgs(record.qty, unit.availableQty - record.qty, unit.availableQty, record.memo)
                .fid(unit.id, ProcureUnit.class)
                .save();
        stockIndex(new StockPost());
    }

    public static void show(Long id) {
        StockRecord record = StockRecord.findById(id);
        render(record);
    }

    public static void changeRecords(Long id) {
        index(new StockRecordPost(id));
    }

}
