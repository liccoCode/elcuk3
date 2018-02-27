package models.finance;

import com.amazonservices.mws.finances.model.*;
import helper.Currency;
import helper.Webs;
import models.market.Account;
import models.market.EbayOrder;
import models.market.M;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.db.jpa.Model;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/9/6
 * Time: 上午10:50
 */
@Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class EbayFee extends Model {

    private static final long serialVersionUID = 3711076569362233280L;

    public EbayFee() {
    }

    @OneToOne
    public Account account;

    @OneToOne(fetch = FetchType.LAZY)
    public EbayOrder order;

    @OneToOne
    public FeeType type;

    /**
     * 费用的状态
     */
    @Enumerated(EnumType.STRING)
    public M market;

    /**
     * 这项费用一个寄存临时信息的地方
     */
    public String memo;

    /**
     * 一个冗余字段, 用来标识一些在 Orderr 中找不到的订单, 用来处理新旧 SaleFee 更替的.
     */
    public String orderId;

    public Date date;

    /**
     * 费用, 系统内的费用使用 USD 结算
     */
    @Column(nullable = false)
    public Float cost;

    @Enumerated(EnumType.STRING)
    public Currency currency;

    /**
     * 最终统计成 USD 的价格
     */
    public Float usdCost;

    public Integer qty = 1;

    public String product_sku;

    public String transaction_type;

    public String md5_id;

    public static boolean parseFinancesApiResult(ListFinancialEventsResponse response, EbayOrder order) {
        ListFinancialEventsResult result = response.getListFinancialEventsResult();
        FinancialEvents financialEvents = result.getFinancialEvents();
        if(financialEvents.getShipmentEventList().size() > 0) {
            List<ShipmentEvent> events = financialEvents.getShipmentEventList();
            events.forEach(event -> {
                event.getShipmentFeeList().forEach(fee -> EbayFee.saveShipmentFeeForApi(fee, event, order));
                event.getShipmentItemList().forEach(item -> item.getItemFeeList().forEach(
                        component -> EbayFee.saveItemFeeForApi(component, event, item, order)));
            });
        }
        return true;
    }


    private static void saveItemFeeForApi(FeeComponent component, ShipmentEvent event, ShipmentItem item,
                                          EbayOrder order) {
        if(component.getFeeAmount().getCurrencyAmount().floatValue() != 0) {
            EbayFee fee = new EbayFee();
            fee.type = FeeType.findById(FeeType.mappingTypeName(component.getFeeType().toLowerCase()));
            fee.currency = Currency.valueOf(component.getFeeAmount().getCurrencyCode());
            fee.cost = component.getFeeAmount().getCurrencyAmount().floatValue();
            EbayFee.commonField(fee, event, item, order);
        }
    }

    private static void saveShipmentFeeForApi(FeeComponent component, ShipmentEvent event, EbayOrder order) {
        if(component.getFeeAmount().getCurrencyAmount().floatValue() != 0) {
            EbayFee fee = new EbayFee();
            fee.type = FeeType.findById(FeeType.mappingTypeName(component.getFeeType().toLowerCase()));
            fee.currency = Currency.valueOf(component.getFeeAmount().getCurrencyCode());
            fee.cost = component.getFeeAmount().getCurrencyAmount().floatValue();
            EbayFee.commonField(fee, event, null, order);
        }
    }

    private static void commonField(EbayFee fee, ShipmentEvent event, ShipmentItem item, EbayOrder order) {
        fee.usdCost = fee.currency.toUSD(fee.cost);
        fee.market = order.market;
        fee.memo = "ERP重新抓取Ebay费用 API";
        fee.orderId = event.getAmazonOrderId();
        fee.order = EbayOrder.findById(fee.orderId);
        fee.qty = item != null ? item.getQuantityShipped() : 1;
        fee.date = event.getPostedDate().toGregorianCalendar().getTime();
        fee.transaction_type = "Order";
        fee.product_sku = item != null ? item.getSellerSKU().split(",")[0] : null;
        fee.account = order.account;
        Map<String, Object> map = new HashMap<>();
        map.put("order_orderId", fee.orderId);
        map.put("account_id", fee.account.id);
        map.put("market", fee.market.name());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        map.put("date", dateFormat.format(fee.date));
        map.put("cost", fee.cost.toString());
        map.put("currency", fee.currency.name());
        map.put("type_name", fee.type.name.toLowerCase());
        if(fee.product_sku != null) {
            map.put("product_sku", fee.product_sku);
        }
        map.put("transaction_type", fee.transaction_type);
        fee.md5_id = Webs.md5ForSaleFee(map);
        fee.save();
    }

}
