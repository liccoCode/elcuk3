package factory.market;

import factory.ModelFactory;
import models.market.Orderr;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/29/13
 * Time: 6:22 PM
 */
public class OrderrFactory extends ModelFactory<Orderr> {
    @Override
    public Orderr define() {
        Orderr orderr = new Orderr();
        orderr.orderId = "sdfjksdjfksdjfksjdkfj";
        orderr.paymentDate = new Date();
        orderr.address = "Kdfdsj";
        orderr.state = Orderr.S.SHIPPED;
        return orderr;
    }
}
