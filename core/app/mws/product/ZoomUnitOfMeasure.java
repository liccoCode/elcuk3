
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ZoomUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ZoomUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="x"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ZoomUnitOfMeasure")
@XmlEnum
public enum ZoomUnitOfMeasure {

    @XmlEnumValue("x")
    X("x");
    private final String value;

    ZoomUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ZoomUnitOfMeasure fromValue(String v) {
        for (ZoomUnitOfMeasure c: ZoomUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
