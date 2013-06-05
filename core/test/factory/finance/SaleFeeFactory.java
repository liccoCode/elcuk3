package factory.finance;

import factory.FactoryBoy;
import factory.ModelFactory;
import helper.Currency;
import models.finance.FeeType;
import models.finance.SaleFee;
import models.market.Account;
import models.market.M;
import models.market.Orderr;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 6/4/13
 * Time: 6:45 PM
 */
public class SaleFeeFactory extends ModelFactory<SaleFee> {
    @Override
    public SaleFee define() {
        SaleFee fee = new SaleFee();
        fee.type = FeeType.productCharger();
        fee.date = new Date();
        fee.currency = Currency.USD;
        fee.cost = 12.99f;
        fee.usdCost = 12.99f;
        fee.account = FactoryBoy.build(Account.class);
        fee.market = M.AMAZON_DE;
        fee.qty = 1;
        fee.order = FactoryBoy.build(Orderr.class);
        fee.orderId = fee.order.orderId;
        return fee;
    }
}
