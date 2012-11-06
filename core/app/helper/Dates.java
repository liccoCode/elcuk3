package helper;

import models.market.M;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/25/12
 * Time: 2:45 PM
 */
public class Dates {
    private static DatatypeFactory df;

    /**
     * 一天拥有 86400 秒
     */
    public static final long DAY_SECONDS = 86400;

    /**
     * 一天拥有 86400000 毫秒
     */
    public static final long DAY_MILLIS = DAY_SECONDS * 1000;

    static {
        try {
            df = DatatypeFactory.newInstance();
        } catch(DatatypeConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static Date parseXMLGregorianDate(String expression) {
        return df.newXMLGregorianCalendar(expression).toGregorianCalendar().getTime();
    }

    /**
     * 返回一个 Date 日期这一天的开始
     *
     * @param date
     * @return
     */
    public static Date morning(Date date) {
        return date2JDate(date);
    }

    /**
     * 返回一个 Date 日期这一天的最后
     *
     * @param date
     * @return
     */
    public static Date night(Date date) {
        return new Date(date2JDate(new DateTime(date.getTime()).plusDays(1).toDate()).getTime() - 1000);
    }

    /**
     * 舍弃掉 Date 后面的 时,分,秒
     *
     * @param date
     * @return
     */
    public static Date date2JDate(Date date) {
        Date tmp = date;
        if(tmp == null) tmp = new Date();
        return DateTime.parse(new DateTime(tmp).toString("yyyy-MM-dd")).toDate();
    }

    public static String date2Date() {
        return date2Date(null);
    }

    public static String date2Date(Date date) {
        if(date == null) return DateTime.now().toString("yyyy-MM-dd");
        else return new DateTime(date).toString("yyyy-MM-dd");
    }

    public static String date2DateTime() {
        return date2DateTime(null);
    }

    public static String date2DateTime(Date date) {
        if(date == null) {
            return DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
        } else {
            return new DateTime(date).toString("yyyy-MM-dd HH:mm:ss");
        }
    }

    public static DateTimeZone timeZone(M market) {
        if(market == null) return DateTimeZone.forID("Asia/Shanghai");
        switch(market) {
            case AMAZON_UK:
            case EBAY_UK:
                return DateTimeZone.forID("Europe/London");
            case AMAZON_DE:
                return DateTimeZone.forID("Europe/Berlin");
            case AMAZON_IT:
                return DateTimeZone.forID("Europe/Rome");
            case AMAZON_FR:
                return DateTimeZone.forID("Europe/Paris");
            case AMAZON_ES:
                return DateTimeZone.forID("Europe/Madrid");
            case AMAZON_US:
                return DateTimeZone.forID("America/Los_Angeles");
            default:
                return DateTimeZone.forID("Asia/Shanghai");
        }
    }

    /**
     * 在进行 Listing 更新的时候, 日期的格式在不同的市场上不同, 这个方法来进行修正格式
     *
     * @param m
     * @param date
     * @return
     */
    public static String listingUpdateFmt(M m, Date date) {
        switch(m) {
            case AMAZON_UK:
            case AMAZON_FR:
                return new DateTime(date).toString("dd/MM/yyyy");
            case AMAZON_DE:
                return new DateTime(date).toString("dd.MM.yyyy");
            case AMAZON_US:
                return new DateTime(date).toString("MM/dd/yyyy");
            default:
                return new DateTime(date).toString("dd/MM/yyyy");
        }
    }

    public static Date listingFromFmt(M m, String dateStr) {
        switch(m) {
            case AMAZON_UK:
            case AMAZON_FR:
                return DateTime.parse(dateStr, DateTimeFormat.forPattern("dd/MM/yyyy")).toDate();
            case AMAZON_DE:
                return DateTime.parse(dateStr, DateTimeFormat.forPattern("dd.MM.yyyy")).toDate();
            case AMAZON_US:
                return DateTime.parse(dateStr, DateTimeFormat.forPattern("MM/dd/yyyy")).toDate();
            default:
                return DateTime.parse(dateStr, DateTimeFormat.forPattern("dd/MM/yyyy")).toDate();
        }
    }
}
