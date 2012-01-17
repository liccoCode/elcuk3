
package com.elcuk.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BaseCurrencyCodeWithDefault.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BaseCurrencyCodeWithDefault">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="USD"/>
 *     &lt;enumeration value="GBP"/>
 *     &lt;enumeration value="EUR"/>
 *     &lt;enumeration value="JPY"/>
 *     &lt;enumeration value="CAD"/>
 *     &lt;enumeration value="DEFAULT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "BaseCurrencyCodeWithDefault")
@XmlEnum
public enum BaseCurrencyCodeWithDefault {

    USD,
    GBP,
    EUR,
    JPY,
    CAD,
    DEFAULT;

    public String value() {
        return name();
    }

    public static BaseCurrencyCodeWithDefault fromValue(String v) {
        return valueOf(v);
    }

}
