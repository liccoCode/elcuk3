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
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.libs.F;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/24/13
 * Time: 10:24 AM
 */
public class MWSOrders {
    private static final Map<String, MarketplaceWebServiceOrders> CLIENT_CACHE = new HashMap<String, MarketplaceWebServiceOrders>();

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
        request.setMarketplaceId(new MarketplaceIdList(Arrays.asList(account.type.amid().name())));

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


    private static List<Orderr> responseToOrders(OrderList orderList, Account account) {
        List<Order> amazonOrders = orderList.getOrder();
        List<Orderr> orders = new ArrayList<Orderr>();

        for(Order amzOrder : amazonOrders) {
            Orderr orderr = new Orderr();
            orderr.account = account;
            orderr.orderId = amzOrder.getAmazonOrderId();
            orderr.state = parseOrderState(amzOrder.getOrderStatus());
            orderr.shipLevel = amzOrder.getShipServiceLevel();
            orderr.market = M.val(amzOrder.getSalesChannel());
            orderr.createDate = new Date();

            if(amzOrder.getPurchaseDate() != null) {
                orderr.paymentDate = amzOrder.getPurchaseDate().toGregorianCalendar().getTime();
                // 付款后的最后更新时间才有可能是发货时间
                orderr.shipDate = amzOrder.getLastUpdateDate().toGregorianCalendar().getTime();
            }
            /* Fee 类型在这里计算?
            if(amzOrder.getOrderTotal() != null) {
                Money money = amzOrder.getOrderTotal();
                SaleFee fee = new SaleFee();
                fee.account = account;
                fee.cost = NumberUtils.toFloat(money.getAmount());
                fee.currency = Currency.valueOf(money.getCurrencyCode());
                fee.usdCost = fee.currency.toUSD(fee.cost);
                fee.orderId = amzOrder.getAmazonOrderId();
                fee.type = FeeType.amazon();
                orderr.fees.add(fee);
            } */

            if(amzOrder.getShippingAddress() != null) {
                Address address = amzOrder.getShippingAddress();

                orderr.city = address.getCity();
                orderr.country = address.getCountryCode();
                orderr.postalCode = address.getPostalCode();
                orderr.phone = address.getPhone();
                orderr.province = address.getStateOrRegion();
                orderr.reciver = address.getName();
                orderr.address = address.getAddressLine1() +
                        "\n" + address.getAddressLine2() +
                        "\n" + address.getAddressLine3();
            }

            orders.add(orderr);
        }
        return orders;
    }

    public static F.T2<String, List<Orderr>> listOrdersByNextToken(Account account, String token)
            throws MarketplaceWebServiceOrdersException {
        ListOrdersByNextTokenRequest request = new ListOrdersByNextTokenRequest(
                account.merchantId, token
        );
        ListOrdersByNextTokenResponse response = client(account).listOrdersByNextToken(request);
        ListOrdersByNextTokenResult result = response.getListOrdersByNextTokenResult();
        return new F.T2<String, List<Orderr>>(result.getNextToken(),
                responseToOrders(result.getOrders(), account));
    }

    private static Orderr.S parseOrderState(OrderStatusEnum orderState) {
        // {"Pending"=>226233, "Shipped"=>1284685, "Cancelled"=>28538, "Shipping"=>1342}, 半年的更新文件
        switch(orderState) {
            case PENDING:
                return Orderr.S.PENDING;
            case PARTIALLY_SHIPPED:
            case UNSHIPPED:
                return Orderr.S.PAYMENT;
            case SHIPPED:
                return Orderr.S.SHIPPED;
            case CANCELED:
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
                    default:
                        throw new UnsupportedOperationException("不支持的 FBA 地址");
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
