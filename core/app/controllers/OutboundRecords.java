package controllers;

import controllers.api.SystemOperation;
import models.ElcukRecord;
import models.procure.Cooperator;
import models.view.Ret;
import models.view.post.OutboundRecordPost;
import models.whouse.OutboundRecord;
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
 * 出库记录控制器
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/6/16
 * Time: 5:07 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class OutboundRecords extends Controller {
    @Before(only = {"index", "blank", "create", "update"})
    public static void setWhouses() {
        renderArgs.put("whouses", Whouse.selfWhouses());
        renderArgs.put("shippers", Cooperator.shippers());
        renderArgs.put("suppliers", Cooperator.suppliers());
    }

    @Check("outboundrecords.index")
    public static void index(OutboundRecordPost p) {
        if(p == null) p = new OutboundRecordPost();
        List<OutboundRecord> records = p.query();
        List<ElcukRecord> elcukRecords = ElcukRecord.records(Arrays.asList(
                Messages.get("outboundrecord.confirm"),
                Messages.get("outboundrecord.update")), 50);
        for(OutboundRecord record : records) record.tryMatchAttrs();
        render(p, records, elcukRecords);
    }

    @Check("outboundrecords.index")
    public static void blank() {
        OutboundRecord record = new OutboundRecord(OutboundRecord.T.Normal, OutboundRecord.O.Other);
        render(record);
    }

    @Check("outboundrecords.index")
    public static void create(OutboundRecord record) {
        record.stockObj.setAttributes(record);
        record.valid();
        if(Validation.hasErrors()) render("OutboundRecords/blank.html", record);
        record.save();
        flash.success("创建成功!");
        redirect("/OutboundRecords/index");
    }

    @Check("outboundrecords.index")
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
    @Check("outboundrecords.index")
    public static void confirm(List<Long> rids, OutboundRecordPost p) {
        if(rids != null && !rids.isEmpty()) {
            List<String> errors = OutboundRecord.batchConfirm(rids);
            if(errors.isEmpty()) {
                flash.success("出库成功!");
            } else {
                flash.error(StringUtils.join(errors, "<br/>"));
            }

        }
        index(p);
    }
}
