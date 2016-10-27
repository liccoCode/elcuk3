package ext;

import models.market.AmazonListingReview;
import models.market.Feedback;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.libs.F;
import play.templates.JavaExtensions;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/9/12
 * Time: 6:04 PM
 */
public class ReviewHelper extends JavaExtensions {

    /**
     * 返回 Review 的长度
     *
     * @param review
     * @return
     */
    public static String length(AmazonListingReview review) {
        if(StringUtils.isBlank(review.review))
            return "0";
        else
            return review.review.length() + "";
    }

    public static String length(Feedback feedback) {
        if(StringUtils.isBlank(feedback.feedback))
            return "0";
        else
            return feedback.feedback.length() + "";
    }

    /**
     * 计算 Review 的 rating 与 lastRating, 比较她们之间的差别
     *
     * @param review
     * @return T2: _.1 颜色 #(468847), _.2 icon class
     */
    public static F.T3<Boolean, String, String> iconRating(AmazonListingReview review) {
        if(review.lastRating > review.rating) {
            return new F.T3<>(true, "B94A48", "icon-arrow-down");
        } else if(review.lastRating < review.rating) {
            return new F.T3<>(true, "468847", "icon-arrow-up");
        } else {
            return new F.T3<>(false, "", "");
        }
    }

    public static String color(Feedback feedback) {
        int length = NumberUtils.toInt(length(feedback));
        if(length <= 15)
            return "#2FCCEF";
        else if(length <= 50)
            return "#6CB4E6";
        else if(length <= 100)
            return "#8CA7DE";
        else if(length <= 200)
            return "#9BA0D8";
        else if(length <= 300)
            return "#AC96D4";
        else
            return "#B38ACE";
    }

    /**
     * 根据 Review 的长度计算颜色
     *
     * @param review
     * @return
     */
    public static String color(AmazonListingReview review) {
        int length = 0;
        try {
            length = review.review.length();
        } catch(Exception e) {//
        }
        if(length <= 100)
            return "#2FCCEF";
        else if(length <= 240)
            return "#6CB4E6";
        else if(length <= 500)
            return "#8CA7DE";
        else if(length <= 1000)
            return "#9BA0D8";
        else if(length <= 2000)
            return "#AC96D4";
        else
            return "#B38ACE";
    }

    public static String color(Orderr.S orderState) {
        switch(orderState) {
            case PENDING:
                return "4169e1";
            case PAYMENT:
                return "FE8322";
            case CANCEL:
            case REFUNDED:
            case RETURNNEW:
                return "FF0601";
            case SHIPPED:
                return "38B800";
            default:
                return "000";
        }

    }

    /**
     * 截取字符串的一部分
     *
     * @param str
     * @return
     */
    public static String part(String str) {
        int len = 100;
        int real_len = str.length();
        len = Math.min(len, real_len);
        return String.format("%s...", str.substring(0, len));
    }

    /**
     * 根据一个数字, 计算这个数字对应的颜色(5 分评价常用)
     *
     * @param rate
     * @return
     */
    public static String color(Number rate) {
        if(rate.intValue() >= 5) {
            return "#3ED76A";
        } else if(rate.intValue() >= 4) {
            return "#ADFF1F";
        } else if(rate.intValue() >= 3) {
            return "#FFE107";
        } else if(rate.intValue() >= 2) {
            return "#D54C00";
        } else if(rate.intValue() >= 1) {
            return "#E03F00";
        } else {
            return "#FF1101";
        }
    }

    /**
     * 根据百分比进行颜色调整
     *
     * @param percent
     * @return
     */
    public static String pctColor(Number percent) {
        float pct = 0;
        if(percent.floatValue() < 1) pct = percent.floatValue() * 100;
        else pct = percent.floatValue();
        if(pct <= 20) {
            return "#DF534E";
        } else if(pct <= 50) {
            return "#F9A732";
        } else if(pct <= 70) {
            return "#4FB5D3";
        } else if(pct <= 100) {
            return "#5FBF5F";
        } else {
            return "#FF1101";
        }
    }

}
