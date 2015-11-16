
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BaseCurrencyCode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BaseCurrencyCode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="USD"/>
 *     &lt;enumeration value="GBP"/>
 *     &lt;enumeration value="EUR"/>
 *     &lt;enumeration value="JPY"/>
 *     &lt;enumeration value="CAD"/>
 *     &lt;enumeration value="CNY"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "BaseCurrencyCode")
@XmlEnum
public enum BaseCurrencyCode {

    USD,
    GBP,
    EUR,
    JPY,
    CAD,
    CNY;

    public String value() {
        return name();
    }

    public static BaseCurrencyCode fromValue(String v) {
        return valueOf(v);
    }

}
