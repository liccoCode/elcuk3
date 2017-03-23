package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import models.ElcukRecord;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.view.Ret;
import models.view.post.DeliverPlanPost;
import models.view.post.InboundPost;
import models.whouse.Inbound;
import models.whouse.InboundUnit;
import models.whouse.Refund;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static play.modules.pdf.PDF.renderPDF;

/**
 * Created by licco on 2016/11/9.
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Inbounds extends Controller {

    @Before(only = {"index", "edit", "showProcureUnitList"})
    public static void beforeIndex() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        renderArgs.put("cooperators", cooperators);
        renderArgs.put("whouses", Whouse.exceptAMZWhoses());
    }


    public static void index(InboundPost p) {
        if(p == null) p = new InboundPost();
        List<Inbound> inbounds = p.query();
        for(Inbound inbound : inbounds) {
            inbound.showTime();
        }
        render(p, inbounds);
    }

    public static void indexDetail(InboundPost p) {
        if(p == null) p = new InboundPost();
        List<InboundUnit> units = p.queryDetail();
        render(p, units);
    }

    public static void createValidate(List<Long> pids, String type) {
        String msg = ProcureUnit.validateIsInbound(pids, type);
        if(StringUtils.isNotBlank(msg)) {
            renderJSON(new Ret(false, msg));
        } else {
            renderJSON(new Ret(true));
        }
    }


    public static void blank(List<Long> pids, String planId) {
        if(pids == null || pids.size() == 0)
            renderText("无可创建收货入库单的采购计划！请重新选择！");
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        ProcureUnit proUnit = units.get(0);
        Inbound.T it;
        if(proUnit.stage == ProcureUnit.STAGE.DELIVERY || proUnit.stage == ProcureUnit.STAGE.IN_STORAGE) {
            it = Inbound.T.Purchase;
        } else {
            it = Inbound.T.Machining;
        }
        String username = Login.current().username;
        render(units, proUnit, planId, it, username);
    }

    public static void create(Inbound inbound, List<InboundUnit> dtos) {
        inbound.id = Inbound.id();
        inbound.receiver = Login.current();
        inbound.createDate = new Date();
        inbound.status = inbound.type == Inbound.T.Purchase ? Inbound.S.Create : Inbound.S.Handing;
        inbound.save();
        inbound.create(dtos);
        flash.success("创建成功!");
        edit(inbound.id);
    }

    /**
     * 通过出货单创建收货入库单
     *
     * @param id
     */
    public static void createByPlanId(String id, DeliverPlanPost p, List<Long> pids) {
        List<ProcureUnit> units = ProcureUnit.find("id IN " + SqlSelect.inlineParam(pids)).fetch();
        for(ProcureUnit unit : units) {
            if(StringUtils.isNotEmpty(Refund.isAllReufund(unit.id))) {
                flash.error("采购计划【" + unit.id + "】正在走退货流程，请查证！ 【" + Refund.isAllReufund(unit.id) + "】");
                DeliverPlans.indexWhouse(p);
            }
        }
        List<Long> ids = units.stream()
                .filter(unit -> unit.stage == ProcureUnit.STAGE.DELIVERY && InboundUnit.validIsCreate(unit.id))
                .map(unit -> unit.id).collect(Collectors.toList());
        if(ids.size() == 0) {
            flash.error("所选的采购计划不符合收货入库条件!");
            DeliverPlans.indexWhouse(p);
        }
        blank(ids, id);
    }

    public static void update(Inbound inbound, String inboundId) {
        Inbound in = Inbound.findById(inboundId);
        in.saveAndLog(inbound);
        flash.success("更新成功!");
        edit(in.id);
    }

    public static void edit(String id) {
        Inbound inbound = Inbound.findById(id);
        renderArgs.put("logs", ElcukRecord.records(id, Arrays.asList("inbound.update"), 50));
        render(inbound);
    }

    /**
     * 根据采购计划ID 进入修改页面
     *
     * @param unitId
     */
    public static void editByUnitId(Long unitId) {
        ProcureUnit unit = ProcureUnit.findById(unitId);
        if(unit.parent != null && unit.type == ProcureUnit.T.StockSplit) {
            unitId = unit.parent.id;
        }
        InboundUnit inboundUnit = InboundUnit.find("unit.id = ? ORDER BY id DESC ", unitId).first();
        if(inboundUnit == null) {
            renderText("该采购计划属于老数据，无收货入库单据！");
        }
        edit(inboundUnit.inbound.id);
    }

    /**
     * 更新入库单信息
     *
     * @param id
     * @param value
     * @param attr
     */
    public static void updateUnit(String id, String value, String attr) {
        InboundUnit unit = InboundUnit.findById(Long.parseLong(id));
        unit.updateAttr(attr, value);
        renderJSON(new Ret());
    }

    /***
     * 确认收货
     *
     * @param inbound
     * @param dtos
     */
    @Check("inbounds.confirmreceivebtn")
    public static void confirmReceive(Inbound inbound, List<InboundUnit> dtos, String inboundId) {
        if(dtos.stream().anyMatch(Inbound::validTailInbound)) {
            flash.error("采购计划已经出库，不能进行收货操作");
            edit(inboundId);
        }
        Inbound bound = Inbound.findById(inboundId);
        if(bound.status == Inbound.S.Create) {
            bound.receiveDate = inbound.receiveDate;
            bound.name = inbound.name;
            bound.memo = inbound.memo;
            bound.status = Inbound.S.Handing;
            bound.saveLog("确认收货", inboundId);
            bound.save();
        } else {
            flash.error("此单已经完成收货操作!");
            edit(inboundId);
        }
        bound.confirmReceive(dtos);
        flash.success("收货成功!");
        edit(inboundId);
    }

    /**
     * 质检员确认质检
     *
     * @param inbound
     * @param dtos
     */
    @Check("inbounds.confirmqcbtn")
    public static void confirmQC(Inbound inbound, List<InboundUnit> dtos, String inboundId) {
        Inbound bound = Inbound.findById(inboundId);
        bound.confirmQC(dtos);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            edit(inboundId);
        }
        bound.saveLog("确认质检", inbound.id);
        bound.checkIsFinish();
        flash.success("质检成功!");
        edit(inboundId);
    }

    public static void refreshFbaCartonContentsByIds(String[] ids) {
        List<InboundUnit> units = InboundUnit.find(" id IN " + SqlSelect.inlineParam(ids)).fetch();
        render("/Inbounds/boxInfo.html", units);
    }

    public static void updateBoxInfo(List<InboundUnit> units) {
        try {
            Inbound.updateBoxInfo(units);
            renderJSON(new Ret(true));
        } catch(Exception e) {
            renderJSON(new Ret(false));
        }
    }

    public static void deleteUnit(Long[] ids) {
        List<InboundUnit> list = InboundUnit.find("id IN " + SqlSelect.inlineParam(ids)).fetch();
        Inbound inbound = list.get(0).inbound;
        list.forEach(InboundUnit::delete);
        if(inbound.units.size() == 0) {
            inbound.status = Inbound.S.Cancel;
            inbound.save();
        }
        renderJSON(new Ret(true));
    }

    public static void printQuaternionForm(List<String> ids) {
        final PDF.Options options = new PDF.Options();
        options.filename = "TEST001.pdf";
        options.pageSize = IHtmlToPdfTransformer.A4L;
        List<Inbound> inbounds = Inbound.find("id IN " + SqlSelect.inlineParam(ids)).fetch();
        Map<Integer, List<InboundUnit>> ten = InboundUnit.pageNumForTen(inbounds);
        renderPDF(options, ten);
    }

    public static void showProcureUnitList(String id) {
        Inbound inbound = Inbound.findById(id);
        List<InboundUnit> units = inbound.units;
        render("/Inbounds/_units.html", units);
    }

}