
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BatteryPowerUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BatteryPowerUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="mAh"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "BatteryPowerUnitOfMeasure")
@XmlEnum
public enum BatteryPowerUnitOfMeasure {

    @XmlEnumValue("mAh")
    M_AH("mAh");
    private final String value;

    BatteryPowerUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BatteryPowerUnitOfMeasure fromValue(String v) {
        for (BatteryPowerUnitOfMeasure c: BatteryPowerUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
