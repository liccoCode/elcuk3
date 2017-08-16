package mws;

import com.amazonservices.mws.orders.MarketplaceWebServiceOrders;
import com.amazonservices.mws.orders.MarketplaceWebServiceOrdersClient;
import com.amazonservices.mws.orders.MarketplaceWebServiceOrdersConfig;
import com.amazonservices.mws.orders.MarketplaceWebServiceOrdersException;
import com.amazonservices.mws.orders.model.*;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import models.market.Account;
import models.market.M;
import models.market.Orderr;
import models.market.Selling;
import models.product.Product;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.libs.F;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/24/13
 * Time: 10:24 AM
 *
 * @deprecated
 */
public class MWSOrders {

    private MWSOrders() {
    }

    private static final Map<String, MarketplaceWebServiceOrders> CLIENT_CACHE = new HashMap<>();

    /**
     * The GetOrder operation has a maximum request quota of six and a restore rate of one request every minute.
     *
     * @param nMinutesAgo N 分钟之前的订单; 在 10mn ~ 360 mn 之间
     * @return
     */
    public static List<Orderr> listOrders(Account account, int nMinutesAgo)
            throws MarketplaceWebServiceOrdersException {

        ListOrdersRequest request = new ListOrdersRequest();
        request.setSellerId(account.merchantId);
        request.setMarketplaceId(Arrays.asList(account.type.amid().name()));

        nMinutesAgo = nMinutesAgo < 10 ? 10 : (nMinutesAgo > 360 ? 360 : nMinutesAgo);
        DateTime dt = DateTime.now().minusMinutes(nMinutesAgo);
        Logger.info("Fetch lastUpdateDate %s minutes ago orders.", nMinutesAgo);

        request.setLastUpdatedAfter(new XMLGregorianCalendarImpl(dt.toGregorianCalendar()));
        ListOrdersResponse response = client(account).listOrders(request);
        ListOrdersResult result = response.getListOrdersResult();

        List<Orderr> orders = responseToOrders(result.getOrders(), account);
        String token = result.getNextToken();
        while(StringUtils.isNotBlank(token)) {
            F.T2<String, List<Orderr>> t2 = listOrdersByNextToken(account, token);
            token = t2._1;
            orders.addAll(t2._2);
        }

        Logger.info("List Total %s Orders", orders.size());
        return orders;
    }

    public static F.T2<String, List<Orderr>> listOrdersByNextToken(Account account, String token)
            throws MarketplaceWebServiceOrdersException {
        ListOrdersByNextTokenRequest request = new ListOrdersByNextTokenRequest(
                account.merchantId, token
        );
        ListOrdersByNextTokenResponse response = client(account).listOrdersByNextToken(request);
        ListOrdersByNextTokenResult result = response.getListOrdersByNextTokenResult();
        return new F.T2<>(result.getNextToken(),
                responseToOrders(result.getOrders(), account));
    }


    /**
     * 将 OrderListType 转换为 List[Order]
     *
     * @param orderList
     * @param account
     * @return
     */
    private static List<Orderr> responseToOrders(List<Order> orderList, Account account) {
        List<Orderr> orders = new ArrayList<>();

        for(Order amzOrder : orderList) {
            Orderr orderr = new Orderr();
            orderr.account = account;
            orderr.orderId = amzOrder.getAmazonOrderId();
            orderr.state = parseOrderState(amzOrder.getOrderStatus());
            orderr.shipLevel = amzOrder.getShipServiceLevel();
            orderr.market = M.val(amzOrder.getSalesChannel());

            if(amzOrder.getPurchaseDate() != null) {
                orderr.createDate = amzOrder.getPurchaseDate().toGregorianCalendar().getTime();
            } else {
                orderr.createDate = new Date();
            }
            orderr.paymentDate = orderr.createDate;

            if(amzOrder.getShippingAddress() != null) {
                Address address = amzOrder.getShippingAddress();

                orderr.city = address.getCity();
                orderr.country = address.getCountryCode();
                orderr.postalCode = address.getPostalCode();
                orderr.phone = address.getPhone();
                orderr.province = address.getStateOrRegion();
                orderr.reciver = address.getName();
                orderr.address = address.getAddressLine1()
                        + "\n" + address.getAddressLine2()
                        + "\n" + address.getAddressLine3();
            }

            orders.add(orderr);
        }
        return orders;
    }


    public static List<models.market.OrderItem> listOrderItems(Account account, String orderId)
            throws MarketplaceWebServiceOrdersException {
        ListOrderItemsRequest request = new ListOrderItemsRequest(account.merchantId, orderId);
        ListOrderItemsResponse response = client(account).listOrderItems(request);
        ListOrderItemsResult result = response.getListOrderItemsResult();

        String token = result.getNextToken();
        List<OrderItem>  orderItemList = result.getOrderItems();

        List<models.market.OrderItem> orderItems = responseToOrderItems(orderItemList, orderId, account
        );

        while(StringUtils.isNotBlank(token)) {
            F.T2<String, List<models.market.OrderItem>> t2 = listOrderItemsByNextToken(account,
                    token, orderId);
            token = t2._1;
            orderItems.addAll(t2._2);
        }
        return orderItems;
    }

    public static F.T2<String, List<models.market.OrderItem>> listOrderItemsByNextToken(
            Account account,
            String token,
            String orderId)
            throws MarketplaceWebServiceOrdersException {

        ListOrderItemsByNextTokenRequest request = new ListOrderItemsByNextTokenRequest(
                account.merchantId, token
        );
        ListOrderItemsByNextTokenResponse response = client(account)
                .listOrderItemsByNextToken(request);
        ListOrderItemsByNextTokenResult result = response.getListOrderItemsByNextTokenResult();

        return new F.T2<>(result.getNextToken(),
                responseToOrderItems(result.getOrderItems(), orderId, account));
    }

