package helper;

import models.market.Listing;
import models.market.Selling;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import play.Logger;
import play.Play;
import play.libs.Mail;

import java.util.concurrent.Future;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-11
 * Time: 上午3:55
 */
public class Webs {

    public static final String SPLIT = "|-|";

    /**
     * <pre>
     * 用来对 Pager 与 pageSize 的值进行修正;
     * page: 1~N
     * size: 1~100 (-> 20)
     * </pre>
     *
     * @param p
     * @param s
     */
    public static void fixPage(Integer p, Integer s) {
        if(p == null || p < 0) p = 1; // 判断在页码
        if(s == null || s < 1 || s > 100) s = 20; // 判断显示的条数控制 
    }

    /**
     * 返回可以访问具体网站的链接
     *
     * @return 如果正常判断则返回对应网站链接, 否则返回 #
     */
    public static String link(Listing listing) {
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

    public static String link(Selling selling) {
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

    public static Future<Boolean> systemMail(String subject, String content) {
        HtmlEmail email = new HtmlEmail();
        try {
            email.setSubject(subject);
            email.addTo("wppurking@gmail.com");
            if(Play.mode.isProd())
                email.setFrom("support@easyacceu.com", "EasyAcc");
            else
                email.setFrom("1733913823@qq.com", "EasyAcc"); // 因为在国内 Gmail 老是被墙, 坑爹!! 所以非 产品环境 使用 QQ 邮箱测试.
            email.setHtmlMsg(content);
        } catch(EmailException e) {
            Logger.warn("Email error: " + e.getMessage());
        }
        return Mail.send(email);
    }
}
