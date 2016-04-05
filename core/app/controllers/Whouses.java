package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import models.User;
import models.market.Account;
import models.procure.Cooperator;
import models.procure.FBACenter;
import models.view.Ret;
import models.view.post.InboundRecordPost;
import models.view.post.OutboundRecordPost;
import models.view.post.StockRecordPost;
import models.view.post.WhousePost;
import models.whouse.InboundRecord;
import models.whouse.OutboundRecord;
import models.whouse.StockRecord;
import models.whouse.Whouse;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/26/12
 * Time: 11:34 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Whouses extends Controller {

    @Before(only = {"index", "blank", "create", "edit", "update"})
    public static void setUpAccs() {
        renderArgs.put("accs", Account.openedSaleAcc());
        renderArgs.put("fbaCenters", FBACenter.all().<FBACenter>fetch());
        renderArgs.put("cooperators", Cooperator.find("type = ?", Cooperator.T.SHIPPER).fetch());
    }

    @Before(only = {"forwards", "updates"})
    public static void setUpSelectData() {
        List<Whouse> cooperators = Cooperator.find("type = ?", Cooperator.T.SHIPPER).fetch();
        List<User> users = User.find("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE 1=1 AND r.roleName " +
                "like ?", "%质检%").fetch();

        renderArgs.put("cooperators", cooperators);
        renderArgs.put("users", users);
    }

    @Check("whouses.index")
    public static void index() {
        List<Whouse> whs = Whouse.all().fetch();
        render(whs);
    }

    @Check("whouses.forwards")
    public static void forwards(WhousePost p) {
        if(p == null) p = new WhousePost(Whouse.T.FORWARD);

        List<Whouse> whs = p.query();
        render(p, whs);
    }

    public static void blank() {
        Whouse wh = new Whouse();
        render(wh);
    }

    public static void create(Whouse wh) {
        validation.valid(wh);
        wh.validate();
        if(Validation.hasErrors()) render("Whouses/blank.html", wh);
        wh.save();
        flash.success("创建成功");
        redirect("/Whouses/index");
    }

    public static void update(Whouse wh) {
        validation.valid(wh);
        wh.validate();
        if(Validation.hasErrors()) {
            renderJSON(new Ret(Webs.V(Validation.errors())));
        }


        wh.save();
        renderJSON(new Ret());
    }

    public static void updates(List<Whouse> whs) {
        for(Whouse wh : whs) {
            Whouse manage = Whouse.findById(wh.id);
            if(wh.user != null && wh.user.id != null) {
                manage.user = wh.user;
                manage.save();
            }
        }
        flash.success("更新成功");
        redirect("/Whouses/forwards");
    }

    public static void edit(long id) {
        Whouse wh = Whouse.findById(id);
        render(wh);
    }

    /**
     * 入库记录
     */
    public static void inboundRecords(InboundRecordPost p) {
        if(p == null) p = new InboundRecordPost();
        List<InboundRecord> records = p.query();
        render(p, records);
    }

    /**
     * 出库记录
     */
    public static void outboundRecords(OutboundRecordPost p) {
        if(p == null) p = new OutboundRecordPost();
        List<OutboundRecord> records = p.query();
        render(p, records);
    }

    /**
     * 库存异动
     */
    public static void stockRecords(StockRecordPost p) {
        if(p == null) p = new StockRecordPost();
        List<StockRecord> records = p.query();
        render(p, records);
    }

    /**
     * 确认入库
     */
    public static void confirmInbound(List<Long> rids) {

    }

    /**
     * 确认出库
     */
    public static void confirmOutbound(List<Long> rids) {

    }
}
