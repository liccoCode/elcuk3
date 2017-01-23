package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.view.Ret;
import models.view.post.ProcurePost;
import models.view.post.RefundPost;
import models.view.post.StockPost;
import models.whouse.InboundUnit;
import models.whouse.Refund;
import models.whouse.RefundUnit;
import models.whouse.Whouse;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
import play.modules.pdf.PDF;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static play.modules.pdf.PDF.renderPDF;

/**
 * Created by licco on 2016/11/28.
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Refunds extends Controller {

    @Before(only = {"index", "edit", "blank"})
    public static void beforeIndex() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        renderArgs.put("cooperators", cooperators);
        renderArgs.put("whouses", Whouse.selfWhouses(false));
        renderArgs.put("users", User.findAll());
    }

    public static void index(RefundPost p) {
        if(p == null) p = new RefundPost();
        List<Refund> refunds = p.query();
        render(p, refunds);
    }

    public static void edit(String id) {
        Refund refund = Refund.findById(id);
        renderArgs.put("logs", ElcukRecord.records(id, Arrays.asList("refundrecord.update"), 50));
        render(refund);
    }

    public static void blank(List<Long> pids) {
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        ProcureUnit proUnit = units.get(0);
        Refund refund = new Refund(proUnit);
        render(proUnit, refund, units);
    }

    public static void create(Refund refund, List<RefundUnit> dtos) {
        refund.createRefund(dtos);
        flash.success("退货单【" + refund.id + "】创建成功!");
        index(new RefundPost());
    }

    public static void update(Refund refund) {
        refund.save();
        flash.success("退货单【" + refund.id + "】更新成功!");
        index(new RefundPost());
    }

    public static void updateUnit(String id, String value, String attr) {
        RefundUnit unit = RefundUnit.findById(Long.parseLong(id));
        unit.updateAttr(attr, value);
        renderJSON(new Ret());
    }

    public static void refreshFbaCartonContentsByIds(String id) {
        RefundUnit unit = RefundUnit.findById(Long.parseLong(id));
        render("/Inbounds/boxInfo.html", unit);
    }

    public static void updateBoxInfo(RefundUnit unit) {
        unit = RefundUnit.findById(unit.id);
        unit.marshalBoxs();
        unit.save();
        renderJSON(new Ret(true));
    }

    public static void confirmRefund(List<String> ids) {
        List<Refund> refunds = Refund.find("id IN " + SqlSelect.inlineParam(ids)).fetch();
        Refund.validConfirmRefund(refunds);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            index(new RefundPost());
        }
        Refund.confirmRefund(refunds);
        flash.success(SqlSelect.inlineParam(ids) + "退货成功!");
        index(new RefundPost());
    }

    public static void printRefundForm(List<String> ids) {
        final PDF.Options options = new PDF.Options();
        options.pageSize = IHtmlToPdfTransformer.A4L;
        List<Refund> refunds = Refund.find("id IN " + SqlSelect.inlineParam(ids)).fetch();
        Map<Integer, List<RefundUnit>> ten = RefundUnit.pageNumForTen(refunds);
        renderPDF(options, ten);
    }

    public static void deleteUnit(Long[] ids) {
        List<RefundUnit> list = RefundUnit.find("id IN " + SqlSelect.inlineParam(ids)).fetch();
        Refund refund = list.get(0).refund;
        list.forEach(unit -> unit.delete());
        if(refund.unitList.size() == 0) {
            refund.status = Refund.S.Cancel;
            refund.save();
        }
        renderJSON(new Ret(true));
    }

    public static void unQualifiedIndex(StockPost p) {
        if(p == null) {
            p = new StockPost();
        }
        p.flag = true;
        List<Cooperator> cooperators = Cooperator.suppliers();
        List<ProcureUnit> units = p.query();
        render(p, units, cooperators);
    }

    public static void unQualifiedHandle(Long unitId, int qty, String memo) {
        Refund.unQualifiedHandle(unitId, qty, memo);
        flash.success("不良品退货成功!");
        unQualifiedIndex(null);
    }

    public static void transferQty(Long unitId, int qty, String memo) {
        Refund.transferQty(unitId, qty, memo);
        flash.success("不良品转入成功!");
        unQualifiedIndex(null);
    }

}
