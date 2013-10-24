package helper;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import play.Logger;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 10/24/13
 * Time: 4:19 PM
 */
public class ES {
    private static Client client;

    public static void init() {
        if(client != null) return;
        try {
            ES.close();
            client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("gengar.easya.cc", 9300));
            Logger.info("ElasticSearch Server success connect.");
        } catch(Exception e) {
            Logger.error("!!!! ElasticSearch Server can not connecte  !!!!");
            System.exit(0);
        }
    }

    public static Map<String, Object> get(String index, String type, String id) {
        return client.prepareGet(index, type, id).execute().actionGet().getSource();
    }

    public static Client client() {
        return client;
    }

    public static void close() {
        if(client != null) {
            try {
                client.close();
            } catch(Exception e) {
                Logger.warn("In Dev runtime error.");
            }
        }
    }
}
