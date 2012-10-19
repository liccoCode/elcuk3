package controllers;

import helper.J;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.procure.Cooperator;
import models.procure.FBAShipment;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.product.Whouse;
import models.view.Ret;
import models.view.post.ShipmentPost;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;

import java.util.List;

/**
 * 货运单的控制器
 * User: wyattpan
 * Date: 6/20/12
 * Time: 3:09 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Shipments extends Controller {
    @Before(only = {"index", "blank", "save"})
    public static void whouses() {
        List<Whouse> whouses = Whouse.findAll();
        renderArgs.put("whouses", whouses);
    }

    public static void index(ShipmentPost p) {
        List<Shipment> shipments = null;
        if(p == null) {
            p = new ShipmentPost();
            shipments = Shipment.find("state=? ORDER By createDate DESC", Shipment.S.PLAN).fetch();
        } else {
            shipments = p.query();
        }
        renderArgs.put("dateTypes", ShipmentPost.DATE_TYPES);
        render(shipments, p);
    }

    public static void blank() {
        Shipment ship = new Shipment(Shipment.id());
        render(ship);
    }

    public static void save(Shipment ship) {
        checkAuthenticity();
        ship.creater = User.findByUserName(Secure.Security.connected());
        validation.valid(ship);
        Validation.required("shipment.whouse", ship.whouse);
        Validation.required("shipment.creater", ship.creater);
        if(Validation.hasErrors()) {
            render("Shipments/blank.html", ship);
        }
        ship.save();
        redirect("/shipments/show/" + ship.id);
    }


    @Before(only = {"show", "update", "beginShip", "refreshProcuress", "updateFba", "ensureDone"})
    public static void setUpShowPage() {
        renderArgs.put("whouses", Whouse.findAll());
        renderArgs.put("shippers", Cooperator.shipper());
        renderArgs.put("fbas", J.json(FBAShipment.uncloseFBAShipmentIds()));
        String shipmentId = request.params.get("id");
        if(StringUtils.isBlank(shipmentId)) shipmentId = request.params.get("ship.id");
        if(StringUtils.isNotBlank(shipmentId)) {
            renderArgs.put("records", ElcukRecord.records(shipmentId));
            renderArgs.put("sameFbaShips", Shipment.<Shipment>findById(shipmentId).sameFBAShipment());
        }
    }

    public static void show(String id) {
        Shipment ship = Shipment.findById(id);
        render(ship);
    }

    @Util
    public static void checkShowError(Shipment ship) {
        if(Validation.hasErrors()) {
            renderArgs.put("ship", ship);
            render("Shipments/show.html");
        }
    }

    public static void update(Shipment ship) {
        checkAuthenticity();
        validation.valid(ship);
        ship.validate();
        checkShowError(ship);
        ship.updateShipment();
        checkShowError(ship);
        new ElcukRecord(Messages.get("shipment.update"), Messages.get("shipment.update.msg", ship.to_log()), ship.id).save();
        flash.success("更新成功.");
        redirect("/Shipments/show/" + ship.id);
    }

    public static void comment(String id, String cmt) {
        validation.required(id);
        if(Validation.hasErrors()) renderJSON(new Ret(false, Webs.V(Validation.errors())));
        Shipment ship = Shipment.findById(id);
        ship.memo = cmt;
        ship.save();
        renderJSON(new Ret(true, Webs.V(Validation.errors())));
    }

    @Before(only = {"shipItem", "ship", "cancelShip"})
    public static void setUpShipPage() {
        String shipmentId = request.params.get("id");
        Shipment ship = Shipment.findById(shipmentId);
        try {
            List<ProcureUnit> units = ProcureUnit.waitToShip(ship.whouse.id, ship.type);
            renderArgs.put("units", units);
        } catch(Exception e) {
            Validation.addError("shipments.setUpShipPage", "%s");
        }
    }

    public static void shipItem(String id) {
        Shipment ship = Shipment.findById(id);
        render(ship);
    }

    /**
     * 取消运输单
     */
    @Check("root")
    public static void cancel(String id) {
        checkAuthenticity();
        Shipment ship = Shipment.findById(id);
        ship.cancel();
        if(Validation.hasErrors()) render("Shipments/show.html", ship);
        new ElcukRecord(Messages.get("shipment.cancel"), Messages.get("action.base", id), id).save();
        flash.success("运输单取消成功.");
        redirect("/Shipments/show/" + id);
    }

    public static void ship(String id, List<Long> unitId, List<Integer> shipQty) {
        Validation.required("shipments.ship.unitId", unitId);
        Validation.required("shipments.ship.shipQty", shipQty);
        Validation.required("shipment.id", id);
        Validation.equals("shipments.ship.equal", unitId.size(), "", shipQty.size());
        Shipment ship = Shipment.findById(id);
        if(Validation.hasError("shipment.id")) redirect("/shipments/index");
        if(Validation.hasErrors()) render("Shipments/shipItem.html", ship);

        ship.addToShip(unitId, shipQty);

        if(Validation.hasErrors()) render("Shipments/shipItem.html", ship);

        redirect("/shipments/shipitem/" + id);
    }

    /**
     * 取消运输单项
     *
     * @param shipItemId
     * @param id
     */
    public static void cancelShip(List<Integer> shipItemId, String id) {
        Validation.required("shipments.ship.shipId", shipItemId);
        Validation.required("shipment.id", id);
        if(Validation.hasError("shipment.id")) redirect("/shipments/index");
        Shipment ship = Shipment.findById(id);
        if(Validation.hasErrors()) render("Shipments/shipItem.html", ship);

        ship.cancelShip(shipItemId, true);

        if(Validation.hasErrors()) render("Shipments/shipItem.html", ship);
        redirect("/shipments/shipitem/" + id);
    }

    public static void beginShip(String id) {
        checkAuthenticity();
        Shipment ship = Shipment.findById(id);
        Validation.required("shipment.id", id);
        if(Validation.hasError("shipment.id")) redirect("/shipments/index");
        Validation.required("shipment.trackNo", ship.trackNo);
        Validation.required("shipment.planArrivDate", ship.planArrivDate);
        Validation.required("shipment.volumn", ship.volumn);
        Validation.required("shipment.weight", ship.weight);
        Validation.required("shipment.declaredValue", ship.declaredValue);
        Validation.required("shipment.deposit", ship.deposit);
        Validation.required("shipment.otherFee", ship.otherFee);
        Validation.required("shipment.shipFee", ship.shipFee);
        Validation.required("shipment.cooper", ship.cooper);
        Validation.min("shipment.items.size", ship.items.size(), 1);

        checkShowError(ship);

        ship.beginShip();

        checkShowError(ship);
        new ElcukRecord(Messages.get("shipment.beginShip"), Messages.get("shipment.beginShip.msg", ship.id), ship.id).save();
        flash.success("运输单已经标记运输, FBA[%s] 已经标记 SHIPPED.", ship.fbaShipment.shipmentId);

        redirect("/shipments/show/" + id);
    }

    /**
     * 用来为 Shipment 关联系统中已经存在的 FBAShipemnt
     * PS: 暂时取消使用
     *
     * @param id
     * @param shipmentId
     */
    public static void assignFbaShipmentId(String id, String shipmentId) {
        Shipment ship = Shipment.findById(id);
        ship.fbaShipment = FBAShipment.findByShipmentId(shipmentId);
        Validation.required("shipment.fbashipment", ship.fbaShipment);
        ship.save();
        if(Validation.hasErrors()) render("Shipments/show.html", ship);
        flash.success("绑定 FBA %s 成功.", shipmentId);
        redirect("/shipments/show/" + id);
    }

    /**
     * 对 Shipment 进行 Confirm, Confirm 以后不再允许添加运输项目
     *
     * @param id
     */
    public static void deployToAmazon(final String id) {
        checkAuthenticity();

        // 由于需要访问远程, 将线程空出 2 s 以便在这 2s 内其他线程可以处理
        //TODO 测试
        await("2s", new F.Action0() {
            @Override
            public void invoke() {
                Shipment ship = Shipment.findById(id);
                Validation.isTrue("shipment.fbashipment", ship.fbaShipment == null);
                if(Validation.hasErrors()) {
                    renderArgs.put("ship", ship);
                    render("Shipments/show.html", ship);
                }
                ship.confirmAndSyncTOAmazon();
                if(Validation.hasErrors()) {
                    renderArgs.put("ship", ship);
                    render("Shipments/show.html", ship);
                }
                new ElcukRecord(Messages.get("shipment.createFBA"), Messages.get("shipment.createFBA.msg", ship.id, ship.fbaShipment.shipmentId), ship.id).save();
                flash.success("Amazon FBA Shipment 创建成功");
                redirect("/shipments/show/" + ship.id);
            }
        });
    }


    public static void updateFba(final String id, final String act) {
        // action 参数是 play! 自己使用的保留字
        checkAuthenticity();
        await("2s", new F.Action0() {
            @Override
            public void invoke() {
                Validation.required("shipments.updateFba.action", act);
                Shipment ship = Shipment.findById(id);
                checkShowError(ship);
                if("update".equals(act)) {
                    ship.updateFbaShipment();
                } else {
                    //TODO 是否需要添加删除 Amazon FBA 还等待研究, 因为系统内的数据也需要处理
                    flash.error("需要执行的 Action 不正确.");
                }
                checkShowError(ship);
                if("update".equals(act)) {
                    flash.success("更新 Amazon FBA %s 成功.", ship.fbaShipment.shipmentId);
                    new ElcukRecord(Messages.get("shipment.updateFBA"),
                            Messages.get("action.base", String.format("FBA [%s] 更新了 %s 个 Items", ship.fbaShipment.shipmentId, ship.items.size())),
                            ship.id).save();
                }
                redirect("/shipments/show/" + id);
            }
        });
    }

    /**
     * 确认运输单已经到库
     *
     * @param id
     */
    public static void ensureDone(String id) {
        checkAuthenticity();
        Shipment ship = Shipment.findById(id);
        ship.ensureDone();
        checkShowError(ship);
        flash.success("成功确认, 运输单已经确认运输完毕.");
        redirect("/shipments/show/" + id);
    }

    public static void refreshProcuress(final String id) {
        checkAuthenticity();
        // 通过 play status 查看这个方法执行平均在 3s, 所以让其放开 3s 线程时间
        await("3s", new F.Action0() {
            @Override
            public void invoke() {
                Shipment ship = Shipment.findById(id);
                Validation.required("shipment.trackNo", ship.trackNo);
                Validation.required("shipment.internationExpress", ship.internationExpress);
                checkShowError(ship);
                ship.refreshIExpressHTML();
                redirect("/shipments/show/" + ship.id);
            }
        });
    }

    /**
     * 将 Shipment 进行分拆
     */
    public static void splitShipment(String id, List<String> shipItemId) {
        validation.required(shipItemId);
        Shipment ship = Shipment.findById(id);
        checkShowError(ship);
        Shipment newShipment = ship.splitShipment(shipItemId);
        checkShowError(ship);
        flash.success("成功分拆运输单, 创建了新运输单 %s", newShipment.id);
        redirect("/shipments/show/" + id);
    }

    /**
     * 加载出来 Whouse 相关的可使用的运输单
     *
     * @param whouseId
     */
    public static void unitShipments(Long whouseId) {
        List<Shipment> unitRelateShipments = Shipment.findUnitRelateShipmentByWhouse(whouseId);
        render(unitRelateShipments);
    }
}
