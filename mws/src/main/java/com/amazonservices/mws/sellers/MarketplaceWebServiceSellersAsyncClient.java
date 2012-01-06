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



package com.amazonservices.mws.sellers;

import com.amazonservices.mws.sellers.model.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


/**
 * This contains the Sellers section of the Marketplace Web Service.
 * 
 *
 */
public class MarketplaceWebServiceSellersAsyncClient extends MarketplaceWebServiceSellersClient implements MarketplaceWebServiceSellersAsync {

    private ExecutorService executor;

    /**
     * Client to make asynchronous calls to the service. Please note, you should
     * configure executor with same number of concurrent threads as number of
     * http connections specified in MarketplaceWebServiceSellersConfig. Default number of
     * max http connections is 100.
     *
     * @param awsAccessKeyId AWS Access Key Id
     * @param awsSecretAccessKey AWS Secret Key
     * @param config service configuration. Pass new MarketplaceWebServiceSellersConfig() if you
     * plan to use defaults
     *
     * @param executor Executor service to manage asynchronous calls.
     *
     */
    public MarketplaceWebServiceSellersAsyncClient(String awsAccessKeyId, String awsSecretAccessKey,String applicationName,
			String applicationVersion, MarketplaceWebServiceSellersConfig config, ExecutorService executor) {
    	super(awsAccessKeyId, awsSecretAccessKey, applicationName,
				applicationVersion, config);
        this.executor = executor;
    }

            

    /**
     * Non-blocking List Marketplace Participations 
     * <p/>
     * Returns <code>future</code> pointer to ListMarketplaceParticipationsResponse
     * <p/>
     * If response is ready, call to <code>future.get()</code> 
     * will return ListMarketplaceParticipationsResponse. 
     * <p/>
     * If response is not ready, call to <code>future.get()</code> will block the 
     * calling thread until response is returned. 
     * <p/>
     * Note, <code>future.get()</code> will throw wrapped runtime exception. 
     * <p/>
     * If service error has occured, MarketplaceWebServiceSellersException can be extracted with
     * <code>exception.getCause()</code>
     * <p/>
     * Usage example for parallel processing:
     * <pre>
     *
     *  List&lt;Future&lt;ListMarketplaceParticipationsResponse&gt;&gt; responses = new ArrayList&lt;Future&lt;ListMarketplaceParticipationsResponse&gt;&gt;();
     *  for (ListMarketplaceParticipationsRequest request : requests) {
     *      responses.add(client.listMarketplaceParticipationsAsync(request));
     *  }
     *  for (Future&lt;ListMarketplaceParticipationsResponse&gt; future : responses) {
     *      while (!future.isDone()) {
     *          Thread.yield();
     *      }
     *      try {
     *          ListMarketplaceParticipationsResponse response = future.get();
     *      // use response
     *      } catch (Exception e) {
     *          if (e instanceof MarketplaceWebServiceSellersException) {
     *              MarketplaceWebServiceSellersException exception = MarketplaceWebServiceSellersException.class.cast(e);
     *          // handle MarketplaceWebServiceSellersException
     *          } else {
     *          // handle other exceptions
     *          }
     *      }
     *  }
     * </pre>
     *
     * @param request
     *          ListMarketplaceParticipationsRequest request
     * @return Future&lt;ListMarketplaceParticipationsResponse&gt; future pointer to ListMarketplaceParticipationsResponse
     * 
     */
    public Future<ListMarketplaceParticipationsResponse> listMarketplaceParticipationsAsync(final ListMarketplaceParticipationsRequest request) {
        Future<ListMarketplaceParticipationsResponse> response = executor.submit(new Callable<ListMarketplaceParticipationsResponse>() {

            public ListMarketplaceParticipationsResponse call() throws MarketplaceWebServiceSellersException {
                return listMarketplaceParticipations(request);
            }
        });
        return response;
    }


            

