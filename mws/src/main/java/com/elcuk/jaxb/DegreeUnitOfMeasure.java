
package com.elcuk.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DegreeUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DegreeUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="degrees"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DegreeUnitOfMeasure")
@XmlEnum
public enum DegreeUnitOfMeasure {

    @XmlEnumValue("degrees")
    DEGREES("degrees");
    private final String value;

    DegreeUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DegreeUnitOfMeasure fromValue(String v) {
        for (DegreeUnitOfMeasure c: DegreeUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
