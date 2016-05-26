package controllers;

import controllers.api.SystemOperation;
import models.ElcukRecord;
import models.procure.Cooperator;
import models.procure.ReceiveRecord;
import models.view.Ret;
import models.view.post.ReceiveRecordPost;
import models.whouse.Whouse;
import org.apache.commons.lang.StringUtils;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.Arrays;
import java.util.List;

/**
 * 收货记录控制器
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 5/23/16
 * Time: 3:53 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class ReceiveRecords extends Controller {
    @Before(only = {"index"})
    public static void beforeIndex() {
        renderArgs.put("cooperators", Cooperator.suppliers());
        renderArgs.put("whouses", Whouse.find("type=?", Whouse.T.FBA).fetch());
        renderArgs.put("elcukRecords", ElcukRecord.records(
                Arrays.asList(Messages.get("receiverecord.confirm"), Messages.get("receiverecord.update")), 50));
    }

    @Check("receiverecords.index")
    public static void index(ReceiveRecordPost p) {
        if(p == null) p = new ReceiveRecordPost();
        List<ReceiveRecord> records = p.query();
        render(p, records);
    }

    /**
     * 确认收货记录
     */
    @Check("receiverecords.index")
    public static void confirm(List<String> rids) {
        if(rids != null && !rids.isEmpty()) {
            List<String> errors = ReceiveRecord.batchConfirm(rids);
            if(errors.isEmpty()) {
                flash.success("确认成功!");
            } else {
                flash.error(StringUtils.join(errors, "<br/>"));
            }
        }
        redirect("/ReceiveRecords/index");

    }

    /**
     * 更新收货记录
     * 主箱: 箱数 数量 重量 长 宽 高
     * 尾箱: 箱数 数量 重量 长 宽 高
     *
     * @param id
     * @param attr  字段名称
     * @param value 值
     */
    @Check("receiverecords.index")
    public static void update(String id, String attr, String value) {
        try {
            ReceiveRecord record = ReceiveRecord.findById(id);
            record.updateAttr(attr, value);
        } catch(FastRuntimeException e) {
            renderJSON(new Ret(false, e.getMessage()));
        }
        renderJSON(new Ret());
    }
}
