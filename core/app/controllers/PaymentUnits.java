package controllers;

import controllers.api.SystemOperation;
import helper.J;
import helper.Webs;
import models.ElcukRecord;
import models.finance.FeeType;
import models.finance.Payment;
import models.finance.PaymentUnit;
import models.procure.Cooperator;
import models.procure.ShipItem;
import models.procure.Shipment;
import models.view.Ret;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.jobs.Job;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/28/13
 * Time: 10:43 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class PaymentUnits extends Controller {

    @Check("paymentunits.destroy")
    public static void destroy(Long id, String reason) {
        PaymentUnit payUnit = PaymentUnit.findById(id);
        payUnit.procureFeeRemove(reason);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("删除成功");
        Applys.procure(payUnit.deliveryment.apply.id);
    }

    @Check("paymentunits.destroy")
    public static void destroyMaterial(Long id, String reason) {
        PaymentUnit payUnit = PaymentUnit.findById(id);
        payUnit.materialFeeRemove(reason);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("删除成功");
        }
        if(payUnit.materialPlan != null) {
            Applys.material(payUnit.materialPlan.apply.id);
        } else {
            Applys.material(payUnit.materialPurchase.applyPurchase.id);
        }
    }

    @Check("paymentunits.destroy")
    public static void destroyByShipment(Long id, String reason) {
        PaymentUnit payUnit = PaymentUnit.findById(id);
        payUnit.transportFeeRemove(reason);
        if(Validation.hasErrors()) {
            renderJSON(Webs.vJson(Validation.errors()));
        } else {
            renderJSON(new Ret(true, "#" + id + " 请款项删除成功"));
        }
    }

    @Check("paymentunits.fixvalue")
    public static void fixValue(Long id, Float fixValue, String reason) {
        PaymentUnit paymentUnit = PaymentUnit.findById(id);
        Validation.required("fixValue", fixValue);
        if(Validation.hasErrors())
            renderJSON(new Ret(false, Validation.errors().toString()));

        paymentUnit.fixValue(fixValue, reason);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("修正值更新成功.");
        }
        // NOTE: 如果 fixValue 的 routes 文件改变, 这里也需要改变
        Applys.procure(paymentUnit.procureUnit.deliveryment.apply.id);
    }

    @Check("paymentunits.fixvalue")
    public static void fixValueMaterial(Long id, Float fixValue, String reason) {
        PaymentUnit paymentUnit = PaymentUnit.findById(id);
        Validation.required("fixValue", fixValue);
        if(Validation.hasErrors()) {
            renderJSON(new Ret(false, Validation.errors().toString()));
        }

        paymentUnit.fixValue(fixValue, reason);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("修正值更新成功.");
        }
        // NOTE: 如果 fixValue 的 routes 文件改变, 这里也需要改变
        if(paymentUnit.materialPlan != null) {
            Applys.material(paymentUnit.materialPlan.apply.id);
        } else {
            Applys.material(paymentUnit.materialPurchase.applyPurchase.id);
        }
    }

    @Check("paymentunits.deny")
    public static void deny(Long id, String reason) {
        PaymentUnit paymentUnit = PaymentUnit.findById(id);
        paymentUnit.deny(reason);
        if(request.isAjax()) {
            if(Validation.hasErrors())
                renderJSON(Webs.vJson(Validation.errors()));
            else
                renderJSON(new Ret(true, "成功驳回"));
        } else {
            if(Validation.hasErrors())
                Webs.errorToFlash(flash);
            else
                flash.success("成功驳回");
            Payments.show(paymentUnit.payment.id, null);
        }
    }

    public static void show(Long id) {
        PaymentUnit fee = PaymentUnit.findById(id);
        render(fee);
    }

    @Check("paymentunits.fixunitvalue")
    public static void update(Long id, PaymentUnit fee) {
        PaymentUnit feeUnit = PaymentUnit.findById(id);
        feeUnit.fixUnitValue(fee);
        if(Validation.hasErrors()) {
            renderJSON(new Ret(Webs.vJson(Validation.errors())));
        }
        renderArgs.put("fee", feeUnit);
        render("PaymentUnits/show.json");
    }

    public static void records(final Long id) {
        List<ElcukRecord> records = await(new Job<List<ElcukRecord>>() {
            @Override
            public List<ElcukRecord> doJobWithResult() {
                PaymentUnit feeUnit = PaymentUnit.findById(id);
                return feeUnit.updateRecords();
            }
        }.now());
        renderJSON(J.json(records));
    }

    @Check("paymentunits.approve")
    public static void approveFromDeliveryment(Long id, List<Long> paymentUnitIds) {
        checkAuthenticity();
        Payment payment = Payment.findById(id);
        if(paymentUnitIds == null || paymentUnitIds.size() <= 0)
            Validation.addError("", "请选择需要批准的请款");
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            Payments.show(id, null);
        }
        payment.unitsApproval(paymentUnitIds);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("批复成功");
        Payments.show(id, null);
    }

    /**
     * 物流批准请款功能
     *
     * @param id
     */
    public static void approvePaymentFromShipment(Long id, List<Long> paymentUnitIds) {
        if(paymentUnitIds == null || paymentUnitIds.size() <= 0)
            Validation.addError("", "请选择需要批准的请款");
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            Payments.show(id, null);
        }
        List<PaymentUnit> units = PaymentUnit.find("id IN " + SqlSelect.inlineParam(paymentUnitIds)).fetch();
        units.forEach(unit -> unit.transportApprove());
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("批复成功");
        Payments.show(id, null);
    }

    /**
     * 批准运输单请款
     *
     * @param id
     */
    @Check("paymentunits.approve")
    public static void approveFromShipment(Long id) {
        PaymentUnit fee = PaymentUnit.findById(id);
        fee.transportApprove();
        if(Validation.hasErrors())
            renderJSON(new Ret(false, Webs.vJson(Validation.errors())));
        render("PaymentUnits/show.json", fee);
    }

    /**
     * 申请运输单请款
     *
     * @param id
     */
    @Check("paymentunits.approve")
    public static void applyFromShipment(Long id) {
        PaymentUnit fee = PaymentUnit.findById(id);
        fee.transportApply();
        if(Validation.hasErrors())
            renderJSON(new Ret(false, Webs.vJson(Validation.errors())));
        render("PaymentUnits/show.json", fee);
    }

    public static void batchApplyFromShipment(Long[] pids) {
        List<PaymentUnit> units = PaymentUnit.find("id IN " + SqlSelect.inlineParam(pids)).fetch();
        units.forEach(PaymentUnit::transportApply);
        if(Validation.hasErrors())
            renderJSON(new Ret(false, Webs.vJson(Validation.errors())));
        renderJSON(new Ret(true, "批量请款运输单请款成功"));
    }

    /**
     * 批量批准运输单请款
     *
     * @param pids
     */
    public static void batchApproveFromShipment(Long[] pids) {
        List<PaymentUnit> units = PaymentUnit.find("id IN " + SqlSelect.inlineParam(pids)).fetch();
        units.forEach(PaymentUnit::transportApprove);
        if(Validation.hasErrors())
            renderJSON(new Ret(false, Webs.vJson(Validation.errors())));
        renderJSON(new Ret(true, "批量批准运输单请款成功"));
    }

    /**
     * 从运输项目创建请款项目资源
     *
     * @param id
     * @param fee
     */
    @Check("paymentunits.postfromtransport")
    public static void fromShipItem(Long id, PaymentUnit fee) {
        ShipItem itm = ShipItem.findById(id);
        itm.produceFee(fee, FeeType.expressFee());
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("运输项目 #%s 费用添加成功.", itm.id);
        Shipments.show(itm.shipment.id);
    }

    /**
     * 从运输单创建请款项目资源
     *
     * @param id
     * @param fee
     */
    @Check("paymentunits.postfromtransport")
    public static void fromShipment(String id, PaymentUnit fee) {
        Shipment ship = Shipment.findById(id);
        fee.cooperator = Cooperator.findById(fee.cooperator.id);
        ship.produceFee(fee);
        if(Validation.hasErrors())
            renderJSON(new Ret(Webs.vJson(Validation.errors())));
        render("PaymentUnits/show.json", fee);
    }

    /**
     * 为当前运输单的所有项目申请预付关税
     *
     * @param id
     */
    @Check("paymentunits.postfromtransport")
    public static void applyDutyFromShipment(String id) {
        Shipment ship = Shipment.findById(id);
        ship.applyShipItemDuty();
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("为运输单 %s 成功申请预付关税", id);
        Shipments.show(id);
    }

    /**
     * 为运输单计算关税, 创建请款项资源
     *
     * @param id
     * @param fee
     */
    @Check("paymentunits.postfromtransport")
    public static void calShipmentLeftDuty(String id, PaymentUnit fee) {
        Shipment ship = Shipment.findById(id);
        fee = ship.calculateDuty(fee.currency, fee.unitQty * fee.unitPrice);
        if(Validation.hasErrors())
            renderJSON(new Ret(Webs.vJson(Validation.errors())));
        render("PaymentUnits/show.json", fee);
    }
}
