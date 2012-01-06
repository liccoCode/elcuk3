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

package com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;



/**

 *
 */
public class FBAInboundServiceMWSAsyncClient extends FBAInboundServiceMWSClient implements FBAInboundServiceMWSAsync {

    private ExecutorService executor;

    /**
     * Client to make asynchronous calls to the service. Please note, you should
     * configure executor with same number of concurrent threads as number of
     * http connections specified in FBAInboundServiceMWSConfig. Default number of
     * max http connections is 100.
     *
     * @param awsAccessKeyId AWS Access Key Id
     * @param awsSecretAccessKey AWS Secret Key
     * @param config service configuration. Pass new FBAInboundServiceMWSConfig() if you
     * plan to use defaults
     *
     * @param executor Executor service to manage asynchronous calls.
     *
     */
    @SuppressWarnings("serial")
    public FBAInboundServiceMWSAsyncClient(String awsAccessKeyId,
            String awsSecretAccessKey, String applicationName,
            String applicationVersion, FBAInboundServiceMWSConfig config) {
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
    public FBAInboundServiceMWSAsyncClient(String awsAccessKeyId,
            String awsSecretAccessKey, FBAInboundServiceMWSConfig config)
    {
        this(awsAccessKeyId, awsSecretAccessKey, APPLICATION_NAME,
                APPLICATION_VERSION, config);

    }

            

    /**
     * Non-blocking Create Inbound Shipment Plan 
     * <p/>
     * Returns <code>future</code> pointer to CreateInboundShipmentPlanResponse
     * <p/>
     * If response is ready, call to <code>future.get()</code> 
     * will return CreateInboundShipmentPlanResponse. 
     * <p/>
     * If response is not ready, call to <code>future.get()</code> will block the 
     * calling thread until response is returned. 
     * <p/>
     * Note, <code>future.get()</code> will throw wrapped runtime exception. 
     * <p/>
     * If service error has occured, FBAInboundServiceMWSException can be extracted with
     * <code>exception.getCause()</code>
     * <p/>
     * Usage example for parallel processing:
     * <pre>
     *
     *  List&lt;Future&lt;CreateInboundShipmentPlanResponse&gt;&gt; responses = new ArrayList&lt;Future&lt;CreateInboundShipmentPlanResponse&gt;&gt;();
     *  for (CreateInboundShipmentPlanRequest request : requests) {
     *      responses.add(client.createInboundShipmentPlanAsync(request));
     *  }
     *  for (Future&lt;CreateInboundShipmentPlanResponse&gt; future : responses) {
     *      while (!future.isDone()) {
     *          Thread.yield();
     *      }
     *      try {
     *          CreateInboundShipmentPlanResponse response = future.get();
     *      // use response
     *      } catch (Exception e) {
     *          if (e instanceof FBAInboundServiceMWSException) {
     *              FBAInboundServiceMWSException exception = FBAInboundServiceMWSException.class.cast(e);
     *          // handle FBAInboundServiceMWSException
     *          } else {
     *          // handle other exceptions
     *          }
     *      }
     *  }
     * </pre>
     *
     * @param request
     *          CreateInboundShipmentPlanRequest request
     * @return Future&lt;CreateInboundShipmentPlanResponse&gt; future pointer to CreateInboundShipmentPlanResponse
     * 
     */
    public Future<CreateInboundShipmentPlanResponse> createInboundShipmentPlanAsync(final CreateInboundShipmentPlanRequest request) {
        Future<CreateInboundShipmentPlanResponse> response = executor.submit(new Callable<CreateInboundShipmentPlanResponse>() {

            public CreateInboundShipmentPlanResponse call() throws FBAInboundServiceMWSException {
                return createInboundShipmentPlan(request);
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
     * If service error has occured, FBAInboundServiceMWSException can be extracted with
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
     *          if (e instanceof FBAInboundServiceMWSException) {
     *              FBAInboundServiceMWSException exception = FBAInboundServiceMWSException.class.cast(e);
     *          // handle FBAInboundServiceMWSException
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

            public GetServiceStatusResponse call() throws FBAInboundServiceMWSException {
                return getServiceStatus(request);
            }
        });
        return response;
    }


            

    /**
     * Non-blocking List Inbound Shipments 
     * <p/>
     * Returns <code>future</code> pointer to ListInboundShipmentsResponse
     * <p/>
     * If response is ready, call to <code>future.get()</code> 
     * will return ListInboundShipmentsResponse. 
     * <p/>
     * If response is not ready, call to <code>future.get()</code> will block the 
     * calling thread until response is returned. 
     * <p/>
     * Note, <code>future.get()</code> will throw wrapped runtime exception. 
     * <p/>
     * If service error has occured, FBAInboundServiceMWSException can be extracted with
     * <code>exception.getCause()</code>
     * <p/>
     * Usage example for parallel processing:
     * <pre>
     *
     *  List&lt;Future&lt;ListInboundShipmentsResponse&gt;&gt; responses = new ArrayList&lt;Future&lt;ListInboundShipmentsResponse&gt;&gt;();
     *  for (ListInboundShipmentsRequest request : requests) {
     *      responses.add(client.listInboundShipmentsAsync(request));
     *  }
     *  for (Future&lt;ListInboundShipmentsResponse&gt; future : responses) {
     *      while (!future.isDone()) {
     *          Thread.yield();
     *      }
     *      try {
     *          ListInboundShipmentsResponse response = future.get();
     *      // use response
     *      } catch (Exception e) {
     *          if (e instanceof FBAInboundServiceMWSException) {
     *              FBAInboundServiceMWSException exception = FBAInboundServiceMWSException.class.cast(e);
     *          // handle FBAInboundServiceMWSException
     *          } else {
     *          // handle other exceptions
     *          }
     *      }
     *  }
     * </pre>
     *
     * @param request
     *          ListInboundShipmentsRequest request
     * @return Future&lt;ListInboundShipmentsResponse&gt; future pointer to ListInboundShipmentsResponse
     * 
     */
    public Future<ListInboundShipmentsResponse> listInboundShipmentsAsync(final ListInboundShipmentsRequest request) {
        Future<ListInboundShipmentsResponse> response = executor.submit(new Callable<ListInboundShipmentsResponse>() {

            public ListInboundShipmentsResponse call() throws FBAInboundServiceMWSException {
                return listInboundShipments(request);
            }
        });
        return response;
    }


            

    /**
     * Non-blocking List Inbound Shipments By Next Token 
     * <p/>
     * Returns <code>future</code> pointer to ListInboundShipmentsByNextTokenResponse
     * <p/>
     * If response is ready, call to <code>future.get()</code> 
     * will return ListInboundShipmentsByNextTokenResponse. 
     * <p/>
     * If response is not ready, call to <code>future.get()</code> will block the 
     * calling thread until response is returned. 
     * <p/>
     * Note, <code>future.get()</code> will throw wrapped runtime exception. 
     * <p/>
     * If service error has occured, FBAInboundServiceMWSException can be extracted with
     * <code>exception.getCause()</code>
     * <p/>
     * Usage example for parallel processing:
     * <pre>
     *
     *  List&lt;Future&lt;ListInboundShipmentsByNextTokenResponse&gt;&gt; responses = new ArrayList&lt;Future&lt;ListInboundShipmentsByNextTokenResponse&gt;&gt;();
     *  for (ListInboundShipmentsByNextTokenRequest request : requests) {
     *      responses.add(client.listInboundShipmentsByNextTokenAsync(request));
     *  }
     *  for (Future&lt;ListInboundShipmentsByNextTokenResponse&gt; future : responses) {
     *      while (!future.isDone()) {
     *          Thread.yield();
     *      }
     *      try {
     *          ListInboundShipmentsByNextTokenResponse response = future.get();
     *      // use response
     *      } catch (Exception e) {
     *          if (e instanceof FBAInboundServiceMWSException) {
     *              FBAInboundServiceMWSException exception = FBAInboundServiceMWSException.class.cast(e);
     *          // handle FBAInboundServiceMWSException
     *          } else {
     *          // handle other exceptions
     *          }
     *      }
     *  }
     * </pre>
     *
     * @param request
     *          ListInboundShipmentsByNextTokenRequest request
     * @return Future&lt;ListInboundShipmentsByNextTokenResponse&gt; future pointer to ListInboundShipmentsByNextTokenResponse
     * 
     */
    public Future<ListInboundShipmentsByNextTokenResponse> listInboundShipmentsByNextTokenAsync(final ListInboundShipmentsByNextTokenRequest request) {
        Future<ListInboundShipmentsByNextTokenResponse> response = executor.submit(new Callable<ListInboundShipmentsByNextTokenResponse>() {

            public ListInboundShipmentsByNextTokenResponse call() throws FBAInboundServiceMWSException {
                return listInboundShipmentsByNextToken(request);
            }
        });
        return response;
    }


            

    /**
     * Non-blocking Update Inbound Shipment 
     * <p/>
     * Returns <code>future</code> pointer to UpdateInboundShipmentResponse
     * <p/>
     * If response is ready, call to <code>future.get()</code> 
     * will return UpdateInboundShipmentResponse. 
     * <p/>
     * If response is not ready, call to <code>future.get()</code> will block the 
     * calling thread until response is returned. 
     * <p/>
     * Note, <code>future.get()</code> will throw wrapped runtime exception. 
     * <p/>
     * If service error has occured, FBAInboundServiceMWSException can be extracted with
     * <code>exception.getCause()</code>
     * <p/>
     * Usage example for parallel processing:
     * <pre>
     *
     *  List&lt;Future&lt;UpdateInboundShipmentResponse&gt;&gt; responses = new ArrayList&lt;Future&lt;UpdateInboundShipmentResponse&gt;&gt;();
     *  for (UpdateInboundShipmentRequest request : requests) {
     *      responses.add(client.updateInboundShipmentAsync(request));
     *  }
     *  for (Future&lt;UpdateInboundShipmentResponse&gt; future : responses) {
     *      while (!future.isDone()) {
     *          Thread.yield();
     *      }
     *      try {
     *          UpdateInboundShipmentResponse response = future.get();
     *      // use response
     *      } catch (Exception e) {
     *          if (e instanceof FBAInboundServiceMWSException) {
     *              FBAInboundServiceMWSException exception = FBAInboundServiceMWSException.class.cast(e);
     *          // handle FBAInboundServiceMWSException
     *          } else {
     *          // handle other exceptions
     *          }
     *      }
     *  }
     * </pre>
     *
     * @param request
     *          UpdateInboundShipmentRequest request
     * @return Future&lt;UpdateInboundShipmentResponse&gt; future pointer to UpdateInboundShipmentResponse
     * 
     */
    public Future<UpdateInboundShipmentResponse> updateInboundShipmentAsync(final UpdateInboundShipmentRequest request) {
        Future<UpdateInboundShipmentResponse> response = executor.submit(new Callable<UpdateInboundShipmentResponse>() {

            public UpdateInboundShipmentResponse call() throws FBAInboundServiceMWSException {
                return updateInboundShipment(request);
            }
        });
        return response;
    }


            

    /**
     * Non-blocking Create Inbound Shipment 
     * <p/>
     * Returns <code>future</code> pointer to CreateInboundShipmentResponse
     * <p/>
     * If response is ready, call to <code>future.get()</code> 
     * will return CreateInboundShipmentResponse. 
     * <p/>
     * If response is not ready, call to <code>future.get()</code> will block the 
     * calling thread until response is returned. 
     * <p/>
     * Note, <code>future.get()</code> will throw wrapped runtime exception. 
     * <p/>
     * If service error has occured, FBAInboundServiceMWSException can be extracted with
     * <code>exception.getCause()</code>
     * <p/>
     * Usage example for parallel processing:
     * <pre>
     *
     *  List&lt;Future&lt;CreateInboundShipmentResponse&gt;&gt; responses = new ArrayList&lt;Future&lt;CreateInboundShipmentResponse&gt;&gt;();
     *  for (CreateInboundShipmentRequest request : requests) {
     *      responses.add(client.createInboundShipmentAsync(request));
     *  }
     *  for (Future&lt;CreateInboundShipmentResponse&gt; future : responses) {
     *      while (!future.isDone()) {
     *          Thread.yield();
     *      }
     *      try {
     *          CreateInboundShipmentResponse response = future.get();
     *      // use response
     *      } catch (Exception e) {
     *          if (e instanceof FBAInboundServiceMWSException) {
     *              FBAInboundServiceMWSException exception = FBAInboundServiceMWSException.class.cast(e);
     *          // handle FBAInboundServiceMWSException
     *          } else {
     *          // handle other exceptions
     *          }
     *      }
     *  }
     * </pre>
     *
     * @param request
     *          CreateInboundShipmentRequest request
     * @return Future&lt;CreateInboundShipmentResponse&gt; future pointer to CreateInboundShipmentResponse
     * 
     */
    public Future<CreateInboundShipmentResponse> createInboundShipmentAsync(final CreateInboundShipmentRequest request) {
        Future<CreateInboundShipmentResponse> response = executor.submit(new Callable<CreateInboundShipmentResponse>() {

            public CreateInboundShipmentResponse call() throws FBAInboundServiceMWSException {
                return createInboundShipment(request);
            }
        });
        return response;
    }


            

    /**
     * Non-blocking List Inbound Shipment Items By Next Token 
     * <p/>
     * Returns <code>future</code> pointer to ListInboundShipmentItemsByNextTokenResponse
     * <p/>
     * If response is ready, call to <code>future.get()</code> 
     * will return ListInboundShipmentItemsByNextTokenResponse. 
     * <p/>
     * If response is not ready, call to <code>future.get()</code> will block the 
     * calling thread until response is returned. 
     * <p/>
     * Note, <code>future.get()</code> will throw wrapped runtime exception. 
     * <p/>
     * If service error has occured, FBAInboundServiceMWSException can be extracted with
     * <code>exception.getCause()</code>
     * <p/>
     * Usage example for parallel processing:
     * <pre>
     *
     *  List&lt;Future&lt;ListInboundShipmentItemsByNextTokenResponse&gt;&gt; responses = new ArrayList&lt;Future&lt;ListInboundShipmentItemsByNextTokenResponse&gt;&gt;();
     *  for (ListInboundShipmentItemsByNextTokenRequest request : requests) {
     *      responses.add(client.listInboundShipmentItemsByNextTokenAsync(request));
     *  }
     *  for (Future&lt;ListInboundShipmentItemsByNextTokenResponse&gt; future : responses) {
     *      while (!future.isDone()) {
     *          Thread.yield();
     *      }
     *      try {
     *          ListInboundShipmentItemsByNextTokenResponse response = future.get();
     *      // use response
     *      } catch (Exception e) {
     *          if (e instanceof FBAInboundServiceMWSException) {
     *              FBAInboundServiceMWSException exception = FBAInboundServiceMWSException.class.cast(e);
     *          // handle FBAInboundServiceMWSException
     *          } else {
     *          // handle other exceptions
     *          }
     *      }
     *  }
     * </pre>
     *
     * @param request
     *          ListInboundShipmentItemsByNextTokenRequest request
     * @return Future&lt;ListInboundShipmentItemsByNextTokenResponse&gt; future pointer to ListInboundShipmentItemsByNextTokenResponse
     * 
     */
    public Future<ListInboundShipmentItemsByNextTokenResponse> listInboundShipmentItemsByNextTokenAsync(final ListInboundShipmentItemsByNextTokenRequest request) {
        Future<ListInboundShipmentItemsByNextTokenResponse> response = executor.submit(new Callable<ListInboundShipmentItemsByNextTokenResponse>() {

            public ListInboundShipmentItemsByNextTokenResponse call() throws FBAInboundServiceMWSException {
                return listInboundShipmentItemsByNextToken(request);
            }
        });
        return response;
    }


            

    /**
     * Non-blocking List Inbound Shipment Items 
     * <p/>
     * Returns <code>future</code> pointer to ListInboundShipmentItemsResponse
     * <p/>
     * If response is ready, call to <code>future.get()</code> 
     * will return ListInboundShipmentItemsResponse. 
     * <p/>
     * If response is not ready, call to <code>future.get()</code> will block the 
     * calling thread until response is returned. 
     * <p/>
     * Note, <code>future.get()</code> will throw wrapped runtime exception. 
     * <p/>
     * If service error has occured, FBAInboundServiceMWSException can be extracted with
     * <code>exception.getCause()</code>
     * <p/>
     * Usage example for parallel processing:
     * <pre>
     *
     *  List&lt;Future&lt;ListInboundShipmentItemsResponse&gt;&gt; responses = new ArrayList&lt;Future&lt;ListInboundShipmentItemsResponse&gt;&gt;();
     *  for (ListInboundShipmentItemsRequest request : requests) {
     *      responses.add(client.listInboundShipmentItemsAsync(request));
     *  }
     *  for (Future&lt;ListInboundShipmentItemsResponse&gt; future : responses) {
     *      while (!future.isDone()) {
     *          Thread.yield();
     *      }
     *      try {
     *          ListInboundShipmentItemsResponse response = future.get();
     *      // use response
     *      } catch (Exception e) {
     *          if (e instanceof FBAInboundServiceMWSException) {
     *              FBAInboundServiceMWSException exception = FBAInboundServiceMWSException.class.cast(e);
     *          // handle FBAInboundServiceMWSException
     *          } else {
     *          // handle other exceptions
     *          }
     *      }
     *  }
     * </pre>
     *
     * @param request
     *          ListInboundShipmentItemsRequest request
     * @return Future&lt;ListInboundShipmentItemsResponse&gt; future pointer to ListInboundShipmentItemsResponse
     * 
     */
    public Future<ListInboundShipmentItemsResponse> listInboundShipmentItemsAsync(final ListInboundShipmentItemsRequest request) {
        Future<ListInboundShipmentItemsResponse> response = executor.submit(new Callable<ListInboundShipmentItemsResponse>() {

            public ListInboundShipmentItemsResponse call() throws FBAInboundServiceMWSException {
                return listInboundShipmentItems(request);
            }
        });
        return response;
    }


}
