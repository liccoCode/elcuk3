package mws;

import com.amazonservices.mws.finances.MWSFinancesServiceClient;
import com.amazonservices.mws.finances.MWSFinancesServiceConfig;
import models.market.Account;
import models.market.M;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/9/4
 * Time: 下午11:12
 */
public class MWSFinances {

    private static final Map<String, MWSFinancesServiceClient> cached = new HashMap<>();

    public static MWSFinancesServiceClient client(Account account, M market) {

        String key = String.format("FinancesServiceClient_%s_%s", account.id, market.name());
        MWSFinancesServiceClient client;
        if(cached.containsKey(key)) return cached.get(key);
        if(cached.containsKey(key)) return cached.get(key);
        else {
            synchronized(cached) {
                if(cached.containsKey(key)) return cached.get(key);
                MWSFinancesServiceConfig config = new MWSFinancesServiceConfig();
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
                client = new MWSFinancesServiceClient(account.accessKey, account.token, "elcuk2", "1.0", config);
                cached.put(key, client);
            }
        }
        return client;
    }
}
