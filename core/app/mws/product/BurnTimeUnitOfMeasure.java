
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BurnTimeUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BurnTimeUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="minutes"/>
 *     &lt;enumeration value="hours"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "BurnTimeUnitOfMeasure")
@XmlEnum
public enum BurnTimeUnitOfMeasure {

    @XmlEnumValue("minutes")
    MINUTES("minutes"),
    @XmlEnumValue("hours")
    HOURS("hours");
    private final String value;

    BurnTimeUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BurnTimeUnitOfMeasure fromValue(String v) {
        for (BurnTimeUnitOfMeasure c: BurnTimeUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
