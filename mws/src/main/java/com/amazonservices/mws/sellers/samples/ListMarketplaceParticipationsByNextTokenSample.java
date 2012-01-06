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

import com.amazonservices.mws.sellers.MarketplaceWebServiceSellers;
import com.amazonservices.mws.sellers.MarketplaceWebServiceSellersClient;
import com.amazonservices.mws.sellers.MarketplaceWebServiceSellersException;
import com.amazonservices.mws.sellers.model.*;

/**
 *
 * List Marketplace Participations By Next Token  Samples
 *
 *
 */
@SuppressWarnings("unused")
public class ListMarketplaceParticipationsByNextTokenSample {

    /**
     * Just add few required parameters, and try the service
     * List Marketplace Participations By Next Token functionality
     *
     * @param args unused
     */
    public static void main(String... args) {
        
		/*
		 * Add required parameters in OrdersSampleConfig.java before trying out
		 * this sample.
		 */

		/************************************************************************
		 * Instantiate Http Client Implementation of Marketplace Web Service
		 * Orders
		 ***********************************************************************/
		MarketplaceWebServiceSellers service = new MarketplaceWebServiceSellersClient(
				SellersSampleConfig.accessKeyId,
				SellersSampleConfig.secretAccessKey,
				SellersSampleConfig.applicationName,
				SellersSampleConfig.applicationVersion,
				SellersSampleConfig.config);
        
        /************************************************************************
         * Uncomment to try advanced configuration options. Available options are:
         *
         *  - Signature Version
         *  - Proxy Host and Proxy Port
         *  - Service URL
         *  - User Agent String to be sent to Marketplace Web Service Sellers   service
         *
         ***********************************************************************/
        // MarketplaceWebServiceSellersConfig config = new MarketplaceWebServiceSellersConfig();
        // config.setSignatureVersion("0");
        // MarketplaceWebServiceSellers service = new MarketplaceWebServiceSellersClient(accessKeyId, secretAccessKey, config);
 
        /************************************************************************
         * Uncomment to try out Mock Service that simulates Marketplace Web Service Sellers 
         * responses without calling Marketplace Web Service Sellers  service.
         *
         * Responses are loaded from local XML files. You can tweak XML files to
         * experiment with various outputs during development
         *
         * XML files available under com/amazonservices/mws/mock tree
         *
         ***********************************************************************/
        // MarketplaceWebServiceSellers service = new MarketplaceWebServiceSellersMock();

        /************************************************************************
         * Setup request parameters and uncomment invoke to try out 
         * sample for List Marketplace Participations By Next Token 
         ***********************************************************************/
         ListMarketplaceParticipationsByNextTokenRequest request = new ListMarketplaceParticipationsByNextTokenRequest();
        
         // @TODO: set request parameters here
         request.setSellerId(SellersSampleConfig.sellerId);

         invokeListMarketplaceParticipationsByNextToken(service, request);

    }


                            
    /**
     * List Marketplace Participations By Next Token  request sample
     * If ListMarketplaces cannot return all the order items in one go, it will
     * provide a nextToken.  That nextToken can be used with this operation to
     * retrieve the next batch of Marketplaces for that SellerId.
     *   
     * @param service instance of MarketplaceWebServiceSellers service
     * @param request Action to invoke
     */
    public static void invokeListMarketplaceParticipationsByNextToken(MarketplaceWebServiceSellers service, ListMarketplaceParticipationsByNextTokenRequest request) {
        try {
            
            ListMarketplaceParticipationsByNextTokenResponse response = service.listMarketplaceParticipationsByNextToken(request);

            
            System.out.println ("ListMarketplaceParticipationsByNextToken Action Response");
            System.out.println ("=============================================================================");
            System.out.println ();

            System.out.println("    ListMarketplaceParticipationsByNextTokenResponse");
            System.out.println();
            if (response.isSetListMarketplaceParticipationsByNextTokenResult()) {
                System.out.println("        ListMarketplaceParticipationsByNextTokenResult");
                System.out.println();
                ListMarketplaceParticipationsByNextTokenResult  listMarketplaceParticipationsByNextTokenResult = response.getListMarketplaceParticipationsByNextTokenResult();
                if (listMarketplaceParticipationsByNextTokenResult.isSetNextToken()) {
                    System.out.println("            NextToken");
                    System.out.println();
                    System.out.println("                " + listMarketplaceParticipationsByNextTokenResult.getNextToken());
                    System.out.println();
                }
                if (listMarketplaceParticipationsByNextTokenResult.isSetListParticipations()) {
                    System.out.println("            ListParticipations");
                    System.out.println();
                    ListParticipations  ListParticipations = listMarketplaceParticipationsByNextTokenResult.getListParticipations();
                    java.util.List<Participation> participationList = ListParticipations.getParticipation();
                    for (Participation participation : participationList) {
                        System.out.println("                Participation");
                        System.out.println();
                        if (participation.isSetMarketplaceId()) {
                            System.out.println("                    MarketplaceId");
                            System.out.println();
                            System.out.println("                        " + participation.getMarketplaceId());
                            System.out.println();
                        }
                        if (participation.isSetSellerId()) {
                            System.out.println("                    SellerId");
                            System.out.println();
                            System.out.println("                        " + participation.getSellerId());
                            System.out.println();
                        }
                        if (participation.isSetHasSellerSuspendedListings()) {
                            System.out.println("                    HasSellerSuspendedListings");
                            System.out.println();
                            System.out.println("                        " + participation.getHasSellerSuspendedListings().value());
                            System.out.println();
                        }
                    }
                } 
                if (listMarketplaceParticipationsByNextTokenResult.isSetListMarketplaces()) {
                    System.out.println("            ListMarketplaces");
                    System.out.println();
                    ListMarketplaces  ListMarketplaces = listMarketplaceParticipationsByNextTokenResult.getListMarketplaces();
                    java.util.List<Marketplace> marketplaceList = ListMarketplaces.getMarketplace();
                    for (Marketplace marketplace : marketplaceList) {
                        System.out.println("                Marketplace");
                        System.out.println();
                        if (marketplace.isSetMarketplaceId()) {
                            System.out.println("                    MarketplaceId");
                            System.out.println();
                            System.out.println("                        " + marketplace.getMarketplaceId());
                            System.out.println();
                        }
                        if (marketplace.isSetName()) {
                            System.out.println("                    Name");
                            System.out.println();
                            System.out.println("                        " + marketplace.getName());
                            System.out.println();
                        }
                        if (marketplace.isSetDefaultLanguageCode()) {
                            System.out.println("                    DefaultLanguageCode");
                            System.out.println();
                            System.out.println("                        " + marketplace.getDefaultLanguageCode());
                            System.out.println();
                        }
                        if (marketplace.isSetDefaultCountryCode()) {
                            System.out.println("                    DefaultCountryCode");
                            System.out.println();
                            System.out.println("                        " + marketplace.getDefaultCountryCode());
                            System.out.println();
                        }
                        if (marketplace.isSetDefaultCurrencyCode()) {
                            System.out.println("                    DefaultCurrencyCode");
                            System.out.println();
                            System.out.println("                        " + marketplace.getDefaultCurrencyCode());
                            System.out.println();
                        }
                        if (marketplace.isSetDomainName()) {
                            System.out.println("                    DomainName");
                            System.out.println();
                            System.out.println("                        " + marketplace.getDomainName());
                            System.out.println();
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

           
        } catch (MarketplaceWebServiceSellersException ex) {
            
            System.out.println("Caught Exception: " + ex.getMessage());
            System.out.println("Response Status Code: " + ex.getStatusCode());
            System.out.println("Error Code: " + ex.getErrorCode());
            System.out.println("Error Type: " + ex.getErrorType());
            System.out.println("Request ID: " + ex.getRequestId());
            System.out.print("XML: " + ex.getXML());
        }
    }
        
}
