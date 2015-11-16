
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WattageUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="WattageUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="watts"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "WattageUnitOfMeasure")
@XmlEnum
public enum WattageUnitOfMeasure {

    @XmlEnumValue("watts")
    WATTS("watts");
    private final String value;

    WattageUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static WattageUnitOfMeasure fromValue(String v) {
        for (WattageUnitOfMeasure c: WattageUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
