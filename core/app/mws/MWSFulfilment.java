package mws;

import com.amazonservices.mws.FulfillmentInboundShipment.FBAInboundServiceMWSClient;
import com.amazonservices.mws.FulfillmentInboundShipment.FBAInboundServiceMWSConfig;
import models.market.Account;
import models.market.M;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2018/2/26
 * Time: 下午3:12
 */
public class MWSFulfilment {

    private static final Map<String, FBAInboundServiceMWSClient> cached = new HashMap<>();

    public static FBAInboundServiceMWSClient client(Account account, M market) {
        String key = String.format("FinancesServiceClient_%s_%s", account.id, market.name());
        FBAInboundServiceMWSClient client;
        if(cached.containsKey(key)) return cached.get(key);
        else {
            synchronized(cached) {
                if(cached.containsKey(key)) return cached.get(key);
                FBAInboundServiceMWSConfig config = new FBAInboundServiceMWSConfig();
                switch(market) {
                    case AMAZON_CA:
                    case AMAZON_MX:
                    case AMAZON_US:
                        config.setServiceURL("https://mws.amazonservices.com");
                        break;
                    case AMAZON_UK:
                    case AMAZON_IT:
                    case AMAZON_FR:
                    case AMAZON_ES:
                    case AMAZON_DE:
                        config.setServiceURL("https://mws-eu.amazonservices.com/");
                        break;
                    case AMAZON_JP:
                        config.setServiceURL("https://mws.amazonservices.jp");
                        break;
                    default:
                        break;
                }
                client = new FBAInboundServiceMWSClient(account.accessKey, account.token, "elcuk2", "1.0", config);
                cached.put(key, client);
            }
        }
        return client;
    }
}
