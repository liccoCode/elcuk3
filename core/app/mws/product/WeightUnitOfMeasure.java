
package mws.product;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WeightUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="WeightUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="GR"/>
 *     &lt;enumeration value="KG"/>
 *     &lt;enumeration value="OZ"/>
 *     &lt;enumeration value="LB"/>
 *     &lt;enumeration value="MG"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "WeightUnitOfMeasure")
@XmlEnum
public enum WeightUnitOfMeasure {

    GR,
    KG,
    OZ,
    LB,
    MG;

    public String value() {
        return name();
    }

    public static WeightUnitOfMeasure fromValue(String v) {
        return valueOf(v);
    }

}
