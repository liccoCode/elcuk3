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
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;





/**

 *
 *
 * FBAInboundServiceMWSClient is implementation of FBAInboundServiceMWS based on the
 * Apache <a href="http://jakarta.apache.org/commons/httpclient/">HttpClient</a>.
 *
 */
public  class FBAInboundServiceMWSClient implements FBAInboundServiceMWS {

    final Log log = LogFactory.getLog(FBAInboundServiceMWSClient.class);

    private String awsAccessKeyId = null;
    private String awsSecretAccessKey = null;
    private FBAInboundServiceMWSConfig config = null;
    private HttpClient httpClient = null;
    private static JAXBContext  jaxbContext;
    private static ThreadLocal<Unmarshaller> unmarshaller;
    private static Pattern ERROR_PATTERN_ONE = Pattern.compile(".*\\<RequestId>(.*)\\</RequestId>.*\\<Error>" +
            "\\<Code>(.*)\\</Code>\\<Message>(.*)\\</Message>\\</Error>.*(\\<Error>)?.*",
            Pattern.MULTILINE | Pattern.DOTALL);
    private static Pattern ERROR_PATTERN_TWO = Pattern.compile(".*\\<Error>\\<Code>(.*)\\</Code>\\<Message>(.*)" +
            "\\</Message>\\</Error>.*(\\<Error>)?.*\\<RequestID>(.*)\\</RequestID>.*",
            Pattern.MULTILINE | Pattern.DOTALL);
    private static String DEFAULT_ENCODING = "UTF-8";
    protected static String APPLICATION_NAME = "FBAInboundServiceMWS";
    protected static String APPLICATION_VERSION = "2010-10-01";
    protected static String MWS_CLIENT_VERSION= "2010-10-01";
    /** Initialize JAXBContext and  Unmarshaller **/
    static {
        try {
            jaxbContext = JAXBContext.newInstance("com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model", FBAInboundServiceMWS.class.getClassLoader());
        } catch (JAXBException ex) {
            throw new ExceptionInInitializerError(ex);
        }
        unmarshaller = new ThreadLocal<Unmarshaller>() {
            @Override
            protected synchronized Unmarshaller initialValue() {
                try {
                    return jaxbContext.createUnmarshaller();
                } catch(JAXBException e) {
                    throw new ExceptionInInitializerError(e);
                }
            }
        };
    }


    /**
     * Constructs FBAInboundServiceMWSClient with AWS Access Key ID and AWS Secret Key
     *
     * @param awsAccessKeyId
     *          AWS Access Key ID
     * @param awsSecretAccessKey
     *          AWS Secret Access Key
     */
    public  FBAInboundServiceMWSClient(String awsAccessKeyId,String awsSecretAccessKey, FBAInboundServiceMWSConfig config) {
        this (awsAccessKeyId, awsSecretAccessKey, APPLICATION_NAME, APPLICATION_VERSION, config);
    }

    /**
     * Constructs FBAInboundServiceMWSClient with AWS Access Key ID and AWS Secret Key
     *
     * @param awsAccessKeyId
     *          AWS Access Key ID
     * @param awsSecretAccessKey
     *          AWS Secret Access Key
     */
    public  FBAInboundServiceMWSClient(String awsAccessKeyId,String awsSecretAccessKey, String applicationName,
            String applicationVersion) {
        this (awsAccessKeyId, awsSecretAccessKey, applicationName, applicationVersion, new FBAInboundServiceMWSConfig());
    }


    /**
     * Constructs FBAInboundServiceMWSClient with AWS Access Key ID, AWS Secret Key
     * and FBAInboundServiceMWSConfig. Use FBAInboundServiceMWSConfig to pass additional
     * configuration that affects how service is being called.
     *
     * @param awsAccessKeyId
     *          AWS Access Key ID
     * @param awsSecretAccessKey
     *          AWS Secret Access Key
     * @param config
     *          Additional configuration options
     */
    public  FBAInboundServiceMWSClient(String awsAccessKeyId, String awsSecretAccessKey,  String applicationName,
            String applicationVersion,
            FBAInboundServiceMWSConfig config) {
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
        this.config = config;
        this.httpClient = configureHttpClient(applicationName,
                applicationVersion);
    }

