package mws;

import com.amazonservices.mws.orders.MarketplaceWebServiceOrders;
import com.amazonservices.mws.orders.MarketplaceWebServiceOrdersClient;
import com.amazonservices.mws.orders.MarketplaceWebServiceOrdersConfig;
import com.amazonservices.mws.orders.model.ListOrderItemsRequest;
import com.amazonservices.mws.orders.model.ListOrderItemsResponse;
import com.amazonservices.mws.orders.model.ListOrderItemsResult;
import helper.Currency;
import models.market.*;
import models.product.Product;
import play.db.jpa.GenericModel;

import java.util.HashMap;
import java.util.Map;

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
                switch(market) {
                    case AMAZON_MX:
                    case AMAZON_US:
                        config.setServiceURL("https://mws.amazonservices.com");
                        break;
                    case AMAZON_UK:
                        config.setServiceURL("https://mws.amazonservices.co.uk");
                        break;
                    case AMAZON_ES:
                    case AMAZON_DE:
                        config.setServiceURL("https://mws.amazonservices.de");
                        break;
                    case AMAZON_FR:
                        config.setServiceURL("https://mws.amazonservices.fr");
                        break;
                    case AMAZON_IT:
                        config.setServiceURL("https://mws.amazonservices.it");
                        break;
                    case AMAZON_JP:
                        config.setServiceURL("https://mws.amazonservices.jp");
                        break;
                    case AMAZON_CA:
                        config.setServiceURL("https://mws.amazonservices.ca");
                        break;
                    default:
                        break;
                }
                client = new MarketplaceWebServiceOrdersClient(account.accessKey, account.token,
                        "elcuk2", "1.0", config);
                cached.put(key, client);
            }
        }
        return client;
    }


    public static void invokeListOrderItems(Orderr orderr) {
        MarketplaceWebServiceOrders client = MWSOrders.client(orderr.account, orderr.market);
        ListOrderItemsRequest request = new ListOrderItemsRequest(orderr.account.merchantId, orderr.orderId);
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
            orderItem.orderItemId = item.getOrderItemId();
            if(item.getPromotionIds().size() > 0) {
                StringBuffer promotionIds = new StringBuffer();
                item.getPromotionIds().forEach(promotion -> promotionIds.append(promotion).append(";"));
                orderItem.promotionIDs = promotionIds.toString();
            }
            orderItem.save();
        });

    }


}
