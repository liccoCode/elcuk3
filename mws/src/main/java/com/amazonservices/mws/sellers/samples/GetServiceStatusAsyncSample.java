/******************************************************************************* 
 *  Copyright 2009 Amazon Services.
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  
 *  You may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at: http://aws.amazon.com/apache2.0
 *  This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 *  CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 *  specific language governing permissions and limitations under the License.
 * ***************************************************************************** 
 * 
 *  Marketplace Web Service Sellers Java Library
 *  API Version: 2011-07-01
 *  Generated: Fri Jun 24 20:08:04 GMT 2011 
 * 
 */



package com.amazonservices.mws.sellers.samples;

import com.amazonservices.mws.sellers.MarketplaceWebServiceSellersAsync;
import com.amazonservices.mws.sellers.MarketplaceWebServiceSellersAsyncClient;
import com.amazonservices.mws.sellers.MarketplaceWebServiceSellersException;
import com.amazonservices.mws.sellers.model.GetServiceStatusRequest;
import com.amazonservices.mws.sellers.model.GetServiceStatusResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

		/*
		 * Add required parameters in SellersSampleConfig.java before trying out
		 * this sample.
		 */

		/************************************************************************
		 * Instantiate Http Client Implementation of Marketplace Web Service
		 * Sellers
		 * 
		 * Important! Number of threads in executor should match number of
		 * connections for http client.
		 * 
		 ***********************************************************************/
		SellersSampleConfig.config.setMaxConnections(100);
		ExecutorService executor = Executors.newFixedThreadPool(100);
		MarketplaceWebServiceSellersAsync service = new MarketplaceWebServiceSellersAsyncClient(
				SellersSampleConfig.accessKeyId,
				SellersSampleConfig.secretAccessKey,
				SellersSampleConfig.applicationName,
				SellersSampleConfig.applicationVersion,
				SellersSampleConfig.config, executor);

		/************************************************************************
		 * Setup requests parameters and invoke parallel processing. Of course
		 * in real world application, there will be much more than a couple of
		 * requests to process.
		 ***********************************************************************/
		GetServiceStatusRequest requestOne = new GetServiceStatusRequest();
		// @TODO: set request parameters here
		requestOne.setSellerId(SellersSampleConfig.sellerId);

		GetServiceStatusRequest requestTwo = new GetServiceStatusRequest();
		// @TODO: set second request parameters here
		requestTwo.setSellerId(SellersSampleConfig.sellerId);

		List<GetServiceStatusRequest> requests = new ArrayList<GetServiceStatusRequest>();
		requests.add(requestOne);
		requests.add(requestTwo);

		invokeGetServiceStatus(service, requests);

		executor.shutdown();

	}

                                
    /**
     * Get Service Status request sample
     * Returns the service status of a particular MWS API section. The operation
     * takes no input. All API sections within the API are required to implement this operation.
     *   
     * @param service instance of MarketplaceWebServiceSellers service
     * @param requests list of requests to process
     */
    public static void invokeGetServiceStatus(MarketplaceWebServiceSellersAsync service, List<GetServiceStatusRequest> requests) {
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
                @SuppressWarnings("unused")
				GetServiceStatusRequest originalRequest = requests.get(responses.indexOf(future));
                System.out.println("Response request id: " + response.getResponseMetadata().getRequestId());
            } catch (Exception e) {
                if (e.getCause() instanceof MarketplaceWebServiceSellersException) {
                    MarketplaceWebServiceSellersException exception = MarketplaceWebServiceSellersException.class.cast(e.getCause());
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
