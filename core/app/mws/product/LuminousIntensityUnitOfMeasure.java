
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LuminousIntensityUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="LuminousIntensityUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="candela"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "LuminousIntensityUnitOfMeasure")
@XmlEnum
public enum LuminousIntensityUnitOfMeasure {

    @XmlEnumValue("candela")
    CANDELA("candela");
    private final String value;

    LuminousIntensityUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LuminousIntensityUnitOfMeasure fromValue(String v) {
        for (LuminousIntensityUnitOfMeasure c: LuminousIntensityUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
