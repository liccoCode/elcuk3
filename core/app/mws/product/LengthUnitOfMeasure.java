
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LengthUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="LengthUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="MM"/>
 *     &lt;enumeration value="CM"/>
 *     &lt;enumeration value="M"/>
 *     &lt;enumeration value="IN"/>
 *     &lt;enumeration value="FT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "LengthUnitOfMeasure")
@XmlEnum
public enum LengthUnitOfMeasure {

    MM,
    CM,
    M,
    IN,
    FT;

    public String value() {
        return name();
    }

    public static LengthUnitOfMeasure fromValue(String v) {
        return valueOf(v);
    }

}
