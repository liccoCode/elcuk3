package mws;

import com.amazonservices.mws.orders.MarketplaceWebServiceOrdersException;
import helper.J;
import models.market.Account;
import models.market.OrderItem;
import models.market.Orderr;
import org.junit.Test;
import play.libs.IO;
import play.test.UnitTest;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/24/13
 * Time: 11:29 AM
 */
public class MWSOrdersTest extends UnitTest {

    //    @Test
    public void testListOrders() throws MarketplaceWebServiceOrdersException {
        Account acc = Account.findById(2l);
        List<Orderr> orders = MWSOrders.listOrders(acc, 12);
        String sbd = J.json(orders);
        IO.writeContent(sbd,
                new File("/Users/wyatt/Programer/repos/elcuk2/core/orders.json")
        );
    }

    @Test
    public void testListOrderItems() throws MarketplaceWebServiceOrdersException {
        Orderr orderr = Orderr.findById("002-0038309-3390664");
        List<OrderItem> orderItems = MWSOrders.listOrderItems(orderr.account, orderr.orderId);
        /*
        ID: 002-0038309-3390664_70EAUB133-CBSPU
        Product: Product[70EAUB133-CBSPU]
        listingName: EasyAcc 13.3 inch Laptop Ultrabook Leather Sleeve Carry Case Cover for Apple Macbook Air 13.3 , Asus Zenbook UX31, Acer Aspire S3, Lenovo IdeaPad U300
        createDate: Fri May 24 14:15:02 CST 2013
        Price: 15.99
        Currency: USD
        USDCost: 15.99
        DiscountPrice: 0.0
        ShippingPrice: null
        Quantity: 1
         */
        for(OrderItem oi : orderItems) {
            System.out.println("ID: " + oi.id);
            System.out.println("Product: " + oi.product);
            System.out.println("listingName: " + oi.listingName);
            System.out.println("createDate: " + oi.createDate);
            System.out.println("Price: " + oi.price);
            System.out.println("Currency: " + oi.currency);
            System.out.println("USDCost: " + oi.usdCost);
            System.out.println("DiscountPrice: " + oi.discountPrice);
            System.out.println("ShippingPrice: " + oi.shippingPrice);
            System.out.println("Quantity: " + oi.quantity);
        }
    }
}
