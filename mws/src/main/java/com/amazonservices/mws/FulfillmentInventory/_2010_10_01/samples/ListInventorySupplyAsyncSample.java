/******************************************************************************* 
 *  Copyright 2009 Amazon Services. All Rights Reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  
 *  You may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at: http://aws.amazon.com/apache2.0
 *  This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 *  CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 *  specific language governing permissions and limitations under the License.
 * ***************************************************************************** 
 * 
 *  FBA Inventory Service MWS  Java Library
 *  API Version: 2010-10-01
 *  Generated: Fri Oct 22 09:47:17 UTC 2010 
 */

package com.amazonservices.mws.FulfillmentInventory._2010_10_01.samples;

import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSAsync;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSAsyncClient;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSConfig;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSException;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.ListInventorySupplyRequest;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.ListInventorySupplyResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 *
 * List Inventory Supply  Samples
 *
 *
 */
public class ListInventorySupplyAsyncSample {

    /**
     * Just add few required parameters, and try the service
     * List Inventory Supply functionality
     *
     * @param args unused
     */
    public static void main(String... args) {

        /************************************************************************
         * Access Key ID and Secret Acess Key ID, obtained from:
         * http://mws.amazon.com
         ***********************************************************************/
        final String accessKeyId = "<Your Access Key ID>";
        final String secretAccessKey = "<Your Secret Access Key>";

        /************************************************************************
         * Marketplace and Seller IDs are required parameters for all 
         * MWS calls.
         ***********************************************************************/
        final String marketplaceId = "<Your Marketplace ID>";
        final String sellerId = "<Your Seller Id>";

        /************************************************************************
         * The application name and version are included in each MWS call's
         * HTTP User-Agent field. These are required fields.
         ***********************************************************************/
        final String applicationName = "<Your Application Name>";
        final String applicationVersion = "<Your Application Version or Build Number or Release Date>";

        /************************************************************************
         * Instantiate Http Client Implementation of FBA Inventory Service MWS 
         * 
         * Important! Number of threads in executor should match number of connections
         * for http client.
         *
         ***********************************************************************/
         FBAInventoryServiceMWSConfig config = new FBAInventoryServiceMWSConfig();

        /************************************************************************
         * Uncomment to set the correct MWS endpoint. You can get your Country
         * Code from MWSEndpoint enum class, e.g. US.
         ************************************************************************/
        // config.setServiceURL(MWSEndpoint.<Your Country Code>.toString());

         /************************************************************************
          * The argument (35) set below is the number of threads client should
          * spawn for processing.
          ***********************************************************************/
        config.setMaxAsyncThreads(35);

        /************************************************************************
         * Instantiate Http Client Implementation of FBA Inventory Service MWS 
         ***********************************************************************/
        // FBAInventoryServiceMWSAsync service = new FBAInventoryServiceMWSAsyncClient(accessKeyId, secretAccessKey, config);
        FBAInventoryServiceMWSAsync service = new FBAInventoryServiceMWSAsyncClient(accessKeyId, secretAccessKey, applicationName, applicationVersion, config);

        /************************************************************************
         * Setup requests parameters and invoke parallel processing. Of course
         * in real world application, there will be much more than a couple of
         * requests to process.
         ***********************************************************************/
         ListInventorySupplyRequest requestOne = new ListInventorySupplyRequest();
         // @TODO: set request parameters here
         requestOne.setSellerId(sellerId);
         requestOne.setMarketplace(marketplaceId);


         ListInventorySupplyRequest requestTwo = new ListInventorySupplyRequest();
         // @TODO: set second request parameters here
         requestTwo.setSellerId(sellerId);
         requestTwo.setMarketplace(marketplaceId);


         List<ListInventorySupplyRequest> requests = new ArrayList<ListInventorySupplyRequest>();
         requests.add(requestOne);
         requests.add(requestTwo);

         invokeListInventorySupply(service, requests);
    }


                        
    /**
     * List Inventory Supply request sample
     * Get information about the supply of seller-owned inventory in
     * Amazon's fulfillment network. "Supply" is inventory that is available
     * for fulfilling (a.k.a. Multi-Channel Fulfillment) orders. In general
     * this includes all sellable inventory that has been received by Amazon,
     * that is not reserved for existing orders or for internal FC processes,
     * and also inventory expected to be received from inbound shipments.
     * This operation provides 2 typical usages by setting different
     * ListInventorySupplyRequest value:
     * 
     * 1. Set value to SellerSkus and not set value to QueryStartDateTime,
     * this operation will return all sellable inventory that has been received
     * by Amazon's fulfillment network for these SellerSkus.
     * 2. Not set value to SellerSkus and set value to QueryStartDateTime,
     * This operation will return information about the supply of all seller-owned
     * inventory in Amazon's fulfillment network, for inventory items that may have had
     * recent changes in inventory levels. It provides the most efficient mechanism
     * for clients to maintain local copies of inventory supply data.
     * Only 1 of these 2 parameters (SellerSkus and QueryStartDateTime) can be set value for 1 request.
     * If both with values or neither with values, an exception will be thrown.
     * This operation is used with ListInventorySupplyByNextToken
     * to paginate over the resultset. Begin pagination by invoking the
     * ListInventorySupply operation, and retrieve the first set of
     * results. If more results are available,continuing iteratively requesting further
     * pages results by invoking the ListInventorySupplyByNextToken operation (each time
     * passing in the NextToken value from the previous result), until the returned NextToken
     * is null, indicating no further results are available.
     *   
     * @param service instance of FBAInventoryServiceMWS service
     * @param requests list of requests to process
     */
    public static void invokeListInventorySupply(FBAInventoryServiceMWSAsync service, List<ListInventorySupplyRequest> requests) {
        List<Future<ListInventorySupplyResponse>> responses = new ArrayList<Future<ListInventorySupplyResponse>>();
        for (ListInventorySupplyRequest request : requests) {
            responses.add(service.listInventorySupplyAsync(request));
        }
        for (Future<ListInventorySupplyResponse> future : responses) {
            while (!future.isDone()) {
                Thread.yield();
            }
            try {
                ListInventorySupplyResponse response = future.get();
                // Original request corresponding to this response, if needed:
                ListInventorySupplyRequest originalRequest = requests.get(responses.indexOf(future));
                System.out.println("Response request id: " + response.getResponseMetadata().getRequestId());
            } catch (Exception e) {
                if (e.getCause() instanceof FBAInventoryServiceMWSException) {
                    FBAInventoryServiceMWSException exception = FBAInventoryServiceMWSException.class.cast(e.getCause());
                    System.out.println("Caught Exception: " + exception.getMessage());
                    System.out.println("Response Status Code: " + exception.getStatusCode());
                    System.out.println("Error Code: " + exception.getErrorCode());
                    System.out.println("Error Type: " + exception.getErrorType());
                    System.out.println("Request ID: " + exception.getRequestId());
                    System.out.print("XML: " + exception.getXML());
                } else {
                    e.printStackTrace();
                }
            }
        }
    }
        
}
