package helper;

import models.market.Selling;
import models.procure.ProcureUnit;
import models.whouse.StockObj;
import models.whouse.StockRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.mvc.Router;
import play.templates.JavaExtensions;

import java.util.Map;

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

    public static String fnSKULabelLink(StockObj obj) {
        if(!obj.attributes().isEmpty() && obj.attributes().containsKey("procureunitId") &&
                StringUtils.isNotBlank(obj.fnsku())) {
            ProcureUnit procureUnit = ProcureUnit.findById(NumberUtils.toLong(obj.attrs.get("procureunitId")
                    .toString()));
            if(procureUnit != null && procureUnit.selling != null) {
                return Router.getFullUrl("ProcureUnits.fnSkuLable",
                        GTs.newMap("id", procureUnit.selling.sellingId).put("includeSku", false).build()
                );
            }
        }
        return "";
    }

    public static String showRecordLink(StockRecord stockRecord) {
        String idMatch = String.format("id:%s", stockRecord.recordId);
        switch(stockRecord.type) {
            case Inbound:
                return Router.getFullUrl("InboundRecords.index", GTs.newMap("p.search", idMatch).build());
            case Outbound:
                return Router.getFullUrl("OutboundRecords.index", GTs.newMap("p.search", idMatch).build());
            case Stocktaking:

            default:
                return "#";
        }
    }

    public static String getRedirect(String id, String target) {
        Map params = GTs.newMap("id", id).build();
        switch(target) {
            case "DeliverPlans":
                return Router.getFullUrl("DeliverPlans.show", params);
            case "Deliveryments":
                return Router.getFullUrl("Deliveryments.show", params);
            default:
                return Router.getFullUrl("Deliveryments.show", params);
        }
    }
}
