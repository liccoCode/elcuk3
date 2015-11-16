
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StoneCreationMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="StoneCreationMethod">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="natural"/>
 *     &lt;enumeration value="simulated"/>
 *     &lt;enumeration value="synthetic"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "StoneCreationMethod")
@XmlEnum
public enum StoneCreationMethod {

    @XmlEnumValue("natural")
    NATURAL("natural"),
    @XmlEnumValue("simulated")
    SIMULATED("simulated"),
    @XmlEnumValue("synthetic")
    SYNTHETIC("synthetic");
    private final String value;

    StoneCreationMethod(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static StoneCreationMethod fromValue(String v) {
        for (StoneCreationMethod c: StoneCreationMethod.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