    // Public API ------------------------------------------------------------//


        
    /**
     * Create Inbound Shipment Plan 
     *
     * Plans inbound shipments for a set of items.  Registers identifiers if needed,
     * and assigns ShipmentIds for planned shipments.
     * When all the items are not all in the same category (e.g. some sortable, some
     * non-sortable) it may be necessary to create multiple shipments (one for each
     * of the shipment groups returned).
     * @param request
     *          CreateInboundShipmentPlanRequest request
     * @return
     *          CreateInboundShipmentPlan Response from the service
     *
     * @throws FBAInboundServiceMWSException
     */
    public CreateInboundShipmentPlanResponse createInboundShipmentPlan(CreateInboundShipmentPlanRequest request) throws FBAInboundServiceMWSException {
        return invoke(CreateInboundShipmentPlanResponse.class, convertCreateInboundShipmentPlan(request));
    }

        
    /**
     * Get Service Status 
     *
     * Gets the status of the service.
     * Status is one of GREEN, RED representing:
     * GREEN: This API section of the service is operating normally.
     * RED: The service is disrupted.
     * @param request
     *          GetServiceStatusRequest request
     * @return
     *          GetServiceStatus Response from the service
     *
     * @throws FBAInboundServiceMWSException
     */
    public GetServiceStatusResponse getServiceStatus(GetServiceStatusRequest request) throws FBAInboundServiceMWSException {
        return invoke(GetServiceStatusResponse.class, convertGetServiceStatus(request));
    }

        
    /**
     * List Inbound Shipments 
     *
     * Get the first set of inbound shipments created by a Seller according to
     * the specified shipment status or the specified shipment Id. A NextToken
     * is also returned to further iterate through the Seller's remaining
     * shipments. If a NextToken is not returned, it indicates the
     * end-of-data.
     * At least one of ShipmentStatusList and ShipmentIdList must be passed in.
     * if both are passed in, then only shipments that match the specified
     * shipment Id and specified shipment status will be returned.
     * the LastUpdatedBefore and LastUpdatedAfter are optional, they are used
     * to filter results based on last update time of the shipment.
     * @param request
     *          ListInboundShipmentsRequest request
     * @return
     *          ListInboundShipments Response from the service
     *
     * @throws FBAInboundServiceMWSException
     */
    public ListInboundShipmentsResponse listInboundShipments(ListInboundShipmentsRequest request) throws FBAInboundServiceMWSException {
        return invoke(ListInboundShipmentsResponse.class, convertListInboundShipments(request));
    }

        
    /**
     * List Inbound Shipments By Next Token 
     *
     * Gets the next set of inbound shipments created by a Seller with the
     * NextToken which can be used to iterate through the remaining inbound
     * shipments. If a NextToken is not returned, it indicates the
     * end-of-data.
     * @param request
     *          ListInboundShipmentsByNextTokenRequest request
     * @return
     *          ListInboundShipmentsByNextToken Response from the service
     *
     * @throws FBAInboundServiceMWSException
     */
    public ListInboundShipmentsByNextTokenResponse listInboundShipmentsByNextToken(ListInboundShipmentsByNextTokenRequest request) throws FBAInboundServiceMWSException {
        return invoke(ListInboundShipmentsByNextTokenResponse.class, convertListInboundShipmentsByNextToken(request));
    }

        
    /**
     * Update Inbound Shipment 
     *
     * Updates an pre-existing inbound shipment specified by the
     * ShipmentId. It may include up to 200 items.
     * If InboundShipmentHeader is set. it replaces the header information
     * for the given shipment.
     * If InboundShipmentItems is set. it adds, replaces and removes
     * the line time to inbound shipment.
     * For non-existing item, it will add the item for new line item;
     * For existing line items, it will replace the QuantityShipped for the item.
     * For QuantityShipped = 0, it indicates the item should be removed from the shipment
     * This operation will simply return a shipment Id upon success,
     * otherwise an explicit error will be returned.
     * @param request
     *          UpdateInboundShipmentRequest request
     * @return
     *          UpdateInboundShipment Response from the service
     *
     * @throws FBAInboundServiceMWSException
     */
    public UpdateInboundShipmentResponse updateInboundShipment(UpdateInboundShipmentRequest request) throws FBAInboundServiceMWSException {
        return invoke(UpdateInboundShipmentResponse.class, convertUpdateInboundShipment(request));
    }

        
    /**
     * Create Inbound Shipment 
     *
     * Creates an inbound shipment. It may include up to 200 items.
     * The initial status of a shipment will be set to 'Working'.
     * This operation will simply return a shipment Id upon success,
     * otherwise an explicit error will be returned.
     * More items may be added using the Update call.
     * @param request
     *          CreateInboundShipmentRequest request
     * @return
     *          CreateInboundShipment Response from the service
     *
     * @throws FBAInboundServiceMWSException
     */
    public CreateInboundShipmentResponse createInboundShipment(CreateInboundShipmentRequest request) throws FBAInboundServiceMWSException {
        return invoke(CreateInboundShipmentResponse.class, convertCreateInboundShipment(request));
    }

        
    /**
     * List Inbound Shipment Items By Next Token 
     *
     * Gets the next set of inbound shipment items with the NextToken
     * which can be used to iterate through the remaining inbound shipment
     * items. If a NextToken is not returned, it indicates the
     * end-of-data. You must first call ListInboundShipmentItems to get
     * a 'NextToken'.
     * @param request
     *          ListInboundShipmentItemsByNextTokenRequest request
     * @return
     *          ListInboundShipmentItemsByNextToken Response from the service
     *
     * @throws FBAInboundServiceMWSException
     */
    public ListInboundShipmentItemsByNextTokenResponse listInboundShipmentItemsByNextToken(ListInboundShipmentItemsByNextTokenRequest request) throws FBAInboundServiceMWSException {
        return invoke(ListInboundShipmentItemsByNextTokenResponse.class, convertListInboundShipmentItemsByNextToken(request));
    }

        
    /**
     * List Inbound Shipment Items 
     *
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
     * @param request
     *          ListInboundShipmentItemsRequest request
     * @return
     *          ListInboundShipmentItems Response from the service
     *
     * @throws FBAInboundServiceMWSException
     */
    public ListInboundShipmentItemsResponse listInboundShipmentItems(ListInboundShipmentItemsRequest request) throws FBAInboundServiceMWSException {
        return invoke(ListInboundShipmentItemsResponse.class, convertListInboundShipmentItems(request));
    }



    // Private API ------------------------------------------------------------//

