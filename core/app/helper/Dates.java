package helper;

import models.market.M;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
     * 返回一个 Date 日期这一天的最后(也就是第二天的最开始)
     *
     * @param date
     * @return
     */
    public static Date night(Date date) {
        // 为了不让时间到达第二天
        return new DateTime(date).plusDays(1).withTimeAtStartOfDay().minusMillis(1).toDate();
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
        return new DateTime(date).withTimeAtStartOfDay().toDate();
    }

    public static String date2Date() {
        return date2Date(DateTime.now());
    }

    public static String date2Date(Date date) {
        if(date == null) date = new Date();
        return date2Date(new DateTime(date));
    }

    public static String date2Date(DateTime date) {
        if(date == null) {
            return DateTime.now().toString("");
        } else {
            return date.toString("yyyy-MM-dd");
        }
    }

    public static String date2DateTime() {
        return date2DateTime(DateTime.now());
    }

    public static String date2DateTime(Date date) {
        if(date == null) date = new Date();
        return date2DateTime(new DateTime(date));
    }

    public static String date2DateTime(DateTime date) {
        if(date == null) {
            return DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
        } else {
            return date.toString("yyyy-MM-dd HH:mm:ss");
        }
    }

    /**
     * 默认时区为 UTC 时区
     *
     * @param market
     * @return
     */
    public static DateTimeZone timeZone(M market) {
        if(market == null) return Dates.CN;
        switch(market) {
            case AMAZON_UK:
            case EBAY_UK:
                return DateTimeZone.forOffsetHours(0);
            case AMAZON_DE:
            case AMAZON_IT:
            case AMAZON_FR:
            case AMAZON_ES:
                return DateTimeZone.forOffsetHours(1);
            case AMAZON_US:
                return DateTimeZone.forOffsetHours(-7);
            default:
                return Dates.CN;
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
            case AMAZON_ES:
            case AMAZON_IT:
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
            case AMAZON_ES:
            case AMAZON_IT:
                return DateTime.parse(dateStr,
                        DateTimeFormat.forPattern("dd/MM/yyyy").withZone(Dates.timeZone(m))).toDate();
            case AMAZON_DE:
                return DateTime.parse(dateStr,
                        DateTimeFormat.forPattern("dd.MM.yyyy").withZone(Dates.timeZone(m))).toDate();
            case AMAZON_US:
                return DateTime.parse(dateStr,
                        DateTimeFormat.forPattern("MM/dd/yyyy").withZone(Dates.timeZone(m))).toDate();
            default:
                return DateTime.parse(dateStr,
                        DateTimeFormat.forPattern("dd/MM/yyyy").withZone(Dates.timeZone(m))).toDate();
        }
    }

    public static Date transactionDate(M m, String dateStr) {
        switch(m) {
            case AMAZON_UK:
            case AMAZON_FR:
            case AMAZON_DE:
            case AMAZON_IT:
            case AMAZON_ES:
                return DateTime.parse(dateStr,
                        DateTimeFormat.forPattern("dd MMM yyyy").withZone(Dates.timeZone(m))).toDate();
            case AMAZON_US:
                return DateTime.parse(dateStr,
                        DateTimeFormat.forPattern("MMM dd, yyyy").withZone(Dates.timeZone(m))).toDate();
            default:
                return DateTime.parse(dateStr,
                        DateTimeFormat.forPattern("dd/MM/yyyy").withZone(Dates.timeZone(m))).toDate();
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


    /**
     * 返回年的第一天
     *
     * @param year
     * @return
     */
    public static Date startDayYear(int year) {
        return DateTime.now().withYear(year).withDayOfYear(1).toDate();

    }

    /**
     * 返回年的最后一天
     *
     * @param year
     * @return
     */
    public static Date endDayYear(int year) {
        DateTime date = DateTime.now().withYear(year);
        //年的最后一天
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date.toDate());
        return date.withDayOfYear(calendar.getActualMaximum(Calendar.DAY_OF_YEAR)).toDate
                ();
    }

    /**
     * 获取当前时间的星期一时间
     *
     * @return
     */
    public static Date getMondayOfWeek() {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        //设置一周起始日期为星期一
        calendar.setFirstDayOfWeek(1);
        //设置格式
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        //获取当前周的星期一
        return calendar.getTime();
    }

    public static Date monthBegin(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.set(Calendar.DATE, 1);
        return calendar.getTime();
    }

    public static Date monthEnd(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.set(Calendar.DATE, 1);//设置为当前月1号
        calendar.add(Calendar.MONTH, 1);//加一个月变成下一月的1号
        calendar.add(Calendar.DATE, -1);//减去一天，变成当月最后一天
        return calendar.getTime();
    }

    /**
     * 获取当年某月第一天
     *
     * @return
     */
    public static Date getMonthFirst(int month) {
        DateTime dateTime = new DateTime().now().withMonthOfYear(month);
        return monthBegin(dateTime.toDate());
    }

    /**
     * 获取当年某月最后一天
     *
     * @return
     */
    public static Date getMonthLast(int month) {
        DateTime dateTime = new DateTime().now().withMonthOfYear(month);
        return monthEnd(dateTime.toDate());
    }

    /**
     * 获取某年某月的第一天
     *
     * @param year
     * @param month
     * @return
     */
    public static Date getMonthFirst(int year, int month) {
        DateTime date = DateTime.now().withYear(year).withMonthOfYear(month);
        return monthBegin(date.toDate());
    }

    /**
     * 获取某年某月最后一天
     * @param year
     * @param month
     * @return
     */
    public static Date getMonthLast(int year, int month) {
        DateTime date = DateTime.now().withYear(year).withMonthOfYear(month);
        return monthEnd(date.toDate());
    }
}
