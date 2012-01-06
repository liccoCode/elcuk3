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
 *  FBA Inbound Service MWS  Java Library
 *  API Version: 2010-10-01
 *  Generated: Fri Oct 22 09:48:04 UTC 2010 
 */

package com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.samples;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSAsync;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSAsyncClient;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSConfig;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.ListInboundShipmentItemsRequest;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.ListInboundShipmentItemsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 *
 * List Inbound Shipment Items  Samples
 *
 *
 */
public class ListInboundShipmentItemsAsyncSample {

    /**
     * Just add few required parameters, and try the service
     * List Inbound Shipment Items functionality
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
         * Seller ID is required parameter for all MWS calls.
         ***********************************************************************/
        final String sellerId = "<Your Seller Id>";

        /************************************************************************
         * The application name and version are included in each MWS call's
         * HTTP User-Agent field. These are required fields.
         ***********************************************************************/
        final String applicationName = "<Your Application Name>";
        final String applicationVersion = "<Your Application Version or Build Number or Release Date>";

        /************************************************************************
         * Instantiate Http Client Implementation of FBA Inbound Service MWS 
         * 
         * Important! Number of threads in executor should match number of connections
         * for http client.
         *
         ***********************************************************************/
         FBAInboundServiceMWSConfig config = new FBAInboundServiceMWSConfig();

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
         * Instantiate Http Client Implementation of FBA Inbound Service MWS 
         ***********************************************************************/
        // FBAInboundServiceMWSAsync service = new FBAInboundServiceMWSAsyncClient(accessKeyId, secretAccessKey, config);
        FBAInboundServiceMWSAsync service = new FBAInboundServiceMWSAsyncClient(accessKeyId, secretAccessKey, applicationName, applicationVersion, config);

        /************************************************************************
         * Setup requests parameters and invoke parallel processing. Of course
         * in real world application, there will be much more than a couple of
         * requests to process.
         ***********************************************************************/
         ListInboundShipmentItemsRequest requestOne = new ListInboundShipmentItemsRequest();
         // @TODO: set request parameters here
         requestOne.setSellerId(sellerId);

         ListInboundShipmentItemsRequest requestTwo = new ListInboundShipmentItemsRequest();
         // @TODO: set second request parameters here
         requestTwo.setSellerId(sellerId);

         List<ListInboundShipmentItemsRequest> requests = new ArrayList<ListInboundShipmentItemsRequest>();
         requests.add(requestOne);
         requests.add(requestTwo);

         invokeListInboundShipmentItems(service, requests);
    }


                                                
    /**
     * List Inbound Shipment Items request sample
     * Gets the first set of inbound shipment items for the given ShipmentId or
     * all inbound shipment items updated between the given date range.
     * A NextToken is also returned to further iterate through the Seller's
     * remaining inbound shipment items. To get the next set of inbound
     * shipment items, you must call ListInboundShipmentItemsByNextToken and
     * pass in the 'NextToken' this call returned. If a NextToken is not
     * returned, it indicates the end-of-data. Use LastUpdatedBefore
     * and LastUpdatedAfter to filter results based on last updated time.
     * Either the ShipmentId or a pair of LastUpdatedBefore and LastUpdatedAfter
     * must be passed in. if ShipmentId is set, the LastUpdatedBefore and
     * LastUpdatedAfter will be ignored.  
     * @param service instance of FBAInboundServiceMWS service
     * @param requests list of requests to process
     */
    public static void invokeListInboundShipmentItems(FBAInboundServiceMWSAsync service, List<ListInboundShipmentItemsRequest> requests) {
        List<Future<ListInboundShipmentItemsResponse>> responses = new ArrayList<Future<ListInboundShipmentItemsResponse>>();
        for (ListInboundShipmentItemsRequest request : requests) {
            responses.add(service.listInboundShipmentItemsAsync(request));
        }
        for (Future<ListInboundShipmentItemsResponse> future : responses) {
            while (!future.isDone()) {
                Thread.yield();
            }
            try {
                ListInboundShipmentItemsResponse response = future.get();
                // Original request corresponding to this response, if needed:
                ListInboundShipmentItemsRequest originalRequest = requests.get(responses.indexOf(future));
                System.out.println("Response request id: " + response.getResponseMetadata().getRequestId());
            } catch (Exception e) {
                if (e.getCause() instanceof FBAInboundServiceMWSException) {
                    FBAInboundServiceMWSException exception = FBAInboundServiceMWSException.class.cast(e.getCause());
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
