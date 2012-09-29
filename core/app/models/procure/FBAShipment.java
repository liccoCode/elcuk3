package models.procure;

import models.market.Account;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.Model;
import query.FBAShipmentQuery;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany
    public List<Shipment> shipments = new ArrayList<Shipment>();

    @Column(unique = true, nullable = false, length = 20)
    public String shipmentId;


    // ShipToAddress

    public String addressLine1;

    public String addressLine2;

    public String city;

    public String countryCode;

    public String postalCode;

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

    public String address() {
        return String.format("%s %s %s %s (%s)", this.addressLine1, this.city, this.stateOrProvinceCode, this.postalCode, this.centerId);
    }

    public String codeToCounrty() {
        if(StringUtils.isBlank(this.countryCode)) return "";
        this.countryCode = this.countryCode.toUpperCase();
        if(this.countryCode.equals("GB")) return "United Kingdom";
        else if(this.countryCode.equals("US")) return "United States";
        else if(this.countryCode.equals("CA")) return "Canada";
        else if(this.countryCode.equals("CN")) return "China (Mainland)";
        else if(this.countryCode.equals("DE")) return "Germany";
        else if(this.countryCode.equals("FR")) return "France";
        else if(this.countryCode.equals("IT")) return "Italy";
        else if(this.countryCode.equals("JP")) return "Japan";
        //http://mindprod.com/jgloss/countrycodes.html
        return "";
    }

    public static List<String> uncloseFBAShipmentIds() {
        return FBAShipmentQuery.uncloseFBAShipmentIds();
    }

    public static FBAShipment findByShipmentId(String shipmentId) {
        return FBAShipment.find("shipmentId=?", shipmentId).first();
    }
}
