
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LithiumBatteryPackagingType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="LithiumBatteryPackagingType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="batteries_only"/>
 *     &lt;enumeration value="batteries_contained_in_equipment"/>
 *     &lt;enumeration value="batteries_packed_with_equipment"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "LithiumBatteryPackagingType")
@XmlEnum
public enum LithiumBatteryPackagingType {

    @XmlEnumValue("batteries_only")
    BATTERIES_ONLY("batteries_only"),
    @XmlEnumValue("batteries_contained_in_equipment")
    BATTERIES_CONTAINED_IN_EQUIPMENT("batteries_contained_in_equipment"),
    @XmlEnumValue("batteries_packed_with_equipment")
    BATTERIES_PACKED_WITH_EQUIPMENT("batteries_packed_with_equipment");
    private final String value;

    LithiumBatteryPackagingType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LithiumBatteryPackagingType fromValue(String v) {
        for (LithiumBatteryPackagingType c: LithiumBatteryPackagingType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
