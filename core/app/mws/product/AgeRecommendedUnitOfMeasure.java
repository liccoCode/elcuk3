
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AgeRecommendedUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="AgeRecommendedUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="months"/>
 *     &lt;enumeration value="years"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "AgeRecommendedUnitOfMeasure")
@XmlEnum
public enum AgeRecommendedUnitOfMeasure {

    @XmlEnumValue("months")
    MONTHS("months"),
    @XmlEnumValue("years")
    YEARS("years");
    private final String value;

    AgeRecommendedUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AgeRecommendedUnitOfMeasure fromValue(String v) {
        for (AgeRecommendedUnitOfMeasure c: AgeRecommendedUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
