package models.procure;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import play.db.jpa.Model;
import play.libs.F;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

/**
 * FBA 仓库. 每一个 FBAShipment 都会关联的
 * User: wyattpan
 * Date: 10/16/12
 * Time: 12:08 PM
 */
@Entity
@DynamicUpdate
public class FBACenter extends Model {
    public FBACenter() {
    }

    public FBACenter(String centerId, String addressLine1, String addressLine2, String city,
                     String name, String countryCode, String stateOrProvinceCode,
                     String postalCode) {
        this.centerId = centerId;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.name = name;
        this.countryCode = countryCode;
        this.stateOrProvinceCode = stateOrProvinceCode;
        this.postalCode = postalCode;
    }
    /*
   例子:
   ShipToAddress: {
       // -> 在 FBACenter
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

    @Column(unique = true, nullable = false)
    public String centerId;

    public String addressLine1;

    public String addressLine2;

    public String city;

    public String name;

    public String countryCode;

    public String stateOrProvinceCode;

    public String postalCode;

    /**
     * 向当前 FBA Center 发货的 FBA 数量
     *
     * @return T2: _.1: 数量. _.2: FBA Ids
     */
    public F.T2<Long, List<String>> fbas() {
        List<FBAShipment> shipments = FBAShipment.find("fbaCenter=?", this).fetch();
        List<String> shipmentIds = new ArrayList<String>();
        for(FBAShipment shipment : shipments) {
            shipmentIds.add(shipment.shipmentId);
        }
        return new F.T2<Long, List<String>>((long) shipments.size(), shipmentIds);
    }

    public String codeToCountry() {
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


    @Override
    public String toString() {
        return "FBACenter{" +
                "centerId='" + centerId + '\'' +
                ", addressLine1='" + addressLine1 + '\'' +
                ", addressLine2='" + addressLine2 + '\'' +
                ", city='" + city + '\'' +
                ", name='" + name + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", stateOrProvinceCode='" + stateOrProvinceCode + '\'' +
                ", postalCode='" + postalCode + '\'' +
                '}';
    }

    public static FBACenter findByCenterId(String centerId) {
        return FBACenter.find("centerId=?", centerId).first();
    }

}
