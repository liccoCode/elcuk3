package controllers;

import controllers.api.SystemOperation;
import helper.GTs;
import helper.J;
import models.ElcukRecord;
import models.market.BtbOrder;
import models.procure.BtbCustom;
import models.procure.BtbCustomAddress;
import models.view.post.BtbCustomPost;
import models.view.post.BtbOrderPost;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/5/9
 * Time: 下午3:47
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class BtbCustoms extends Controller {

    @Before(only = {"btbOrderIndex", "createBtbOrderPage", "createBtbOrderByCustom"})
    public static void setUpShowPage() {
        List<BtbCustom> customList = BtbCustom.findAll();
        renderArgs.put("customList", customList);
    }

    public static void b2bCustomInfoIndex(BtbCustomPost p) {
        if(p == null) p = new BtbCustomPost();
        List<BtbCustom> dots = p.query();
        render(p, dots);
    }

    public static void showBtbOrderList(Long id) {
        BtbCustom custom = BtbCustom.findById(id);
        List<BtbOrder> orderList = custom.orders;
        render("/BtbCustoms/_order_list.html", orderList);
    }

    public static void deleteB2BCustom(Long id) {
        BtbCustom custom = BtbCustom.findById(id);
        custom.isDel = true;
        custom.save();
        flash.success("删除客户成功~");
        b2bCustomInfoIndex(new BtbCustomPost());
    }

    public static void createB2BCustom(BtbCustom b) {
        if(b.validRepeatCustomName()) {
            flash.error("客户/公司名称重复了，请重新填写！");
            render("Cooperators/createB2BCustomInfoPage.html", b);
        }
        if(b.id == null) {
            b.createDate = new Date();
            b.creator = Login.current();
            b.save();
            b.addresses.forEach(address -> {
                address.btbCustom = b;
                address.save();
            });
        } else {
            BtbCustom old = BtbCustom.findById(b.id);
            old.customName = b.customName;
            old.contactPhone = b.contactPhone;
            old.email = b.email;
            old.contacts = b.contacts;
            old.updateDate = new Date();
            old.save();
            b.addresses.forEach(address -> {
                if(address.id == null) {
                    address.btbCustom = old;
                    address.save();
                } else {
                    BtbCustomAddress entity = BtbCustomAddress.findById(address.id);
                    entity.receiver = address.receiver;
                    entity.receiverPhone = address.receiverPhone;
                    entity.countryCode = address.countryCode;
                    entity.city = address.city;
                    entity.postalCode = address.postalCode;
                    entity.address = address.address;
                    entity.save();
                }
            });
        }
        b2bCustomInfoIndex(new BtbCustomPost());
    }

    public static void btbOrderIndex(BtbOrderPost p) {
        if(p == null) p = new BtbOrderPost();
        List<BtbOrder> orderList = p.query();
        render(orderList, p);
    }

    public static void createB2BCustomInfoPage(Long id) {
        BtbCustom b = new BtbCustom();
        if(id != null) {
            b = BtbCustom.findById(id);
        }
        render(b);
    }

    public static void createBtbOrderByCustom(Long id) {
        BtbCustom custom = BtbCustom.findById(id);
        BtbOrder b = new BtbOrder();
        b.btbCustom = custom;
        render("BtbCustoms/createBtbOrderPage.html", b, custom);
    }

    public static void createBtbOrderPage(Long id) {
        String pageTitle = "新增B2B订单";
        BtbOrder b = new BtbOrder();
        if(id != null) {
            b = BtbOrder.findById(id);
            pageTitle = "修改B2B订单";
            List<ElcukRecord> logs = ElcukRecord.records(b.orderNo, "B2B订单管理");
            renderArgs.put("logs", logs);
        }
        render(b, pageTitle);
    }

    public static void cancelBtbOrder(Long id) {
        BtbOrder b = BtbOrder.findById(id);
        b.stage = BtbOrder.STAGE.Cancel;
        b.save();
        flash.success("取消订单成功！");
        btbOrderIndex(new BtbOrderPost());
    }

    public static void createBtbOrder(BtbOrder b) {
        if(StringUtils.isEmpty(b.orderNo)) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            BtbCustom custom = BtbCustom.findById(b.btbCustom.id);
            b.orderNo = "PO-" + custom.customName + "-" + formatter.format(new Date());
        }
        b.validOrder(b);
        if(Validation.hasErrors()) {
            render("Orders/createBtbOrderPage.html", b);
        }
        b.saveEntity(b);
        btbOrderIndex(new BtbOrderPost());
    }


    public static void updateBtbOrder(BtbOrder b, Long id) {
        if(Validation.hasErrors()) {
            render("Orders/createBtbOrderPage.html", b);
        }
        BtbOrder old = BtbOrder.findById(id);
        old.saveEntity(b);
        btbOrderIndex(new BtbOrderPost());
    }

    public static void btbOrderItemList(Long id) {
        BtbOrder order = BtbOrder.findById(id);
        render(order);
    }

    public static void findInfoById(Long id) {
        BtbCustom custom = BtbCustom.findById(id);
        BtbCustomAddress address = custom.addresses.get(0);
        if(address != null)
            renderJSON(J.json(GTs.MapBuilder.map("receiver", address.receiver)
                    .put("receiverPhone", address.receiverPhone)
                    .put("countryCode", address.countryCode)
                    .put("city", address.city)
                    .put("address", address.address)
                    .put("postalCode", address.postalCode).build()));
    }


}
