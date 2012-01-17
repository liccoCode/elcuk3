
package com.elcuk.jaxb;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for PositiveNonZeroWeightDimension complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PositiveNonZeroWeightDimension">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;>PositiveNonZeroDimension">
 *       &lt;attribute name="unitOfMeasure" use="required" type="{}WeightUnitOfMeasure" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PositiveNonZeroWeightDimension", propOrder = {
    "value"
})
public class PositiveNonZeroWeightDimension {

    @XmlValue
    protected BigDecimal value;
    @XmlAttribute(required = true)
    protected WeightUnitOfMeasure unitOfMeasure;

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
     *     {@link WeightUnitOfMeasure }
     *     
     */
    public WeightUnitOfMeasure getUnitOfMeasure() {
        return unitOfMeasure;
    }

    /**
     * Sets the value of the unitOfMeasure property.
     * 
     * @param value
     *     allowed object is
     *     {@link WeightUnitOfMeasure }
     *     
     */
    public void setUnitOfMeasure(WeightUnitOfMeasure value) {
        this.unitOfMeasure = value;
    }

}
