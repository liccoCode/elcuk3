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
                switch(market) {
                    case AMAZON_MX:
                    case AMAZON_US:
                        config.setServiceURL("https://mws.amazonservices.com");
                        break;
                    case AMAZON_UK:
                        // 无法使用这个地址, 因为 toString() 使用了 FulfillmentInventory
                        //                        config.setServiceURL(MWSEndpoint.UK.toString());
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
                    case AMAZON_CA:
                        config.setServiceURL("https://mws.amazonservices.ca");
                        break;
                    default:
                        break;
                }
                client = new MarketplaceWebServiceProductsClient(account.accessKey, account.token,
                        "elcuk2", "1.0", config);
                cached.put(key, client);
            }
        }
        return client;
    }
}

