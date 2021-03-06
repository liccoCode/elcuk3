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

package com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.mock;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWS;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * FBAInboundServiceMWSMock is the implementation of FBAInboundServiceMWS based
 * on the pre-populated set of XML files that serve local data. It simulates
 * responses from FBA Inbound Service MWS service.
 *
 * Use this to test your application without making a call to FBA Inbound Service MWS 
 *
 * Note, current Mock Service implementation does not valiadate requests
 *
 */
public  class FBAInboundServiceMWSMock implements FBAInboundServiceMWS {

    private final Log log = LogFactory.getLog(FBAInboundServiceMWSMock.class);
    private static JAXBContext  jaxbContext;
    private static ThreadLocal<Unmarshaller> unmarshaller;


    /** Initialize JAXBContext and  Unmarshaller **/
    static {
        try {
            jaxbContext = JAXBContext.newInstance("com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.model", FBAInboundServiceMWS.class.getClassLoader());
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
     * Create Inbound Shipment Plan 
     *
     * Plans inbound shipments for a set of items.  Registers identifiers if needed,
     * and assigns ShipmentIds for planned shipments.
     * When all the items are not all in the same category (e.g. some sortable, some
     * non-sortable) it may be necessary to create multiple shipments (one for each
     * of the shipment groups returned).
     * @param request
     *          CreateInboundShipmentPlan Action
     * @return
     *          CreateInboundShipmentPlan Response from the service
     *
     * @throws FBAInboundServiceMWSException
     */
    public CreateInboundShipmentPlanResponse createInboundShipmentPlan(CreateInboundShipmentPlanRequest request)
        throws FBAInboundServiceMWSException {
        CreateInboundShipmentPlanResponse response;
        try {
            response = (CreateInboundShipmentPlanResponse)getUnmarshaller().unmarshal
                    (new InputSource(this.getClass().getResourceAsStream("CreateInboundShipmentPlanResponse.xml")));

            log.debug("Response from Mock Service: " + response.toXML());

        } catch (JAXBException jbe) {
            throw new FBAInboundServiceMWSException("Unable to process mock response", jbe);
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
     * @param request
     *          GetServiceStatus Action
     * @return
     *          GetServiceStatus Response from the service
     *
     * @throws FBAInboundServiceMWSException
     */
    public GetServiceStatusResponse getServiceStatus(GetServiceStatusRequest request)
        throws FBAInboundServiceMWSException {
        GetServiceStatusResponse response;
        try {
            response = (GetServiceStatusResponse)getUnmarshaller().unmarshal
                    (new InputSource(this.getClass().getResourceAsStream("GetServiceStatusResponse.xml")));

            log.debug("Response from Mock Service: " + response.toXML());

        } catch (JAXBException jbe) {
            throw new FBAInboundServiceMWSException("Unable to process mock response", jbe);
        }
        return response;
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
     *          ListInboundShipments Action
     * @return
     *          ListInboundShipments Response from the service
     *
     * @throws FBAInboundServiceMWSException
     */
    public ListInboundShipmentsResponse listInboundShipments(ListInboundShipmentsRequest request)
        throws FBAInboundServiceMWSException {
        ListInboundShipmentsResponse response;
        try {
            response = (ListInboundShipmentsResponse)getUnmarshaller().unmarshal
                    (new InputSource(this.getClass().getResourceAsStream("ListInboundShipmentsResponse.xml")));

            log.debug("Response from Mock Service: " + response.toXML());

        } catch (JAXBException jbe) {
            throw new FBAInboundServiceMWSException("Unable to process mock response", jbe);
        }
        return response;
    }

        
    /**
     * List Inbound Shipments By Next Token 
     *
     * Gets the next set of inbound shipments created by a Seller with the
     * NextToken which can be used to iterate through the remaining inbound
     * shipments. If a NextToken is not returned, it indicates the
     * end-of-data.
     * @param request
     *          ListInboundShipmentsByNextToken Action
     * @return
     *          ListInboundShipmentsByNextToken Response from the service
     *
     * @throws FBAInboundServiceMWSException
     */
    public ListInboundShipmentsByNextTokenResponse listInboundShipmentsByNextToken(ListInboundShipmentsByNextTokenRequest request)
        throws FBAInboundServiceMWSException {
        ListInboundShipmentsByNextTokenResponse response;
        try {
            response = (ListInboundShipmentsByNextTokenResponse)getUnmarshaller().unmarshal
                    (new InputSource(this.getClass().getResourceAsStream("ListInboundShipmentsByNextTokenResponse.xml")));

            log.debug("Response from Mock Service: " + response.toXML());

        } catch (JAXBException jbe) {
            throw new FBAInboundServiceMWSException("Unable to process mock response", jbe);
        }
        return response;
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
     *          UpdateInboundShipment Action
     * @return
     *          UpdateInboundShipment Response from the service
     *
     * @throws FBAInboundServiceMWSException
     */
    public UpdateInboundShipmentResponse updateInboundShipment(UpdateInboundShipmentRequest request)
        throws FBAInboundServiceMWSException {
        UpdateInboundShipmentResponse response;
        try {
            response = (UpdateInboundShipmentResponse)getUnmarshaller().unmarshal
                    (new InputSource(this.getClass().getResourceAsStream("UpdateInboundShipmentResponse.xml")));

            log.debug("Response from Mock Service: " + response.toXML());

        } catch (JAXBException jbe) {
            throw new FBAInboundServiceMWSException("Unable to process mock response", jbe);
        }
        return response;
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
     *          CreateInboundShipment Action
     * @return
     *          CreateInboundShipment Response from the service
     *
     * @throws FBAInboundServiceMWSException
     */
    public CreateInboundShipmentResponse createInboundShipment(CreateInboundShipmentRequest request)
        throws FBAInboundServiceMWSException {
        CreateInboundShipmentResponse response;
        try {
            response = (CreateInboundShipmentResponse)getUnmarshaller().unmarshal
                    (new InputSource(this.getClass().getResourceAsStream("CreateInboundShipmentResponse.xml")));

            log.debug("Response from Mock Service: " + response.toXML());

        } catch (JAXBException jbe) {
            throw new FBAInboundServiceMWSException("Unable to process mock response", jbe);
        }
        return response;
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
     *          ListInboundShipmentItemsByNextToken Action
     * @return
     *          ListInboundShipmentItemsByNextToken Response from the service
     *
     * @throws FBAInboundServiceMWSException
     */
    public ListInboundShipmentItemsByNextTokenResponse listInboundShipmentItemsByNextToken(ListInboundShipmentItemsByNextTokenRequest request)
        throws FBAInboundServiceMWSException {
        ListInboundShipmentItemsByNextTokenResponse response;
        try {
            response = (ListInboundShipmentItemsByNextTokenResponse)getUnmarshaller().unmarshal
                    (new InputSource(this.getClass().getResourceAsStream("ListInboundShipmentItemsByNextTokenResponse.xml")));

            log.debug("Response from Mock Service: " + response.toXML());

        } catch (JAXBException jbe) {
            throw new FBAInboundServiceMWSException("Unable to process mock response", jbe);
        }
        return response;
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
     *          ListInboundShipmentItems Action
     * @return
     *          ListInboundShipmentItems Response from the service
     *
     * @throws FBAInboundServiceMWSException
     */
    public ListInboundShipmentItemsResponse listInboundShipmentItems(ListInboundShipmentItemsRequest request)
        throws FBAInboundServiceMWSException {
        ListInboundShipmentItemsResponse response;
        try {
            response = (ListInboundShipmentItemsResponse)getUnmarshaller().unmarshal
                    (new InputSource(this.getClass().getResourceAsStream("ListInboundShipmentItemsResponse.xml")));

            log.debug("Response from Mock Service: " + response.toXML());

        } catch (JAXBException jbe) {
            throw new FBAInboundServiceMWSException("Unable to process mock response", jbe);
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