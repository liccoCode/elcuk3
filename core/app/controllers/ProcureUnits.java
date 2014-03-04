package controllers;

import exception.PaymentException;
import helper.Constant;
import helper.Dates;
import helper.J;
import helper.Webs;
import models.ElcukRecord;
import models.Notification;
import models.User;
import models.embedded.UnitAttrs;
import models.finance.FeeType;
import models.market.Selling;
import models.procure.Cooperator;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.product.Product;
import models.product.Whouse;
import models.view.post.ProcurePost;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.libs.F;
import play.libs.Files;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
     * 将搜索结果 打成ZIP包，进行下载
     */
    public static synchronized void downloadFBAZIP(ProcurePost p) throws Exception {
        List<ProcureUnit> procureUnitsList = p.query();
        if(procureUnitsList != null && procureUnitsList.size() != 0) {
            //创建FBA根目录，存放工厂FBA文件
            File dirfile = new File(Constant.TMP, "FBA");
            try {
                Files.delete(dirfile);
                dirfile.mkdir();
                for(ProcureUnit procureUnit : procureUnitsList) {
                    if(!StringUtils.isBlank(p.unitIds)) {
                        if(!StringUtils.contains(p.unitIds, procureUnit.id.toString())) {
                            continue;
                        }
                    }
                    String name = procureUnit.cooperator.name;
                    String date = Dates.date2Date(procureUnit.attrs.planDeliveryDate);

                    //生成工厂的文件夹. 格式：预计交货日期-工厂名称
                    File factoryDir = new File(dirfile, String.format("%s-%s-出货FBA", date, name));
                    factoryDir.mkdir();
                    //生成 PDF
                    procureUnit.fbaAsPDF(factoryDir);
                }
            } finally {
                File zip = new File(Constant.TMP + "/FBA.zip");
                Files.zip(dirfile, zip);
                zip.deleteOnExit();
                renderBinary(zip);
            }
        } else {
            renderText("没有数据无法生成zip文件！");
        }
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
        flash.success("成功修改采购计划!", id);
        edit(id);
    }

    public static void destroy(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        Set<User> users = unit.editToUsers();
        unit.remove();
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            ProcurePost p = new ProcurePost();
            p.search = "id:" + id;
            index(p);
        }
        //通知当前操作用户 和采购计划创建人，发送删除成功的通知
        String notifiMessage = String.format("采购计划 %s 删除", id);
        Notification.newSystemNoty(notifiMessage, Notification.INDEX, users.iterator().next())
                .notifySomeone(users.toArray(new User[users.size()]));
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
        F.T2<List<Selling>, List<String>> sellingAndSellingIds = Selling.sameFamilySellings(unit.sku);
        F.T2<List<String>, List<String>> skusToJson = Product.fetchSkusJson();
        renderArgs.put("skus", J.json(skusToJson._2));
        renderArgs.put("sids", J.json(sellingAndSellingIds._2));
        renderArgs.put("whouses", Whouse.findAll());
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
        if(Validation.hasErrors()) {
            List<Whouse> whouses = Whouse.findAll();
            render("ProcureUnits/splitUnit.html", unit, newUnit, whouses);
        }

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

    public static void manualProcureUnit(String id) {
        ProcureUnit unit = new ProcureUnit();
        unit.deliveryment = Deliveryment.findById(id);
        F.T2<List<String>, List<String>> skusToJson = Product.fetchSkusJson();
        renderArgs.put("skus", J.json(skusToJson._2));
        render(unit);
    }

    public static void editManual(Long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        render("ProcureUnits/manualProcureUnit.html", unit);
    }

    /**
     * 修改手动单采购计划
     */
    public static void updateManual(Long id, ProcureUnit unit) {
        ProcureUnit managedUnit = ProcureUnit.findById(id);
        managedUnit.updateManualData(unit);
        managedUnit.validateManual();
        if(Validation.hasErrors()) {
            render("ProcureUnits/manualProcureUnit.html", unit);
        }
        managedUnit.save();
        flash.success("成功修改采购计划!", id);
        renderArgs.put("unit", managedUnit);
        render("ProcureUnits/manualProcureUnit.html");
    }
}
