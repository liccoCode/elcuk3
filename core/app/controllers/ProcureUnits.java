package controllers;

import exception.PaymentException;
import helper.Dates;
import helper.Webs;
import models.ElcukRecord;
import models.Notification;
import models.User;
import models.finance.FeeType;
import models.market.Selling;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.product.Whouse;
import models.view.post.ProcurePost;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 3/26/13
 * Time: 5:56 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class ProcureUnits extends Controller {

    public static void blank(String sid) {
        ProcureUnit unit = new ProcureUnit();
        unit.selling = Selling.findById(sid);
        List<Whouse> whouses = Whouse.findByAccount(unit.selling.account);
        if(unit.selling == null) {
            flash.error("请通过 SellingId 进行, 没有执行合法的 SellingId 无法创建 ProcureUnit!");
            Analyzes.index();
        }
        render(unit, whouses);
    }

    public static void create(ProcureUnit unit, String shipmentId) {
        unit.handler = User.findByUserName(Secure.Security.connected());
        unit.validate();
        if(unit.shipType == Shipment.T.EXPRESS && StringUtils.isNotBlank(shipmentId))
            Validation.addError("", "快递运输方式, 不需要指定运输单");

        if(Validation.hasErrors()) {
            List<Whouse> whouses = Whouse.findByAccount(unit.selling.account);
            render("ProcureUnits/blank.html", unit, whouses);
        }


        unit.save();

        if(unit.shipType != Shipment.T.EXPRESS) {
            Shipment ship = Shipment.findById(shipmentId);
            ship.addToShip(unit);
        }

        if(Validation.hasErrors()) {
            List<Whouse> whouses = Whouse.findByAccount(unit.selling.account);
            unit.remove();
            render("ProcureUnits/blank.html", unit, whouses);
        }

        flash.success("创建成功, 并且采购计划同时被指派到运输单 %s", shipmentId);
        new ElcukRecord(Messages.get("procureunit.save"),
                Messages.get("action.base", unit.to_log()), unit.id + "").save();

        Analyzes.index();
    }

    public static void edit(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        int oldPlanQty = unit.attrs.planQty;
        List<Whouse> whouses = Whouse.findByAccount(unit.selling.account);
        render(unit, oldPlanQty, whouses);
    }

    /**
     * TODO 需要调整的采购计划的修改
     *
     * @param id
     * @param oldPlanQty
     */
    public static void update(Long id, Integer oldPlanQty) {
        String planDeliveryDate = params.get("unit.attrs.planDeliveryDate");
        String planShipDate = params.get("unit.attrs.planShipDate");
        String planArrivDate = params.get("unit.attrs.planArrivDate");
        Integer planQty = NumberUtils.toInt(params.get("unit.attrs.planQty"), -1);
        Shipment.T shipType = Shipment.T.valueOf(params.get("unit.shipType"));

        if(oldPlanQty == null) Validation.addError("", "历史预计采购数量必须存在");
        if(StringUtils.isBlank(planDeliveryDate)) Validation.addError("", "预计交货时间必须存在");
        if(StringUtils.isBlank(planShipDate)) Validation.addError("", "预计发货时间必须存在");
        if(StringUtils.isBlank(planArrivDate)) Validation.addError("", "预计抵达时间必须存在");
        if(planQty <= 0) Validation.addError("", "预计采购数量输入错误");

        ProcureUnit unit = ProcureUnit.findById(id);
        List<Whouse> whouses = Whouse.findByAccount(unit.selling.account);
        if(Validation.hasErrors()) {
            render("ProcureUnits/edit.html", unit, oldPlanQty, whouses);
        }

        unit.updatePlanDates(
                Dates.cn(planDeliveryDate).toDate(),
                Dates.cn(planShipDate).toDate(),
                Dates.cn(planArrivDate).toDate()
        );
        if(Validation.hasErrors()) {
            render("ProcureUnits/edit.html", unit, oldPlanQty, whouses);
        }

        unit.attrs.planQty = planQty;
        unit.shipType = shipType;
        unit.save();

        if(Validation.hasErrors()) {
            render("ProcureUnits/edit.html", unit, oldPlanQty, whouses);
        }

        new ElcukRecord(Messages.get("procureunit.update"),
                Messages.get("action.base", unit.to_log()), unit.id + "").save();
        //TODO effects: Notification 调整
        if(!oldPlanQty.equals(unit.attrs.planQty)) {
            Notification.notifies(String.format("采购计划 #%s(%s) 变更", unit.id, unit.sku),
                    String.format("计划采购量从 %s 变更为 %s, 预计交货日期: %s, 请检查相关采购单",
                            oldPlanQty, unit.attrs.planQty,
                            Dates.date2Date(unit.attrs.planDeliveryDate)),
                    Notification.PROCURE, Notification.SHIPPER);
        }
        flash.success("ProcureUnit %s update success!", unit.id);
        redirect("/procures/index?p.search=id:" + unit.id);
    }

    public static void destroy(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        unit.remove();
        if(Validation.hasErrors()) {
            renderArgs.put("p", new ProcurePost());
            render("Procures/index");
        }
        flash.success("删除成功, 所关联的运输项目也成功删除.");
        Procures.index(null);
    }

    /**
     * 分拆采购计划页面
     *
     * @param id
     */
    public static void splitUnit(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        ProcureUnit newUnit = new ProcureUnit();
        newUnit.comment(String.format("此采购计划由于 #%s 采购计划分拆创建.", unit.id));
        newUnit.attrs.qty = 0;
        render(unit, newUnit);
    }

    /**
     * 分拆操作
     *
     * @param id
     * @param newUnit
     */
    @Check("procures.dosplitunit")
    public static void doSplitUnit(long id, ProcureUnit newUnit) {
        checkAuthenticity();
        ProcureUnit unit = ProcureUnit.findById(id);
        newUnit.handler = User.current();
        ProcureUnit nUnit = unit.split(newUnit);
        if(Validation.hasErrors())
            render("ProcureUnits/splitUnit.html", unit, newUnit);

        flash.success("采购计划 #%s 成功分拆出 #%s", id, nUnit.id);
        Deliveryments.show(unit.deliveryment.id);
    }

    /**
     * 抵达货代
     *
     * @param id
     */
    public static void markPlace(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        if(unit.cooperator == null || unit.shipType == null) {
            Validation.addError("", "[合作者] 或者 [运输方式] 需要填写完整.");
        } else {
            unit.isPlaced = true;
            unit.save();
        }
        render(unit);
    }


    /**
     * 预付款申请
     *
     * @param id
     */
    @Check("procureunits.billingprepay")
    public static void billingPrePay(Long id, Long applyId) {
        ProcureUnit unit = ProcureUnit.findById(id);
        try {
            unit.billingPrePay();
        } catch(PaymentException e) {
            Validation.addError("", e.getMessage());
        }
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 请款成功", FeeType.cashpledge().nickName);
        Applys.procure(applyId);
    }

    /**
     * 尾款申请
     *
     * @param id
     */
    @Check("procureunits.billingtailpay")
    public static void billingTailPay(Long id, Long applyId) {
        ProcureUnit unit = ProcureUnit.findById(id);
        try {
            unit.billingTailPay();
        } catch(PaymentException e) {
            Validation.addError("", e.getMessage());
        }
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 请款成功", FeeType.procurement().nickName);
        Applys.procure(applyId);
    }

}
