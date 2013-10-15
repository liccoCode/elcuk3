package factory.market;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import helper.Currency;
import models.market.M;
import models.market.OrderItem;
import models.market.Orderr;
import models.market.Selling;
import models.product.Product;
import util.DateHelper;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 10/12/13
 * Time: 11:29 AM
 */
public class OrderItemFactory extends ModelFactory<OrderItem> {
    @Override
    public OrderItem define() {
        OrderItem itm = new OrderItem();
        itm.id = "orderItem_id";
        itm.createDate = DateHelper.t("2011-10-07 10:02:19");
        itm.discountPrice = 0f;
        itm.feesAmaount = 0f;
        itm.price = 46.371f;
        itm.listingName = "EasyAcc Black High Quality Leather Protective Case and Multi Angle Stand for HP Touchpad Tablet + 2 Screen Protectors for HP Touchpad 16/32GB";
        itm.quantity = 3;
        itm.currency = Currency.GBP;
        itm.market = M.AMAZON_DE;
        return itm;
    }

    @Factory(name = "de")
    public OrderItem de() {
        OrderItem itm = define();
        itm.order = FactoryBoy.lastOrCreate(Orderr.class);
        itm.selling = FactoryBoy.lastOrCreate(Selling.class);
        itm.product = FactoryBoy.lastOrCreate(Product.class);
        return itm;
    }
}
