
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for JewelryLengthUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="JewelryLengthUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="MM"/>
 *     &lt;enumeration value="CM"/>
 *     &lt;enumeration value="IN"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "JewelryLengthUnitOfMeasure")
@XmlEnum
public enum JewelryLengthUnitOfMeasure {

    MM,
    CM,
    IN;

    public String value() {
        return name();
    }

    public static JewelryLengthUnitOfMeasure fromValue(String v) {
        return valueOf(v);
    }

}
