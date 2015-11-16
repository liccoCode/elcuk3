
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DateUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DateUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="days"/>
 *     &lt;enumeration value="weeks"/>
 *     &lt;enumeration value="months"/>
 *     &lt;enumeration value="years"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DateUnitOfMeasure")
@XmlEnum
public enum DateUnitOfMeasure {

    @XmlEnumValue("days")
    DAYS("days"),
    @XmlEnumValue("weeks")
    WEEKS("weeks"),
    @XmlEnumValue("months")
    MONTHS("months"),
    @XmlEnumValue("years")
    YEARS("years");
    private final String value;

    DateUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DateUnitOfMeasure fromValue(String v) {
        for (DateUnitOfMeasure c: DateUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