    /**
     * Configure HttpClient with set of defaults as well as configuration
     * from FBAInboundServiceMWSConfig instance
     *
     */
    private HttpClient configureHttpClient(String applicationName,
            String applicationVersion) {

        /* Set http client parameters */
        HttpClientParams httpClientParams = new HttpClientParams();

        // respect a user-provided User-Agent header as-is, but if none is provided
        // then generate one satisfying the MWS User-Agent requirements
        if(config.getUserAgent()==null) {
            config.setUserAgent(
                    quoteAppName(applicationName), 
                    quoteAppVersion(applicationVersion), 
                    quoteAttributeValue("Java/"+System.getProperty("java.version")+
                    "/"+System.getProperty("java.class.version")+
                    "/"+System.getProperty("java.vendor")), 
                    
                    quoteAttributeName("Platform"), 
                    quoteAttributeValue(""+System.getProperty("os.name")+
                    "/"+System.getProperty("os.arch")+
                    "/"+System.getProperty("os.version")),
                    
                    quoteAttributeName("MWSClientVersion"), 
                    quoteAttributeValue(MWS_CLIENT_VERSION));
        }

        httpClientParams.setParameter(HttpClientParams.RETRY_HANDLER, new HttpMethodRetryHandler() {

            public boolean retryMethod(HttpMethod method, IOException exception, int executionCount) {
                if (executionCount > 3) {
                    log.debug("Maximum Number of Retry attempts reached, will not retry");
                    return false;
                }
                log.debug("Retrying request. Attempt " + executionCount);
                if (exception instanceof NoHttpResponseException) {
                    log.debug("Retrying on NoHttpResponseException");
                    return true;
                }
                if (exception instanceof InterruptedIOException) {
                    log.debug("Will not retry on InterruptedIOException", exception);
                    return false;
                }
                if (exception instanceof UnknownHostException) {
                    log.debug("Will not retry on UnknownHostException", exception);
                    return false;
                }
                if (!method.isRequestSent()) {
                    log.debug("Retrying on failed sent request");
                    return true;
                }
                return false;
            }
        });

        /* Set host configuration */
        HostConfiguration hostConfiguration = new HostConfiguration();

        /* Set connection manager parameters */
        HttpConnectionManagerParams connectionManagerParams = new HttpConnectionManagerParams();
        connectionManagerParams.setConnectionTimeout(50000);
        connectionManagerParams.setSoTimeout(50000);
        connectionManagerParams.setStaleCheckingEnabled(true);
        connectionManagerParams.setTcpNoDelay(true);
        connectionManagerParams.setMaxTotalConnections(config.getMaxAsyncQueueSize());
        connectionManagerParams.setMaxConnectionsPerHost(hostConfiguration, config.getMaxAsyncQueueSize());

        /* Set connection manager */
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.setParams(connectionManagerParams);

        /* Set http client */
        httpClient = new HttpClient(httpClientParams, connectionManager);

        /* Set proxy if configured */
        if (config.isSetProxyHost() && config.isSetProxyPort()) {
            log.info("Configuring Proxy. Proxy Host: " + config.getProxyHost() +
                    "Proxy Port: " + config.getProxyPort() );
            hostConfiguration.setProxy(config.getProxyHost(), config.getProxyPort());
            if (config.isSetProxyUsername() &&   config.isSetProxyPassword()) {
                httpClient.getState().setProxyCredentials (new AuthScope(
                                          config.getProxyHost(),
                                          config.getProxyPort()),
                                          new UsernamePasswordCredentials(
                                              config.getProxyUsername(),
                                              config.getProxyPassword()));

            }
        }

        httpClient.setHostConfiguration(hostConfiguration);
        return httpClient;
    }

    /**
     * Invokes request using parameters from parameters map.
     * Returns response of the T type passed to this method
     */
    private <T> T invoke(Class<T> clazz, Map<String, String> parameters)
            throws FBAInboundServiceMWSException {

        String actionName = parameters.get("Action");
        T response = null;
        String responseBodyString = null;
        PostMethod method = new PostMethod(config.getServiceURL());
        int status = -1;

        log.debug("Invoking" + actionName + " request. Current parameters: " + parameters);

        try {

            /* Set content type and encoding */
            log.debug("Setting content-type to application/x-www-form-urlencoded; charset=" + DEFAULT_ENCODING.toLowerCase());
            method.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=" + DEFAULT_ENCODING.toLowerCase());
            /* Set X-Amazon-User-Agent to header */
            method.addRequestHeader("X-Amazon-User-Agent", config.getUserAgent());

            /* Add required request parameters and set request body */
            log.debug("Adding required parameters...");
            addRequiredParametersToRequest(method, parameters);
            log.debug("Done adding additional required parameteres. Parameters now: " + parameters);

            boolean shouldRetry = true;
            int retries = 0;
            do {
                log.debug("Sending Request to host:  " + config.getServiceURL());

                try {

                    /* Submit request */
                    status = httpClient.executeMethod(method);

                    /* Consume response stream */
                    responseBodyString = getResponsBodyAsString(method.getResponseBodyAsStream());

                    /* Successful response. Attempting to unmarshal into the <Action>Response type */
                    if (status == HttpStatus.SC_OK && responseBodyString != null) {
                        shouldRetry = false;
                        log.debug("Received Response. Status: " + status + ". " +
                                "Response Body: " + responseBodyString);
                        log.debug("Attempting to unmarshal into the " + actionName + "Response type...");
                        response = clazz.cast(getUnmarshaller().unmarshal(new StreamSource(new StringReader(responseBodyString))));

                        log.debug("Unmarshalled response into " + actionName + "Response type.");

                    } else { /* Unsucessful response. Attempting to unmarshall into ErrorResponse  type */

                        log.debug("Received Response. Status: " + status + ". " +
                                "Response Body: " + responseBodyString);

                        if ((status == HttpStatus.SC_INTERNAL_SERVER_ERROR
                            || status == HttpStatus.SC_SERVICE_UNAVAILABLE)
                            && pauseIfRetryNeeded(++retries)){
                            shouldRetry = true;
                        } else {
                            log.debug("Attempting to unmarshal into the ErrorResponse type...");
                            ErrorResponse errorResponse = (ErrorResponse) getUnmarshaller().unmarshal(new StreamSource(new StringReader(responseBodyString)));

                            log.debug("Unmarshalled response into the ErrorResponse type.");

                            com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.Error error = errorResponse.getError().get(0);

                                    throw new FBAInboundServiceMWSException(error.getMessage(),
                                    status,
                                    error.getCode(),
                                    error.getType(),
                                    errorResponse.getRequestId(),
                                    errorResponse.toXML());
                        }
                    }
                } catch (JAXBException je) {
                    /* Response cannot be unmarshalled neither as <Action>Response or ErrorResponse types.
                    Checking for other possible errors. */

                    log.debug ("Caught JAXBException", je);
                    log.debug("Response cannot be unmarshalled neither as " + actionName + "Response or ErrorResponse types." +
                            "Checking for other possible errors.");

                    FBAInboundServiceMWSException awse = processErrors(responseBodyString, status);

                    throw awse;

                } catch (IOException ioe) {
                    log.debug("Caught IOException exception", ioe);
                    throw new FBAInboundServiceMWSException("Internal Error", ioe);
                } catch (Exception e) {
                    log.debug("Caught Exception", e);
                    throw new FBAInboundServiceMWSException(e);
                } finally {
                    method.releaseConnection();
                }
            } while (shouldRetry);

        } catch (FBAInboundServiceMWSException se) {
            log.debug("Caught FBAInboundServiceMWSException", se);
            throw se;

        } catch (Throwable t) {
            log.debug("Caught Exception", t);
            throw new FBAInboundServiceMWSException(t);
        }
        return response;
    }

