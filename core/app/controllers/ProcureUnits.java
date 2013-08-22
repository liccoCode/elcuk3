package controllers;

import exception.PaymentException;
import helper.Dates;
import helper.Webs;
import models.ElcukRecord;
import models.Notification;
import models.User;
import models.embedded.UnitAttrs;
import models.finance.FeeType;
import models.market.Selling;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.product.Whouse;
import models.view.post.ProcurePost;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 3/26/13
 * Time: 5:56 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class ProcureUnits extends Controller {

    @Before(only = {"index"})
    public static void beforeIndex() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        renderArgs.put("whouses", Whouse.<Whouse>findAll());
        renderArgs.put("logs", ElcukRecord.fid("procures.remove").<ElcukRecord>fetch(50));
        renderArgs.put("cooperators", cooperators);

        //为视图提供日期
        DateTime dateTime = new DateTime();
        renderArgs.put("tomorrow1", dateTime.plusDays(1).toString("yyyy-MM-dd"));
        renderArgs.put("tomorrow2", dateTime.plusDays(2).toString("yyyy-MM-dd"));
        renderArgs.put("tomorrow3", dateTime.plusDays(3).toString("yyyy-MM-dd"));
    }

    @Check("procures.index")
    public static void index(ProcurePost p) {
        if(p == null) p = new ProcurePost();
        render(p);
    }

    /**
     * 明天 后天 大后天 计划视图
     */
    public static void planView(Date date) {
        ProcurePost p = new ProcurePost(ProcureUnit.STAGE.DELIVERY);
        p.dateType = "attrs.planDeliveryDate";
        p.from = date;
        p.to = date;

        ProcureUnits.index(p);
    }

    /**
     * 发货时间为当天, 同时货物还没有抵达货代的采购计划
     */
    public static void noPlaced() {
        ProcurePost p = new ProcurePost();
        p.dateType = "attrs.planArrivDate";
        p.from = new Date();
        p.to = new Date();
        p.isPlaced = ProcurePost.PLACEDSTATE.NOARRIVE;

        ProcureUnits.index(p);
    }


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

    /**
     * 某一个 ProcureUnit 交货
     */
    public static void deliveryUnit(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        renderArgs.put("attrs", unit.attrs);
        render(unit);
    }

    @Check("procures.delivery")
    public static void reverDelivery(long id, String msg) {
        ProcureUnit unit = ProcureUnit.findById(id);
        unit.revertDelivery(msg);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("撤销成功");
        }
        deliveryUnit(id);
    }

    /**
     * 交货更新
     *
     * @param attrs
     */
    @Check("procures.delivery")
    public static void delivery(UnitAttrs attrs, long id, String cmt) {
        attrs.validate();
        ProcureUnit unit = ProcureUnit.findById(id);
        if(Validation.hasErrors()) {
            render("../views/ProcureUnits/deliveryUnit.html", unit, attrs);
        }
        unit.comment = cmt;
        try {
            Boolean isFullDelivery = unit.delivery(attrs);
            if(isFullDelivery) {
                flash.success("ProcureUnit %s 全部交货!", unit.id);
            } else {
                flash.success("ProcureUnits %s 超额交货, 预计交货 %s, 实际交货 %s",
                        unit.id, unit.attrs.planQty, unit.attrs.qty);
            }
        } catch(Exception e) {
            Validation.addError("", Webs.E(e));
            render("../views/ProcureUnits/deliveryUnit.html", unit, attrs);
        }

        Deliveryments.show(unit.deliveryment.id);
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
     * TODO effect: 需要调整的采购计划的修改
     *
     * @param id
     * @param oldPlanQty
     */
    public static void update(Long id, Integer oldPlanQty, ProcureUnit unit, String shipmentId) {
        List<Whouse> whouses = Whouse.findByAccount(unit.selling.account);
        ProcureUnit managedUnit = ProcureUnit.findById(id);


        managedUnit.update(unit, shipmentId);
        if(Validation.hasErrors()) {
            unit.id = managedUnit.id;
            render("ProcureUnits/edit.html", unit, oldPlanQty, whouses);
        }

        new ElcukRecord(Messages.get("procureunit.update"),
                Messages.get("action.base", managedUnit.to_log()), managedUnit.id + "").save();
        //TODO effects: Notification 调整
        if(!oldPlanQty.equals(managedUnit.attrs.planQty)) {
            Notification.notifies(String.format("采购计划 #%s(%s) 变更", managedUnit.id, managedUnit.sku),
                    String.format("计划采购量从 %s 变更为 %s, 预计交货日期: %s, 请检查相关采购单",
                            oldPlanQty, managedUnit.attrs.planQty,
                            Dates.date2Date(managedUnit.attrs.planDeliveryDate)),
                    Notification.PROCURE, Notification.SHIPPER);
        }
        flash.success("成功修改采购计划!", id);
        edit(id);
    }

    public static void destroy(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        unit.remove();
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            ProcurePost p = new ProcurePost();
            p.search = "id:" + id;
            index(p);
        }
        flash.success("删除成功, 所关联的运输项目也成功删除.");
        index(null);
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
