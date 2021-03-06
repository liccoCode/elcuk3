
package com.elcuk.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for JewelryWeightUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="JewelryWeightUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="GR"/>
 *     &lt;enumeration value="KG"/>
 *     &lt;enumeration value="OZ"/>
 *     &lt;enumeration value="LB"/>
 *     &lt;enumeration value="CARATS"/>
 *     &lt;enumeration value="DWT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "JewelryWeightUnitOfMeasure")
@XmlEnum
public enum JewelryWeightUnitOfMeasure {

    GR,
    KG,
    OZ,
    LB,
    CARATS,
    DWT;

    public String value() {
        return name();
    }

    public static JewelryWeightUnitOfMeasure fromValue(String v) {
        return valueOf(v);
    }

}
