package ext;

import models.market.Orderr;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import play.templates.JavaExtensions;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 12/27/16
 * Time: 2:20 PM
 */
public class OrderHelper extends JavaExtensions {
    public static String euDate(Orderr order) {
        return new DateTime(order.createDate).toString("dd/MM/yyyy");
    }

    /**
     * reciver + address1
     *
     * @param order
     * @return
     */
    public static String formatAddress1(Orderr order) {
        if(StringUtils.isNotBlank(order.address1)) {
            return String.format("%s,%s", order.reciver, order.address1.replace("DE", "Deutschland"));
        }
        return StringUtils.EMPTY;
    }

    public static String formatSku(String temp) {
        String[] sku = temp.split(";");
        if(sku.length == 1) {
            return temp;
        } else {
            return sku[0] + ";  ...";
        }
    }
}
