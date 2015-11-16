
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AreaUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="AreaUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="square-in"/>
 *     &lt;enumeration value="square-ft"/>
 *     &lt;enumeration value="square-cm"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "AreaUnitOfMeasure")
@XmlEnum
public enum AreaUnitOfMeasure {

    @XmlEnumValue("square-in")
    SQUARE_IN("square-in"),
    @XmlEnumValue("square-ft")
    SQUARE_FT("square-ft"),
    @XmlEnumValue("square-cm")
    SQUARE_CM("square-cm");
    private final String value;

    AreaUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AreaUnitOfMeasure fromValue(String v) {
        for (AreaUnitOfMeasure c: AreaUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
