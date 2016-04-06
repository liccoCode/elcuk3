package controllers;

import controllers.api.SystemOperation;
import models.view.Ret;
import models.view.post.InboundRecordPost;
import models.whouse.InboundRecord;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

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
    public static void index(InboundRecordPost p) {
        if(p == null) p = new InboundRecordPost();
        List<InboundRecord> records = p.query();
        render(p, records);
    }

    public static void blank() {
        InboundRecord record = new InboundRecord();
        render(record);
    }

    /**
     * 修改入库记录
     *
     * @param attr
     * @param value
     */
    public static void updateInboundRecord(Long id, String attr, String value) {
        InboundRecord record = InboundRecord.findById(id);
        try {
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
    public static void confirm(List<Long> rids) {
        if(rids == null || rids.isEmpty()) renderJSON(new Ret(false, "未选中任何入库记录!"));
        for(Long rid : rids) {
            InboundRecord record = InboundRecord.findById(rid);
            record.confirm();
        }
        renderJSON(new Ret());
    }
}
