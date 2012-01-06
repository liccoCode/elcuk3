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

package com.amazonservices.mws.FulfillmentInventory._2010_10_01;

import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;



/**
 * The inventory service allows sellers to stay up to date on the
 * status of inventory in Amazon’s fulfillment centers.
 * Check Inventory Status: Sellers can discover when inventory
 * items change status and get the current availability status to
 * keep product listing information up to date
 * 
 *
 */
public class FBAInventoryServiceMWSAsyncClient extends FBAInventoryServiceMWSClient implements FBAInventoryServiceMWSAsync {

    private ExecutorService executor;

    /**
     * Client to make asynchronous calls to the service. Please note, you should
     * configure executor with same number of concurrent threads as number of
     * http connections specified in FBAInventoryServiceMWSConfig. Default number of
     * max http connections is 100.
     *
     * @param awsAccessKeyId AWS Access Key Id
     * @param awsSecretAccessKey AWS Secret Key
     * @param config service configuration. Pass new FBAInventoryServiceMWSConfig() if you
     * plan to use defaults
     *
     * @param executor Executor service to manage asynchronous calls.
     *
     */
    @SuppressWarnings("serial")
    public FBAInventoryServiceMWSAsyncClient(String awsAccessKeyId,
            String awsSecretAccessKey, String applicationName,
            String applicationVersion, FBAInventoryServiceMWSConfig config) {
        super(awsAccessKeyId, awsSecretAccessKey, applicationName, applicationVersion, config);
        this.executor = new ThreadPoolExecutor(config.getMaxAsyncThreads(),
            config.getMaxAsyncThreads(), 60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(config.getMaxAsyncQueueSize())
            {
                @Override
                public boolean offer(Runnable task)
                {
                    log
                            .debug("Maximum number of concurrent threads reached, queuing task...");
                    return super.offer(task);
                }
            }, new ThreadFactory()
            {
                private final AtomicInteger threadNumber = new AtomicInteger(
                        1);

                public Thread newThread(Runnable task)
                {
                    Thread thread = new Thread(task,
                            "MWS-Fulfillment-Client-Thread-"
                                    + threadNumber.getAndIncrement());
                    thread.setDaemon(true);
                    if (thread.getPriority() != Thread.NORM_PRIORITY)
                    {
                        thread.setPriority(Thread.NORM_PRIORITY);
                    }
                    log.debug("ThreadFactory created new thread: "
                            + thread.getName());
                    return thread;
                }
            }, new RejectedExecutionHandler()
            {
                public void rejectedExecution(Runnable task,
                        ThreadPoolExecutor executor)
                {
                    log
                            .debug("Maximum number of concurrent threads reached, and queue is full. "
                                    + "Running task in the calling thread..."
                                    + Thread.currentThread().getName());
                    if (!executor.isShutdown())
                    {
                        task.run();
                    }
                }
            }
        );
    }

    /**
     * @param awsAccessKeyId
     * @param awsSecretAccessKey
     * @param config
     */
    public FBAInventoryServiceMWSAsyncClient(String awsAccessKeyId,
            String awsSecretAccessKey, FBAInventoryServiceMWSConfig config)
    {
        this(awsAccessKeyId, awsSecretAccessKey, APPLICATION_NAME,
                APPLICATION_VERSION, config);

    }

            

