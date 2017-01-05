package ext;

import models.market.OrderItem;
import play.templates.JavaExtensions;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 12/27/16
 * Time: 2:43 PM
 */
public class OrderItemHelper extends JavaExtensions {
    public static float invoiceTotalPrice(OrderItem item) {
        if(item.quantity > 0) {
            return new BigDecimal(item.price - item.discountPrice)
                    .divide(new BigDecimal(item.quantity), 2, 4)
                    .floatValue();
        } else {
            return 0;
        }
    }

    public static float invoiceTotalPrice(OrderItem item, Float price) {
        return new BigDecimal(item.quantity)
                .multiply(new BigDecimal(price))
                .setScale(2, 4)
                .floatValue();
    }

    public static float invoiceTotalPriceWithRate(OrderItem item, Float rate) {
        return new BigDecimal(item.quantity)
                .multiply(new BigDecimal(item.price - item.discountPrice)
                        .divide(new BigDecimal(item.quantity), 2, 4)
                        .divide(new BigDecimal(rate), 2, RoundingMode.HALF_DOWN))
                .setScale(2, 4)
                .floatValue();
    }

    public static float invoicePrice(OrderItem item) {
        return new BigDecimal(item.price - item.discountPrice).setScale(2, 4).floatValue();
    }

    public static float invoicePrice(OrderItem item, Float rate) {
        return new BigDecimal(item.price - item.discountPrice)
                .divide(new BigDecimal(item.quantity), 2, 4)
                .divide(new BigDecimal(rate), 2, RoundingMode.HALF_DOWN)
                .floatValue();
    }
}
