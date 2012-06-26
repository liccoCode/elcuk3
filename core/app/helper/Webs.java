package helper;

import com.alibaba.fastjson.JSON;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import models.market.Account;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import play.Logger;
import play.Play;
import play.libs.F;
import play.libs.Mail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
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
     * 与 OsTicket 系统中的 Topic 对应的
     */
    public enum TopicID {
        BILLING {
            @Override
            public int id() {
                return 2;
            }
        },
        SUPPORT {
            @Override
            public int id() {
                return 1;
            }
        },
        REVIEW {
            @Override
            public int id() {
                return 3;
            }
        },
        FEEDBACK {
            @Override
            public int id() {
                return 4;
            }
        };

        public abstract int id();
    }

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
     * 向 OsTicket 系统开一个新的 Ticket.
     *
     * @param name     Ticket 的用户名称
     * @param email    Ticket 回复的邮箱
     * @param subject  Ticket 的标题
     * @param content  Ticket 的内容
     * @param topicId  Ticket 所处的 Topic, Topic 会有对应的优先级(1:Support, 2:Billing, 3:Review, 4:Feedback)
     * @param errorMsg 系统中需要 log 的错误信息,主要记录 orderid, reviewid 等这样的信息
     * @return
     */
    public static String openOsTicket(String name, String email, String subject, String content, TopicID topicId, String errorMsg) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("phone", ""));
        params.add(new BasicNameValuePair("phone_ext", ""));
        // 如果 Topicid 不再 1~4 之间则默认使用 1(Support)
        params.add(new BasicNameValuePair("topicId", topicId.id() + ""));
        params.add(new BasicNameValuePair("submit_x", "Submit Ticket"));
        params.add(new BasicNameValuePair("subject", subject));
        params.add(new BasicNameValuePair("message", content));

        try {
            JsonElement jsonel = HTTP.postJson(Constant.OS_TICKET_NEW_TICKET, params);
            JsonObject obj = jsonel.getAsJsonObject();
            if(obj == null) {
                Logger.error("OpenOsTicket fetch content Error!");
                return "";
            }
            if(obj.get("flag").getAsBoolean()) { // 成功创建
                return obj.get("tid").getAsString();
            } else {
                Logger.warn(String.format("%s post to OsTicket failed because of [%s]", errorMsg, obj.get("message").getAsString()));
            }
        } catch(Exception e) {
            Logger.error("OpenOsTicket fetch IO Error!");
        }
        return "";
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
     * 仅仅支持在 Amazon 上更新 Listing 的时候价格的解析
     * 主要是因为 Amazon 总变更上传的价格的格式, 所以程序需要对其进行解析判断再设值
     *
     * @param priceStr
     * @return
     */
    public static F.T2<Account.M, Float> amazonPriceNumberAutoJudgeFormat(String priceStr, Account.M defaultMarket) {
        if(StringUtils.isBlank(priceStr)) return new F.T2<Account.M, Float>(defaultMarket, 999f);
        StringBuilder sbd = new StringBuilder(priceStr);
        String dot = Character.toString(sbd.charAt(sbd.length() - 3));
        if(dot.equals(".")) { // uk 格式
            return new F.T2<Account.M, Float>(Account.M.AMAZON_UK, Webs.amazonPriceNumber(Account.M.AMAZON_UK, priceStr));
        } else if(dot.equals(",")) { // de 格式
            return new F.T2<Account.M, Float>(Account.M.AMAZON_DE, Webs.amazonPriceNumber(Account.M.AMAZON_DE, priceStr));
        } else {
            Logger.error("Not support price format.");
            return new F.T2<Account.M, Float>(defaultMarket, 999f);
        }
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
        return sw.toString();
    }

    /**
     * 按照(Gson) @Expose 输出 JSON 格式
     *
     * @param o
     * @return
     */
    public static String G(Object o) {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(o);
    }

    /**
     * 利用 FastJSON 进行的解决了循环依赖的 toJson 的方法
     *
     * @return
     */
    public static String json(Object o) {
        return JSON.toJSONString(o);
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
