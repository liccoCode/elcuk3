package helper;

import com.google.gson.GsonBuilder;
import models.MT;
import org.apache.commons.lang.StringUtils;
import play.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

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


    /**
     * 这种是可以解析   1.234,23(DE) 与 1,234.23(US) 为 1234.23(CN)
     *
     * @param market
     * @param priceStr
     * @return
     */
    public static Float amazonPriceNumber(MT market, String priceStr) {
        try {
            switch(market) {
                case AUS:
                case AUK:
                    return NN_UK.parse(priceStr).floatValue();
                case ADE:
                case AFR:
                case AES:
                case AIT:
                    return NN_DE.parse(priceStr).floatValue();
                default:
                    Logger.warn("Not Support Market." + market);
            }
        } catch(Exception e) {
            Logger.warn("Webs.amazonPriceNumber error.(" + market + ") [" + priceStr + "|" + e.getMessage() + "]");
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
    public static String priceLocalNumberFormat(MT market, Float price) {
        switch(market) {
            case AUS:
            case AUK:
                return NN_UK.format(price);
            case ADE:
            case AFR:
            case AES:
            case AIT:
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
    public static Float amazonPriceCurrency(MT market, String priceStr) {
        try {
            switch(market) {
                case AUS:
                    return NC_US.parse(priceStr).floatValue();
                case AUK:
                    return NC_UK.parse(priceStr).floatValue();
                case ADE:
                case AFR:
                case AES:
                case AIT:
                    return NC_DE.parse(priceStr).floatValue();
                default:
                    Logger.warn("Not Support Market." + market);
            }
        } catch(Exception e) {
            Logger.warn("Webs.amazonPriceCurrency error.(" + market + ") [" + priceStr + "|" + e.getMessage() + "]");
        }
        return -0.1f;
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
