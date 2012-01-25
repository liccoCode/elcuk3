package helper;

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
}
