package models.procure;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.F;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.List;
import java.util.stream.Collectors;

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

    @Required
    @Column(unique = true, nullable = false)
    public String centerId;

    @Required
    public String addressLine1;

    public String addressLine2;

    @Required
    public String city;

    @Required
    public String name;

    @Required
    public String countryCode;

    public String stateOrProvinceCode;

    @Required
    public String postalCode;

    /**
     * 是否使用 Amazon 返回的地址来自动同步数据
     */
    public boolean autoSync = false;

    /**
     * 向当前 FBA Center 发货的 FBA 数量
     *
     * @return T2: _.1: 数量. _.2: FBA Ids
     */
    public F.T2<Long, List<String>> fbas() {
        List<FBAShipment> shipments = FBAShipment.find("fbaCenter=?", this).fetch();
        List<String> shipmentIds = shipments.stream().map(shipment -> shipment.shipmentId).collect(Collectors.toList());
        return new F.T2<>((long) shipments.size(), shipmentIds);
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
        return "FBACenter{"
                + "centerId='" + centerId + '\''
                + ", addressLine1='" + addressLine1 + '\''
                + ", addressLine2='" + addressLine2 + '\''
                + ", city='" + city + '\''
                + ", name='" + name + '\''
                + ", countryCode='" + countryCode + '\''
                + ", stateOrProvinceCode='" + stateOrProvinceCode + '\''
                + ", postalCode='" + postalCode + '\''
                + '}';
    }

    public static FBACenter findByCenterId(String centerId) {
        return FBACenter.find("centerId=?", centerId).first();
    }

    public FBACenter createOrUpdate() {
        FBACenter manager = FBACenter.findByCenterId(this.centerId);
        if(manager != null) {
            if(manager.autoSync) {
                manager.addressLine1 = this.addressLine1;
                manager.addressLine2 = this.addressLine2;
                manager.city = this.city;
                manager.name = this.name;
                manager.countryCode = this.countryCode;
                manager.stateOrProvinceCode = this.stateOrProvinceCode;
                manager.postalCode = this.postalCode;
                manager.save();
            }
            return manager;
        } else {
            return this.save();
        }
    }

    public void update(FBACenter center) {
        this.addressLine1 = center.addressLine1;
        this.addressLine2 = center.addressLine2;
        this.city = center.city;
        this.countryCode = center.countryCode;
        this.name = center.name;
        this.postalCode = center.postalCode;
        this.stateOrProvinceCode = center.stateOrProvinceCode;
        this.save();
    }

    public void enableAutoSync() {
        this.autoSync = true;
        this.save();
    }

    public void disableAutoSync() {
        this.autoSync = false;
        this.save();
    }
}
