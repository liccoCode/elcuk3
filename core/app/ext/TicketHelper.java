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
     * 1. Review 的 td score, 颜色区分
     * 2. rating 变化, 上下箭头
     * 3. 删除的 叉叉
     *
     * @param review
     * @return
     */
    public static BaseTemplate.RawData tdScore(AmazonListingReview review) {
        StringBuilder sbd = new StringBuilder("<td");
        if(review != null) {
            int upOrDown = review.isUpOrDown();
            // title
            if(upOrDown != 0)
                sbd.append(" rel='tooltip' title='From ").append(review.lastRating)
                        .append(" To ").append(review.rating).append("' ");

            //  分数
            sbd.append(tdScore(review.rating));

            // 上下箭头
            if(upOrDown > 0)
                sbd.append("<i").append(" style='color:#468847' ")
                        .append("class='pull-right icon-arrow-up'>");
            else if(upOrDown < 0)
                sbd.append("<i").append(" style='color:#B94A48' ")
                        .append("class='pull-right icon-arrow-down'>");

            // 是否删除
            if(review.isRemove)
                sbd.append("<i style='color:#468847' class='pull-right icon-remove'></i>");
        } else {
            sbd.append(">");
        }

        sbd.append("</td>");
        return raw(sbd.toString());
    }

    public static BaseTemplate.RawData tdScore(Feedback feedback) {
        StringBuilder sbd = new StringBuilder("<td");
        if(feedback != null) {
            sbd.append(tdScore(feedback.score));
            // 是否删除
            if(feedback.isRemove)
                sbd.append("<i style='color:#468847' class='pull-right icon-remove'></i>");
        } else {
            sbd.append(">");
        }

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
