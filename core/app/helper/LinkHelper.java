package helper;

import models.market.Selling;
import models.whouse.StockObj;
import org.apache.commons.lang.StringUtils;
import play.mvc.Router;
import play.templates.JavaExtensions;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-1-3
 * Time: AM10:36
 */
public class LinkHelper extends JavaExtensions {
    /**
     * 生成 Listing 在 Amazon 展示的页面链接
     *
     * @param s
     * @return
     */
    public static String showListingLink(Selling s) {
        if(StringUtils.isBlank(s.asin) || StringUtils.isBlank(s.market.toString())) {
            return "#";
        }
        switch(s.market) {
            case AMAZON_CA:
            case AMAZON_US:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_JP:
            case AMAZON_FR:
            case AMAZON_IT:
                return String.format("http://www.%s/gp/product/%s", s.market, s.asin);
        }
        return "#";
    }

    public static String showStockObjLink(StockObj obj) {
        switch(obj.stockObjType) {
            case SKU:
                return Router.getFullUrl("Products.show", GTs.newMap("id", obj.stockObjId).build());
            case PRODUCT_MATERIEL:
                //TODO
                return "";
            case PACKAGE_MATERIEL:
                //TODO
                return "";
            default:
                return "";
        }
    }
}
