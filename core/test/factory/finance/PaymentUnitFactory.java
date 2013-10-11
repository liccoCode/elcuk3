package factory.finance;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import helper.Currency;
import models.finance.FeeType;
import models.finance.Payment;
import models.finance.PaymentUnit;
import models.procure.ProcureUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 7/8/13
 * Time: 3:48 PM
 */
public class PaymentUnitFactory extends ModelFactory<PaymentUnit> {
    @Override
    public PaymentUnit define() {
        PaymentUnit unit = new PaymentUnit();
        unit.amount = 1000;
        unit.currency = Currency.CNY;
        unit.procureUnit = FactoryBoy.lastOrCreate(ProcureUnit.class);
        unit.feeType = FactoryBoy.lastOrCreate(FeeType.class);
        unit.state = PaymentUnit.S.APPLY;
        return unit;
    }

    @Factory(name = "deny")
    public PaymentUnit plan() {
        PaymentUnit unit = withOutState();
        unit.state = PaymentUnit.S.DENY;
        return unit;
    }

    @Factory(name = "noPayment")
    public PaymentUnit noPayment() {
        PaymentUnit unit = new PaymentUnit();
        unit.amount = 1000;
        unit.currency = Currency.CNY;
        unit.procureUnit = FactoryBoy.lastOrCreate(ProcureUnit.class);
        unit.feeType = FactoryBoy.lastOrCreate(FeeType.class);
        return unit;
    }

    private PaymentUnit withOutState() {
        PaymentUnit unit = new PaymentUnit();
        unit.amount = 1000;
        unit.currency = Currency.CNY;
        unit.procureUnit = FactoryBoy.lastOrCreate(ProcureUnit.class);
        unit.feeType = FactoryBoy.lastOrCreate(FeeType.class);
        unit.payment = FactoryBoy.lastOrCreate(Payment.class);
        return unit;
    }
}
