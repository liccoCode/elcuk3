package ext;

import models.finance.Payment;
import models.finance.PaymentUnit;
import models.procure.ProcureUnit;
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
                return "#333333";
            case DENY:
                return "#BA4A48";
            case APPROVAL:
                return "#3A87AD";
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
            case CANCEL:
                return "#999999";
            case WAITING:
            default:
                return "#F8941D";
        }
    }

    public static String badgeInfo(ProcureUnit unit) {
        int size = unit.fees.size();
        if(size >= 2) {
            return "badge-warning";
        } else if(size > 0) {
            return "badge-info";
        } else {
            return "";
        }
    }

    public static String stateLabel(PaymentUnit unit) {
        switch(unit.state) {
            case APPLY:
                return "label-inverse";
            case DENY:
                return "label-important";
            case APPROVAL:
                return "label-info";
            case PAID:
                return "label-success";
            default:
                return "";
        }
    }
}
