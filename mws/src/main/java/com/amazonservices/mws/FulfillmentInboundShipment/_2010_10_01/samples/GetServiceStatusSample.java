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

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWS;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSClient;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSConfig;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.GetServiceStatusRequest;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.GetServiceStatusResponse;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.GetServiceStatusResult;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.ResponseMetadata;

/**
 *
 * Get Service Status  Samples
 *
 *
 */
public class GetServiceStatusSample {

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
         * Uncomment to try advanced configuration options. Available options are:
         *
         *  - Proxy Host and Proxy Port
         *  - Service URL
         *
         ***********************************************************************/
        FBAInboundServiceMWSConfig config = new FBAInboundServiceMWSConfig();

        /************************************************************************
         * Uncomment to set the correct MWS endpoint. You can get your Country
         * Code from MWSEndpoint enum class, e.g. US.
         ************************************************************************/
        // config.setServiceURL(MWSEndpoint.<Your Country Code>.toString());

        /************************************************************************
         * Instantiate Http Client Implementation of FBA Inbound Service MWS 
         ***********************************************************************/
        // FBAInboundServiceMWS service = new FBAInboundServiceMWSClient(accessKeyId, secretAccessKey, config);
        FBAInboundServiceMWS service = new FBAInboundServiceMWSClient(accessKeyId, secretAccessKey, applicationName, applicationVersion, config);
 
        /************************************************************************
         * Uncomment to try out Mock Service that simulates FBA Inbound Service MWS 
         * responses without calling FBA Inbound Service MWS  service.
         *
         * Responses are loaded from local XML files. You can tweak XML files to
         * experiment with various outputs during development
         *
         * XML files available under com/amazonservices/mws/FulfillmentInboundShipment/_2010_10_01/mock tree
         *
         ***********************************************************************/
        // FBAInboundServiceMWS service = new FBAInboundServiceMWSMock();

        /************************************************************************
         * Setup request parameters and uncomment invoke to try out 
         * sample for Get Service Status 
         ***********************************************************************/
         GetServiceStatusRequest request = new GetServiceStatusRequest();
         // @TODO: set request parameters here
         request.setSellerId(sellerId);

         // invokeGetServiceStatus(service, request);

    }


                        
    /**
     * Get Service Status  request sample
     * Gets the status of the service.
     * Status is one of GREEN, RED representing:
     * GREEN: This API section of the service is operating normally.
     * RED: The service is disrupted.  
     * @param service instance of FBAInboundServiceMWS service
     * @param request Action to invoke
     */
    public static void invokeGetServiceStatus(FBAInboundServiceMWS service, GetServiceStatusRequest request) {
        try {
            
            GetServiceStatusResponse response = service.getServiceStatus(request);

            
            System.out.println ("GetServiceStatus Action Response");
            System.out.println ("=============================================================================");
            System.out.println ();

            System.out.println("    GetServiceStatusResponse");
            System.out.println();
            if (response.isSetGetServiceStatusResult()) {
                System.out.println("        GetServiceStatusResult");
                System.out.println();
                GetServiceStatusResult  getServiceStatusResult = response.getGetServiceStatusResult();
                if (getServiceStatusResult.isSetStatus()) {
                    System.out.println("            Status");
                    System.out.println();
                    System.out.println("                " + getServiceStatusResult.getStatus());
                    System.out.println();
                }
                if (getServiceStatusResult.isSetTimestamp()) {
                    System.out.println("            Timestamp");
                    System.out.println();
                    System.out.println("                " + getServiceStatusResult.getTimestamp());
                    System.out.println();
                }
            } 
            if (response.isSetResponseMetadata()) {
                System.out.println("        ResponseMetadata");
                System.out.println();
                ResponseMetadata  responseMetadata = response.getResponseMetadata();
                if (responseMetadata.isSetRequestId()) {
                    System.out.println("            RequestId");
                    System.out.println();
                    System.out.println("                " + responseMetadata.getRequestId());
                    System.out.println();
                }
            } 
            System.out.println();

           
        } catch (FBAInboundServiceMWSException ex) {
            
            System.out.println("Caught Exception: " + ex.getMessage());
            System.out.println("Response Status Code: " + ex.getStatusCode());
            System.out.println("Error Code: " + ex.getErrorCode());
            System.out.println("Error Type: " + ex.getErrorType());
            System.out.println("Request ID: " + ex.getRequestId());
            System.out.print("XML: " + ex.getXML());
        }
    }
                            
}
