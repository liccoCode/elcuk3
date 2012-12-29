package ext;

import models.market.AmazonListingReview;
import models.market.Feedback;
import play.templates.BaseTemplate;
import play.templates.JavaExtensions;

/**
 * 负责 Ticket/Review/Feedback 页面的辅助生成 HTML
 * User: wyatt
 * Date: 12/29/12
 * Time: 3:15 PM
 */
public class TicketHelper extends JavaExtensions {
    /**
     * Review 的 td score, 颜色区分
     *
     * @param review
     * @return
     */
    public static BaseTemplate.RawData tdScore(AmazonListingReview review) {
        StringBuilder sbd = new StringBuilder("<td");
        if(review != null)
            sbd.append(tdScore(review.rating));
        else
            sbd.append(">");


        sbd.append("</td>");
        return raw(sbd.toString());
    }

    public static BaseTemplate.RawData tdScore(Feedback feedback) {
        StringBuilder sbd = new StringBuilder("<td");
        if(feedback != null)
            sbd.append(tdScore(feedback.score));
        else
            sbd.append(">");

        sbd.append("</td>");
        return raw(sbd.toString());
    }

    private static String tdScore(Float score) {
        StringBuilder sbd = new StringBuilder();
        if(score <= 2)
            sbd.append(" style='background:#D54C00'>").append(score);
        else if(score < 4)
            sbd.append(" style='background:#FFE107'>").append(score);
        else
            sbd.append(" style='background:#ADFF1F'>").append(score);
        return sbd.toString();
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

}
