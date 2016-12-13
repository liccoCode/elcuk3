package controllers;

import controllers.api.SystemOperation;
import models.OperatorConfig;
import models.procure.Cooperator;
import models.procure.DeliverPlan;
import models.procure.ProcureUnit;
import models.qc.CheckTaskDTO;
import models.view.Ret;
import models.view.post.InboundPost;
import models.whouse.Inbound;
import models.whouse.InboundUnit;
import models.whouse.Whouse;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.commons.lang.StringUtils;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
import play.modules.pdf.PDF;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static play.modules.pdf.PDF.renderPDF;

/**
 * Created by licco on 2016/11/9.
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Inbounds extends Controller {

    @Before(only = {"index", "edit"})
    public static void beforeIndex() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        renderArgs.put("cooperators", cooperators);
        renderArgs.put("whouses", Whouse.selfWhouses(false));
    }


    public static void index(InboundPost p) {
        if(p == null) p = new InboundPost();
        List<Inbound> inbounds = p.query();
        for(Inbound inbound : inbounds) {
            inbound.showTime();
        }
        render(p, inbounds);
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
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        ProcureUnit proUnit = units.get(0);
        Inbound.T it = proUnit.stage == ProcureUnit.STAGE.DELIVERY ? Inbound.T.Purchase : Inbound.T.Machining;
        render(units, proUnit, planId, it);
    }

    public static void create(Inbound inbound, List<InboundUnit> dtos) {
        inbound.id = Inbound.id();
        inbound.receiver = Login.current();
        inbound.createDate = new Date();
        inbound.projectName = inbound.isb2b ? "B2B" : OperatorConfig.getVal("brandname");
        inbound.status = inbound.type == Inbound.T.Purchase ? Inbound.S.Create : Inbound.S.Handing;
        inbound.save();
        inbound.create(dtos);
        flash.success("创建成功!");
        index(new InboundPost());
    }

    /**
     * 通过出货单创建收货入库单
     *
     * @param id
     */
    public static void createByPlanId(String id) {
        DeliverPlan plan = DeliverPlan.findById(id);
        List<Long> ids = new ArrayList<>();
        for(ProcureUnit unit : plan.units) {
            if(unit.stage == ProcureUnit.STAGE.DELIVERY) {
                ids.add(unit.id);
            }
        }
        blank(ids, id);
    }

    public static void update(Inbound inbound) {
        inbound.save();
        flash.success("更新成功!");
        index(new InboundPost());
    }

    public static void edit(String id) {
        Inbound inbound = Inbound.findById(id);

        render(inbound);
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
    public static void confirmReceive(Inbound inbound, List<InboundUnit> dtos) {
        inbound.status = Inbound.S.Handing;
        inbound.save();
        inbound.confirmReceive(dtos);
        flash.success("收货成功!");
        index(new InboundPost());
    }

    /**
     * 质检员确认质检
     *
     * @param inbound
     * @param dtos
     */
    public static void confirmQC(Inbound inbound, List<InboundUnit> dtos) {
        inbound.confirmQC(dtos);
        inbound.checkIsFinish();
        flash.success("质检成功!");
        index(new InboundPost());

    }

    /**
     * 确认入库
     *
     * @param inbound
     * @param dtos
     */
    public static void confirmInbound(Inbound inbound, List<InboundUnit> dtos) {
        inbound.confirmInbound(dtos);
        inbound.checkIsFinish();
        flash.success("入库成功!");
        index(new InboundPost());
    }

    public static void refreshFbaCartonContentsByIds(String id) {
        InboundUnit unit = InboundUnit.findById(Long.parseLong(id));

        render("/Inbounds/boxInfo.html", unit);
    }

    public static void updateBoxInfo(InboundUnit unit) {
        unit = InboundUnit.findById(unit.id);
        unit.marshalBoxs();
        unit.save();
        renderJSON(new Ret(true));
    }

    public static void printQuaternionForm(List<String> ids) {
        final PDF.Options options = new PDF.Options();
        options.filename = "TEST001.pdf";
        options.pageSize = IHtmlToPdfTransformer.A4L;
        List<Inbound> inbounds = Inbound.find("id IN " + SqlSelect.inlineParam(ids)).fetch();
        Map<Integer, List<InboundUnit>> ten = InboundUnit.pageNumForTen(inbounds);
        int page = ten.keySet().size();
        renderPDF(options, ten, page);
    }

}