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
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.GetServiceStatusRequest;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.GetServiceStatusResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 *
 * Get Service Status  Samples
 *
 *
 */
public class GetServiceStatusAsyncSample {

    /**
     * Just add few required parameters, and try the service
     * Get Service Status functionality
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
         GetServiceStatusRequest requestOne = new GetServiceStatusRequest();
         // @TODO: set request parameters here
         requestOne.setSellerId(sellerId);
         requestOne.setMarketplace(marketplaceId);


         GetServiceStatusRequest requestTwo = new GetServiceStatusRequest();
         // @TODO: set second request parameters here
         requestTwo.setSellerId(sellerId);
         requestTwo.setMarketplace(marketplaceId);


         List<GetServiceStatusRequest> requests = new ArrayList<GetServiceStatusRequest>();
         requests.add(requestOne);
         requests.add(requestTwo);

         invokeGetServiceStatus(service, requests);
    }


                            
    /**
     * Get Service Status request sample
     * Gets the status of the service.
     * Status is one of GREEN, RED representing:
     * GREEN: This API section of the service is operating normally.
     * RED: The service is disrupted.
     *   
     * @param service instance of FBAInventoryServiceMWS service
     * @param requests list of requests to process
     */
    public static void invokeGetServiceStatus(FBAInventoryServiceMWSAsync service, List<GetServiceStatusRequest> requests) {
        List<Future<GetServiceStatusResponse>> responses = new ArrayList<Future<GetServiceStatusResponse>>();
        for (GetServiceStatusRequest request : requests) {
            responses.add(service.getServiceStatusAsync(request));
        }
        for (Future<GetServiceStatusResponse> future : responses) {
            while (!future.isDone()) {
                Thread.yield();
            }
            try {
                GetServiceStatusResponse response = future.get();
                // Original request corresponding to this response, if needed:
                GetServiceStatusRequest originalRequest = requests.get(responses.indexOf(future));
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
