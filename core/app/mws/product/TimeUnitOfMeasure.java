
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TimeUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TimeUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="sec"/>
 *     &lt;enumeration value="min"/>
 *     &lt;enumeration value="hr"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "TimeUnitOfMeasure")
@XmlEnum
public enum TimeUnitOfMeasure {

    @XmlEnumValue("sec")
    SEC("sec"),
    @XmlEnumValue("min")
    MIN("min"),
    @XmlEnumValue("hr")
    HR("hr");
    private final String value;

    TimeUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TimeUnitOfMeasure fromValue(String v) {
        for (TimeUnitOfMeasure c: TimeUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
