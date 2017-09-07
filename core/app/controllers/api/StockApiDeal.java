package controllers.api;

import helper.Currency;
import models.User;
import models.procure.Cooperator;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import models.product.Product;
import models.view.Ret;
import models.whouse.InboundUnit;
import models.whouse.Whouse;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/8/4
 * Time: 下午2:40
 */
@With({APIChecker.class})
public class StockApiDeal extends Controller {

    public static void checkIsOutBoundB2b() {
        String[] sku = request.params.get("sku").split(",");
        for(int i = 0; i < sku.length; i++) {
            List<Product> pro = Product.find("origin_sku=?", sku[0]).fetch();
            if(pro.size() == 0) {
                renderJSON(new Ret(false, "MengTop没有对应SKU，请先通知MengTop新建【" + sku[0] + "】对应的SKU"));
            }
        }
        renderJSON(new Ret(true));
    }

    public static void createProcureUnit() {
        try {
            Date now = new Date();
            User user = User.findById(178L);
            String projectName = request.params.get("projectName");
            Cooperator cooperator = Cooperator.findB2bCooperator(User.COR.valueOf(projectName));
            String[] skus = request.params.get("sku").split(",");
            String[] qtyList = request.params.get("qty").split(",");
            String[] currencyList = request.params.get("currency").split(",");
            String[] prices = request.params.get("price").split(",");
            Deliveryment deliveryment = new Deliveryment();
            deliveryment.id = Deliveryment.id();
            deliveryment.name = Deliveryment.b2bName();
            deliveryment.deliveryType = Deliveryment.T.MOVE;
            deliveryment.createDate = now;
            deliveryment.projectName = User.COR.MengTop;
            deliveryment.handler = user;
            deliveryment.cooperator = cooperator;
            deliveryment.state = Deliveryment.S.DONE;
            deliveryment.save();

            for(int i = 0; i < skus.length; i++) {
                String sku = skus[i];
                Currency currency = Currency.valueOf(currencyList[i]);
                Float price = Float.valueOf(prices[i]);
                int qty = Integer.parseInt(qtyList[i]);
                ProcureUnit unit = new ProcureUnit();
                unit.product = Product.findSkuForB2b(sku);
                unit.sku = unit.product.sku;
                unit.stage = ProcureUnit.STAGE.IN_STORAGE;
                unit.attrs.currency = currency;
                unit.attrs.price = price;
                unit.attrs.planQty = qty;
                unit.attrs.qty = qty;
                unit.inboundQty = qty;
                unit.availableQty = qty;
                unit.originQty = qty;
                unit.cooperator = cooperator;
                unit.attrs.deliveryDate = now;
                unit.attrs.planDeliveryDate = now;
                unit.createDate = now;
                unit.projectName = User.COR.MengTop.name();
                unit.result = InboundUnit.R.Qualified;
                unit.handler = user;
                unit.currWhouse = Whouse.findById(19L);
                unit.deliveryment = deliveryment;
                unit.save();
            }
            renderJSON(new Ret(true, "Mengtop创建库存数据成功！"));
        } catch(Exception e) {
            renderJSON(new Ret(false, e.getMessage()));
        }
    }

}
