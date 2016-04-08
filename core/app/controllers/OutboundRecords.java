package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import models.procure.Cooperator;
import models.view.Ret;
import models.view.post.OutboundRecordPost;
import models.whouse.OutboundRecord;
import models.whouse.Whouse;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

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
    @Before(only = {"index", "blank"})
    public static void setWhouses() {
        renderArgs.put("whouses", Whouse.selfWhouses(null));
        renderArgs.put("shippers", Cooperator.shippers());
    }

    public static void index(OutboundRecordPost p) {
        if(p == null) p = new OutboundRecordPost();
        List<OutboundRecord> records = p.query();
        render(p, records);
    }

    public static void blank() {
        OutboundRecord record = new OutboundRecord();
        render(record);
    }

    public static void create(OutboundRecord record) {
        validation.valid(record);
        record.stockObj.valid();
        if(Validation.hasErrors()) render("OutboundRecords/blank.html", record);
        record.save();
        flash.success("创建成功!");
        redirect("/OutboundRecords/index");
    }

    public static void update(Long id, String attr, String value) {
        OutboundRecord record = OutboundRecord.findById(id);
        try {
            record.updateAttr(attr, value);
        } catch(FastRuntimeException e) {
            renderJSON(new Ret(false, e.getMessage()));
        }
        renderJSON(new Ret());
    }


    /**
     * 确认出库
     *
     * @param rids
     */
    public static void confirm(List<Long> rids) {
        if(!rids.isEmpty()) {
            for(Long rid : rids) {
                OutboundRecord record = OutboundRecord.findById(rid);
                record.confirm();
            }
        }
        if(Validation.hasErrors()) Webs.errorToFlash(flash);
        redirect("/OutboundRecords/index");
    }
}
