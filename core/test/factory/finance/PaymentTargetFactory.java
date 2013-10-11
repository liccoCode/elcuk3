package factory.finance;

import factory.ModelFactory;
import models.finance.PaymentTarget;


/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 6/4/13
 * Time: 6:45 PM
 */
public class PaymentTargetFactory extends ModelFactory<PaymentTarget> {
    @Override
    public PaymentTarget define() {
        PaymentTarget paymentTarget = new PaymentTarget();
        paymentTarget.accountNumber = "88888888";
        paymentTarget.accountUser = "8888888";
        paymentTarget.name = "88888";
        return paymentTarget;
    }
}
