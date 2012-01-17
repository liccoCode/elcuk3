package market;

import com.elcuk.jaxb.BaseCurrencyCodeWithDefault;
import com.elcuk.jaxb.OverrideCurrencyAmount;
import com.elcuk.jaxb.Price;
import models.market.Account;
import models.market.Orderr;
import org.junit.Test;
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
public class OrderTest /*extends UnitTest*/ {

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

    @Test
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
}
