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
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.*;

/**
 *
 * List Inbound Shipments  Samples
 *
 *
 */
public class ListInboundShipmentsSample {

    /**
     * Just add few required parameters, and try the service
     * List Inbound Shipments functionality
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
         * sample for List Inbound Shipments 
         ***********************************************************************/
         ListInboundShipmentsRequest request = new ListInboundShipmentsRequest();
         // @TODO: set request parameters here
         request.setSellerId(sellerId);

         // invokeListInboundShipments(service, request);

    }


                            
    /**
     * List Inbound Shipments  request sample
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
     * @param service instance of FBAInboundServiceMWS service
     * @param request Action to invoke
     */
    public static void invokeListInboundShipments(FBAInboundServiceMWS service, ListInboundShipmentsRequest request) {
        try {
            
            ListInboundShipmentsResponse response = service.listInboundShipments(request);

            
            System.out.println ("ListInboundShipments Action Response");
            System.out.println ("=============================================================================");
            System.out.println ();

            System.out.println("    ListInboundShipmentsResponse");
            System.out.println();
            if (response.isSetListInboundShipmentsResult()) {
                System.out.println("        ListInboundShipmentsResult");
                System.out.println();
                ListInboundShipmentsResult  listInboundShipmentsResult = response.getListInboundShipmentsResult();
                if (listInboundShipmentsResult.isSetShipmentData()) {
                    System.out.println("            ShipmentData");
                    System.out.println();
                    InboundShipmentList  shipmentData = listInboundShipmentsResult.getShipmentData();
                    java.util.List<InboundShipmentInfo> memberList = shipmentData.getMember();
                    for (InboundShipmentInfo member : memberList) {
                        System.out.println("                member");
                        System.out.println();
                        if (member.isSetShipmentId()) {
                            System.out.println("                    ShipmentId");
                            System.out.println();
                            System.out.println("                        " + member.getShipmentId());
                            System.out.println();
                        }
                        if (member.isSetShipmentName()) {
                            System.out.println("                    ShipmentName");
                            System.out.println();
                            System.out.println("                        " + member.getShipmentName());
                            System.out.println();
                        }
                        if (member.isSetShipFromAddress()) {
                            System.out.println("                    ShipFromAddress");
                            System.out.println();
                            Address  shipFromAddress = member.getShipFromAddress();
                            if (shipFromAddress.isSetName()) {
                                System.out.println("                        Name");
                                System.out.println();
                                System.out.println("                            " + shipFromAddress.getName());
                                System.out.println();
                            }
                            if (shipFromAddress.isSetAddressLine1()) {
                                System.out.println("                        AddressLine1");
                                System.out.println();
                                System.out.println("                            " + shipFromAddress.getAddressLine1());
                                System.out.println();
                            }
                            if (shipFromAddress.isSetAddressLine2()) {
                                System.out.println("                        AddressLine2");
                                System.out.println();
                                System.out.println("                            " + shipFromAddress.getAddressLine2());
                                System.out.println();
                            }
                            if (shipFromAddress.isSetDistrictOrCounty()) {
                                System.out.println("                        DistrictOrCounty");
                                System.out.println();
                                System.out.println("                            " + shipFromAddress.getDistrictOrCounty());
                                System.out.println();
                            }
                            if (shipFromAddress.isSetCity()) {
                                System.out.println("                        City");
                                System.out.println();
                                System.out.println("                            " + shipFromAddress.getCity());
                                System.out.println();
                            }
                            if (shipFromAddress.isSetStateOrProvinceCode()) {
                                System.out.println("                        StateOrProvinceCode");
                                System.out.println();
                                System.out.println("                            " + shipFromAddress.getStateOrProvinceCode());
                                System.out.println();
                            }
                            if (shipFromAddress.isSetCountryCode()) {
                                System.out.println("                        CountryCode");
                                System.out.println();
                                System.out.println("                            " + shipFromAddress.getCountryCode());
                                System.out.println();
                            }
                            if (shipFromAddress.isSetPostalCode()) {
                                System.out.println("                        PostalCode");
                                System.out.println();
                                System.out.println("                            " + shipFromAddress.getPostalCode());
                                System.out.println();
                            }
                        } 
                        if (member.isSetDestinationFulfillmentCenterId()) {
                            System.out.println("                    DestinationFulfillmentCenterId");
                            System.out.println();
                            System.out.println("                        " + member.getDestinationFulfillmentCenterId());
                            System.out.println();
                        }
                        if (member.isSetShipmentStatus()) {
                            System.out.println("                    ShipmentStatus");
                            System.out.println();
                            System.out.println("                        " + member.getShipmentStatus());
                            System.out.println();
                        }
                        if (member.isSetLabelPrepType()) {
                            System.out.println("                    LabelPrepType");
                            System.out.println();
                            System.out.println("                        " + member.getLabelPrepType());
                            System.out.println();
                        }
                    }
                } 
                if (listInboundShipmentsResult.isSetNextToken()) {
                    System.out.println("            NextToken");
                    System.out.println();
                    System.out.println("                " + listInboundShipmentsResult.getNextToken());
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
