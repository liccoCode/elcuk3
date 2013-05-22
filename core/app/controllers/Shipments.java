package controllers;

import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.procure.Cooperator;
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
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * 货运单的控制器
 * User: wyattpan
 * Date: 6/20/12
 * Time: 3:09 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Shipments extends Controller {
    @Before(only = {"index", "blank", "save"})
    public static void whouses() {
        List<Whouse> whouses = Whouse.findAll();
        List<Cooperator> cooperators = Cooperator.shippers();
        renderArgs.put("whouses", whouses);
        renderArgs.put("cooperators", cooperators);
    }

    @Check("shipments.index")
    public static void index(ShipmentPost p) {
        List<Shipment> shipments = null;
        if(p == null)
            p = new ShipmentPost();
        shipments = p.query();
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
        Shipment shipment = new Shipment(ship).save();
        show(shipment.id);
    }

    /**
     * 通过采购计划创建运输单
     *
     * @param units
     */
    public static void procureUnitToShipment(List<Long> units) {
        if(units == null || units.size() <= 0)
            Validation.addError("", "必须选择采购计划");
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            ShipItems.index(null);
        }

        Shipment shipment = new Shipment().buildFromProcureUnits(units);

        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            ShipItems.index(null);
        }
        flash.success("成功为 %s 个采购计划创建运输单 %s", units.size(), shipment.id);
        show(shipment.id);
    }

    @Before(only = {"show", "update", "beginShip", "refreshProcuress", "updateFba"})
    public static void setUpShowPage() {
        renderArgs.put("whouses", Whouse.findAll());
        renderArgs.put("shippers", Cooperator.shippers());
        String shipmentId = request.params.get("id");
        if(StringUtils.isBlank(shipmentId)) shipmentId = request.params.get("ship.id");
        if(StringUtils.isNotBlank(shipmentId)) {
            //TODO effect 这里需要将所有的 records 修改为执行 action 的
            renderArgs.put("records", ElcukRecord.records(shipmentId));
        }
    }

    public static void show(String id) {
        Shipment ship = Shipment.findById(id);
        ship.autoCheckDone();
        render(ship);
    }

    public static void preview(String id) {
        Shipment ship = Shipment.findById(id);
        if(ship == null) {
            Validation.addError("", "ShipmentId 错误");
            Webs.errorToFlash(flash);
        }

        render(ship);
    }

    public static void confirm(String id, Boolean undo) {
        if(undo == null) {
            Validation.addError("", "缺少 undo 参数.");
            show(id);
        }

        Shipment shipment = Shipment.findById(id);
        shipment.state = undo ? Shipment.S.PLAN : Shipment.S.CONFIRM;
        shipment.save();
        show(id);
    }

    //TODO effect: 需要调整
    public static void update(Shipment ship) {
        checkAuthenticity();
        validation.valid(ship);
        ship.validate();
        if(Validation.hasErrors()) {
            renderArgs.put("ship", ship);
            render("Shipments/show.html");
        }
        ship.updateShipment();
        if(Validation.hasErrors()) {
            renderArgs.put("ship", ship);
            render("Shipments/show.html");
        }
        new ElcukRecord(Messages.get("shipment.update"),
                Messages.get("shipment.update.msg", ship.to_log()), ship.id).save();
        flash.success("更新成功.");
        show(ship.id);
    }

    /**
     * 用来更新 Shipment 的 coment 与 trackNo
     */
    public static void comment(String id, String cmt, String track) {
        validation.required(id);
        if(Validation.hasErrors()) renderJSON(new Ret(false, Webs.V(Validation.errors())));
        Shipment ship = Shipment.findById(id);
        ship.memo = cmt;
        if(StringUtils.isNotBlank(track))
            ship.trackNo = track;
        ship.save();
        renderJSON(new Ret(true, Webs.V(Validation.errors())));
    }

    /**
     * 取消运输单
     */
    @Check("shipments.cancel")
    public static void cancel(String id) {
        Shipment ship = Shipment.findById(id);
        ship.destroy();
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            show(id);
        } else {
            flash.success("运输单取消成功.");
            index(null);
        }
    }


    @Check("shipments.beginship")
    public static void beginShip(String id, Date date) {
        Shipment ship = Shipment.findById(id);
        Validation.required("shipment.planArrivDate", ship.dates.planArrivDate);

        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            show(id);
        }

        try {
            ship.beginShip(date);
        } catch(Exception e) {
            Validation.addError("", Webs.E(e));
        }
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            show(id);
        }

        new ElcukRecord(Messages.get("shipment.beginShip"),
                Messages.get("shipment.beginShip.msg", ship.id), ship.id).save();
        flash.success("运输单已经标记运输, FBA 已经标记 SHIPPED.");

        show(id);
    }

    /**
     * 到港
     */
    public static void landPort(String id, Date date) {
        Shipment shipment = Shipment.findById(id);
        shipment.landPort(date);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 到港!", shipment.type.label(), id);
        show(id);
    }

    /**
     * 港口提货
     *
     * @param id
     * @param date
     */
    public static void pickGoods(String id, Date date) {
        Shipment shipment = Shipment.findById(id);
        shipment.pickGoods(date);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 开始提货!", shipment.type.label(), id);
        show(id);
    }

    /**
     * 预约
     *
     * @param id
     */
    public static void booking(String id, Date date) {
        Shipment shipment = Shipment.findById(id);
        shipment.booking(date);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 已经预约!", shipment.type.label(), id);
        show(id);
    }

    /**
     * 派送
     */
    public static void deliverying(String id, Date date) {
        Shipment shipment = Shipment.findById(id);
        shipment.beginDeliver(date);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 已经派送!", shipment.type.label(), id);
        show(id);
    }

    /**
     * 签收
     *
     * @param id
     * @param date
     */
    public static void receipt(String id, Date date) {
        Shipment shipment = Shipment.findById(id);
        shipment.receipt(date);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 已经签收!", shipment.type.label(), id);
        show(id);
    }

    /**
     * 入库
     */
    public static void inbounding(String id, Date date) {
        Shipment shipment = Shipment.findById(id);
        shipment.inbounding(date);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 正在入库!", shipment.type.label(), id);
        show(id);
    }

    public static void revertState(String id) {
        Shipment shipment = Shipment.findById(id);
        shipment.revertState();
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 成功返回到上一状态!", shipment.type.label(), id);
        show(id);
    }

    public static void log(String id, String msg) {
        Shipment shipment = Shipment.findById(id);
        shipment.logEvent(msg);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 成功记录一条事件!", shipment.type.label(), id);
        show(id);
    }

    public static void endShip(String id, Date date) {
        Shipment shipment = Shipment.findById(id);
        shipment.endShip(date);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 正在入库!", shipment.type.label(), id);
        show(id);
    }


    public static void refreshProcuress(final String id) {
        Shipment ship = Shipment.findById(id);
        Validation.required("shipment.trackNo", ship.trackNo);
        Validation.required("shipment.internationExpress", ship.internationExpress);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            show(ship.id);
        }
        // 通过 play status 查看这个方法执行平均在 3s, 所以让其放开 3s 线程时间
        await("3s", new F.Action0() {
            @Override
            public void invoke() {
                // 由于使用 await 后, 就与原来不是同一个线程, 所以无法使用 Validate
                Shipment ship = Shipment.findById(id);
                ship.trackWebSite();
                ship.monitor();
                show(ship.id);
            }
        });
    }

    /**
     * 加载出来 Whouse 相关的可使用的运输单
     *
     * @param whouseId
     */
    public static void unitShipments(Long whouseId, Shipment.T shipType) {
        List<Shipment> unitRelateShipments = Shipment
                .findUnitRelateShipmentByWhouse(whouseId, shipType);
        render(unitRelateShipments);
    }
}
