package models.procure;

import models.market.Account;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * User: wyattpan
 * Date: 9/12/12
 * Time: 4:27 PM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class FBAShipment extends Model {

    public enum S {
        PLAN,
        /**
         * 表示 FBA Shipment 的状态
         */
        WORKING,
        SHIPPED,
        CANCEL
    }
    /*
    例子:
    ShipToAddress: {
    	"addressLine1":"Boundary Way",
    	"city":"Hemel Hempstead",
    	"countryCode":"GB",
    	"name":"Amazon.co.uk",
    	"postalCode":"HP27LF",
    	"stateOrProvinceCode":"Hertfordshire"


    	"setAddressLine1":true,
    	"setAddressLine2":false,
    	"setCity":true,
    	"setCountryCode":true,
    	"setDistrictOrCounty":false,
    	"setName":true,
    	"setPostalCode":true,
    	"setStateOrProvinceCode":true,
    }
     */

    @OneToOne
    public Account account;

    @OneToOne
    public Shipment shipment;

    @Column(unique = true, nullable = false, length = 20)
    public String shipmentId;



    // ShipToAddress

    public String addressLine1;

    public String addressLine2;

    public String city;

    public String countryCode;

    /**
     * 哪一个网站?
     */
    public String name;

    public String stateOrProvinceCode;

    /**
     * 是否自己贴 Label
     * SELLER_LABEL
     * AMAZON_LABEL_ONLY
     * AMAZON_LABEL_PREFERRED
     * Note: Unless you are part of Amazon's label preparation program, SELLER_LABEL is the only valid
     */
    public String labelPrepType = "SELLER_LABEL";

    public String centerId;

    public S state = S.PLAN;

    /**
     * Amazon FBA 上的 title
     */
    public String title;
}