    /**
     * Non-blocking List Marketplace Participations By Next Token 
     * <p/>
     * Returns <code>future</code> pointer to ListMarketplaceParticipationsByNextTokenResponse
     * <p/>
     * If response is ready, call to <code>future.get()</code> 
     * will return ListMarketplaceParticipationsByNextTokenResponse. 
     * <p/>
     * If response is not ready, call to <code>future.get()</code> will block the 
     * calling thread until response is returned. 
     * <p/>
     * Note, <code>future.get()</code> will throw wrapped runtime exception. 
     * <p/>
     * If service error has occured, MarketplaceWebServiceSellersException can be extracted with
     * <code>exception.getCause()</code>
     * <p/>
     * Usage example for parallel processing:
     * <pre>
     *
     *  List&lt;Future&lt;ListMarketplaceParticipationsByNextTokenResponse&gt;&gt; responses = new ArrayList&lt;Future&lt;ListMarketplaceParticipationsByNextTokenResponse&gt;&gt;();
     *  for (ListMarketplaceParticipationsByNextTokenRequest request : requests) {
     *      responses.add(client.listMarketplaceParticipationsByNextTokenAsync(request));
     *  }
     *  for (Future&lt;ListMarketplaceParticipationsByNextTokenResponse&gt; future : responses) {
     *      while (!future.isDone()) {
     *          Thread.yield();
     *      }
     *      try {
     *          ListMarketplaceParticipationsByNextTokenResponse response = future.get();
     *      // use response
     *      } catch (Exception e) {
     *          if (e instanceof MarketplaceWebServiceSellersException) {
     *              MarketplaceWebServiceSellersException exception = MarketplaceWebServiceSellersException.class.cast(e);
     *          // handle MarketplaceWebServiceSellersException
     *          } else {
     *          // handle other exceptions
     *          }
     *      }
     *  }
     * </pre>
     *
     * @param request
     *          ListMarketplaceParticipationsByNextTokenRequest request
     * @return Future&lt;ListMarketplaceParticipationsByNextTokenResponse&gt; future pointer to ListMarketplaceParticipationsByNextTokenResponse
     * 
     */
    public Future<ListMarketplaceParticipationsByNextTokenResponse> listMarketplaceParticipationsByNextTokenAsync(final ListMarketplaceParticipationsByNextTokenRequest request) {
        Future<ListMarketplaceParticipationsByNextTokenResponse> response = executor.submit(new Callable<ListMarketplaceParticipationsByNextTokenResponse>() {

            public ListMarketplaceParticipationsByNextTokenResponse call() throws MarketplaceWebServiceSellersException {
                return listMarketplaceParticipationsByNextToken(request);
            }
        });
        return response;
    }


            

    /**
     * Non-blocking Get Service Status 
     * <p/>
     * Returns <code>future</code> pointer to GetServiceStatusResponse
     * <p/>
     * If response is ready, call to <code>future.get()</code> 
     * will return GetServiceStatusResponse. 
     * <p/>
     * If response is not ready, call to <code>future.get()</code> will block the 
     * calling thread until response is returned. 
     * <p/>
     * Note, <code>future.get()</code> will throw wrapped runtime exception. 
     * <p/>
     * If service error has occured, MarketplaceWebServiceSellersException can be extracted with
     * <code>exception.getCause()</code>
     * <p/>
     * Usage example for parallel processing:
     * <pre>
     *
     *  List&lt;Future&lt;GetServiceStatusResponse&gt;&gt; responses = new ArrayList&lt;Future&lt;GetServiceStatusResponse&gt;&gt;();
     *  for (GetServiceStatusRequest request : requests) {
     *      responses.add(client.getServiceStatusAsync(request));
     *  }
     *  for (Future&lt;GetServiceStatusResponse&gt; future : responses) {
     *      while (!future.isDone()) {
     *          Thread.yield();
     *      }
     *      try {
     *          GetServiceStatusResponse response = future.get();
     *      // use response
     *      } catch (Exception e) {
     *          if (e instanceof MarketplaceWebServiceSellersException) {
     *              MarketplaceWebServiceSellersException exception = MarketplaceWebServiceSellersException.class.cast(e);
     *          // handle MarketplaceWebServiceSellersException
     *          } else {
     *          // handle other exceptions
     *          }
     *      }
     *  }
     * </pre>
     *
     * @param request
     *          GetServiceStatusRequest request
     * @return Future&lt;GetServiceStatusResponse&gt; future pointer to GetServiceStatusResponse
     * 
     */
    public Future<GetServiceStatusResponse> getServiceStatusAsync(final GetServiceStatusRequest request) {
        Future<GetServiceStatusResponse> response = executor.submit(new Callable<GetServiceStatusResponse>() {

            public GetServiceStatusResponse call() throws MarketplaceWebServiceSellersException {
                return getServiceStatus(request);
            }
        });
        return response;
    }


}