    /**
     * Read stream into string
     * @param input stream to read
     */
    private String getResponsBodyAsString(InputStream input) throws IOException {
        String responsBodyString = null;
        try {
            Reader reader = new InputStreamReader(input, DEFAULT_ENCODING);
            StringBuilder b = new StringBuilder();
            char[] c = new char[1024];
            int len;
            while (0 < (len = reader.read(c))) {
                b.append(c, 0, len);
            }
            responsBodyString = b.toString();
        } finally {
            input.close();
        }
        return responsBodyString;
    }

    /**
     * Exponential sleep on failed request. Sleeps and returns true if retry needed
     * @param retries current retry
     * @throws java.lang.InterruptedException
     */
    private boolean pauseIfRetryNeeded(int retries)
          throws InterruptedException {
        if (retries <= config.getMaxErrorRetry()) {
            long delay = (long) (Math.pow(4, retries) * 100L);
            log.debug("Retriable error detected, will retry in " + delay + "ms, attempt numer: " + retries);
            Thread.sleep(delay);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add authentication related and version parameter and set request body
     * with all of the parameters
     */
    private void addRequiredParametersToRequest(PostMethod method, Map<String, String> parameters)
            throws SignatureException {
        parameters.put("Version", config.getServiceVersion());
        parameters.put("SignatureVersion", "2");
        parameters.put("Timestamp", getFormattedTimestamp());
        parameters.put("AWSAccessKeyId",  this.awsAccessKeyId);
        parameters.put("Signature", signParameters(parameters, this.awsSecretAccessKey));
        for (Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // filter empty parameters
            if(key == null || key.equals("") || value == null || value.equals("")) continue;
            method.addParameter(key, value);
        }
    }

    private FBAInboundServiceMWSException processErrors(String responseString, int status)  {
        FBAInboundServiceMWSException ex = null;
        Matcher matcher = null;
        if (responseString != null && responseString.startsWith("<")) {
            matcher = ERROR_PATTERN_ONE.matcher(responseString);
            if (matcher.matches()) {
                ex = new FBAInboundServiceMWSException(matcher.group(3), status,
                        matcher.group(2), "Unknown", matcher.group(1), responseString);
            } else {
                matcher = ERROR_PATTERN_TWO.matcher(responseString);
                if (matcher.matches()) {
                    ex = new FBAInboundServiceMWSException(matcher.group(2), status,
                            matcher.group(1), "Unknown", matcher.group(4), responseString);
                } else {
                    ex =  new FBAInboundServiceMWSException("Internal Error", status);
                    log.debug("Service Error. Response Status: " + status);
                }
            }
        } else {
            ex =  new FBAInboundServiceMWSException("Internal Error", status);
            log.debug("Service Error. Response Status: " + status);
        }
        return ex;
    }

    /**
     * Formats date as ISO 8601 timestamp
     */
    private String getFormattedTimestamp() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date());
    }

    /**
     * Computes RFC 2104-compliant HMAC signature for request parameters
     * Implements AWS Signature, as per following spec:
     *
     * Signature Version is 2, string to sign is based on following:
     *
     *    1. The HTTP Request Method followed by an ASCII newline (%0A)
     *    2. The HTTP Host header in the form of lowercase host, followed by an ASCII newline.
     *    3. The URL encoded HTTP absolute path component of the URI
     *       (up to but not including the query string parameters);
     *       if this is empty use a forward '/'. This parameter is followed by an ASCII newline.
     *    4. The concatenation of all query string components (names and values)
     *       as UTF-8 characters which are URL encoded as per RFC 3986
     *       (hex characters MUST be uppercase), sorted using lexicographic byte ordering.
     *       Parameter names are separated from their values by the '=' character
     *       (ASCII character 61), even if the value is empty.
     *       Pairs of parameter and values are separated by the '&' character (ASCII code 38).
     *
     */
    private String signParameters(Map<String, String> parameters, String key)
            throws  SignatureException {

        String signatureVersion = parameters.get("SignatureVersion");
        String algorithm = "HmacSHA1";
        String stringToSign = null;
        if ("2".equals(signatureVersion)) {
            algorithm = config.getSignatureMethod();
            parameters.put("SignatureMethod", algorithm);
            stringToSign = calculateStringToSignV2(parameters);
        } else {
            throw new SignatureException("Invalid Signature Version specified");
        }
        log.debug("Calculated string to sign: " + stringToSign);
        return sign(stringToSign, key, algorithm);
    }

