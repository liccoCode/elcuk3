package ext;

import models.finance.SaleFee;
import play.templates.JavaExtensions;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 12/27/16
 * Time: 3:03 PM
 */
public class SaleFeeHelper extends JavaExtensions {
    public static float invoiceCost(SaleFee fee, Float rate) {
        return new BigDecimal(fee.cost)
                .divide(new BigDecimal(rate), 2, java.math.RoundingMode.HALF_DOWN)
                .floatValue();
    }

    public static float invoiceScaleCost(SaleFee fee, Float rate) {
        return new BigDecimal(fee.cost)
                .divide(new BigDecimal(rate), 2, java.math.RoundingMode.HALF_DOWN)
                .setScale(2, 4)
                .floatValue();
    }
}
