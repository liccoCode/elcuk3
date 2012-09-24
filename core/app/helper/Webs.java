package helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import models.market.Account;
import models.market.M;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.message.BasicNameValuePair;
import play.Logger;
import play.Play;
import play.data.validation.Error;
import play.libs.F;
import play.libs.Mail;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-11
 * Time: 上午3:55
 */
public class Webs {

    public static final String SPLIT = "|-|";
    /**
     * 分隔符号: '|'
     */
    public static final String S = "|";
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
     * @return ._1: page, ._2: perSize
     */
    public static F.T2<Integer, Integer> fixPage(Integer p, Integer s) {
        int page = 1;
        int size = 20;
        if(p != null && p >= 1) page = p; // 判断在页码
        if(s != null && s >= 1 && s <= 100) size = s; // 判断显示的条数控制
        return new F.T2<Integer, Integer>(page, size);
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
        M market = M.val(args[1]);
        return String.format("http://www.%s/dp/%s", market.toString(), args[0]);
    }

    /**
     * 测试环境下使用的辅助登陆方法.  会将用户登陆的 CookieStore 保存到 test 目录下, 如果超过 10 小时则放弃缓存, 重新登陆
     * @param acc
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void dev_login(Account acc) throws IOException, ClassNotFoundException {
        File jsonFile = Play.getFile("/test/" + acc.prettyName() + ".json");
        if(jsonFile.exists() && (System.currentTimeMillis() - jsonFile.lastModified() > TimeUnit.HOURS.toMillis(10)))
            jsonFile.delete();

        if(!jsonFile.exists()) {
            acc.loginAmazonSellerCenter();
            FileOutputStream fos = new FileOutputStream(new File(Play.applicationPath + "/test", acc.prettyName() + ".json"));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(acc.cookieStore());
            oos.close();
        } else {
            FileInputStream fis = new FileInputStream(jsonFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            CookieStore cookieStore = (CookieStore) ois.readObject();
            Account.cookieMap().put(Account.cookieKey(acc.id, acc.type), cookieStore);
        }
        Account.cookieMap().get(Account.cookieKey(acc.id, acc.type)).clearExpired(new Date());
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
            email.setCharset("UTF-8");
            email.setSubject(subject);
            email.addTo("wppurking@gmail.com");
            email.setFrom("support@easyacceu.com", "EasyAcc");
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
    public static Float amazonPriceNumber(M market, String priceStr) {
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
     * 仅仅支持在 Amazon 上更新 Listing 的时候价格的解析
     * 主要是因为 Amazon 总变更上传的价格的格式, 所以程序需要对其进行解析判断再设值
     *
     * @param priceStr
     * @return
     */
    public static F.T2<M, Float> amazonPriceNumberAutoJudgeFormat(String priceStr, M defaultMarket) {
        if(StringUtils.isBlank(priceStr)) return new F.T2<M, Float>(defaultMarket, 999f);
        StringBuilder sbd = new StringBuilder(priceStr);
        String dot = Character.toString(sbd.charAt(sbd.length() - 3));
        if(dot.equals(".")) { // uk/us 格式
            return new F.T2<M, Float>(M.AMAZON_UK, Webs.amazonPriceNumber(M.AMAZON_UK, priceStr));
        } else if(dot.equals(",")) { // de 格式
            return new F.T2<M, Float>(M.AMAZON_DE, Webs.amazonPriceNumber(M.AMAZON_DE, priceStr));
        } else {
            Logger.error("Not support price format.");
            return new F.T2<M, Float>(defaultMarket, 999f);
        }
    }

    /**
     * 将价格转换成不同市场格式的价格,例如: 1999.99 , uk: 1,999.99; de: 1.999,99 ...
     *
     * @param market
     * @param price
     * @return
     */
    public static String priceLocalNumberFormat(M market, Float price) {
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
    public static Float amazonPriceCurrency(M market, String priceStr) {
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
    public static String priceLocalCurrencyFormat(M market, Float price) {
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
    public static <E extends Throwable> String E(E e) {
        if(Play.mode.isDev()) e.printStackTrace();
        return e.getMessage() + "|" + e.getClass().getSimpleName();
    }

    /**
     * 直接把 Exception 的堆栈信息全部打印出来
     *
     * @param e
     * @return
     */
    public static <E extends Throwable> String S(E e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        // 便与前台查看异常
        return StringUtils.replace(sw.toString(), "\\n", "<br/>");
    }

    /**
     * 将产生的 Play Error 替换成字符串形式
     *
     * @param errors
     * @return
     */
    public static String V(List<play.data.validation.Error> errors) {
        StringBuilder sbd = new StringBuilder();
        for(Error err : errors)
            sbd.append(err.getKey()).append("=>").append(err.message()).append("<br>");
        return sbd.toString();
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
