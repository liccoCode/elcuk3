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



package com.amazonservices.mws.sellers.mock;

import com.amazonservices.mws.sellers.MarketplaceWebServiceSellers;
import com.amazonservices.mws.sellers.MarketplaceWebServiceSellersException;
import com.amazonservices.mws.sellers.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * MarketplaceWebServiceSellersMock is the implementation of MarketplaceWebServiceSellers based
 * on the pre-populated set of XML files that serve local data. It simulates
 * responses from Marketplace Web Service Sellers service.
 *
 * Use this to test your application without making a call to Marketplace Web Service Sellers 
 *
 * Note, current Mock Service implementation does not valiadate requests
 *
 */
public  class MarketplaceWebServiceSellersMock implements MarketplaceWebServiceSellers {

    private final Log log = LogFactory.getLog(MarketplaceWebServiceSellersMock.class);
    private static JAXBContext  jaxbContext;
    private static ThreadLocal<Unmarshaller> unmarshaller;


    /** Initialize JAXBContext and  Unmarshaller **/
    static {
        try {
            jaxbContext = JAXBContext.newInstance("com.amazonservices.mws.sellers.model", MarketplaceWebServiceSellers.class.getClassLoader());
        } catch (JAXBException ex) {
            throw new ExceptionInInitializerError(ex);
        }
        unmarshaller = new ThreadLocal<Unmarshaller>() {
            protected synchronized Unmarshaller initialValue() {
                try {
                    return jaxbContext.createUnmarshaller();
                } catch(JAXBException e) {
                    throw new ExceptionInInitializerError(e);
                }
            }
        };
    }

    // Public API ------------------------------------------------------------//

        
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
     *
     * @throws MarketplaceWebServiceSellersException
     */
    public ListMarketplaceParticipationsResponse listMarketplaceParticipations(ListMarketplaceParticipationsRequest request)
        throws MarketplaceWebServiceSellersException {
        ListMarketplaceParticipationsResponse response;
        try {
            response = (ListMarketplaceParticipationsResponse)getUnmarshaller().unmarshal
                    (new InputSource(this.getClass().getResourceAsStream("ListMarketplaceParticipationsResponse.xml")));

            log.debug("Response from Mock Service: " + response.toXML());

        } catch (JAXBException jbe) {
            throw new MarketplaceWebServiceSellersException("Unable to process mock response", jbe);
        }
        return response;
    }

        
    /**
     * List Marketplace Participations By Next Token 
     *
     * If ListMarketplaces cannot return all the order items in one go, it will
     * provide a nextToken.  That nextToken can be used with this operation to
     * retrieve the next batch of Marketplaces for that SellerId.
     * 
     * @param request
     *          ListMarketplaceParticipationsByNextToken Action
     * @return
     *          ListMarketplaceParticipationsByNextToken Response from the service
     *
     * @throws MarketplaceWebServiceSellersException
     */
    public ListMarketplaceParticipationsByNextTokenResponse listMarketplaceParticipationsByNextToken(ListMarketplaceParticipationsByNextTokenRequest request)
        throws MarketplaceWebServiceSellersException {
        ListMarketplaceParticipationsByNextTokenResponse response;
        try {
            response = (ListMarketplaceParticipationsByNextTokenResponse)getUnmarshaller().unmarshal
                    (new InputSource(this.getClass().getResourceAsStream("ListMarketplaceParticipationsByNextTokenResponse.xml")));

            log.debug("Response from Mock Service: " + response.toXML());

        } catch (JAXBException jbe) {
            throw new MarketplaceWebServiceSellersException("Unable to process mock response", jbe);
        }
        return response;
    }

        
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
     *
     * @throws MarketplaceWebServiceSellersException
     */
    public GetServiceStatusResponse getServiceStatus(GetServiceStatusRequest request)
        throws MarketplaceWebServiceSellersException {
        GetServiceStatusResponse response;
        try {
            response = (GetServiceStatusResponse)getUnmarshaller().unmarshal
                    (new InputSource(this.getClass().getResourceAsStream("GetServiceStatusResponse.xml")));

            log.debug("Response from Mock Service: " + response.toXML());

        } catch (JAXBException jbe) {
            throw new MarketplaceWebServiceSellersException("Unable to process mock response", jbe);
        }
        return response;
    }


    /**
     * Get unmarshaller for current thread
     */
    private Unmarshaller getUnmarshaller() {
        return unmarshaller.get();
    }
}