package helper;

import models.market.Account;
import org.joda.time.DateTime;

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
                return new DateTime(date).toString("dd/MM/yyyy");
            case AMAZON_DE:
                return new DateTime(date).toString("dd.MM.yyyy");
            default:
                return new DateTime(date).toString("dd/MM/yyyy");
        }
    }
}
