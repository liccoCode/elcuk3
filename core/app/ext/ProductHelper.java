package ext;

import models.product.Product;
import play.templates.JavaExtensions;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-4-25
 * Time: AM11:26
 */
public class ProductHelper extends JavaExtensions {
    public static String fontcolor(Product.S state) {
        switch(state) {
            case NEW:
                return "#0000ff";
            case SELLING:
                return "#008000";
            case DOWN:
                return "#ff0000";
            default:
                return "#";
        }
    }
}
