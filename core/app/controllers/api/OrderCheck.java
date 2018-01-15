package controllers.api;

import models.market.OrderItem;
import models.market.Orderr;
import models.market.Selling;
import models.view.Ret;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 检查订单是否存在
 * <p/>
 * User: mac
 * Date: 14-11-6
 * Time: AM9:51
 */
@With(APIChecker.class)
public class OrderCheck extends Controller {

    /**
     * 检查订单是否存在
     */
    public static void order() {
        String orderid = request.params.get("orderid");
        Orderr order = Orderr.findById(orderid);
        if(order != null) {
            renderJSON(new Ret(true, "存在订单!"));
        } else
            renderJSON(new Ret(false, "不存在订单!"));
    }

    /**
     * 通过 asin 查品线
     * 通过订单号查品线
     * 通过 sku 查品线
     * 通过邮箱查品线
     */
    public static void categorycheck() {
        String checkinfo = request.params.get("checkinfo");
        String checktype = request.params.get("checktype");
        if(checktype.equals("asin")) {
            Selling sell = Selling.find("asin=?", checkinfo).first();
            if(sell != null) {
                String category = sell.sellingId.substring(0, 2);
                renderJSON(new Ret(true, category));
            } else {
                renderJSON(new Ret(false, "asin不存在!"));
            }
        } else if(checktype.equals("order")) {
            OrderItem item = OrderItem.find("order.orderId=?", checkinfo).first();
            if(item != null) {
                if(item.selling == null)
                    renderJSON(new Ret(false, "selling不存在!"));
                String category = item.selling.sellingId.substring(0, 2);
                renderJSON(new Ret(true, category));
            } else {
                renderJSON(new Ret(false, "order不存在!"));
            }
        } else if(checktype.equals("sku")) {
            List<Selling> sellings = Selling.find("product.sku=?", checkinfo).fetch();
            if(sellings.size() > 0) {
                for(Selling selling : sellings) {
                    String category = selling.product.category.categoryId;
                    renderJSON(new Ret(true, category));
                    break;
                }
            } else {
                renderJSON(new Ret(false, "sku不存在!"));
            }
        } else if(checktype.equals("email")) {
            if(!(checkinfo.indexOf("marketplace.amazon.com") > 0
                    || checkinfo.indexOf("marketplace.amazon.co.uk") > 0
                    || checkinfo.indexOf("marketplace.amazon.de") > 0
                    || checkinfo.indexOf("marketplace.amazon.ca") > 0
                    || checkinfo.indexOf("marketplace.amazon.jp") > 0
                    || checkinfo.indexOf("marketplace.amazon.it") > 0
                    || checkinfo.indexOf("marketplace.amazon.es") > 0
                    || checkinfo.indexOf("marketplace.amazon.fr") > 0)) {
                renderJSON(new Ret(false, "email格式不存在!"));
            }
            List<Orderr> orderrs = Orderr.find("email=?", checkinfo).fetch();
            if(orderrs.size() > 1) {
                renderJSON(new Ret(false, "订单大于1个!"));
            }
            if(orderrs.size() > 0) {
                String category = orderrs.get(0).items.get(0).selling.sellingId.substring(0, 2);
                renderJSON(new Ret(true, category));
            } else {
                renderJSON(new Ret(false, "email不存在!"));
            }
        } else {
            renderJSON(new Ret(false, "类型不存在!"));
        }
    }
}
