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

package com.amazonservices.mws.FulfillmentInventory._2010_10_01.mock;

import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWS;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSException;
import com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * FBAInventoryServiceMWSMock is the implementation of FBAInventoryServiceMWS based
 * on the pre-populated set of XML files that serve local data. It simulates
 * responses from FBA Inventory Service MWS service.
 *
 * Use this to test your application without making a call to FBA Inventory Service MWS 
 *
 * Note, current Mock Service implementation does not valiadate requests
 *
 */
public  class FBAInventoryServiceMWSMock implements FBAInventoryServiceMWS {

    private final Log log = LogFactory.getLog(FBAInventoryServiceMWSMock.class);
    private static JAXBContext  jaxbContext;
    private static ThreadLocal<Unmarshaller> unmarshaller;


    /** Initialize JAXBContext and  Unmarshaller **/
    static {
        try {
            jaxbContext = JAXBContext.newInstance("com.amazonservices.mws.FulfillmentInventory._2010_10_01.model", FBAInventoryServiceMWS.class.getClassLoader());
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
     * List Inventory Supply By Next Token 
     *
     * Continues pagination over a resultset of inventory data for inventory
     * items.
     * 
     * This operation is used in conjunction with ListUpdatedInventorySupply.
     * Please refer to documentation for that operation for further details.
     * 
     * @param request
     *          ListInventorySupplyByNextToken Action
     * @return
     *          ListInventorySupplyByNextToken Response from the service
     *
     * @throws com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSException
     */
    public ListInventorySupplyByNextTokenResponse listInventorySupplyByNextToken(ListInventorySupplyByNextTokenRequest request)
        throws FBAInventoryServiceMWSException {
        ListInventorySupplyByNextTokenResponse response;
        try {
            response = (ListInventorySupplyByNextTokenResponse)getUnmarshaller().unmarshal
                    (new InputSource(this.getClass().getResourceAsStream("ListInventorySupplyByNextTokenResponse.xml")));

            log.debug("Response from Mock Service: " + response.toXML());

        } catch (JAXBException jbe) {
            throw new FBAInventoryServiceMWSException("Unable to process mock response", jbe);
        }
        return response;
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
     *          ListInventorySupply Action
     * @return
     *          ListInventorySupply Response from the service
     *
     * @throws com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSException
     */
    public ListInventorySupplyResponse listInventorySupply(ListInventorySupplyRequest request)
        throws FBAInventoryServiceMWSException {
        ListInventorySupplyResponse response;
        try {
            response = (ListInventorySupplyResponse)getUnmarshaller().unmarshal
                    (new InputSource(this.getClass().getResourceAsStream("ListInventorySupplyResponse.xml")));

            log.debug("Response from Mock Service: " + response.toXML());

        } catch (JAXBException jbe) {
            throw new FBAInventoryServiceMWSException("Unable to process mock response", jbe);
        }
        return response;
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
     *          GetServiceStatus Action
     * @return
     *          GetServiceStatus Response from the service
     *
     * @throws com.amazonservices.mws.FulfillmentInventory._2010_10_01.FBAInventoryServiceMWSException
     */
    public GetServiceStatusResponse getServiceStatus(GetServiceStatusRequest request)
        throws FBAInventoryServiceMWSException {
        GetServiceStatusResponse response;
        try {
            response = (GetServiceStatusResponse)getUnmarshaller().unmarshal
                    (new InputSource(this.getClass().getResourceAsStream("GetServiceStatusResponse.xml")));

            log.debug("Response from Mock Service: " + response.toXML());

        } catch (JAXBException jbe) {
            throw new FBAInventoryServiceMWSException("Unable to process mock response", jbe);
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