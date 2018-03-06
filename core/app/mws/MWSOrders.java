package mws;

import com.amazonservices.mws.orders.MarketplaceWebServiceOrders;
import com.amazonservices.mws.orders.MarketplaceWebServiceOrdersClient;
import com.amazonservices.mws.orders.MarketplaceWebServiceOrdersConfig;
import com.amazonservices.mws.orders.model.*;
import helper.Currency;
import models.market.*;
import models.market.OrderItem;
import models.product.Product;
import play.db.jpa.GenericModel;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 12/28/17
 * Time: 3:21 PM
 */
public class MWSOrders {

    private static final Map<String, MarketplaceWebServiceOrdersClient> cached = new HashMap<>();

    public static MarketplaceWebServiceOrdersClient client(Account account, M market) {
        String key = String.format("ProductsServiceClient_%s_%s", account.id, market.name());
        MarketplaceWebServiceOrdersClient client;
        if(cached.containsKey(key)) return cached.get(key);
        else {
            synchronized(cached) {
                if(cached.containsKey(key)) return cached.get(key);
                MarketplaceWebServiceOrdersConfig config = new MarketplaceWebServiceOrdersConfig();
                config.setServiceURL(MWSProducts.getMwsUrl(market));
                client = new MarketplaceWebServiceOrdersClient(account.accessKey, account.token,
                        "elcuk2", "1.0", config);
                cached.put(key, client);
            }
        }
        return client;
    }


    public static void invokeListOrderItems(Orderr orderr) {
        MarketplaceWebServiceOrders client = MWSOrders.client(orderr.account, orderr.market);
        String sellerId = orderr.account.merchantId;
        GetOrderRequest orderRequest = new GetOrderRequest(sellerId, Collections.singletonList(orderr.orderId));
        /*订单信息同步*/
        GetOrderResponse orderResponse = client.getOrder(orderRequest);
        List<Order> orderList = orderResponse.getGetOrderResult().getOrders();
        orderList.forEach(order -> {
            orderr.shipDate = order.getEarliestShipDate().toGregorianCalendar().getTime();
            orderr.createDate = order.getPurchaseDate().toGregorianCalendar().getTime();
            orderr.paymentDate = order.getPurchaseDate().toGregorianCalendar().getTime();
            orderr.state = Orderr.getState(order.getOrderStatus().toLowerCase());
            orderr.market = M.toM(order.getSalesChannel().toLowerCase());

            orderr.buyer = order.getBuyerName();
            if(order.getShippingAddress() != null) {
                orderr.city = order.getShippingAddress().getCity();
                orderr.postalCode = order.getShippingAddress().getPostalCode();
                orderr.province = order.getShippingAddress().getStateOrRegion();
                orderr.country = order.getShippingAddress().getCountryCode();
                orderr.reciver = order.getShippingAddress().getName();
                orderr.address = order.getShippingAddress().getAddressLine1();
                orderr.address1 = order.getShippingAddress().getAddressLine2();
            }

            orderr.email = order.getBuyerEmail();
            orderr.businessOrder = order.getIsBusinessOrder();

            orderr.memo = "重新抓取订单信息";
            if(order.getOrderTotal() != null) {
                orderr.totalAmount = Float.parseFloat(order.getOrderTotal().getAmount());
            }
            orderr.shipLevel = order.getShipServiceLevel();
            orderr.save();
        });

        /*订单项同步*/
        ListOrderItemsRequest request = new ListOrderItemsRequest(sellerId, orderr.orderId);
        request.setMWSAuthToken(orderr.account.token);
        ListOrderItemsResponse response = client.listOrderItems(request);
        ListOrderItemsResult result = response.getListOrderItemsResult();
        orderr.items.forEach(GenericModel::delete);
        result.getOrderItems().forEach(item -> {
            OrderItem orderItem = new OrderItem();
            orderItem.id = orderr.orderId + "_" + item.getSellerSKU().split(",")[0];
            orderItem.market = orderr.market;
            orderItem.createDate = orderr.paymentDate;
            orderItem.price = Float.parseFloat(item.getItemPrice().getAmount());
            orderItem.currency = Currency.valueOf(item.getItemPrice().getCurrencyCode());
            if(item.getShippingDiscount() != null)
                orderItem.discountPrice = Float.parseFloat(item.getShippingDiscount().getAmount());
            orderItem.memo = "ERP订单项重新抓取";
            orderItem.listingName = item.getTitle();
            orderItem.quantity = item.getQuantityOrdered();
            if(item.getShippingPrice() != null)
                orderItem.shippingPrice = Float.parseFloat(item.getShippingPrice().getAmount());
            orderItem.order = orderr;
            orderItem.product = Product.findByMerchantSKU(item.getSellerSKU());
            Selling selling = Selling.querySellingByAPI(item.getSellerSKU(), orderr.market, orderr.account.id);
            if(selling != null) orderItem.selling = selling;
            orderItem.usdCost = orderItem.currency.toUSD(orderItem.price);
            if(item.getGiftWrapPrice() != null)
                orderItem.giftWrap = Float.parseFloat(item.getGiftWrapPrice().getAmount());
            if(item.getPromotionIds().size() > 0) {
                StringBuffer promotionIds = new StringBuffer();
                item.getPromotionIds().forEach(promotion -> promotionIds.append(promotion).append(";"));
                orderItem.promotionIDs = promotionIds.toString();
            }
            orderItem.save();
        });
    }


}
