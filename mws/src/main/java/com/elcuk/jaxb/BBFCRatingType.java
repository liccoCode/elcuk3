
package com.elcuk.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BBFCRatingType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BBFCRatingType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ages_12_and_over"/>
 *     &lt;enumeration value="ages_15_and_over"/>
 *     &lt;enumeration value="ages_18_and_over"/>
 *     &lt;enumeration value="exempt"/>
 *     &lt;enumeration value="parental_guidance"/>
 *     &lt;enumeration value="to_be_announced"/>
 *     &lt;enumeration value="universal_childrens"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "BBFCRatingType")
@XmlEnum
public enum BBFCRatingType {

    @XmlEnumValue("ages_12_and_over")
    AGES_12_AND_OVER("ages_12_and_over"),
    @XmlEnumValue("ages_15_and_over")
    AGES_15_AND_OVER("ages_15_and_over"),
    @XmlEnumValue("ages_18_and_over")
    AGES_18_AND_OVER("ages_18_and_over"),
    @XmlEnumValue("exempt")
    EXEMPT("exempt"),
    @XmlEnumValue("parental_guidance")
    PARENTAL_GUIDANCE("parental_guidance"),
    @XmlEnumValue("to_be_announced")
    TO_BE_ANNOUNCED("to_be_announced"),
    @XmlEnumValue("universal_childrens")
    UNIVERSAL_CHILDRENS("universal_childrens");
    private final String value;

    BBFCRatingType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BBFCRatingType fromValue(String v) {
        for (BBFCRatingType c: BBFCRatingType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
