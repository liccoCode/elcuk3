package controllers;

import exception.PaymentException;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.finance.FeeType;
import models.market.Selling;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.product.Whouse;
import org.apache.commons.lang.StringUtils;
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
        List<Whouse> whouses = Whouse.findByMarket(unit.selling.market);
        if(unit.selling == null) {
            flash.error("请通过 SellingId 进行, 没有执行合法的 SellingId 无法创建 ProcureUnit!");
            Analyzes.index();
        }
        render(unit, whouses);
    }

    public static void create(ProcureUnit unit, String shipmentId) {
        unit.handler = User.findByUserName(Secure.Security.connected());
        unit.validate();
        if(StringUtils.isBlank(shipmentId))
            Validation.addError("", "必须选择运输单");

        if(Validation.hasErrors()) {
            List<Whouse> whouses = Whouse.findByMarket(unit.selling.market);
            render("ProcureUnits/blank.html", unit, whouses);
        }

        Shipment ship = Shipment.findById(shipmentId);

        unit.save();
        ship.addToShip(unit);

        if(Validation.hasErrors()) {
            List<Whouse> whouses = Whouse.findByMarket(unit.selling.market);
            unit.remove();
            render("ProcureUnits/blank.html", unit, whouses);
        }

        flash.success("创建成功, 并且采购计划同时被指派到运输单 %s", shipmentId);
        new ElcukRecord(Messages.get("procureunit.save"),
                Messages.get("action.base", unit.to_log()), unit.id + "").save();

        Shipments.show(shipmentId);
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
