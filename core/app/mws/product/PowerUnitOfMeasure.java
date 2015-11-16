
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PowerUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PowerUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="watts"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PowerUnitOfMeasure")
@XmlEnum
public enum PowerUnitOfMeasure {

    @XmlEnumValue("watts")
    WATTS("watts");
    private final String value;

    PowerUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PowerUnitOfMeasure fromValue(String v) {
        for (PowerUnitOfMeasure c: PowerUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
