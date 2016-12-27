package ext;

import models.market.OrderInvoice;
import org.apache.commons.lang3.StringUtils;
import play.templates.JavaExtensions;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 12/27/16
 * Time: 2:32 PM
 */
public class OrderInvoiceHelper extends JavaExtensions {
    public static String formatStr(OrderInvoice invoice, String str) {
        if(StringUtils.isNotBlank(str)) {
            return str.replace("DE", "Deutschland");
        } else {
            return StringUtils.EMPTY;
        }
    }
}
