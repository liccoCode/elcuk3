package helper;

import models.market.Selling;
import models.whouse.InboundUnit;
import models.whouse.RefundUnit;
import models.whouse.StockRecord;
import org.apache.commons.lang.StringUtils;
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
     * gs
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
            case AMAZON_MX:
                return String.format("http://www.%s/gp/product/%s", s.market, s.asin);
            default:
                return "#";
        }
    }

    public static String showPirateLink(Selling s) {
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
            case AMAZON_MX:
                return String.format("http://www.%s/gp/offer-listing/%s", s.market, s.asin);
            default:
                return "#";
        }
    }

    public static String showRecordLink(StockRecord stockRecord) {
        Long idMatch = stockRecord.recordId;
        switch(stockRecord.type) {
            case Inbound:
                InboundUnit unit = InboundUnit.findById(idMatch);
                return fullUrl("Inbounds.edit", unit.inbound.id, idMatch.toString());
            case Outbound:
                if(stockRecord.unit.outbound != null)
                    return fullUrl("Outbounds.edit", stockRecord.unit.outbound.id, idMatch.toString());
                else
                    return fullUrl("ProcureUnits.detail", idMatch.toString(), idMatch.toString());
            case Stocktaking:
                return Router.getFullUrl("StockRecords.show", GTs.newMap("id", idMatch).build());
            case Unqualified_Refund:
            case Refund:
                RefundUnit refundUnit = RefundUnit.findById(idMatch);
                return fullUrl("Refunds.edit", refundUnit.refund.id, idMatch.toString());
            case OtherOutbound:
                if(stockRecord.outbound != null)
                    return fullUrl("Outbounds.edit", stockRecord.outbound.id, stockRecord.unit.id.toString());
                else
                    return "";
            case Split:
            case Split_Stock:
            case CancelOutbound:
            case Unqualified_Transfer:
                return fullUrl("ProcureUnits.detail", idMatch.toString(), idMatch.toString());
            default:
                return "#";
        }
    }

    private static String fullUrl(String action, String id, String append) {
        return Router.getFullUrl(action, GTs.newMap("id", id).build()) + "#" + append;
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
