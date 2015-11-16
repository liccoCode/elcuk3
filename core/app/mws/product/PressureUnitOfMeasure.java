
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PressureUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PressureUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="bars"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PressureUnitOfMeasure")
@XmlEnum
public enum PressureUnitOfMeasure {

    @XmlEnumValue("bars")
    BARS("bars");
    private final String value;

    PressureUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PressureUnitOfMeasure fromValue(String v) {
        for (PressureUnitOfMeasure c: PressureUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
