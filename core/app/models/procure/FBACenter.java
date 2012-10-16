package models.procure;

import org.apache.commons.lang.StringUtils;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * FBA 仓库. 每一个 FBAShipment 都会关联的
 * User: wyattpan
 * Date: 10/16/12
 * Time: 12:08 PM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class FBACenter extends Model {
    public FBACenter() {
    }

    public FBACenter(String centerId, String addressLine1, String addressLine2, String city,
                     String name, String countryCode, String stateOrProvinceCode, String postalCode) {
        this.centerId = centerId;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.name = name;
        this.countryCode = countryCode;
        this.stateOrProvinceCode = stateOrProvinceCode;
        this.postalCode = postalCode;
    }

    @Column(unique = true, nullable = false)
    public String centerId;

    public String addressLine1;

    public String addressLine2;

    public String city;

    public String name;

    public String countryCode;

    public String stateOrProvinceCode;

    public String postalCode;

    public static FBACenter findByCenterId(String centerId) {
        return FBACenter.find("centerId=?", centerId).first();
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
}
