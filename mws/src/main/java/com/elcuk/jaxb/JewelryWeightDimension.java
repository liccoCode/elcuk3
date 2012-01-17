
package com.elcuk.jaxb;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for JewelryWeightDimension complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="JewelryWeightDimension">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;>FourDecimal">
 *       &lt;attribute name="unitOfMeasure" use="required" type="{}JewelryWeightUnitOfMeasure" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "JewelryWeightDimension", propOrder = {
    "value"
})
public class JewelryWeightDimension {

    @XmlValue
    protected BigDecimal value;
    @XmlAttribute(required = true)
    protected JewelryWeightUnitOfMeasure unitOfMeasure;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    /**
     * Gets the value of the unitOfMeasure property.
     * 
     * @return
     *     possible object is
     *     {@link JewelryWeightUnitOfMeasure }
     *     
     */
    public JewelryWeightUnitOfMeasure getUnitOfMeasure() {
        return unitOfMeasure;
    }

    /**
     * Sets the value of the unitOfMeasure property.
     * 
     * @param value
     *     allowed object is
     *     {@link JewelryWeightUnitOfMeasure }
     *     
     */
    public void setUnitOfMeasure(JewelryWeightUnitOfMeasure value) {
        this.unitOfMeasure = value;
    }

}
