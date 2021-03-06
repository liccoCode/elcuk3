
package com.elcuk.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MemorySizeUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MemorySizeUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="TB"/>
 *     &lt;enumeration value="GB"/>
 *     &lt;enumeration value="MB"/>
 *     &lt;enumeration value="KB"/>
 *     &lt;enumeration value="KO"/>
 *     &lt;enumeration value="MO"/>
 *     &lt;enumeration value="GO"/>
 *     &lt;enumeration value="TO"/>
 *     &lt;enumeration value="bytes"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MemorySizeUnitOfMeasure")
@XmlEnum
public enum MemorySizeUnitOfMeasure {

    TB("TB"),
    GB("GB"),
    MB("MB"),
    KB("KB"),
    KO("KO"),
    MO("MO"),
    GO("GO"),
    TO("TO"),
    @XmlEnumValue("bytes")
    BYTES("bytes");
    private final String value;

    MemorySizeUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MemorySizeUnitOfMeasure fromValue(String v) {
        for (MemorySizeUnitOfMeasure c: MemorySizeUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
