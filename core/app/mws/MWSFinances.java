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
        else {
            synchronized(cached) {
                if(cached.containsKey(key)) return cached.get(key);
                MWSFinancesServiceConfig config = new MWSFinancesServiceConfig();
                config.setServiceURL(MWSProducts.getMwsUrl(market));
                client = new MWSFinancesServiceClient(account.accessKey, account.token, "elcuk2", "1.0", config);
                cached.put(key, client);
            }
        }
        return client;
    }
}
