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

package com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01;

public enum MWSEndpoint
{
    // United States
    US("mws.amazonservices.com"),
    // United Kingdom
    UK("mws.amazonservices.co.uk"),
    // Germany
    DE("mws.amazonservices.de"),
    // France
    FR("mws.amazonservices.fr"),
    // Japan
    JP("mws.amazonservices.jp"),
    // China
    CN("mws.amazonservices.com.cn"),
    // Italy
    IT("mws.amazonservices.it");

    private String domain;

    private MWSEndpoint(String domain)
    {
        this.domain = domain;
    }

    private static final String URI = "FulfillmentInboundShipment/2010-10-01/";
    private static final String PROTOCOL = "https";

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder().append(PROTOCOL).append("://")
                .append(domain).append("/").append(URI);

        return sb.toString();
    }
}
