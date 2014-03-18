package ext;

import models.SaleTarget;
import play.templates.JavaExtensions;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-3-17
 * Time: AM11:17
 */
public class SaleTargetHelper extends JavaExtensions {

    public static String typeColor(SaleTarget.T saleTargetType) {
        switch(saleTargetType) {
            case YEAR:
                return "#5CB85C";
            case TEAM:
                return "#FAA52C";
            case CATEGORY:
                return "#4DB2D0";
            case SKU:
                return "#D14741";
            default:
                return "#5BB75B";
        }
    }
}
