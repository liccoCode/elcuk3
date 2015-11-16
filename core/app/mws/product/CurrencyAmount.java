
package mws.product;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for CurrencyAmount complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CurrencyAmount">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;>BaseCurrencyAmount">
 *       &lt;attribute name="currency" use="required" type="{}BaseCurrencyCode" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CurrencyAmount", propOrder = {
    "value"
})
public class CurrencyAmount {

    @XmlValue
    protected BigDecimal value;
    @XmlAttribute(name = "currency", required = true)
    protected BaseCurrencyCode currency;

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
     *     {@link BaseCurrencyCode }
     *     
     */
    public BaseCurrencyCode getCurrency() {
        return currency;
    }

    /**
     * Sets the value of the currency property.
     * 
     * @param value
     *     allowed object is
     *     {@link BaseCurrencyCode }
     *     
     */
    public void setCurrency(BaseCurrencyCode value) {
        this.currency = value;
    }

}
