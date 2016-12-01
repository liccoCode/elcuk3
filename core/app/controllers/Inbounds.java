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
import org.apache.commons.lang.StringUtils;
import play.db.helper.JpqlSelect;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

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

    public static void createValidate(List<Long> pids) {
        String msg = ProcureUnit.validateIsInbound(pids);
        if(StringUtils.isNotBlank(msg)) {
            renderJSON(new Ret(false, msg));
        } else {
            renderJSON(new Ret(true));
        }
    }


    public static void blank(List<Long> pids) {
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        List<DeliverPlan> plans = DeliverPlan.find("state = ? ", DeliverPlan.P.CREATE).fetch();
        ProcureUnit proUnit = units.get(0);

        render(units, plans, proUnit);
    }

    public static void create(Inbound inbound, List<InboundUnit> dtos) {
        inbound.id = Inbound.id();
        inbound.receiver = Login.current();
        inbound.createDate = new Date();
        inbound.projectName = inbound.isb2b ? "B2B" : OperatorConfig.getVal("brandname");
        inbound.save();
        inbound.create(dtos);
        flash.success("创建成功!");
        index(new InboundPost());
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

}