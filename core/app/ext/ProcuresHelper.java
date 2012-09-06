package ext;

import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import play.templates.JavaExtensions;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/31/12
 * Time: 5:19 PM
 */
public class ProcuresHelper extends JavaExtensions {

    public static String rgb(ProcureUnit.STAGE stage) {
        switch(stage) {
            case PLAN:
                return "3da4c2";
            case DELIVERY:
                return "006acc";
            case DONE:
                return "5bb75b";
            case SHIP_OVER:
                return "108080";
            case CLOSE:
            default:
                return "f9a021";
        }
    }

    public static String rgb(Deliveryment.S state) {
        switch(state) {
            case PENDING:
                return "5CB85C";
            case DELIVERING:
                return "FAA52C";
            case DELIVERY:
                return "F67300";
            case NEEDPAY:
                return "4DB2D0";
            case FULPAY:
                return "007BCC";
            case CANCEL:
            default:
                return "D14741";
        }
    }
}
