package ext;

import helper.Webs;
import models.market.*;
import models.procure.FBAShipment;
import models.view.dto.AnalyzeDTO;
import org.apache.commons.lang.StringUtils;
import play.templates.JavaExtensions;

/**
 * 有关不同 Model 的超链接的在 View 使用的辅助类
 * User: wyattpan
 * Date: 8/9/12
 * Time: 6:04 PM
 */
public class LinkHelper extends JavaExtensions {

    public static String userReviewLink(AmazonListingReview r) {
        String baseAmazon = "http://www.%s/gp/pdp/profile/%s";
        String[] args = r.listing.listingId.split("_");
        M market = M.val(args[1]);
        switch(market) {
            case AMAZON_CA:
            case AMAZON_US:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_ES:
            case AMAZON_JP:
            case AMAZON_IT:
                return String.format(baseAmazon, market.toString(), r.userid);
        }
        return "#";
    }

    public static String userFeedbackink(Feedback f) {
        if(f.orderr == null) return "#";
        String baseAmazon = "http://www.%s/gp/pdp/profile/%s";
        switch(f.market) {
            case AMAZON_CA:
            case AMAZON_US:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_ES:
            case AMAZON_JP:
            case AMAZON_IT:
                return String.format(baseAmazon, f.market, f.orderr.userid);
        }
        return "#";
    }

    public static String reviewLink(AmazonListingReview review) {
        String[] sites = StringUtils.split(review.listingId, "_");
        if(sites.length <= 1) return "";
        return String.format("http://www.%s/review/%s", sites[1], review.reviewId);
    }

    /**
     * 根据 Selling 来获取此 Selling 在不同市场上的留 Review 的地址
     *
     * @param selling
     * @return
     */
    public static String makeReviewLink(Selling selling) {
        //http://www.amazon.co.uk/review/create-review/ref=cm_cr_pr_wr_but_top?ie=UTF8&nodeID=&asin=B003TQ3NCY
        String baseAmazon = "http://www.%s/review/create-review/ref=cm_cr_pr_wr_but_top?ie=UTF8&nodeID=&asin=%s";
        switch(selling.market) {
            case AMAZON_CA:
            case AMAZON_US:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_ES:
            case AMAZON_JP:
            case AMAZON_IT:
                return String.format(baseAmazon, selling.market.toString(), selling.asin);
        }
        return "#";
    }

    public static String searchAsinByUPCLink(M market, String upc) {
        String baseAmazon = "https://sellercentral.%s/myi/search/ProductSummary?keyword=%s";
        return String.format(baseAmazon, market.toString(), upc);
    }

    public static String asinLink(Selling selling) {
        //http://www.amazon.co.uk/dp/B005UNXHC0
        String baseAmazon = "http://www.%s/dp/%s";
        //http://www.ebay.co.uk/itm/170724459305
        String baseEbay = "http://www.%s/itm/%s";
        switch(selling.market) {
            case AMAZON_CA:
            case AMAZON_US:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_ES:
            case AMAZON_JP:
            case AMAZON_IT:
                return String.format(baseAmazon, selling.market.toString(), selling.asin);
            case EBAY_UK:
                return String.format(baseEbay, selling.market.toString(), selling.asin);
        }
        return "#";
    }

    /**
     * 返回可以访问具体网站的链接
     *
     * @return 如果正常判断则返回对应网站链接, 否则返回 #
     */
    public static String asinLink(Listing listing) {
        //http://www.amazon.co.uk/dp/B005UNXHC0
        String baseAmazon = "http://www.%s/dp/%s";
        //http://www.ebay.co.uk/itm/170724459305
        String baseEbay = "http://www.%s/itm/%s";
        switch(listing.market) {
            case AMAZON_CA:
            case AMAZON_US:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_ES:
            case AMAZON_JP:
            case AMAZON_IT:
                return String.format(baseAmazon, listing.market.toString(), listing.asin);
            case EBAY_UK:
                return String.format(baseEbay, listing.market.toString(), listing.asin);
        }
        return "#";
    }

    public static String asinLink(AnalyzeDTO dto) {
        try {
            M market = M
                    .val(StringUtils.remove(StringUtils.splitByWholeSeparator(dto.fid, "|")[1], "_")
                            .toLowerCase());
            return String.format("http://www.%s/dp/%s", market.toString(), dto.asin);
        } catch(Exception e) {
            return Webs.E(e);
        }
    }

    public static String orderLink(Orderr orderr) {
        //https://sellercentral.amazon.co.uk/gp/orders-v2/details?orderID=203-5364157-2572327
        switch(orderr.market) {
            case AMAZON_CA:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_JP:
            case AMAZON_US:
                return "https://sellercentral." + orderr.market.toString() +
                        "/gp/orders-v2/details?orderID=" + orderr.orderId;
            case EBAY_UK:
            default:
                return "";
        }
    }

    public static String fbaLink(FBAShipment fba) {
        //https://sellercentral.amazon.de/gp/ssof/workflow/workflow.html/ref=ag_fbaworkflo_cont_fbaworkflo?ie=UTF8&shipmentId=FBA5LBGGC
        switch(fba.account.type) {
            case AMAZON_CA:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_JP:
            case AMAZON_US:
                return "https://sellercentral." + fba.account.type.toString() +
                        "/gp/fba/inbound-shipment-workflow/index.html#" +
                        fba.shipmentId+"/summary/tracking";
            case EBAY_UK:
            default:
                return "";
        }
    }
}