    /**
     * Calculate String to Sign for SignatureVersion 2
     * @param parameters request parameters
     * @return String to Sign
     * @throws java.security.SignatureException
     */
    private String calculateStringToSignV2(Map<String, String> parameters)
            throws SignatureException {
        StringBuilder data = new StringBuilder();
        data.append("POST");
        data.append("\n");
        URI endpoint = null;
        try {
            endpoint = new URI(config.getServiceURL());
        } catch (URISyntaxException ex) {
            log.error("URI Syntax Exception", ex);
            throw new SignatureException("URI Syntax Exception thrown " +
                    "while constructing string to sign", ex);
        }
        data.append(endpoint.getHost());
        if (!usesAStandardPort(config.getServiceURL())) {
            data.append(":");
            data.append(endpoint.getPort());
        }
        data.append("\n");
        String uri = endpoint.getPath();
        if (uri == null || uri.length() == 0) {
            uri = "/";
        }
        data.append(uri);
        data.append("\n");
        Map<String, String> sorted = new TreeMap<String, String>();
        sorted.putAll(parameters);
        Iterator<Map.Entry<String, String>> pairs = sorted.entrySet().iterator();
        while (pairs.hasNext()) {
            Map.Entry<String, String> pair = pairs.next();
            String value = pair.getValue();
            String key = pair.getKey();
            // filter empty parameters
            if(key == null || key.equals("") || value == null || value.equals("")) continue;
            data.append(urlEncode(key));
            data.append("=");
            data.append(urlEncode(value));
            if (pairs.hasNext()) {
                data.append("&");
            }
        }
        return data.toString();
    }

    private String urlEncode(String value, boolean path) {
        String encoded = null;
        try {
            encoded = URLEncoder.encode(value, DEFAULT_ENCODING)
                                        .replace("+", "%20")
                                        .replace("*", "%2A")
                                        .replace("%7E","~");
            if (path) {
                encoded = encoded.replace("%2F", "/");
            }
        } catch (UnsupportedEncodingException ex) {
            log.debug("Unsupported Encoding Exception", ex);
            throw new RuntimeException(ex);
        }
        return encoded;
    }

