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
 * The inventory service allows sellers to stay up to date on the
 * status of inventory in Amazon’s fulfillment centers.
 * Check Inventory Status: Sellers can discover when inventory
 * items change status and get the current availability status to
 * keep product listing information up to date
 * 
 *
 *
 * FBAInventoryServiceMWSClient is implementation of FBAInventoryServiceMWS based on the
 * Apache <a href="http://jakarta.apache.org/commons/httpclient/">HttpClient</a>.
 *
 */
public  class FBAInventoryServiceMWSClient implements FBAInventoryServiceMWS {

    final Log log = LogFactory.getLog(FBAInventoryServiceMWSClient.class);

    private String awsAccessKeyId = null;
    private String awsSecretAccessKey = null;
    private FBAInventoryServiceMWSConfig config = null;
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
    protected static String APPLICATION_NAME = "FBAInventoryServiceMWS";
    protected static String APPLICATION_VERSION = "2010-10-01";
    protected static String MWS_CLIENT_VERSION= "2010-10-01";
    /** Initialize JAXBContext and  Unmarshaller **/
    static {
        try {
            jaxbContext = JAXBContext.newInstance("com.amazonservices.mws.FulfillmentInventory._2010_10_01.model", FBAInventoryServiceMWS.class.getClassLoader());
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
     * Constructs FBAInventoryServiceMWSClient with AWS Access Key ID and AWS Secret Key
     *
     * @param awsAccessKeyId
     *          AWS Access Key ID
     * @param awsSecretAccessKey
     *          AWS Secret Access Key
     */
    public  FBAInventoryServiceMWSClient(String awsAccessKeyId,String awsSecretAccessKey, FBAInventoryServiceMWSConfig config) {
        this (awsAccessKeyId, awsSecretAccessKey, APPLICATION_NAME, APPLICATION_VERSION, config);
    }

    /**
     * Constructs FBAInventoryServiceMWSClient with AWS Access Key ID and AWS Secret Key
     *
     * @param awsAccessKeyId
     *          AWS Access Key ID
     * @param awsSecretAccessKey
     *          AWS Secret Access Key
     */
    public  FBAInventoryServiceMWSClient(String awsAccessKeyId,String awsSecretAccessKey, String applicationName,
            String applicationVersion) {
        this (awsAccessKeyId, awsSecretAccessKey, applicationName, applicationVersion, new FBAInventoryServiceMWSConfig());
    }


    /**
     * Constructs FBAInventoryServiceMWSClient with AWS Access Key ID, AWS Secret Key
     * and FBAInventoryServiceMWSConfig. Use FBAInventoryServiceMWSConfig to pass additional
     * configuration that affects how service is being called.
     *
     * @param awsAccessKeyId
     *          AWS Access Key ID
     * @param awsSecretAccessKey
     *          AWS Secret Access Key
     * @param config
     *          Additional configuration options
     */
    public  FBAInventoryServiceMWSClient(String awsAccessKeyId, String awsSecretAccessKey,  String applicationName,
            String applicationVersion,
            FBAInventoryServiceMWSConfig config) {
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
        this.config = config;
        this.httpClient = configureHttpClient(applicationName,
                applicationVersion);
    }

    // Public API ------------------------------------------------------------//


        
    /**
     * List Inventory Supply By Next Token 
     *
     * Continues pagination over a resultset of inventory data for inventory
     * items.
     * 
     * This operation is used in conjunction with ListUpdatedInventorySupply.
     * Please refer to documentation for that operation for further details.
     * 
     * @param request
     *          ListInventorySupplyByNextTokenRequest request
     * @return
     *          ListInventorySupplyByNextToken Response from the service
     *
     * @throws FBAInventoryServiceMWSException
     */
    public ListInventorySupplyByNextTokenResponse listInventorySupplyByNextToken(ListInventorySupplyByNextTokenRequest request) throws FBAInventoryServiceMWSException {
        return invoke(ListInventorySupplyByNextTokenResponse.class, convertListInventorySupplyByNextToken(request));
    }

        
    /**
     * List Inventory Supply 
     *
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
     * @param request
     *          ListInventorySupplyRequest request
     * @return
     *          ListInventorySupply Response from the service
     *
     * @throws FBAInventoryServiceMWSException
     */
    public ListInventorySupplyResponse listInventorySupply(ListInventorySupplyRequest request) throws FBAInventoryServiceMWSException {
        return invoke(ListInventorySupplyResponse.class, convertListInventorySupply(request));
    }

        
    /**
     * Get Service Status 
     *
     * Gets the status of the service.
     * Status is one of GREEN, RED representing:
     * GREEN: This API section of the service is operating normally.
     * RED: The service is disrupted.
     * 
     * @param request
     *          GetServiceStatusRequest request
     * @return
     *          GetServiceStatus Response from the service
     *
     * @throws FBAInventoryServiceMWSException
     */
    public GetServiceStatusResponse getServiceStatus(GetServiceStatusRequest request) throws FBAInventoryServiceMWSException {
        return invoke(GetServiceStatusResponse.class, convertGetServiceStatus(request));
    }



    // Private API ------------------------------------------------------------//

    /**
     * Configure HttpClient with set of defaults as well as configuration
     * from FBAInventoryServiceMWSConfig instance
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
            throws FBAInventoryServiceMWSException {

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

                            com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.Error error = errorResponse.getError().get(0);

                                    throw new FBAInventoryServiceMWSException(error.getMessage(),
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

                    FBAInventoryServiceMWSException awse = processErrors(responseBodyString, status);

                    throw awse;

                } catch (IOException ioe) {
                    log.debug("Caught IOException exception", ioe);
                    throw new FBAInventoryServiceMWSException("Internal Error", ioe);
                } catch (Exception e) {
                    log.debug("Caught Exception", e);
                    throw new FBAInventoryServiceMWSException(e);
                } finally {
                    method.releaseConnection();
                }
            } while (shouldRetry);

        } catch (FBAInventoryServiceMWSException se) {
            log.debug("Caught FBAInventoryServiceMWSException", se);
            throw se;

        } catch (Throwable t) {
            log.debug("Caught Exception", t);
            throw new FBAInventoryServiceMWSException(t);
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
     * @throws InterruptedException
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

    private FBAInventoryServiceMWSException processErrors(String responseString, int status)  {
        FBAInventoryServiceMWSException ex = null;
        Matcher matcher = null;
        if (responseString != null && responseString.startsWith("<")) {
            matcher = ERROR_PATTERN_ONE.matcher(responseString);
            if (matcher.matches()) {
                ex = new FBAInventoryServiceMWSException(matcher.group(3), status,
                        matcher.group(2), "Unknown", matcher.group(1), responseString);
            } else {
                matcher = ERROR_PATTERN_TWO.matcher(responseString);
                if (matcher.matches()) {
                    ex = new FBAInventoryServiceMWSException(matcher.group(2), status,
                            matcher.group(1), "Unknown", matcher.group(4), responseString);
                } else {
                    ex =  new FBAInventoryServiceMWSException("Internal Error", status);
                    log.debug("Service Error. Response Status: " + status);
                }
            }
        } else {
            ex =  new FBAInventoryServiceMWSException("Internal Error", status);
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
        Iterator<Entry<String, String>> pairs = sorted.entrySet().iterator();
        while (pairs.hasNext()) {
            Entry<String, String> pair = pairs.next();
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
     * Convert ListInventorySupplyByNextTokenRequest to name value pairs
     */
    private Map<String, String> convertListInventorySupplyByNextToken(ListInventorySupplyByNextTokenRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "ListInventorySupplyByNextToken");
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
     * Convert ListInventorySupplyRequest to name value pairs
     */
    private Map<String, String> convertListInventorySupply(ListInventorySupplyRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "ListInventorySupply");
        if (request.isSetSellerId()) {
            params.put("SellerId", request.getSellerId());
        }
        if (request.isSetMarketplace()) {
            params.put("Marketplace", request.getMarketplace());
        }
        if (request.isSetSellerSkus()) {
            SellerSkuList  sellerSkus = request.getSellerSkus();
            java.util.List<String> memberList  =  sellerSkus.getMember();
            int memberListIndex = 1;
            for  (String member : memberList) { 
                params.put("SellerSkus" + "." + "member" + "."  + memberListIndex, member);
                memberListIndex++;
            }	
        } 
        if (request.isSetQueryStartDateTime()) {
            params.put("QueryStartDateTime", request.getQueryStartDateTime() + "");
        }
        if (request.isSetResponseGroup()) {
            params.put("ResponseGroup", request.getResponseGroup());
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