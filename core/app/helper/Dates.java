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
     * 中国时区
     */
    public static DateTimeZone CN = DateTimeZone.forID("Asia/Shanghai");

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
        try {
            return df.newXMLGregorianCalendar(expression).toGregorianCalendar().getTime();
        } catch(Exception e) {
            return new Date();
        }
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
        return new Date(
                date2JDate(new DateTime(date.getTime()).plusDays(1).toDate()).getTime() - 1000);
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

    /**
     * 默认时区为 UTC 时区
     *
     * @param market
     * @return
     */
    public static DateTimeZone timeZone(M market) {
        if(market == null) return DateTimeZone.UTC;
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
                return DateTimeZone.UTC;
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
                return DateTime.parse(dateStr,
                        DateTimeFormat.forPattern("dd/MM/yyyy").withZone(Dates.timeZone(m)))
                        .toDate();
            case AMAZON_DE:
                return DateTime.parse(dateStr,
                        DateTimeFormat.forPattern("dd.MM.yyyy").withZone(Dates.timeZone(m)))
                        .toDate();
            case AMAZON_US:
                return DateTime.parse(dateStr,
                        DateTimeFormat.forPattern("MM/dd/yyyy").withZone(Dates.timeZone(m)))
                        .toDate();
            default:
                return DateTime.parse(dateStr,
                        DateTimeFormat.forPattern("dd/MM/yyyy").withZone(Dates.timeZone(m)))
                        .toDate();
        }
    }

    public static Date transactionDate(M m, String dateStr) {
        switch(m) {
            case AMAZON_UK:
            case AMAZON_FR:
            case AMAZON_DE:
                return DateTime.parse(dateStr,
                        DateTimeFormat.forPattern("dd MMM yyyy").withZone(Dates.timeZone(m)))
                        .toDate();
            case AMAZON_US:
                return DateTime.parse(dateStr,
                        DateTimeFormat.forPattern("MMM dd, yyyy").withZone(Dates.timeZone(m)))
                        .toDate();
            default:
                return DateTime.parse(dateStr,
                        DateTimeFormat.forPattern("dd/MM/yyyy").withZone(Dates.timeZone(m)))
                        .toDate();
        }
    }

    /**
     * 解析 yyyy-MM-dd HH:mm:ss 格式的字符串成为 Datetime(with timezone)
     *
     * @param str
     * @param market
     * @return
     */
    public static DateTime fromDatetime(String str, M market) {
        return DateTime.parse(str,
                DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(Dates.timeZone(market)));
    }

    /**
     * 解析 yyyy-MM-dd 格式的字符串成为 Datetime(with timezone)
     *
     * @param str
     * @param market
     * @return
     */
    public static DateTime fromDate(String str, M market) {
        return DateTime.parse(str,
                DateTimeFormat.forPattern("yyyy-MM-dd").withZone(Dates.timeZone(market)));
    }

    public static DateTime cn(String time) {
        //yyyy-MM-dd HH:mm:ss
        if(time.contains(":")) {
            return DateTime.parse(time,
                    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(CN));
        } else {
            return DateTime.parse(time, DateTimeFormat.forPattern("yyyy-MM-dd").withZone(CN));
        }
    }

    public static DateTime cn(Date time) {
        return cn(Dates.date2DateTime(time));
    }
}
