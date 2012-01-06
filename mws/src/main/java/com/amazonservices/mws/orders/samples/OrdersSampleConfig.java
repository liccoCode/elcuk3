package com.amazonservices.mws.orders.samples;

import com.amazonservices.mws.orders.MarketplaceWebServiceOrdersConfig;
import com.amazonservices.mws.orders.model.MarketplaceIdList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final public class OrdersSampleConfig {

    /**
     * *********************************************************************
     * Set Access Key ID, Secret Acess Key ID, Seller ID, etc.
     * *********************************************************************
     */
    public static final String accessKeyId = "AKIAI6EBPJLG64HWDBGQ";
    public static final String secretAccessKey = "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";
	public static final String applicationName = "<Application Name>";
	public static final String applicationVersion = "<Application Version>";

    public static final String sellerId = "AJUR3R8UN71M4";

    private static List<String> marketplaceIdArrayList = new ArrayList<String>();
    public static MarketplaceIdList marketplaceIdList = new MarketplaceIdList(Collections.singletonList("A1F83G8C2ARO7P"));

    public static MarketplaceWebServiceOrdersConfig config = new MarketplaceWebServiceOrdersConfig();

    static {
        /**************************************************************
         * Add marketplaceIds as required and set to the request object.
         **************************************************************/
        // marketplaceIdArrayList.add("<MarketplaceID1>");
        // marketplaceIdArrayList.add("<MarketplaceID2>");
        // marketplaceIdList = new MarketplaceIdList(marketplaceIdArrayList);

        /************************************************************************
         * Uncomment to set the appropriate MWS endpoint.
         ************************************************************************/
        // US
        // config.setServiceURL("https://mws.amazonservices.com/Orders/2011-01-01");
        // UK
        config.setServiceURL("https://mws.amazonservices.co.uk/Orders/2011-01-01");
        // Germany
        // config.setServiceURL("https://mws.amazonservices.de/Orders/2011-01-01");
        // France
        // config.setServiceURL("https://mws.amazonservices.fr/Orders/2011-01-01");
        // Italy
        // config.setServiceURL("https://mws.amazonservices.it/Orders/2011-01-01");
        // Japan
        // config.setServiceURL("https://mws.amazonservices.jp/Orders/2011-01-01");
        // China
        // config.setServiceURL("https://mws.amazonservices.com.cn/Orders/2011-01-01");
        // Canada
        // config.setServiceURL("https://mws.amazonservices.ca/Orders/2011-01-01");
    }

    /************************************************************************
     * You can also try advanced configuration options. Available options are:
     *
     * - Signature Version - Proxy Host and Proxy Port - User Agent String to be
     * sent to Marketplace Web Service
     *************************************************************************/

}
