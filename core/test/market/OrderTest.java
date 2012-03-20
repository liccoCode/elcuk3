package market;

import com.elcuk.jaxb.BaseCurrencyCodeWithDefault;
import com.elcuk.jaxb.OverrideCurrencyAmount;
import com.elcuk.jaxb.Price;
import models.market.Account;
import models.market.Orderr;
import org.junit.Test;
import play.Logger;
import play.test.UnitTest;

import javax.xml.bind.JAXB;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 11:10 AM
 */
public class OrderTest extends UnitTest {

    @Test
    public void testFindOrderWithHibernateFirstCache() {
        Orderr o1 = Orderr.findById("026-0029259-0221158");
        Orderr o2 = Orderr.findById("026-0029259-0221158");
        System.out.println("User FindById Find: " + (o1 == o2) + "||o1:" + o1.hashCode() + "....o2:" + o2.hashCode());

        Orderr o3 = Orderr.find("orderId", "026-0029259-0221158").first();
        Orderr o4 = Orderr.find("orderId", "026-0029259-0221158").first();
        System.out.println("User Find(orderId) Find: " + (o3 == o4) + "||o3:" + o3.hashCode() + "....o4:" + o4.hashCode());

    }

    //    @Test
    public void saveOrder() {
        Orderr order = new Orderr();
        order.paymentDate = new Date();
        order.market = Account.M.AMAZON_UK;
        order.state = Orderr.S.PENDING;
        order.buyer = "wyatt";
        order.address = "address";
        order.email = "df";
        order.orderId = "88d8d8d8";
        order.save();
    }

//    @Test
    public void testEncode() {

        Price price = new Price();

        OverrideCurrencyAmount cu = new OverrideCurrencyAmount();
        cu.setCurrency(BaseCurrencyCodeWithDefault.GBP);
        cu.setValue(BigDecimal.valueOf(16.99));

        OverrideCurrencyAmount cu2 = new OverrideCurrencyAmount();
        cu2.setCurrency(BaseCurrencyCodeWithDefault.GBP);
        cu2.setValue(BigDecimal.valueOf(30.99));

        Price.Sale sale = new Price.Sale();
        sale.setSalePrice(cu);

        price.setSKU("kdjfkdjf");
        price.setStandardPrice(cu2);
        price.setSale(sale);

        StringWriter sw = new StringWriter();
        JAXB.marshal(price, sw);
        System.out.println(sw.toString());
    }

//    @Test
    public void testEmailed() {
        Orderr or2 = Orderr.findById("302-0924220-3736363");
        or2.emailed(1, '0');
        Logger.info("================:" + or2.emailed + "::" + Integer.toHexString(or2.emailed));
        or2.emailed(1, 'f');
        Logger.info("================:" + or2.emailed + "::" + Integer.toHexString(or2.emailed));
        or2.emailed(2, '0');
        Logger.info("================:" + or2.emailed + "::" + Integer.toHexString(or2.emailed));
        or2.emailed(2, 'f');
        Logger.info("================:" + or2.emailed + "::" + Integer.toHexString(or2.emailed));
        or2.emailed(3, '0');
        Logger.info("================:" + or2.emailed + "::" + Integer.toHexString(or2.emailed));
        or2.emailed(3, 'f');
        Logger.info("================:" + or2.emailed + "::" + Integer.toHexString(or2.emailed));
        or2.emailed(4, '0');
        Logger.info("================:" + or2.emailed + "::" + Integer.toHexString(or2.emailed));
        or2.emailed(4, 'f');
        Logger.info("================:" + or2.emailed + "::" + Integer.toHexString(or2.emailed));
        /*
        12-02-28 11:28:19 [play] INFO  ~ ================:0::0
        12-02-28 11:28:19 [play] INFO  ~ ================:15::f
        12-02-28 11:28:19 [play] INFO  ~ ================:15::f
        12-02-28 11:28:19 [play] INFO  ~ ================:255::ff
        12-02-28 11:28:19 [play] INFO  ~ ================:255::ff
        12-02-28 11:28:19 [play] INFO  ~ ================:4095::fff
        12-02-28 11:28:19 [play] INFO  ~ ================:4095::fff
        12-02-28 11:28:19 [play] INFO  ~ ================:65535::ffff

         */
    }
}
