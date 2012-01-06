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

import java.util.concurrent.Future;



/**
 * This contains the Sellers section of the Marketplace Web Service.
 * 
 *
 */

public interface MarketplaceWebServiceSellersAsync extends MarketplaceWebServiceSellers {


            
    /**
     * List Marketplace Participations 
     *
     * This operation can be used to list all Marketplaces that a seller can sell in.
     * The operation returns a List of Participation elements and a List of Marketplace
     * elements. The SellerId is the only parameter required by this operation.
     * 
     * @param request
     *          ListMarketplaceParticipations Action
     * @return
     *          ListMarketplaceParticipations Response from the service
     */
    public Future<ListMarketplaceParticipationsResponse> listMarketplaceParticipationsAsync(final ListMarketplaceParticipationsRequest request);


            
    /**
     * List Marketplace Participations By Next Token 
     *
     * If ListMarketplaces cannot return all the marketplaces in one go, it will
     * provide a nextToken.  That nextToken can be used with this operation to
     * retrieve the next batch of Marketplaces for that SellerId.
     * 
     * @param request
     *          ListMarketplaceParticipationsByNextToken Action
     * @return
     *          ListMarketplaceParticipationsByNextToken Response from the service
     */
    public Future<ListMarketplaceParticipationsByNextTokenResponse> listMarketplaceParticipationsByNextTokenAsync(final ListMarketplaceParticipationsByNextTokenRequest request);


            
    /**
     * Get Service Status 
     *
     * Returns the service status of a particular MWS API section. The operation
     * takes no input. All API sections within the API are required to implement this operation.
     * 
     * @param request
     *          GetServiceStatus Action
     * @return
     *          GetServiceStatus Response from the service
     */
    public Future<GetServiceStatusResponse> getServiceStatusAsync(final GetServiceStatusRequest request);



}
