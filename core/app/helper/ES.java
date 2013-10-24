package helper;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import play.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 10/24/13
 * Time: 4:19 PM
 */
public class ES {
    private static Client client;

    public static void init() {
        try {
            client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("gengar.easya.cc", 9300));
        } catch(Exception e) {
            Logger.error("!!!! ElasticSearch Server can not connecte  !!!!");
            System.exit(0);
        }
    }

    public static void close() {
        if(client != null)
            client.close();
    }
}
