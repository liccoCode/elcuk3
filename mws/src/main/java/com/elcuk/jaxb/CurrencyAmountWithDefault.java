
package com.elcuk.jaxb;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for CurrencyAmountWithDefault complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CurrencyAmountWithDefault">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;>BaseCurrencyAmount">
 *       &lt;attribute name="currency" use="required" type="{}BaseCurrencyCodeWithDefault" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CurrencyAmountWithDefault", propOrder = {
    "value"
})
@XmlSeeAlso({
    OverrideCurrencyAmount.class
})
public class CurrencyAmountWithDefault {

    @XmlValue
    protected BigDecimal value;
    @XmlAttribute(required = true)
    protected BaseCurrencyCodeWithDefault currency;

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
     * Gets the value of the currency property.
     * 
     * @return
     *     possible object is
     *     {@link BaseCurrencyCodeWithDefault }
     *     
     */
    public BaseCurrencyCodeWithDefault getCurrency() {
        return currency;
    }

    /**
     * Sets the value of the currency property.
     * 
     * @param value
     *     allowed object is
     *     {@link BaseCurrencyCodeWithDefault }
     *     
     */
    public void setCurrency(BaseCurrencyCodeWithDefault value) {
        this.currency = value;
    }

}
