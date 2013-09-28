package factory.finance;

import factory.ModelFactory;
import factory.annotation.Factory;
import helper.Currency;
import models.finance.Payment;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 9/11/13
 * Time: 6:01 PM
 */
public class PaymentFactory extends ModelFactory<Payment> {
    @Override
    public Payment define() {
        Payment p = new Payment();
        p.currency = Currency.CNY;
        return p;
    }

    @Factory(name = "paid")
    public Payment paid() {
        Payment p = define();
        p.state = Payment.S.PAID;
        p.paymentDate = new Date();
        return p;
    }
}
