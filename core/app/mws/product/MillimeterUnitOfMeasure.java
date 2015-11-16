
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MillimeterUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MillimeterUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="millimeters"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MillimeterUnitOfMeasure")
@XmlEnum
public enum MillimeterUnitOfMeasure {

    @XmlEnumValue("millimeters")
    MILLIMETERS("millimeters");
    private final String value;

    MillimeterUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MillimeterUnitOfMeasure fromValue(String v) {
        for (MillimeterUnitOfMeasure c: MillimeterUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
