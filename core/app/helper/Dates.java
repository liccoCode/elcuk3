package helper;

import models.market.M;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import play.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/25/12
 * Time: 2:45 PM
 */
public class Dates {

    private Dates() {
    }

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
            Logger.error(Webs.s(e));
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
     * 返回一个 Date 日期这一天的开始, 2016-01-01 00:00:00
     *
     * @param date
     * @return
     */
    public static Date morning(Date date) {
        return new DateTime(date).withTimeAtStartOfDay().toDate();
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
        return new DateTime(tmp).withTimeAtStartOfDay().toDate();
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
            case AMAZON_MX:
                return DateTimeZone.forOffsetHours(-6);
            case AMAZON_US:
                return DateTimeZone.forOffsetHours(-7);
            case AMAZON_CA:
                return DateTimeZone.forOffsetHours(-8);
            case AMAZON_DE:
            case AMAZON_IT:
            case AMAZON_FR:
            case AMAZON_ES:
                return DateTimeZone.forOffsetHours(1);
            case AMAZON_UK:
            case EBAY_UK:
                return DateTimeZone.forOffsetHours(0);
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
            case AMAZON_JP:
                return new DateTime(date).toString("dd/MM/yyyy");
            default:
                return new DateTime(date).toString("dd/MM/yyyy");
        }
    }

    public static Date listingFromFmt(String dateStr) {
        return DateTime.parse(dateStr).toDate();
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
            case AMAZON_JP:
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
        return DateTime.parse(str, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(Dates.timeZone(market)));
    }

    /**
     * 解析 yyyy-MM-dd 格式的字符串成为 Datetime(with timezone)
     *
     * @param str
     * @param market
     * @return
     */
    public static DateTime fromDate(String str, M market) {
        return DateTime.parse(str, DateTimeFormat.forPattern("yyyy-MM-dd").withZone(Dates.timeZone(market)));
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

    public static Date beginOfYear() {
        int year = DateTime.now().getYear();
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
        return date.withDayOfYear(calendar.getActualMaximum(Calendar.DAY_OF_YEAR)).toDate();
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

    /**
     * 获取当前时间的星期天时间
     *
     * @return
     */
    public static Date getSundayOfWeek() {
        return Dates.getSundayOfSpecifyTime(DateTime.now().toDate());
    }

    public static Date getMondayOfSpecifyTime(Date time) {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setTime(time);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return calendar.getTime();
    }

    public static Date getSundayOfSpecifyTime(Date time) {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setTime(time);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
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
        DateTime dateTime = DateTime.now().withMonthOfYear(month);
        return monthBegin(dateTime.toDate());
    }

    /**
     * 获取当年某月最后一天
     *
     * @return
     */
    public static Date getMonthLast(int month) {
        DateTime dateTime = DateTime.now().withMonthOfYear(month);
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
     *
     * @param year
     * @param month
     * @return
     */
    public static Date getMonthLast(int year, int month) {
        DateTime date = DateTime.now().withYear(year).withMonthOfYear(month);
        return monthEnd(date.toDate());
    }

    public static String scaleNumber(double number) {
        java.text.DecimalFormat format = new java.text.DecimalFormat("0.00");
        format.setRoundingMode(java.math.RoundingMode.HALF_UP);
        return format.format(number);
    }

    /**
     * 获取给定时间范围内每个完整的一周的周末
     *
     * @param begin
     * @param end
     */
    public static List<Date> getAllSunday(Date begin, Date end) {
        Date _begin = begin;
        Date _end = end;
        //日期数据的容错处理
        if(Dates.getSundayOfSpecifyTime(begin).getTime() != _begin.getTime()) {
            //开始日期不是星期日(end 无法构成完整的一周，所以加上 6 天重新计算) 之所以加上 6 天而不是加上 7 天是为了防止 begin 是星期一,加上 6 天还在当前星期内
            DateTime nextWeek = new DateTime(begin).plusDays(6);
            _begin = Dates.getSundayOfSpecifyTime(nextWeek.toDate());
        }
        if(Dates.getSundayOfSpecifyTime(end).getTime() != _end.getTime()) {
            //结束日期不是星期日(end 无法构成完整的一周，所以减去 6 天重新计算)
            DateTime lastWeek = new DateTime(_end).minusDays(6);
            _end = Dates.getSundayOfSpecifyTime(lastWeek.toDate());
        }
        Calendar c = Calendar.getInstance();
        List<Date> dates = new ArrayList<>();
        while(_begin.getTime() <= _end.getTime()) {
            dates.add(Dates.night(_begin));
            c.setTime(_begin);
            c.add(Calendar.DATE, 7); // 每次日期加7天
            _begin = c.getTime();
        }
        return dates;
    }

    /**
     * 获取给定时间范围内每个完整的一周的周一
     *
     * @param begin
     * @param end
     */
    public static List<Date> getAllMonday(Date begin, Date end) {
        Date _begin = begin;
        Date _end = end;
        //日期数据的容错处理
        if(Dates.getMondayOfSpecifyTime(_begin).getTime() != _begin.getTime()) {
            //开始日期不是星期一(end 无法构成完整的一周，所以加上 6 天重新计算)
            DateTime nextWeek = new DateTime(_begin).plusDays(6);
            _begin = Dates.getMondayOfSpecifyTime(nextWeek.toDate());
        }
        if(Dates.getMondayOfSpecifyTime(_end).getTime() != _end.getTime()) {
            //开始日期不是星期一(end 无法构成完整的一周，所以往减去 6 天重新计算)
            DateTime lastWeek = new DateTime(_end).minusDays(6);
            _end = Dates.getMondayOfSpecifyTime(lastWeek.toDate());
        }
        Calendar c = Calendar.getInstance();
        List<Date> dates = new ArrayList<>();
        while(_begin.getTime() <= _end.getTime()) {
            //这里还有一个问题 当结束日期刚好是周一时,由于无法找到对应的周日(超出了结束时间)来构成完整的一周,所以需要舍弃该日期
            if(Dates.getSundayOfSpecifyTime(_begin).getTime() <= end.getTime()) {
                //获取到对应的周末需要小于或等于结束日期才是一个合法的周一
                dates.add(Dates.morning(_begin));
            }
            c.setTime(_begin);
            c.add(Calendar.DATE, 7); // 每次日期加7天
            _begin = c.getTime();
        }
        return dates;
    }

    /**
     * 获取给定时间月份的天数
     *
     * @return
     */
    public static int getDays(Date date) {
        DateTime dateTime = new DateTime(date);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, dateTime.getYear());
        cal.set(Calendar.MONTH, dateTime.getMonthOfYear() - 1);
        return cal.getActualMaximum(Calendar.DATE);
    }

    /**
     * 给定开始结束时间，按照给定的天数进行切割
     * 返回map，格式 yyyy-MM-dd, type为string
     *
     * @param begin
     * @param end
     * @param splitDay
     * @return
     */
    public static Map<String, String> splitDayForDate(Date begin, Date end, int splitDay) {
        Calendar c = Calendar.getInstance();
        Map<String, String> map = new TreeMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        int day = (int) ((end.getTime() - begin.getTime()) / (1000 * 3600 * 24));
        if(day > splitDay) {
            Date to;
            for(int i = 0; i <= Math.floor(day / (splitDay)); i++) {
                c.setTime(begin);
                if(i == Math.floor(day / (splitDay))) {
                    map.put(format.format(begin), format.format(end));
                } else {
                    c.add(Calendar.DATE, splitDay - 1);
                    to = c.getTime();
                    map.put(format.format(begin), format.format(to));
                    c.add(Calendar.DATE, 1);
                    begin = c.getTime();
                }
            }
        } else {
            map.put(format.format(begin), format.format(end));
        }
        return map;
    }

    public static Date aMonthAgo() {
        return DateTime.now().minusMonths(1).toDate();
    }

    public static Date yesterday() {
        return DateTime.now().minusDays(1).toDate();
    }
}
