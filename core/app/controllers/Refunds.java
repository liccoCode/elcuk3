package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.view.Ret;
import models.view.post.RefundPost;
import models.view.post.StockPost;
import models.whouse.Refund;
import models.whouse.RefundUnit;
import models.whouse.Whouse;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
import play.modules.pdf.PDF;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.time.LocalDate;
import java.util.*;

import static play.modules.pdf.PDF.renderPDF;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2016/11/28
 * Time: 下午2:15
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
        renderArgs.put("logs", ElcukRecord.records(id, Arrays.asList("refund.update"), 50));
        render(refund);
    }

    public static void blank(List<Long> pids) {
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        ProcureUnit proUnit = units.get(0);
        Refund refund = new Refund(proUnit);
        refund.name = String.format("%s_%s_退货", proUnit.cooperator.name, LocalDate.now());
        render(proUnit, refund, units);
    }

    public static void create(Refund refund, List<RefundUnit> dtos) {
        refund.createRefund(dtos);
        flash.success("退货单【" + refund.id + "】创建成功!");
        index(new RefundPost());
    }

    public static void update(Refund refund, String rid) {
        Refund old = Refund.findById(rid);
        old.saveAndLog(refund);
        flash.success("退货单【" + rid + "】更新成功!");
        edit(rid);
    }

    public static void quickAddByEdit(Long procureId, String refundId) {
        Refund refund = Refund.findById(refundId);
        refund.quickAddByEdit(procureId);
        if(!Validation.hasErrors())
            flash.success("采购计划【" + procureId + "】添加成功!");
        render("/Refunds/edit.html", refund);
    }

    public static void updateUnit(String id, String value, String attr) {
        RefundUnit unit = RefundUnit.findById(Long.parseLong(id));
        unit.updateAttr(attr, value);
        renderJSON(new Ret());
    }

    public static void refreshFbaCartonContentsByIds(String[] ids) {
        List<RefundUnit> units = RefundUnit.find("id IN " + SqlSelect.inlineParam(ids)).fetch();
        render("/Inbounds/boxInfo.html", units);
    }

    public static void updateBoxInfo(List<RefundUnit> units) {
        try {
            Refund.updateBoxInfo(units);
            if(Validation.hasErrors()) {
                renderJSON(new Ret(false, "包装信息超过计划退货数"));
            }
            renderJSON(new Ret(true));
        } catch(Exception e) {
            renderJSON(new Ret(false));
        }
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
        if(StringUtils.isNotBlank(Refund.isAllReufund(unitId))) {
            flash.error("采购计划" + unitId + "已经创建了退货单，请先处理！");
            unQualifiedIndex(null);
        }
        ProcureUnit unit = new ProcureUnit();
        unit.id = unitId;
        unit.attrs.qty = qty;
        Refund.unQualifiedHandle(Collections.singletonList(unit), memo);
        flash.success("不良品退货成功!");
        unQualifiedIndex(null);
    }

    public static void transferQty(Long unitId, int qty, String memo, String type) {
        if(StringUtils.isNotBlank(Refund.isAllReufund(unitId))) {
            flash.error("采购计划" + unitId + "已经创建了退货单，请先处理！");
            unQualifiedIndex(null);
        }
        Refund.transferQty(unitId, qty, memo, type);
        flash.success("不良品转入成功!");
        unQualifiedIndex(null);
    }

    /**
     * @param units
     * @param batchMemo
     */
    public static void batchRefund(List<ProcureUnit> units, String batchMemo) {
        Refund.unQualifiedHandle(units, batchMemo);
        flash.success("不良品退货成功!");
        unQualifiedIndex(null);
    }

    public static void validateAddRefund(Long id, Long cooperId) {
        ProcureUnit unit = ProcureUnit.findById(id);
        if(unit.stage != ProcureUnit.STAGE.IN_STORAGE) {
            renderJSON(new Ret(false, "该采购计划状态不是已入仓！"));
        }
        if(unit.parent != null && ProcureUnit.T.StockSplit == unit.type) {
            renderJSON(new Ret(false, "库存分拆的子采购计划【" + unit.id + "】无法进行退货！"));
        }
        if(unit.outbound != null) {
            renderJSON(new Ret(false, "采购计划【" + unit.id + "】已经在出库单 【" + unit.outbound.id + "】中,请先解除！"));
        }
        if(unit.cooperator != null && !Objects.equals(unit.cooperator.id, cooperId)) {
            renderJSON(new Ret(false, "请输入同供应商下的采购计划！"));
        }
        if(StringUtils.isNotEmpty(Refund.isAllReufund(id))) {
            renderJSON(new Ret(false, "采购计划【" + id + "】正在走退货流程，请查证！ 【" + Refund.isAllReufund(id) + "】"));
        }
        renderJSON(new Ret(true));
    }

}