    /**
     * Computes RFC 2104-compliant HMAC signature.
     *
     */
    private String sign(String data, String key, String algorithm) throws SignatureException {
        byte [] signature;
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key.getBytes(), algorithm));
            signature = Base64.encodeBase64(mac.doFinal(data.getBytes(DEFAULT_ENCODING)));
        } catch (Exception e) {
            throw new SignatureException("Failed to generate signature: " + e.getMessage(), e);
        }

        return new String(signature);
    }

    /**
     * Get unmarshaller for current thread
     */
    private Unmarshaller getUnmarshaller() {
        return unmarshaller.get();
    }
    
    
    
                    
   /**
     * Convert CreateInboundShipmentPlanRequest to name value pairs
     */
    private Map<String, String> convertCreateInboundShipmentPlan(CreateInboundShipmentPlanRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "CreateInboundShipmentPlan");
        if (request.isSetSellerId()) {
            params.put("SellerId", request.getSellerId());
        }
        if (request.isSetMarketplace()) {
            params.put("Marketplace", request.getMarketplace());
        }
        if (request.isSetShipFromAddress()) {
            Address  shipFromAddress = request.getShipFromAddress();
            if (shipFromAddress.isSetName()) {
                params.put("ShipFromAddress" + "." + "Name", shipFromAddress.getName());
            }
            if (shipFromAddress.isSetAddressLine1()) {
                params.put("ShipFromAddress" + "." + "AddressLine1", shipFromAddress.getAddressLine1());
            }
            if (shipFromAddress.isSetAddressLine2()) {
                params.put("ShipFromAddress" + "." + "AddressLine2", shipFromAddress.getAddressLine2());
            }
            if (shipFromAddress.isSetDistrictOrCounty()) {
                params.put("ShipFromAddress" + "." + "DistrictOrCounty", shipFromAddress.getDistrictOrCounty());
            }
            if (shipFromAddress.isSetCity()) {
                params.put("ShipFromAddress" + "." + "City", shipFromAddress.getCity());
            }
            if (shipFromAddress.isSetStateOrProvinceCode()) {
                params.put("ShipFromAddress" + "." + "StateOrProvinceCode", shipFromAddress.getStateOrProvinceCode());
            }
            if (shipFromAddress.isSetCountryCode()) {
                params.put("ShipFromAddress" + "." + "CountryCode", shipFromAddress.getCountryCode());
            }
            if (shipFromAddress.isSetPostalCode()) {
                params.put("ShipFromAddress" + "." + "PostalCode", shipFromAddress.getPostalCode());
            }
        } 
        if (request.isSetLabelPrepPreference()) {
            params.put("LabelPrepPreference", request.getLabelPrepPreference());
        }
        if (request.isSetInboundShipmentPlanRequestItems()) {
            InboundShipmentPlanRequestItemList  inboundShipmentPlanRequestItems = request.getInboundShipmentPlanRequestItems();
            java.util.List<InboundShipmentPlanRequestItem> memberList = inboundShipmentPlanRequestItems.getMember();
            int memberListIndex = 1;
            for (InboundShipmentPlanRequestItem member : memberList) {
                if (member.isSetSellerSKU()) {
                    params.put("InboundShipmentPlanRequestItems" + "." + "member" + "."  + memberListIndex + "." + "SellerSKU", member.getSellerSKU());
                }
                if (member.isSetASIN()) {
                    params.put("InboundShipmentPlanRequestItems" + "." + "member" + "."  + memberListIndex + "." + "ASIN", member.getASIN());
                }
                if (member.isSetCondition()) {
                    params.put("InboundShipmentPlanRequestItems" + "." + "member" + "."  + memberListIndex + "." + "Condition", member.getCondition());
                }
                if (member.isSetQuantity()) {
                    params.put("InboundShipmentPlanRequestItems" + "." + "member" + "."  + memberListIndex + "." + "Quantity", member.getQuantity() + "");
                }

                memberListIndex++;
            }
        } 

        return params;
    }
        
        
    
    
                    
   /**
     * Convert GetServiceStatusRequest to name value pairs
     */
    private Map<String, String> convertGetServiceStatus(GetServiceStatusRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "GetServiceStatus");
        if (request.isSetSellerId()) {
            params.put("SellerId", request.getSellerId());
        }
        if (request.isSetMarketplace()) {
            params.put("Marketplace", request.getMarketplace());
        }

        return params;
    }
        
        
    
    
                    
   /**
     * Convert ListInboundShipmentsRequest to name value pairs
     */
    private Map<String, String> convertListInboundShipments(ListInboundShipmentsRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "ListInboundShipments");
        if (request.isSetSellerId()) {
            params.put("SellerId", request.getSellerId());
        }
        if (request.isSetMarketplace()) {
            params.put("Marketplace", request.getMarketplace());
        }
        if (request.isSetShipmentStatusList()) {
            ShipmentStatusList  shipmentStatusList = request.getShipmentStatusList();
            java.util.List<String> memberList  =  shipmentStatusList.getMember();
            int memberListIndex = 1;
            for  (String member : memberList) { 
                params.put("ShipmentStatusList" + "." + "member" + "."  + memberListIndex, member);
                memberListIndex++;
            }	
        } 
        if (request.isSetShipmentIdList()) {
            ShipmentIdList  shipmentIdList = request.getShipmentIdList();
            java.util.List<String> shipmentIdMemberList  =  shipmentIdList.getMember();
            int shipmentIdMemberListIndex = 1;
            for  (String shipmentIdMember : shipmentIdMemberList) { 
                params.put("ShipmentIdList" + "." + "member" + "."  + shipmentIdMemberListIndex, shipmentIdMember);
                shipmentIdMemberListIndex++;
            }	
        } 
        if (request.isSetLastUpdatedBefore()) {
            params.put("LastUpdatedBefore", request.getLastUpdatedBefore() + "");
        }
        if (request.isSetLastUpdatedAfter()) {
            params.put("LastUpdatedAfter", request.getLastUpdatedAfter() + "");
        }

        return params;
    }
        
        
    
    
                    
   /**
     * Convert ListInboundShipmentsByNextTokenRequest to name value pairs
     */
    private Map<String, String> convertListInboundShipmentsByNextToken(ListInboundShipmentsByNextTokenRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "ListInboundShipmentsByNextToken");
        if (request.isSetSellerId()) {
            params.put("SellerId", request.getSellerId());
        }
        if (request.isSetMarketplace()) {
            params.put("Marketplace", request.getMarketplace());
        }
        if (request.isSetNextToken()) {
            params.put("NextToken", request.getNextToken());
        }

        return params;
    }
        
        
    
    
                    
   /**
     * Convert UpdateInboundShipmentRequest to name value pairs
     */
    private Map<String, String> convertUpdateInboundShipment(UpdateInboundShipmentRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "UpdateInboundShipment");
        if (request.isSetSellerId()) {
            params.put("SellerId", request.getSellerId());
        }
        if (request.isSetMarketplace()) {
            params.put("Marketplace", request.getMarketplace());
        }
        if (request.isSetShipmentId()) {
            params.put("ShipmentId", request.getShipmentId());
        }
        if (request.isSetInboundShipmentHeader()) {
            InboundShipmentHeader  inboundShipmentHeader = request.getInboundShipmentHeader();
            if (inboundShipmentHeader.isSetShipmentName()) {
                params.put("InboundShipmentHeader" + "." + "ShipmentName", inboundShipmentHeader.getShipmentName());
            }
            if (inboundShipmentHeader.isSetShipFromAddress()) {
                Address  shipFromAddress = inboundShipmentHeader.getShipFromAddress();
                if (shipFromAddress.isSetName()) {
                    params.put("InboundShipmentHeader" + "." + "ShipFromAddress" + "." + "Name", shipFromAddress.getName());
                }
                if (shipFromAddress.isSetAddressLine1()) {
                    params.put("InboundShipmentHeader" + "." + "ShipFromAddress" + "." + "AddressLine1", shipFromAddress.getAddressLine1());
                }
                if (shipFromAddress.isSetAddressLine2()) {
                    params.put("InboundShipmentHeader" + "." + "ShipFromAddress" + "." + "AddressLine2", shipFromAddress.getAddressLine2());
                }
                if (shipFromAddress.isSetDistrictOrCounty()) {
                    params.put("InboundShipmentHeader" + "." + "ShipFromAddress" + "." + "DistrictOrCounty", shipFromAddress.getDistrictOrCounty());
                }
                if (shipFromAddress.isSetCity()) {
                    params.put("InboundShipmentHeader" + "." + "ShipFromAddress" + "." + "City", shipFromAddress.getCity());
                }
                if (shipFromAddress.isSetStateOrProvinceCode()) {
                    params.put("InboundShipmentHeader" + "." + "ShipFromAddress" + "." + "StateOrProvinceCode", shipFromAddress.getStateOrProvinceCode());
                }
                if (shipFromAddress.isSetCountryCode()) {
                    params.put("InboundShipmentHeader" + "." + "ShipFromAddress" + "." + "CountryCode", shipFromAddress.getCountryCode());
                }
                if (shipFromAddress.isSetPostalCode()) {
                    params.put("InboundShipmentHeader" + "." + "ShipFromAddress" + "." + "PostalCode", shipFromAddress.getPostalCode());
                }
            } 
            if (inboundShipmentHeader.isSetDestinationFulfillmentCenterId()) {
                params.put("InboundShipmentHeader" + "." + "DestinationFulfillmentCenterId", inboundShipmentHeader.getDestinationFulfillmentCenterId());
            }
            if (inboundShipmentHeader.isSetShipmentStatus()) {
                params.put("InboundShipmentHeader" + "." + "ShipmentStatus", inboundShipmentHeader.getShipmentStatus());
            }
            if (inboundShipmentHeader.isSetLabelPrepPreference()) {
                params.put("InboundShipmentHeader" + "." + "LabelPrepPreference", inboundShipmentHeader.getLabelPrepPreference());
            }
        } 
        if (request.isSetInboundShipmentItems()) {
            InboundShipmentItemList  inboundShipmentItems = request.getInboundShipmentItems();
            java.util.List<InboundShipmentItem> memberList = inboundShipmentItems.getMember();
            int memberListIndex = 1;
            for (InboundShipmentItem member : memberList) {
                if (member.isSetShipmentId()) {
                    params.put("InboundShipmentItems" + "." + "member" + "."  + memberListIndex + "." + "ShipmentId", member.getShipmentId());
                }
                if (member.isSetSellerSKU()) {
                    params.put("InboundShipmentItems" + "." + "member" + "."  + memberListIndex + "." + "SellerSKU", member.getSellerSKU());
                }
                if (member.isSetFulfillmentNetworkSKU()) {
                    params.put("InboundShipmentItems" + "." + "member" + "."  + memberListIndex + "." + "FulfillmentNetworkSKU", member.getFulfillmentNetworkSKU());
                }
                if (member.isSetQuantityShipped()) {
                    params.put("InboundShipmentItems" + "." + "member" + "."  + memberListIndex + "." + "QuantityShipped", member.getQuantityShipped() + "");
                }
                if (member.isSetQuantityReceived()) {
                    params.put("InboundShipmentItems" + "." + "member" + "."  + memberListIndex + "." + "QuantityReceived", member.getQuantityReceived() + "");
                }

                memberListIndex++;
            }
        } 

        return params;
    }
        
        
    
    
                    
   /**
     * Convert CreateInboundShipmentRequest to name value pairs
     */
    private Map<String, String> convertCreateInboundShipment(CreateInboundShipmentRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "CreateInboundShipment");
        if (request.isSetSellerId()) {
            params.put("SellerId", request.getSellerId());
        }
        if (request.isSetMarketplace()) {
            params.put("Marketplace", request.getMarketplace());
        }
        if (request.isSetShipmentId()) {
            params.put("ShipmentId", request.getShipmentId());
        }
        if (request.isSetInboundShipmentHeader()) {
            InboundShipmentHeader  inboundShipmentHeader = request.getInboundShipmentHeader();
            if (inboundShipmentHeader.isSetShipmentName()) {
                params.put("InboundShipmentHeader" + "." + "ShipmentName", inboundShipmentHeader.getShipmentName());
            }
            if (inboundShipmentHeader.isSetShipFromAddress()) {
                Address  shipFromAddress = inboundShipmentHeader.getShipFromAddress();
                if (shipFromAddress.isSetName()) {
                    params.put("InboundShipmentHeader" + "." + "ShipFromAddress" + "." + "Name", shipFromAddress.getName());
                }
                if (shipFromAddress.isSetAddressLine1()) {
                    params.put("InboundShipmentHeader" + "." + "ShipFromAddress" + "." + "AddressLine1", shipFromAddress.getAddressLine1());
                }
                if (shipFromAddress.isSetAddressLine2()) {
                    params.put("InboundShipmentHeader" + "." + "ShipFromAddress" + "." + "AddressLine2", shipFromAddress.getAddressLine2());
                }
                if (shipFromAddress.isSetDistrictOrCounty()) {
                    params.put("InboundShipmentHeader" + "." + "ShipFromAddress" + "." + "DistrictOrCounty", shipFromAddress.getDistrictOrCounty());
                }
                if (shipFromAddress.isSetCity()) {
                    params.put("InboundShipmentHeader" + "." + "ShipFromAddress" + "." + "City", shipFromAddress.getCity());
                }
                if (shipFromAddress.isSetStateOrProvinceCode()) {
                    params.put("InboundShipmentHeader" + "." + "ShipFromAddress" + "." + "StateOrProvinceCode", shipFromAddress.getStateOrProvinceCode());
                }
                if (shipFromAddress.isSetCountryCode()) {
                    params.put("InboundShipmentHeader" + "." + "ShipFromAddress" + "." + "CountryCode", shipFromAddress.getCountryCode());
                }
                if (shipFromAddress.isSetPostalCode()) {
                    params.put("InboundShipmentHeader" + "." + "ShipFromAddress" + "." + "PostalCode", shipFromAddress.getPostalCode());
                }
            } 
            if (inboundShipmentHeader.isSetDestinationFulfillmentCenterId()) {
                params.put("InboundShipmentHeader" + "." + "DestinationFulfillmentCenterId", inboundShipmentHeader.getDestinationFulfillmentCenterId());
            }
            if (inboundShipmentHeader.isSetShipmentStatus()) {
                params.put("InboundShipmentHeader" + "." + "ShipmentStatus", inboundShipmentHeader.getShipmentStatus());
            }
            if (inboundShipmentHeader.isSetLabelPrepPreference()) {
                params.put("InboundShipmentHeader" + "." + "LabelPrepPreference", inboundShipmentHeader.getLabelPrepPreference());
            }
        } 
        if (request.isSetInboundShipmentItems()) {
            InboundShipmentItemList  inboundShipmentItems = request.getInboundShipmentItems();
            java.util.List<InboundShipmentItem> memberList = inboundShipmentItems.getMember();
            int memberListIndex = 1;
            for (InboundShipmentItem member : memberList) {
                if (member.isSetShipmentId()) {
                    params.put("InboundShipmentItems" + "." + "member" + "."  + memberListIndex + "." + "ShipmentId", member.getShipmentId());
                }
                if (member.isSetSellerSKU()) {
                    params.put("InboundShipmentItems" + "." + "member" + "."  + memberListIndex + "." + "SellerSKU", member.getSellerSKU());
                }
                if (member.isSetFulfillmentNetworkSKU()) {
                    params.put("InboundShipmentItems" + "." + "member" + "."  + memberListIndex + "." + "FulfillmentNetworkSKU", member.getFulfillmentNetworkSKU());
                }
                if (member.isSetQuantityShipped()) {
                    params.put("InboundShipmentItems" + "." + "member" + "."  + memberListIndex + "." + "QuantityShipped", member.getQuantityShipped() + "");
                }
                if (member.isSetQuantityReceived()) {
                    params.put("InboundShipmentItems" + "." + "member" + "."  + memberListIndex + "." + "QuantityReceived", member.getQuantityReceived() + "");
                }

                memberListIndex++;
            }
        } 

        return params;
    }
        
        
    
    
                    
   /**
     * Convert ListInboundShipmentItemsByNextTokenRequest to name value pairs
     */
    private Map<String, String> convertListInboundShipmentItemsByNextToken(ListInboundShipmentItemsByNextTokenRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "ListInboundShipmentItemsByNextToken");
        if (request.isSetSellerId()) {
            params.put("SellerId", request.getSellerId());
        }
        if (request.isSetMarketplace()) {
            params.put("Marketplace", request.getMarketplace());
        }
        if (request.isSetNextToken()) {
            params.put("NextToken", request.getNextToken());
        }

        return params;
    }
        
        
    
    
                    
   /**
     * Convert ListInboundShipmentItemsRequest to name value pairs
     */
    private Map<String, String> convertListInboundShipmentItems(ListInboundShipmentItemsRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "ListInboundShipmentItems");
        if (request.isSetSellerId()) {
            params.put("SellerId", request.getSellerId());
        }
        if (request.isSetMarketplace()) {
            params.put("Marketplace", request.getMarketplace());
        }
        if (request.isSetShipmentId()) {
            params.put("ShipmentId", request.getShipmentId());
        }
        if (request.isSetLastUpdatedBefore()) {
            params.put("LastUpdatedBefore", request.getLastUpdatedBefore() + "");
        }
        if (request.isSetLastUpdatedAfter()) {
            params.put("LastUpdatedAfter", request.getLastUpdatedAfter() + "");
        }

        return params;
    }
        
        
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    


    /**
     * Remove all leading whitespace, trailing whitespace, repeated whitespace
     * and replace any interior whitespace with a single space
     */
    private static String clean(String s) {
        return s
            .replaceAll("\\s", " ")
            .replaceAll(" {2,}", " ")
            .trim();
    }

    public static String quoteAppName(String s) {
        return clean(s)
            .replace("\\", "\\\\")
            .replace("/", "\\/");
    }

    public static String quoteAppVersion(String s) {
        return clean(s)
            .replace("\\", "\\\\")
            .replace("(", "\\(");
    }

    public static String quoteAttributeName(String s) {
        return clean(s)
            .replace("\\", "\\\\")
            .replace("=", "\\=");
    }

    public static String quoteAttributeValue(String s) {
        return clean(s)
            .replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(")", "\\)");
    }
    private static boolean usesHttps(String url) {
        try {
            new HttpsURL(url) /* throws an exception if not HTTPS */;
            return true;
        } catch (URIException e) {
            return false;
        }
    }

    private static int extractPortNumber(String url, boolean usesHttps) {
        try {
            HttpURL httpUrl = usesHttps ? new HttpsURL(url) : new HttpURL(url);
            return httpUrl.getPort();
        } catch (URIException e) {
            throw new RuntimeException("not a URL", e);
        }
    }

    private static boolean usesAStandardPort(String url) {
        boolean usesHttps = usesHttps(url);
        int portNumber = extractPortNumber(url, usesHttps);
        return usesHttps && portNumber == HttpsURL.DEFAULT_PORT
            || !usesHttps && portNumber == HttpURL.DEFAULT_PORT;
    }

    private String urlEncode(String rawValue) {
        return urlEncode(rawValue, false);
    }
}