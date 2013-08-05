package controllers;

import helper.J;
import helper.Webs;
import models.ElcukRecord;
import models.finance.FeeType;
import models.finance.PaymentUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import models.view.Ret;
import play.data.validation.Validation;
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
@With({GlobalExceptionHandler.class, Secure.class})
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
    public static void destroyByShipment(Long id, String reason) {
        PaymentUnit payUnit = PaymentUnit.findById(id);
        payUnit.transportFeeRemove(reason);
        if(Validation.hasErrors())
            renderJSON(Webs.VJson(Validation.errors()));
        else
            renderJSON(new Ret(true, "#" + id + " 请款项删除成功"));
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
        redirect("/apply/" + paymentUnit.procureUnit.deliveryment.apply.id + "/procure#" + id);
    }

    @Check("paymentunits.deny")
    public static void deny(Long id, String reason) {
        PaymentUnit paymentUnit = PaymentUnit.findById(id);
        paymentUnit.deny(reason);
        if(request.isAjax()) {
            if(Validation.hasErrors())
                renderJSON(Webs.VJson(Validation.errors()));
            else
                renderJSON(new Ret(true, "成功驳回"));
        } else {
            if(Validation.hasErrors())
                Webs.errorToFlash(flash);
            else
                flash.success("成功驳回");
            Payments.show(paymentUnit.payment.id);
        }
    }

    public static void show(Long id) {
        PaymentUnit fee = PaymentUnit.findById(id);
        render(fee);
    }

    public static void update(Long id, PaymentUnit fee) {
        PaymentUnit feeUnit = PaymentUnit.findById(id);
        feeUnit.update(fee);
        if(Validation.hasErrors()) {
            renderJSON(new Ret(Webs.VJson(Validation.errors())));
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

    /**
     * 批准运输单请款
     *
     * @param id
     */
    public static void approve(Long id) {
        PaymentUnit fee = PaymentUnit.findById(id);
        fee.transportApprove();
        if(Validation.hasErrors())
            renderJSON(new Ret(false, Webs.VJson(Validation.errors())));
        render("PaymentUnits/show.json", fee);
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
        itm.produceFee(fee, FeeType.transportShipping());
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
        ship.produceFee(fee);
        if(Validation.hasErrors())
            renderJSON(new Ret(Webs.VJson(Validation.errors())));
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
            renderJSON(new Ret(Webs.VJson(Validation.errors())));
        render("PaymentUnits/show.json", fee);
    }
}
