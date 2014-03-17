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
                return "#B0BFD6";
            case TEAM:
                return "#006ACC";
            case CATEGORY:
                return "#3DA4C2";
            case SKU:
                return "#108080";
            default:
                return "#5BB75B";
        }
    }
}
