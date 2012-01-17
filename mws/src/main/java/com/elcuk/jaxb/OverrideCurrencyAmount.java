
package com.elcuk.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OverrideCurrencyAmount complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OverrideCurrencyAmount">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;>CurrencyAmountWithDefault">
 *       &lt;attribute name="zero" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OverrideCurrencyAmount")
public class OverrideCurrencyAmount
    extends CurrencyAmountWithDefault
{

    @XmlAttribute
    protected Boolean zero;

    /**
     * Gets the value of the zero property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isZero() {
        return zero;
    }

    /**
     * Sets the value of the zero property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setZero(Boolean value) {
        this.zero = value;
    }

}
