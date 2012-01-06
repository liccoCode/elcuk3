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

package com.amazonservices.mws.FulfillmentInventory._2010_10_01.samples;

import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWS;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSClient;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSConfig;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSException;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.*;

import java.util.List;

/**
 *
 * List Inventory Supply By Next Token  Samples
 *
 *
 */
public class ListInventorySupplyByNextTokenSample {

    /**
     * Just add few required parameters, and try the service
     * List Inventory Supply By Next Token functionality
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
         * Marketplace and Seller IDs are required parameters for all 
         * MWS calls.
         ***********************************************************************/
        final String marketplaceId = "<Your Marketplace ID>";
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
        FBAInventoryServiceMWSConfig config = new FBAInventoryServiceMWSConfig();

        /************************************************************************
         * Uncomment to set the correct MWS endpoint. You can get your Country
         * Code from MWSEndpoint enum class, e.g. US.
         ************************************************************************/
        // config.setServiceURL(MWSEndpoint.<Your Country Code>.toString());

        /************************************************************************
         * Instantiate Http Client Implementation of FBA Inventory Service MWS 
         ***********************************************************************/
        // FBAInventoryServiceMWS service = new FBAInventoryServiceMWSClient(accessKeyId, secretAccessKey, config);
        FBAInventoryServiceMWS service = new FBAInventoryServiceMWSClient(accessKeyId, secretAccessKey, applicationName, applicationVersion, config);
 
        /************************************************************************
         * Uncomment to try out Mock Service that simulates FBA Inventory Service MWS 
         * responses without calling FBA Inventory Service MWS  service.
         *
         * Responses are loaded from local XML files. You can tweak XML files to
         * experiment with various outputs during development
         *
         * XML files available under com/amazonservices/mws/FulfillmentInventory/_2010_10_01/mock tree
         *
         ***********************************************************************/
        // FBAInventoryServiceMWS service = new FBAInventoryServiceMWSMock();

        /************************************************************************
         * Setup request parameters and uncomment invoke to try out 
         * sample for List Inventory Supply By Next Token 
         ***********************************************************************/
         ListInventorySupplyByNextTokenRequest request = new ListInventorySupplyByNextTokenRequest();
         // @TODO: set request parameters here
         request.setSellerId(sellerId);
         request.setMarketplace(marketplaceId);