    /**
     * Non-blocking List Inventory Supply By Next Token 
     * <p/>
     * Returns <code>future</code> pointer to ListInventorySupplyByNextTokenResponse
     * <p/>
     * If response is ready, call to <code>future.get()</code> 
     * will return ListInventorySupplyByNextTokenResponse. 
     * <p/>
     * If response is not ready, call to <code>future.get()</code> will block the 
     * calling thread until response is returned. 
     * <p/>
     * Note, <code>future.get()</code> will throw wrapped runtime exception. 
     * <p/>
     * If service error has occured, FBAInventoryServiceMWSException can be extracted with
     * <code>exception.getCause()</code>
     * <p/>
     * Usage example for parallel processing:
     * <pre>
     *
     *  List&lt;Future&lt;ListInventorySupplyByNextTokenResponse&gt;&gt; responses = new ArrayList&lt;Future&lt;ListInventorySupplyByNextTokenResponse&gt;&gt;();
     *  for (ListInventorySupplyByNextTokenRequest request : requests) {
     *      responses.add(client.listInventorySupplyByNextTokenAsync(request));
     *  }
     *  for (Future&lt;ListInventorySupplyByNextTokenResponse&gt; future : responses) {
     *      while (!future.isDone()) {
     *          Thread.yield();
     *      }
     *      try {
     *          ListInventorySupplyByNextTokenResponse response = future.get();
     *      // use response
     *      } catch (Exception e) {
     *          if (e instanceof FBAInventoryServiceMWSException) {
     *              FBAInventoryServiceMWSException exception = FBAInventoryServiceMWSException.class.cast(e);
     *          // handle FBAInventoryServiceMWSException
     *          } else {
     *          // handle other exceptions
     *          }
     *      }
     *  }
     * </pre>
     *
     * @param request
     *          ListInventorySupplyByNextTokenRequest request
     * @return Future&lt;ListInventorySupplyByNextTokenResponse&gt; future pointer to ListInventorySupplyByNextTokenResponse
     * 
     */
    public Future<ListInventorySupplyByNextTokenResponse> listInventorySupplyByNextTokenAsync(final ListInventorySupplyByNextTokenRequest request) {
        Future<ListInventorySupplyByNextTokenResponse> response = executor.submit(new Callable<ListInventorySupplyByNextTokenResponse>() {

            public ListInventorySupplyByNextTokenResponse call() throws FBAInventoryServiceMWSException {
                return listInventorySupplyByNextToken(request);
            }
        });
        return response;
    }


            

    /**
     * Non-blocking List Inventory Supply 
     * <p/>
     * Returns <code>future</code> pointer to ListInventorySupplyResponse
     * <p/>
     * If response is ready, call to <code>future.get()</code> 
     * will return ListInventorySupplyResponse. 
     * <p/>
     * If response is not ready, call to <code>future.get()</code> will block the 
     * calling thread until response is returned. 
     * <p/>
     * Note, <code>future.get()</code> will throw wrapped runtime exception. 
     * <p/>
     * If service error has occured, FBAInventoryServiceMWSException can be extracted with
     * <code>exception.getCause()</code>
     * <p/>
     * Usage example for parallel processing:
     * <pre>
     *
     *  List&lt;Future&lt;ListInventorySupplyResponse&gt;&gt; responses = new ArrayList&lt;Future&lt;ListInventorySupplyResponse&gt;&gt;();
     *  for (ListInventorySupplyRequest request : requests) {
     *      responses.add(client.listInventorySupplyAsync(request));
     *  }
     *  for (Future&lt;ListInventorySupplyResponse&gt; future : responses) {
     *      while (!future.isDone()) {
     *          Thread.yield();
     *      }
     *      try {
     *          ListInventorySupplyResponse response = future.get();
     *      // use response
     *      } catch (Exception e) {
     *          if (e instanceof FBAInventoryServiceMWSException) {
     *              FBAInventoryServiceMWSException exception = FBAInventoryServiceMWSException.class.cast(e);
     *          // handle FBAInventoryServiceMWSException
     *          } else {
     *          // handle other exceptions
     *          }
     *      }
     *  }
     * </pre>
     *
     * @param request
     *          ListInventorySupplyRequest request
     * @return Future&lt;ListInventorySupplyResponse&gt; future pointer to ListInventorySupplyResponse
     * 
     */
    public Future<ListInventorySupplyResponse> listInventorySupplyAsync(final ListInventorySupplyRequest request) {
        Future<ListInventorySupplyResponse> response = executor.submit(new Callable<ListInventorySupplyResponse>() {

            public ListInventorySupplyResponse call() throws FBAInventoryServiceMWSException {
                return listInventorySupply(request);
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
     * If service error has occured, FBAInventoryServiceMWSException can be extracted with
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
     *          if (e instanceof FBAInventoryServiceMWSException) {
     *              FBAInventoryServiceMWSException exception = FBAInventoryServiceMWSException.class.cast(e);
     *          // handle FBAInventoryServiceMWSException
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

            public GetServiceStatusResponse call() throws FBAInventoryServiceMWSException {
                return getServiceStatus(request);
            }
        });
        return response;
    }


}
