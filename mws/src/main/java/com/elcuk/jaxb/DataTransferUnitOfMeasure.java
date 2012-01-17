
package com.elcuk.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DataTransferUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DataTransferUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="KHz"/>
 *     &lt;enumeration value="MHz"/>
 *     &lt;enumeration value="GHz"/>
 *     &lt;enumeration value="Mbps"/>
 *     &lt;enumeration value="Gbps"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DataTransferUnitOfMeasure")
@XmlEnum
public enum DataTransferUnitOfMeasure {

    @XmlEnumValue("KHz")
    K_HZ("KHz"),
    @XmlEnumValue("MHz")
    M_HZ("MHz"),
    @XmlEnumValue("GHz")
    G_HZ("GHz"),
    @XmlEnumValue("Mbps")
    MBPS("Mbps"),
    @XmlEnumValue("Gbps")
    GBPS("Gbps");
    private final String value;

    DataTransferUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DataTransferUnitOfMeasure fromValue(String v) {
        for (DataTransferUnitOfMeasure c: DataTransferUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
