package ext;

import helper.Dates;
import models.ElcukRecord;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.i18n.Messages;
import play.templates.JavaExtensions;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 5/16/12
 * Time: 4:24 PM
 */
//BEGIN GENERATED CODE
public class vExtensions extends JavaExtensions {

    /**
     * 中国时区
     *
     * @return
     */
    public static String CN(Date date) {
        return new DateTime(date, Dates.CN).toLocalDateTime().toString();
    }

    /*util.Date*/
    public static String datetime(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public static String date(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    /*sql.Timestamp*/
    public static String datetime(Timestamp date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public static String date(Timestamp date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public static String date(DateTime date) {
        return date.toString("yyyy-MM-dd");
    }
    public static String datetime(DateTime date) {
        return date.toString("yyyy-MM-dd HH:mm:ss");
    }

    public static String dayAfter(int day) {
        return DateTime.now().plusDays(day).toString("yyyy-MM-dd");
    }

    public static String dayOfWeek(Date date) {
        DateTime dt = new DateTime(date);
        return dt.dayOfWeek().getAsText(Locale.CHINA);
    }

    /**
     * 传入的日期与现在的日期之间相差多久?
     *
     * @param date
     * @return
     */
    public static String left(Date date) {
        return left(date, false);
    }

    /**
     * 传入的日期与现在的日期之间相差多久?
     *
     * @param date
     * @return
     */
    public static String left(Date date, Boolean stopAtMonth) {
        Date now = new Date();
        if(now.after(date)) {
            return "";
        }
        long delta = (date.getTime() - now.getTime()) / 1000;
        if(delta < 60) {
            return Messages.get("left.seconds", delta, pluralize(delta));
        }
        if(delta < 60 * 60) {
            long minutes = delta / 60;
            return Messages.get("left.minutes", minutes, pluralize(minutes));
        }
        if(delta < 24 * 60 * 60) {
            long hours = delta / (60 * 60);
            return Messages.get("left.hours", hours, pluralize(hours));
        }
        if(delta < 30 * 24 * 60 * 60) {
            long days = delta / (24 * 60 * 60);
            return Messages.get("left.days", days, pluralize(days));
        }
        if(stopAtMonth) {
            return asdate(date.getTime(), Messages.get("since.format"));
        }
        if(delta < 365 * 24 * 60 * 60) {
            long months = delta / (30 * 24 * 60 * 60);
            return Messages.get("left.months", months, pluralize(months));
        }
        long years = delta / (365 * 24 * 60 * 60);
        return Messages.get("left.years", years, pluralize(years));
    }

    public static String los(Date date) {
        return los(date, false);
    }

    public static String los(Date date, Boolean stopAtMonth) {
        if(date.getTime() > System.currentTimeMillis())
            return vExtensions.left(date, stopAtMonth);
        else
            return JavaExtensions.since(date, stopAtMonth);
    }

    public static String record(ElcukRecord record) {
        // [Who] do [What] effect [What]
        return String.format("%s do %s at %s <strong style='color:red;'>=></strong> %s",
                JavaExtensions.capFirst(record.username), record.action,
                Dates.date2DateTime(record.createAt), record.message);
    }

    /**
     * 截取字符串的一部分
     *
     * @param str
     * @param size 从多长开始截取
     * @return
     */
    public static String omis(String str, Integer size) {
        if(StringUtils.isBlank(str)) return "";
        if(str.length() > size) return String.format("%s...", str.substring(0, size));
        else return str;
    }
}