    /**
     * 将 Response 转换为 OrderItem
     *
     * @param orderItemList
     * @return 注意缺失 Selling
     */
    private static List<models.market.OrderItem> responseToOrderItems(List<OrderItem> orderItemList,
                                                                      String orderId, Account acc) {
        List<models.market.OrderItem> orderItems = new ArrayList<>();
        for(OrderItem amzItem : orderItemList) {
            models.market.OrderItem item = new models.market.OrderItem();

            if(!Product.validSKU(Product.merchantSKUtoSKU(amzItem.getSellerSKU()))) {
                Logger.warn("MSku %s is not valid sku format.", amzItem.getSellerSKU());
                continue;
            }

            item.product = Product.findByMerchantSKU(amzItem.getSellerSKU());
            // use first-level cache
            item.order = Orderr.findById(orderId);
            item.market = item.order.market;
            String mappingSku = Selling.getMappingSKU(amzItem.getSellerSKU());
            if(amzItem.getSellerSKU().contains(",2")) { // 如果包含 ,2 尝试寻找正确的 Selling
                String likeSellingId = Product.merchantSKUtoSKU(mappingSku)
                        + "%|" + item.order.market.nickName() + "|" + acc.id;
                item.selling = Selling.find("sellingId like ?", likeSellingId).first();
            } else {
                item.selling = Selling.findById(Selling.sid(
                        mappingSku,
                        item.order.market, acc
                ));
            }
            item.memo = item.memo + "getSellerSKU:" + amzItem.getSellerSKU();

            item.id = String.format("%s_%s", orderId,
                    Product.merchantSKUtoSKU(amzItem.getSellerSKU()));
            item.quantity = amzItem.getQuantityOrdered();
            item.listingName = amzItem.getTitle();
            item.createDate = new Date();
            if(amzItem.getItemPrice() != null) {
                item.price = NumberUtils.toFloat(amzItem.getItemPrice().getAmount());
                item.currency = helper.Currency.valueOf(amzItem.getItemPrice().getCurrencyCode());
            }

            if(amzItem.getPromotionDiscount() != null) {
                item.discountPrice = NumberUtils.toFloat(
                        amzItem.getPromotionDiscount().getAmount());
            }
            if(amzItem.getPromotionIds() != null) {
                item.promotionIDs = StringUtils.join(amzItem.getPromotionIds(), ",");
            }

            if(amzItem.getGiftWrapPrice() != null) {
                item.giftWrap = NumberUtils.toFloat(amzItem.getGiftWrapPrice().getAmount());
            }
            if(amzItem.getGiftWrapTax() != null) {
                if(item.giftWrap == null) item.giftWrap = 0f;
                item.giftWrap += NumberUtils.toFloat(amzItem.getGiftWrapTax().getAmount());
            }

            item.calUsdCose();
            // 临时使用(这里知道 SKU, SellingId, 与 Account), 使用后删除.(AmazonOrderItemDiscover)
            item.memo = Selling.sid(amzItem.getSellerSKU(), item.order.market, acc);
            orderItems.add(item);
        }
        return orderItems;
    }


    private static Orderr.S parseOrderState(String orderState) {
        // {"Pending"=>226233, "Shipped"=>1284685, "Cancelled"=>28538, "Shipping"=>1342}, 半年的更新文件
        switch(orderState) {
            case "PENDING":
                return Orderr.S.PENDING;
            case "PARTIALLY_SHIPPED":
            case "UNSHIPPED":
                return Orderr.S.PAYMENT;
            case "SHIPPED":
                return Orderr.S.SHIPPED;
            case "CANCELED":
                return Orderr.S.CANCEL;
            default:
                return Orderr.S.PENDING;
        }
    }


    private static MarketplaceWebServiceOrders client(Account acc) {
        if(!acc.isSaleAcc) throw new IllegalArgumentException("需要销售账户!");
        String key = String.format("%s_%s", acc.type, acc.id);
        MarketplaceWebServiceOrders client;
        if(CLIENT_CACHE.containsKey(key)) client = CLIENT_CACHE.get(key);
        else {
            synchronized(CLIENT_CACHE) {
                if(CLIENT_CACHE.containsKey(key)) return CLIENT_CACHE.get(key);
                MarketplaceWebServiceOrdersConfig config = new MarketplaceWebServiceOrdersConfig();
                // 设置服务器地址
                switch(acc.type) {
                    case AMAZON_UK:
                        config.setServiceURL("https://mws.amazonservices.co.uk/Orders/2011-01-01");
                        break;
                    case AMAZON_DE:
                        config.setServiceURL("https://mws.amazonservices.de/Orders/2011-01-01");
                        break;
                    case AMAZON_US:
                        config.setServiceURL("https://mws.amazonservices.com/Orders/2011-01-01");
                        break;
                    case AMAZON_JP:
                        config.setServiceURL("https://mws.amazonservices.jp/Orders/2011-01-01");
                        break;
                    case AMAZON_FR:
                        config.setServiceURL("https://mws.amazonservices.fr/Orders/2011-01-01");
                        break;
                    case AMAZON_CA:
                        config.setServiceURL("https://mws.amazonservices.ca/Orders/2011-01-01");
                        break;
                    case AMAZON_IT:
                        config.setServiceURL("https://mws.amazonservices.it/Orders/2011-01-01");
                        break;
                    default:
                        throw new UnsupportedOperationException("不支持的 FBA 地址" + acc.type);
                }
                client = new MarketplaceWebServiceOrdersClient(acc.accessKey, acc.token, "elcuk2",
                        "1.0",
                        config);
                CLIENT_CACHE.put(key, client);
            }
        }
        return client;
    }
}
