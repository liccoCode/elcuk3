
package com.elcuk.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}SKU"/>
 *         &lt;element name="ShippingOverride" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{}ShipOption"/>
 *                   &lt;choice>
 *                     &lt;element name="IsShippingRestricted" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *                     &lt;sequence>
 *                       &lt;element name="Type">
 *                         &lt;simpleType>
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                             &lt;enumeration value="Additive"/>
 *                             &lt;enumeration value="Exclusive"/>
 *                           &lt;/restriction>
 *                         &lt;/simpleType>
 *                       &lt;/element>
 *                       &lt;element name="ShipAmount" type="{}CurrencyAmount"/>
 *                     &lt;/sequence>
 *                   &lt;/choice>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sku",
    "shippingOverride"
})
@XmlRootElement(name = "Override")
public class Override {

    @XmlElement(name = "SKU", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String sku;
    @XmlElement(name = "ShippingOverride")
    protected List<Override.ShippingOverride> shippingOverride;

    /**
     * Gets the value of the sku property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSKU() {
        return sku;
    }

    /**
     * Sets the value of the sku property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSKU(String value) {
        this.sku = value;
    }

    /**
     * Gets the value of the shippingOverride property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the shippingOverride property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getShippingOverride().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Override.ShippingOverride }
     * 
     * 
     */
    public List<Override.ShippingOverride> getShippingOverride() {
        if (shippingOverride == null) {
            shippingOverride = new ArrayList<Override.ShippingOverride>();
        }
        return this.shippingOverride;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element ref="{}ShipOption"/>
     *         &lt;choice>
     *           &lt;element name="IsShippingRestricted" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
     *           &lt;sequence>
     *             &lt;element name="Type">
     *               &lt;simpleType>
     *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                   &lt;enumeration value="Additive"/>
     *                   &lt;enumeration value="Exclusive"/>
     *                 &lt;/restriction>
     *               &lt;/simpleType>
     *             &lt;/element>
     *             &lt;element name="ShipAmount" type="{}CurrencyAmount"/>
     *           &lt;/sequence>
     *         &lt;/choice>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "shipOption",
        "isShippingRestricted",
        "type",
        "shipAmount"
    })
    public static class ShippingOverride {

        @XmlElement(name = "ShipOption", required = true)
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String shipOption;
        @XmlElement(name = "IsShippingRestricted")
        protected Boolean isShippingRestricted;
        @XmlElement(name = "Type")
        protected String type;
        @XmlElement(name = "ShipAmount")
        protected CurrencyAmount shipAmount;

        /**
         * Gets the value of the shipOption property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getShipOption() {
            return shipOption;
        }

        /**
         * Sets the value of the shipOption property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setShipOption(String value) {
            this.shipOption = value;
        }

        /**
         * Gets the value of the isShippingRestricted property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isIsShippingRestricted() {
            return isShippingRestricted;
        }

        /**
         * Sets the value of the isShippingRestricted property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setIsShippingRestricted(Boolean value) {
            this.isShippingRestricted = value;
        }

        /**
         * Gets the value of the type property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setType(String value) {
            this.type = value;
        }

        /**
         * Gets the value of the shipAmount property.
         * 
         * @return
         *     possible object is
         *     {@link CurrencyAmount }
         *     
         */
        public CurrencyAmount getShipAmount() {
            return shipAmount;
        }

        /**
         * Sets the value of the shipAmount property.
         * 
         * @param value
         *     allowed object is
         *     {@link CurrencyAmount }
         *     
         */
        public void setShipAmount(CurrencyAmount value) {
            this.shipAmount = value;
        }

    }

}
