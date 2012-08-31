package ext;

import models.procure.ProcureUnit;
import play.templates.JavaExtensions;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/31/12
 * Time: 5:19 PM
 */
public class ProcuresHelper extends JavaExtensions {

    public static String ucolor(ProcureUnit unit) {
        switch(unit.stage) {
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
}
