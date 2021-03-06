package mws;

import com.amazonservices.mws.products.MarketplaceWebServiceProductsClient;
import com.amazonservices.mws.products.MarketplaceWebServiceProductsConfig;
import models.market.Account;
import models.market.M;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/10/13
 * Time: 上午10:11
 */
public class MWSProducts {

    private static final Map<String, MarketplaceWebServiceProductsClient> cached = new HashMap<>();

    public static MarketplaceWebServiceProductsClient client(Account account, M market) {

        String key = String.format("ProductsServiceClient_%s_%s", account.id, market.name());
        MarketplaceWebServiceProductsClient client;
        if(cached.containsKey(key)) return cached.get(key);
        else {
            synchronized(cached) {
                if(cached.containsKey(key)) return cached.get(key);
                MarketplaceWebServiceProductsConfig config = new MarketplaceWebServiceProductsConfig();
                config.setServiceURL(getMwsUrl(market));
                client = new MarketplaceWebServiceProductsClient(account.accessKey, account.token,
                        "elcuk2", "1.0", config);
                cached.put(key, client);
            }
        }
        return client;
    }

    public static String getMwsUrl(M market) {
        switch(market) {
            case AMAZON_CA:
            case AMAZON_MX:
            case AMAZON_US:
                return "https://mws.amazonservices.com";
            case AMAZON_UK:
            case AMAZON_IT:
            case AMAZON_FR:
            case AMAZON_ES:
            case AMAZON_DE:
                return "https://mws-eu.amazonservices.com";
            case AMAZON_JP:
                return "https://mws.amazonservices.jp";
            case AMAZON_AU:
                return "https://mws.amazonservices.com.au";
            case AMAZON_IN:
                return "https://mws.amazonservices.in";
            default:
                return "";
        }
    }

}