         // invokeListInventorySupplyByNextToken(service, request);

    }


                    
    /**
     * List Inventory Supply By Next Token  request sample
     * Continues pagination over a resultset of inventory data for inventory
     * items.
     * 
     * This operation is used in conjunction with ListUpdatedInventorySupply.
     * Please refer to documentation for that operation for further details.
     *   
     * @param service instance of FBAInventoryServiceMWS service
     * @param request Action to invoke
     */
    public static void invokeListInventorySupplyByNextToken(FBAInventoryServiceMWS service, ListInventorySupplyByNextTokenRequest request) {
        try {
            
            ListInventorySupplyByNextTokenResponse response = service.listInventorySupplyByNextToken(request);

            
            System.out.println ("ListInventorySupplyByNextToken Action Response");
            System.out.println ("=============================================================================");
            System.out.println ();

            System.out.println("    ListInventorySupplyByNextTokenResponse");
            System.out.println();
            if (response.isSetListInventorySupplyByNextTokenResult()) {
                System.out.println("        ListInventorySupplyByNextTokenResult");
                System.out.println();
                ListInventorySupplyByNextTokenResult  listInventorySupplyByNextTokenResult = response.getListInventorySupplyByNextTokenResult();
                if (listInventorySupplyByNextTokenResult.isSetInventorySupplyList()) {
                    System.out.println("            InventorySupplyList");
                    System.out.println();
                    InventorySupplyList  inventorySupplyList = listInventorySupplyByNextTokenResult.getInventorySupplyList();
                    List<InventorySupply> memberList = inventorySupplyList.getMember();
                    for (InventorySupply member : memberList) {
                        System.out.println("                member");
                        System.out.println();
                        if (member.isSetSellerSKU()) {
                            System.out.println("                    SellerSKU");
                            System.out.println();
                            System.out.println("                        " + member.getSellerSKU());
                            System.out.println();
                        }
                        if (member.isSetFNSKU()) {
                            System.out.println("                    FNSKU");
                            System.out.println();
                            System.out.println("                        " + member.getFNSKU());
                            System.out.println();
                        }
                        if (member.isSetASIN()) {
                            System.out.println("                    ASIN");
                            System.out.println();
                            System.out.println("                        " + member.getASIN());
                            System.out.println();
                        }
                        if (member.isSetCondition()) {
                            System.out.println("                    Condition");
                            System.out.println();
                            System.out.println("                        " + member.getCondition());
                            System.out.println();
                        }
                        if (member.isSetTotalSupplyQuantity()) {
                            System.out.println("                    TotalSupplyQuantity");
                            System.out.println();
                            System.out.println("                        " + member.getTotalSupplyQuantity());
                            System.out.println();
                        }
                        if (member.isSetInStockSupplyQuantity()) {
                            System.out.println("                    InStockSupplyQuantity");
                            System.out.println();
                            System.out.println("                        " + member.getInStockSupplyQuantity());
                            System.out.println();
                        }
                        if (member.isSetEarliestAvailability()) {
                            System.out.println("                    EarliestAvailability");
                            System.out.println();
                            Timepoint  earliestAvailability = member.getEarliestAvailability();
                            if (earliestAvailability.isSetTimepointType()) {
                                System.out.println("                        TimepointType");
                                System.out.println();
                                System.out.println("                            " + earliestAvailability.getTimepointType());
                                System.out.println();
                            }
                            if (earliestAvailability.isSetDateTime()) {
                                System.out.println("                        DateTime");
                                System.out.println();
                                System.out.println("                            " + earliestAvailability.getDateTime());
                                System.out.println();
                            }
                        } 
                        if (member.isSetSupplyDetail()) {
                            System.out.println("                    SupplyDetail");
                            System.out.println();
                            InventorySupplyDetailList  supplyDetail = member.getSupplyDetail();
                            List<InventorySupplyDetail> member1List = supplyDetail.getMember();
                            for (InventorySupplyDetail member1 : member1List) {
                                System.out.println("                        member");
                                System.out.println();
                                if (member1.isSetQuantity()) {
                                    System.out.println("                            Quantity");
                                    System.out.println();
                                    System.out.println("                                " + member1.getQuantity());
                                    System.out.println();
                                }
                                if (member1.isSetSupplyType()) {
                                    System.out.println("                            SupplyType");
                                    System.out.println();
                                    System.out.println("                                " + member1.getSupplyType());
                                    System.out.println();
                                }
                                if (member1.isSetEarliestAvailableToPick()) {
                                    System.out.println("                            EarliestAvailableToPick");
                                    System.out.println();
                                    Timepoint  earliestAvailableToPick = member1.getEarliestAvailableToPick();
                                    if (earliestAvailableToPick.isSetTimepointType()) {
                                        System.out.println("                                TimepointType");
                                        System.out.println();
                                        System.out.println("                                    " + earliestAvailableToPick.getTimepointType());
                                        System.out.println();
                                    }
                                    if (earliestAvailableToPick.isSetDateTime()) {
                                        System.out.println("                                DateTime");
                                        System.out.println();
                                        System.out.println("                                    " + earliestAvailableToPick.getDateTime());
                                        System.out.println();
                                    }
                                } 
                                if (member1.isSetLatestAvailableToPick()) {
                                    System.out.println("                            LatestAvailableToPick");
                                    System.out.println();
                                    Timepoint  latestAvailableToPick = member1.getLatestAvailableToPick();
                                    if (latestAvailableToPick.isSetTimepointType()) {
                                        System.out.println("                                TimepointType");
                                        System.out.println();
                                        System.out.println("                                    " + latestAvailableToPick.getTimepointType());
                                        System.out.println();
                                    }
                                    if (latestAvailableToPick.isSetDateTime()) {
                                        System.out.println("                                DateTime");
                                        System.out.println();
                                        System.out.println("                                    " + latestAvailableToPick.getDateTime());
                                        System.out.println();
                                    }
                                } 
                            }
                        } 
                    }
                } 
                if (listInventorySupplyByNextTokenResult.isSetNextToken()) {
                    System.out.println("            NextToken");
                    System.out.println();
                    System.out.println("                " + listInventorySupplyByNextTokenResult.getNextToken());
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

           
        } catch (FBAInventoryServiceMWSException ex) {
            
            System.out.println("Caught Exception: " + ex.getMessage());
            System.out.println("Response Status Code: " + ex.getStatusCode());
            System.out.println("Error Code: " + ex.getErrorCode());
            System.out.println("Error Type: " + ex.getErrorType());
            System.out.println("Request ID: " + ex.getRequestId());
            System.out.print("XML: " + ex.getXML());
        }
    }
            
}
