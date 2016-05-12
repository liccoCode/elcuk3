package controllers;

import controllers.api.SystemOperation;
import models.ElcukRecord;
import models.view.Ret;
import models.view.post.InboundRecordPost;
import models.whouse.InboundRecord;
import models.whouse.Whouse;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.Arrays;
import java.util.List;

/**
 * 入库记录控制器
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/6/16
 * Time: 5:07 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class InboundRecords extends Controller {
    @Before(only = {"index", "blank", "create"})
    public static void setWhouses() {
        renderArgs.put("whouses", Whouse.selfWhouses(false));
    }

    @Check("inboundrecords.index")
    public static void index(InboundRecordPost p) {
        if(p == null) p = new InboundRecordPost();
        List<InboundRecord> records = p.query();
        List<ElcukRecord> elcukRecords = ElcukRecord.records(Arrays.asList(
                Messages.get("inboundrecord.confirm"),
                Messages.get("inboundrecord.update")
        ), 50);
        render(p, records, elcukRecords);
    }

    @Check("inboundrecords.index")
    public static void blank() {
        InboundRecord record = new InboundRecord(InboundRecord.O.Other);
        render(record);
    }

    @Check("inboundrecords.index")
    public static void create(InboundRecord record) {
        record.beforeCreate();
        record.valid();
        if(Validation.hasErrors()) render("InboundRecords/blank.html", record);
        record.save();
        record.stockObj.setAttributes(record);
        flash.success("创建成功!");
        redirect("/InboundRecords/index");
    }

    /**
     * 修改入库记录
     *
     * @param attr
     * @param value
     */
    @Check("inboundrecords.index")
    public static void update(Long id, String attr, String value) {
        try {
            InboundRecord record = InboundRecord.findById(id);
            record.updateAttr(attr, value);
        } catch(FastRuntimeException e) {
            renderJSON(new Ret(false, e.getMessage()));
        }
        renderJSON(new Ret());
    }

    /**
     * 确认入库
     *
     * @param rids
     */
    @Check("inboundrecords.index")
    public static void confirm(List<Long> rids) {
        if(rids != null && !rids.isEmpty()) {
            List<String> errors = InboundRecord.batchConfirm(rids);
            if(errors.isEmpty()) {
                flash.success("入库成功!");
            } else {
                flash.error(StringUtils.join(errors, "<br/>"));
            }
        }
        redirect("/InboundRecords/index");
    }
}
