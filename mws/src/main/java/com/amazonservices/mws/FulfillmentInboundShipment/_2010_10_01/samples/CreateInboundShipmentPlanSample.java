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
 * Create Inbound Shipment Plan  Samples
 *
 *
 */
public class CreateInboundShipmentPlanSample {

    /**
     * Just add few required parameters, and try the service
     * Create Inbound Shipment Plan functionality
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
//        FBAInboundServiceMWS service = new FBAInboundServiceMWSClient(accessKeyId, secretAccessKey, config);
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
         * sample for Create Inbound Shipment Plan 
         ***********************************************************************/
         CreateInboundShipmentPlanRequest request = new CreateInboundShipmentPlanRequest();
         // @TODO: set request parameters here
         request.setSellerId(sellerId);

         // invokeCreateInboundShipmentPlan(service, request);

    }


                    
    /**
     * Create Inbound Shipment Plan  request sample
     * Plans inbound shipments for a set of items.  Registers identifiers if needed,
     * and assigns ShipmentIds for planned shipments.
     * When all the items are not all in the same category (e.g. some sortable, some
     * non-sortable) it may be necessary to create multiple shipments (one for each
     * of the shipment groups returned).  
     * @param service instance of FBAInboundServiceMWS service
     * @param request Action to invoke
     */
    public static void invokeCreateInboundShipmentPlan(FBAInboundServiceMWS service, CreateInboundShipmentPlanRequest request) {
        try {
            
            CreateInboundShipmentPlanResponse response = service.createInboundShipmentPlan(request);

            
            System.out.println ("CreateInboundShipmentPlan Action Response");
            System.out.println ("=============================================================================");
            System.out.println ();

            System.out.println("    CreateInboundShipmentPlanResponse");
            System.out.println();
            if (response.isSetCreateInboundShipmentPlanResult()) {
                System.out.println("        CreateInboundShipmentPlanResult");
                System.out.println();
                CreateInboundShipmentPlanResult  createInboundShipmentPlanResult = response.getCreateInboundShipmentPlanResult();
                if (createInboundShipmentPlanResult.isSetInboundShipmentPlans()) {
                    System.out.println("            InboundShipmentPlans");
                    System.out.println();
                    InboundShipmentPlanList  inboundShipmentPlans = createInboundShipmentPlanResult.getInboundShipmentPlans();
                    java.util.List<InboundShipmentPlan> memberList = inboundShipmentPlans.getMember();
                    for (InboundShipmentPlan member : memberList) {
                        System.out.println("                member");
                        System.out.println();
                        if (member.isSetShipmentId()) {
                            System.out.println("                    ShipmentId");
                            System.out.println();
                            System.out.println("                        " + member.getShipmentId());
                            System.out.println();
                        }
                        if (member.isSetDestinationFulfillmentCenterId()) {
                            System.out.println("                    DestinationFulfillmentCenterId");
                            System.out.println();
                            System.out.println("                        " + member.getDestinationFulfillmentCenterId());
                            System.out.println();
                        }
                        if (member.isSetShipToAddress()) {
                            System.out.println("                    ShipToAddress");
                            System.out.println();
                            Address  shipToAddress = member.getShipToAddress();
                            if (shipToAddress.isSetName()) {
                                System.out.println("                        Name");
                                System.out.println();
                                System.out.println("                            " + shipToAddress.getName());
                                System.out.println();
                            }
                            if (shipToAddress.isSetAddressLine1()) {
                                System.out.println("                        AddressLine1");
                                System.out.println();
                                System.out.println("                            " + shipToAddress.getAddressLine1());
                                System.out.println();
                            }
                            if (shipToAddress.isSetAddressLine2()) {
                                System.out.println("                        AddressLine2");
                                System.out.println();
                                System.out.println("                            " + shipToAddress.getAddressLine2());
                                System.out.println();
                            }
                            if (shipToAddress.isSetDistrictOrCounty()) {
                                System.out.println("                        DistrictOrCounty");
                                System.out.println();
                                System.out.println("                            " + shipToAddress.getDistrictOrCounty());
                                System.out.println();
                            }
                            if (shipToAddress.isSetCity()) {
                                System.out.println("                        City");
                                System.out.println();
                                System.out.println("                            " + shipToAddress.getCity());
                                System.out.println();
                            }
                            if (shipToAddress.isSetStateOrProvinceCode()) {
                                System.out.println("                        StateOrProvinceCode");
                                System.out.println();
                                System.out.println("                            " + shipToAddress.getStateOrProvinceCode());
                                System.out.println();
                            }
                            if (shipToAddress.isSetCountryCode()) {
                                System.out.println("                        CountryCode");
                                System.out.println();
                                System.out.println("                            " + shipToAddress.getCountryCode());
                                System.out.println();
                            }
                            if (shipToAddress.isSetPostalCode()) {
                                System.out.println("                        PostalCode");
                                System.out.println();
                                System.out.println("                            " + shipToAddress.getPostalCode());
                                System.out.println();
                            }
                        } 
                        if (member.isSetLabelPrepType()) {
                            System.out.println("                    LabelPrepType");
                            System.out.println();
                            System.out.println("                        " + member.getLabelPrepType());
                            System.out.println();
                        }
                        if (member.isSetItems()) {
                            System.out.println("                    Items");
                            System.out.println();
                            InboundShipmentPlanItemList  items = member.getItems();
                            java.util.List<InboundShipmentPlanItem> member1List = items.getMember();
                            for (InboundShipmentPlanItem member1 : member1List) {
                                System.out.println("                        member");
                                System.out.println();
                                if (member1.isSetSellerSKU()) {
                                    System.out.println("                            SellerSKU");
                                    System.out.println();
                                    System.out.println("                                " + member1.getSellerSKU());
                                    System.out.println();
                                }
                                if (member1.isSetFulfillmentNetworkSKU()) {
                                    System.out.println("                            FulfillmentNetworkSKU");
                                    System.out.println();
                                    System.out.println("                                " + member1.getFulfillmentNetworkSKU());
                                    System.out.println();
                                }
                                if (member1.isSetQuantity()) {
                                    System.out.println("                            Quantity");
                                    System.out.println();
                                    System.out.println("                                " + member1.getQuantity());
                                    System.out.println();
                                }
                            }
                        } 
                    }
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
