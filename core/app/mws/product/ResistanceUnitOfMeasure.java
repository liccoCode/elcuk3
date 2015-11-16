
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ResistanceUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ResistanceUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ohms"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ResistanceUnitOfMeasure")
@XmlEnum
public enum ResistanceUnitOfMeasure {

    @XmlEnumValue("ohms")
    OHMS("ohms");
    private final String value;

    ResistanceUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ResistanceUnitOfMeasure fromValue(String v) {
        for (ResistanceUnitOfMeasure c: ResistanceUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
