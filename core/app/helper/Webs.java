package helper;

import com.google.gson.GsonBuilder;
import models.market.Account;
import models.market.Listing;
import models.market.Selling;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import play.Logger;
import play.Play;
import play.libs.Mail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.Future;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-11
 * Time: 上午3:55
 */
public class Webs {

    public static final String SPLIT = "|-|";
    //这种是可以解析   1.234,23(DE) 与 1,234.23(US) 为 1234.23(CN)
    public static final NumberFormat NN_DE = NumberFormat.getNumberInstance(Locale.GERMANY);
    public static final NumberFormat NN_UK = NumberFormat.getNumberInstance(Locale.UK);

    //这种是可以解析  £1,234.23(UK), $1,234.23(US), 1.234,23 €(DE)
    public static final NumberFormat NC_UK = NumberFormat.getCurrencyInstance(Locale.UK);
    public static final NumberFormat NC_US = NumberFormat.getCurrencyInstance(Locale.US);
    public static final NumberFormat NC_DE = NumberFormat.getCurrencyInstance(Locale.GERMANY);

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
    public static Integer[] fixPage(Integer p, Integer s) {
        Integer[] rtVal = new Integer[]{p, s};
        if(p == null || p < 0) rtVal[0] = 1; // 判断在页码
        if(s == null || s < 1 || s > 100) rtVal[1] = 20; // 判断显示的条数控制
        return rtVal;
    }

    /**
     * 保留小数点后面两位, 并且向上取整
     *
     * @param val
     * @return
     */
    public static Float scale2PointUp(Float val) {
        return scalePointUp(2, val);
    }

    public static Float scalePointUp(int scala, Float val) {
        return new BigDecimal(val).setScale(scala, RoundingMode.HALF_UP).floatValue();
    }

    public static String link(String listingId) {
        String[] args = listingId.split("_");
        Account.M market = Account.M.val(args[1]);
        return String.format("http://www.%s/dp/%s", market.toString(), args[0]);
    }

    /**
     * 简单的发送 HTML 的系统邮件
     *
     * @param subject 邮件标题
     * @param content 邮件内容
     * @return
     */
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

    /**
     * 这种是可以解析   1.234,23(DE) 与 1,234.23(US) 为 1234.23(CN)
     *
     * @param market
     * @param priceStr
     * @return
     */
    public static Float amazonPriceNumber(Account.M market, String priceStr) {
        try {
            switch(market) {
                case AMAZON_US:
                case AMAZON_UK:
                    return NN_UK.parse(priceStr).floatValue();
                case AMAZON_DE:
                case AMAZON_FR:
                case AMAZON_ES:
                case AMAZON_IT:
                    return NN_DE.parse(priceStr).floatValue();
                default:
                    Logger.warn("Not Support Market." + market);
            }
        } catch(Exception e) {
            Logger.warn("AmazonPrice parse error.(" + market + ") [" + e.getMessage() + "]");
        }
        return -0.1f;
    }

    /**
     * 将价格转换成不同市场格式的价格,例如: 1999.99 , uk: 1,999.99; de: 1.999,99 ...
     *
     * @param market
     * @param price
     * @return
     */
    public static String priceLocalNumberFormat(Account.M market, Float price) {
        switch(market) {
            case AMAZON_US:
            case AMAZON_UK:
                return NN_UK.format(price);
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_ES:
            case AMAZON_IT:
                return NN_DE.format(price);
            default:
                return NN_UK.format(price);
        }
    }

    /**
     * 这种是可以解析  £1,234.23(UK), $1,234.23(US), 1.234,23 €(DE)
     *
     * @param market
     * @param priceStr
     * @return
     */
    public static Float amazonPriceCurrency(Account.M market, String priceStr) {
        try {
            switch(market) {
                case AMAZON_US:
                    return NC_US.parse(priceStr).floatValue();
                case AMAZON_UK:
                    return NC_UK.parse(priceStr).floatValue();
                case AMAZON_DE:
                case AMAZON_FR:
                case AMAZON_ES:
                case AMAZON_IT:
                    return NC_DE.parse(priceStr).floatValue();
                default:
                    Logger.warn("Not Support Market." + market);
            }
        } catch(Exception e) {
            Logger.warn("AmazonPrice parse error.(" + market + ") [" + e.getMessage() + "]");
        }
        return -0.1f;
    }

    /**
     * 将数字按照市场的 Currency 格式输出出来. £1,234.23(UK), $1,234.23(US), 1.234,23 €(DE)
     *
     * @param market
     * @param price
     * @return
     */
    public static String priceLocalCurrencyFormat(Account.M market, Float price) {
        if(price == null) price = 0f;
        switch(market) {
            case AMAZON_US:
                return NC_US.format(price);
            case AMAZON_UK:
                return NC_UK.format(price);
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_ES:
            case AMAZON_IT:
                return NC_DE.format(price);
            default:
                return NC_UK.format(price);
        }
    }

    /**
     * 简单的获取 Exception 的文本
     *
     * @return
     */
    public static String E(Exception e) {
        return e.getClass().getSimpleName() + "|" + e.getMessage();
    }

    /**
     * 直接把 Exception 的堆栈信息全部打印出来
     *
     * @param e
     * @return
     */
    public static String S(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static String exposeGson(Object o) {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(o);
    }

    /**
     * 支持 DE 与 FR 语言中的月份字符串转换成 英语 月份的字符串
     *
     * @param m
     * @return
     */
    public static String dateMap(String m) {
        return StringUtils.replaceEach(m, new String[]{
                // de -> uk
                "Januar",
                "Februar",
                "März",
                "April",
                "Mai",
                "Juni",
                "Juli",
                "August",
                "September",
                "Oktober",
                "November",
                "Dezember",

                // fr -> uk
                "janvier",
                "février",
                "mars",
                "avril",
                "mai",
                "juin",
                "juillet",
                "août",
                "septembre",
                "octobre",
                "novembre",
                "décembre"
        }, new String[]{
                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December",

                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December",
        });
    }
}
