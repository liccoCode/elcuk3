
package com.elcuk.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OpticalPowerUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="OpticalPowerUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="diopters"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "OpticalPowerUnitOfMeasure")
@XmlEnum
public enum OpticalPowerUnitOfMeasure {

    @XmlEnumValue("diopters")
    DIOPTERS("diopters");
    private final String value;

    OpticalPowerUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OpticalPowerUnitOfMeasure fromValue(String v) {
        for (OpticalPowerUnitOfMeasure c: OpticalPowerUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
