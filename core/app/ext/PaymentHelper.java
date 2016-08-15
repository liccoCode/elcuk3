package ext;

import models.finance.FeeType;
import models.finance.Payment;
import models.finance.PaymentUnit;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import org.joda.time.DateTime;
import play.templates.JavaExtensions;
import query.PaymentUnitQuery;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 3/11/13
 * Time: 4:56 PM
 */
public class PaymentHelper extends JavaExtensions {
    public static String stateColor(PaymentUnit punit) {
        return stateColor(punit.state);
    }

    public static String stateColor(PaymentUnit.S state) {
        switch(state) {
            case APPLY:
                return "#333333";
            case DENY:
                return "#BA4A48";
            case APPROVAL:
                return "#3A87AD";
            case PAID:
                return "#4B8644";
            default:
                return "#999999";
        }
    }

    public static String stateColor(Payment payment) {
        switch(payment.state) {
            case PAID:
                return "#468847";
            case CANCEL:
                return "#999999";
            case WAITING:
            default:
                return "#F8941D";
        }
    }

    public static String badgeInfo(ProcureUnit unit) {
        int size = unit.fees.size();
        if(size >= 2) {
            return "badge-warning";
        } else if(size > 0) {
            return "badge-info";
        } else {
            return "";
        }
    }

    public static String stateLabel(PaymentUnit unit) {
        switch(unit.state) {
            case APPLY:
                return "label-inverse";
            case DENY:
                return "label-important";
            case APPROVAL:
                return "label-info";
            case PAID:
                return "label-success";
            default:
                return "";
        }
    }

    /**
     * 页面上 Payment 是否可以取消
     *
     * @param payment
     * @return
     */
    public static boolean cancelable(Payment payment) {
        return !(payment.state == Payment.S.CANCEL || payment.state == Payment.S.PAID);
    }

    /**
     * 运输费用的均价, 没有运输项目. 统一币种为 CNY 则为单价(unitPrice)
     *
     * @return
     */
    public static float averagePrice(PaymentUnit unit) {
        if(unit.feeType != FeeType.expressFee()) return unit.currency.toCNY(unit.unitPrice);
        if(unit.shipment != null) {
            Shipment.T shipType = unit.shipment.type;
            PaymentUnitQuery aveFeeQuery = new PaymentUnitQuery();
            DateTime now = DateTime.now();
            DateTime threeMonthAgo = now.minusMonths(3);

            if(unit.shipItem != null && shipType == Shipment.T.EXPRESS) {
                String sku = unit.shipItem.unit().sku;
                Float price = aveFeeQuery.avgSkuExpressTransportshippingFee(threeMonthAgo.toDate(), now.toDate(), sku)
                        .get(sku);
                return price == null ? 0 : price;
            } else if(shipType == Shipment.T.SEA) {
                return aveFeeQuery.avgSkuSEATransportshippingFee(threeMonthAgo.toDate(), now.toDate());
            } else if(shipType == Shipment.T.AIR) {
                return aveFeeQuery.avgSkuAIRTransportshippingFee(threeMonthAgo.toDate(), now.toDate());
            } else {
                return unit.currency.toCNY(unit.unitPrice);
            }
        }
        return unit.currency.toCNY(unit.unitPrice);
    }

    public static float currentAvgPrice(PaymentUnit unit) {
        if(unit.feeType != FeeType.expressFee()) return unit.currency.toCNY(unit.unitPrice);
        if(unit.shipment != null) {
            Shipment.T shipType = unit.shipment.type;
            DateTime now = DateTime.now();

            if(unit.shipItem != null && shipType == Shipment.T.EXPRESS) {
                return unit.shipItem.qty == 0 ? 0 : (unit.currency.toCNY(unit.amount()) /
                        unit.shipItem.unit().realQty());
            } else if(Arrays.asList(Shipment.T.AIR, Shipment.T.SEA).contains(shipType)) {
                // 总运输费用平摊到所有产品
                int sumQty = 0;
                for(ShipItem itm : unit.shipment.items) sumQty += itm.qty;
                return sumQty == 0 ? 0 : (unit.currency.toCNY(unit.amount()) / sumQty);
            } else {
                return unit.currency.toCNY(unit.unitPrice);
            }
        }
        return unit.currency.toCNY(unit.unitPrice);
    }
}
