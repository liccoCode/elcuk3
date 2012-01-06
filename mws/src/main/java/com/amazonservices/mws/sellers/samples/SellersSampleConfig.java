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

import com.amazonservices.mws.sellers.MarketplaceWebServiceSellersConfig;

final public class SellersSampleConfig {

	/************************************************************************
	 * Set Access Key ID, Secret Acess Key ID, Seller ID, etc.
	 ***********************************************************************/
	public static final String accessKeyId = "<Access Key Id>";
	public static final String secretAccessKey = "<Secret Access Key Id>";
	public static final String applicationName = "<Application Name>";
	public static final String applicationVersion = "<Application Version>";
	
	public static final String sellerId = "<Seller Id>";

	public static MarketplaceWebServiceSellersConfig config = new MarketplaceWebServiceSellersConfig();
	

	static {

		/************************************************************************
		 * Uncomment to set the appropriate MWS endpoint.
		 ************************************************************************/
		// US
		// config.setServiceURL("https://mws.amazonservices.com/Sellers/2011-07-01");
		// UK
		// config.setServiceURL("https://mws.amazonservices.co.uk/Sellers/2011-07-01");
		// Germany
		// config.setServiceURL("https://mws.amazonservices.de/Sellers/2011-07-01");
		// France
		// config.setServiceURL("https://mws.amazonservices.fr/Sellers/2011-07-01");
		// Italy
		// config.setServiceURL("https://mws.amazonservices.it/Sellers/2011-07-01");
		// Japan
		// config.setServiceURL("https://mws.amazonservices.jp/Sellers/2011-07-01");
		// China
		// config.setServiceURL("https://mws.amazonservices.com.cn/Sellers/2011-07-01");
		// Canada
		// config.setServiceURL("https://mws.amazonservices.ca/Sellers/2011-07-01");
				
	}

	/************************************************************************
	 * You can also try advanced configuration options. Available options are:
	 * 
	 * - Signature Version - Proxy Host and Proxy Port - User Agent String to be
	 * sent to Marketplace Web Service
	 *************************************************************************/

}
