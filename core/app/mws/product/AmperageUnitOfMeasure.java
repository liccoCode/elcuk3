
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AmperageUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="AmperageUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="amps"/>
 *     &lt;enumeration value="kiloamps"/>
 *     &lt;enumeration value="microamps"/>
 *     &lt;enumeration value="milliamps"/>
 *     &lt;enumeration value="nanoamps"/>
 *     &lt;enumeration value="picoamps"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "AmperageUnitOfMeasure")
@XmlEnum
public enum AmperageUnitOfMeasure {

    @XmlEnumValue("amps")
    AMPS("amps"),
    @XmlEnumValue("kiloamps")
    KILOAMPS("kiloamps"),
    @XmlEnumValue("microamps")
    MICROAMPS("microamps"),
    @XmlEnumValue("milliamps")
    MILLIAMPS("milliamps"),
    @XmlEnumValue("nanoamps")
    NANOAMPS("nanoamps"),
    @XmlEnumValue("picoamps")
    PICOAMPS("picoamps");
    private final String value;

    AmperageUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AmperageUnitOfMeasure fromValue(String v) {
        for (AmperageUnitOfMeasure c: AmperageUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
