package ext;

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
    public static int applyBadge(ProcureUnit procureUnit) {
        int apply = 0;
        for(PaymentUnit unit : procureUnit.fees()) {
            if(unit.state == PaymentUnit.S.APPLY)
                apply++;
        }
        return apply;
    }

    public static int denyBadge(ProcureUnit procureUnit) {
        int deny = 0;
        for(PaymentUnit unit : procureUnit.fees()) {
            if(unit.state == PaymentUnit.S.DENY)
                deny++;
        }
        return deny;
    }

    public static int approvalBadge(ProcureUnit procureUnit) {
        int approval = 0;
        for(PaymentUnit unit : procureUnit.fees()) {
            if(unit.state == PaymentUnit.S.APPROVAL)
                approval++;
        }
        return approval;
    }

    public static int paidBadge(ProcureUnit procureUnit) {
        int paid = 0;
        for(PaymentUnit unit : procureUnit.fees()) {
            if(unit.state == PaymentUnit.S.PAID)
                paid++;
        }
        return paid;
    }

    public static String stateColor(PaymentUnit punit) {
        switch(punit.state) {
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
}
