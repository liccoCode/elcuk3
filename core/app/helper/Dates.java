package helper;

import models.market.Account;
import org.joda.time.DateTime;
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

    public static String date2DateTime(Date date) {
        if(date == null) {
            return DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
        } else {
            return new DateTime(date).toString("yyyy-MM-dd HH:mm:ss");
        }
    }

    /**
     * 返回一个 Date 日期这一天的开始
     *
     * @param date
     * @return
     */
    public static Date morning(Date date) {
        return data2Date(date);
    }

    /**
     * 返回一个 Date 日期这一天的最后
     *
     * @param date
     * @return
     */
    public static Date night(Date date) {
        return new Date(data2Date(new DateTime(date.getTime()).plusDays(1).toDate()).getTime() - 1000);
    }

    /**
     * 舍弃掉 Date 后面的 时,分,秒
     *
     * @param date
     * @return
     */
    public static Date data2Date(Date date) {
        Date tmp = date;
        if(tmp == null) tmp = new Date();
        return DateTime.parse(new DateTime(tmp).toString("yyyy-MM-dd")).toDate();
    }

    public static String date2Date(Date date) {
        if(date == null) return DateTime.now().toString("yyyy-MM-dd");
        else return new DateTime(date).toString("yyyy-MM-dd");
    }

    /**
     * 在进行 Listing 更新的时候, 日期的格式在不同的市场上不同, 这个方法来进行修正格式
     *
     * @param m
     * @param date
     * @return
     */
    public static String listingUpdateFmt(Account.M m, Date date) {
        switch(m) {
            case AMAZON_UK:
            case AMAZON_FR:
                return new DateTime(date).toString("dd/MM/yyyy");
            case AMAZON_DE:
                return new DateTime(date).toString("dd.MM.yyyy");
            default:
                return new DateTime(date).toString("dd/MM/yyyy");
        }
    }

    public static Date listingFromFmt(Account.M m, String dateStr) {
        switch(m) {
            case AMAZON_UK:
            case AMAZON_FR:
                return DateTime.parse(dateStr, DateTimeFormat.forPattern("dd/MM/yyyy")).toDate();
            case AMAZON_DE:
                return DateTime.parse(dateStr, DateTimeFormat.forPattern("dd.MM.yyyy")).toDate();
            default:
                return DateTime.parse(dateStr, DateTimeFormat.forPattern("dd/MM/yyyy")).toDate();
        }
    }
}
