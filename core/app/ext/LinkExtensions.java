package ext;

import models.market.Account;
import models.market.AmazonListingReview;
import models.market.Listing;
import models.market.Selling;
import play.templates.JavaExtensions;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 5/16/12
 * Time: 4:24 PM
 */
public class LinkExtensions extends JavaExtensions {

    public static String userReviewLink(AmazonListingReview r) {
        String baseAmazon = "http://www.%s/gp/pdp/profile/%s";
        String[] args = r.listingId.split("_");
        Account.M market = Account.M.val(args[1]);
        switch(market) {
            case AMAZON_US:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_ES:
            case AMAZON_IT:
                return String.format(baseAmazon, market.toString(), r.userid);
        }
        return "#";
    }

    /**
     * 根据 Selling 来获取此 Selling 在不同市场上的留 Review 的地址
     *
     * @param selling
     * @return
     */
    public static String reviewLink(Selling selling) {
        //http://www.amazon.co.uk/review/create-review/ref=cm_cr_pr_wr_but_top?ie=UTF8&nodeID=&asin=B003TQ3NCY
        String baseAmazon = "http://www.%s/review/create-review/ref=cm_cr_pr_wr_but_top?ie=UTF8&nodeID=&asin=%s";
        switch(selling.market) {
            case AMAZON_US:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_ES:
            case AMAZON_IT:
                return String.format(baseAmazon, selling.market.toString(), selling.asin);
        }
        return "#";
    }

    public static String asinLink(Selling selling) {
        //http://www.amazon.co.uk/dp/B005UNXHC0
        String baseAmazon = "http://www.%s/dp/%s";
        //http://www.ebay.co.uk/itm/170724459305
        String baseEbay = "http://www.%s/itm/%s";
        switch(selling.market) {
            case AMAZON_US:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_ES:
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
            case AMAZON_US:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_ES:
            case AMAZON_IT:
                return String.format(baseAmazon, listing.market.toString(), listing.asin);
            case EBAY_UK:
                return String.format(baseEbay, listing.market.toString(), listing.asin);
        }
        return "#";
    }
}
