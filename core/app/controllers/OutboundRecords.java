package controllers;

import controllers.api.SystemOperation;
import models.view.Ret;
import models.view.post.OutboundRecordPost;
import models.whouse.OutboundRecord;
import models.whouse.Whouse;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 出库记录控制器
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/6/16
 * Time: 5:07 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class OutboundRecords extends Controller {
    public static void index(OutboundRecordPost p) {
        if(p == null) p = new OutboundRecordPost();
        List<OutboundRecord> records = p.query();
        render(p, records);
    }

    public static void blank() {
        OutboundRecord record = new OutboundRecord();
        List<Whouse> whouses = Whouse.selfWhouses(null);
        render(record);
    }

    public static void create(OutboundRecord record) {
        validation.valid(record);
        if(Validation.hasErrors()) render("OutboundRecords/blank.html", record);
        record.save();
        flash.success("创建成功!");
        redirect("/OutboundRecords/index");
    }

    /**
     * 确认出库
     *
     * @param rids
     */
    public static void confirm(List<Long> rids) {
        if(rids == null || rids.isEmpty()) renderJSON(new Ret(false, "未选中任何出库记录!"));
        for(Long rid : rids) {
            OutboundRecord record = OutboundRecord.findById(rid);
            record.confirm();
        }
        renderJSON(new Ret());
    }
}
