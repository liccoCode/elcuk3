
package com.elcuk.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PromotionApplicationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PromotionApplicationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Principal"/>
 *     &lt;enumeration value="Shipping"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PromotionApplicationType")
@XmlEnum
public enum PromotionApplicationType {

    @XmlEnumValue("Principal")
    PRINCIPAL("Principal"),
    @XmlEnumValue("Shipping")
    SHIPPING("Shipping");
    private final String value;

    PromotionApplicationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PromotionApplicationType fromValue(String v) {
        for (PromotionApplicationType c: PromotionApplicationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
