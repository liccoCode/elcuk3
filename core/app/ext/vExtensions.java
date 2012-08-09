package ext;

import models.market.*;
import models.support.Ticket;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.i18n.Messages;
import play.templates.JavaExtensions;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 5/16/12
 * Time: 4:24 PM
 */
public class vExtensions extends JavaExtensions {

    public static String userReviewLink(AmazonListingReview r) {
        String baseAmazon = "http://www.%s/gp/pdp/profile/%s";
        String[] args = r.listingId.split("_");
        M market = M.val(args[1]);
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
    public static String makeReviewLink(Selling selling) {
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

    public static String orderLink(Orderr orderr) {
        //https://sellercentral.amazon.co.uk/gp/orders-v2/details?orderID=203-5364157-2572327
        switch(orderr.market) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
                return "https://sellercentral." + orderr.market.toString() + "/gp/orders-v2/details?orderID=" + orderr.orderId;
            case EBAY_UK:
            default:
                return "";
        }
    }

    /**
     * 根据一个数字, 计算这个数字对应的颜色(5 分评价常用)
     *
     * @param rate
     * @return
     */
    public static String color(Number rate) {
        if(rate.intValue() >= 5) {
            return "3ED76A";
        } else if(rate.intValue() >= 4) {
            return "ADFF1F";
        } else if(rate.intValue() >= 3) {
            return "FFE107";
        } else if(rate.intValue() >= 2) {
            return "D54C00";
        } else if(rate.intValue() >= 1) {
            return "E03F00";
        } else {
            return "FF1101";
        }
    }

    public static String reviewLink(AmazonListingReview review) {
        String site = StringUtils.split(review.listingId, "_")[1];
        return String.format("http://www.%s/review/%s", site, review.reviewId);
    }

    public static String osTicketLink(Ticket ticket) {
        return String.format("http://t.easyacceu.com/scp/tickets.php?id=%s", ticket.osTicketId());
    }

    /*util.Date*/
    public static String datetime(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public static String date(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    /*sql.Timestamp*/
    public static String datetime(Timestamp date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public static String date(Timestamp date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public static String dayAfter(int day) {
        return DateTime.now().plusDays(day).toString("yyyy-MM-dd");
    }

    /**
     * 传入的日期与现在的日期之间相差多久?
     *
     * @param date
     * @return
     */
    public static String left(Date date) {
        return left(date, false);
    }

    /**
     * 传入的日期与现在的日期之间相差多久?
     *
     * @param date
     * @return
     */
    public static String left(Date date, Boolean stopAtMonth) {
        Date now = new Date();
        if(now.after(date)) {
            return "";
        }
        long delta = (date.getTime() - now.getTime()) / 1000;
        if(delta < 60) {
            return Messages.get("left.seconds", delta, pluralize(delta));
        }
        if(delta < 60 * 60) {
            long minutes = delta / 60;
            return Messages.get("left.minutes", minutes, pluralize(minutes));
        }
        if(delta < 24 * 60 * 60) {
            long hours = delta / (60 * 60);
            return Messages.get("left.hours", hours, pluralize(hours));
        }
        if(delta < 30 * 24 * 60 * 60) {
            long days = delta / (24 * 60 * 60);
            return Messages.get("left.days", days, pluralize(days));
        }
        if(stopAtMonth) {
            return asdate(date.getTime(), Messages.get("since.format"));
        }
        if(delta < 365 * 24 * 60 * 60) {
            long months = delta / (30 * 24 * 60 * 60);
            return Messages.get("left.months", months, pluralize(months));
        }
        long years = delta / (365 * 24 * 60 * 60);
        return Messages.get("left.years", years, pluralize(years));
    }
}
