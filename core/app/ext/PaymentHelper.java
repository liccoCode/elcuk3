package ext;

import models.finance.Payment;
import models.finance.PaymentUnit;
import play.templates.JavaExtensions;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 3/11/13
 * Time: 4:56 PM
 */
public class PaymentHelper extends JavaExtensions {
    public static String stateColor(PaymentUnit punit) {
        return stateColor(punit.state);
    }

    public static String stateColor(PaymentUnit.S state) {
        switch(state) {
            case APPLY:
                return "#999999";
            case DENY:
                return "#BA4A48";
            case APPROVAL:
                return "#3987AD";
            case PAID:
                return "#4B8644";
            default:
                return "#999999";
        }
    }

    public static String stateColor(Payment payment) {
        switch(payment.state) {
            case PAID:
                return "#468847";
            case CLOSE:
                return "#999999";
            case WAITING:
            default:
                return "#B34745";
        }
    }
}
