
package com.elcuk.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="ProductType">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element ref="{}WirelessAccessories"/>
 *                   &lt;element ref="{}WirelessDownloads"/>
 *                 &lt;/choice>
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
    "productType"
})
@XmlRootElement(name = "Wireless")
public class Wireless {

    @XmlElement(name = "ProductType", required = true)
    protected Wireless.ProductType productType;

    /**
     * Gets the value of the productType property.
     * 
     * @return
     *     possible object is
     *     {@link Wireless.ProductType }
     *     
     */
    public Wireless.ProductType getProductType() {
        return productType;
    }

    /**
     * Sets the value of the productType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Wireless.ProductType }
     *     
     */
    public void setProductType(Wireless.ProductType value) {
        this.productType = value;
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
     *       &lt;choice>
     *         &lt;element ref="{}WirelessAccessories"/>
     *         &lt;element ref="{}WirelessDownloads"/>
     *       &lt;/choice>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "wirelessAccessories",
        "wirelessDownloads"
    })
    public static class ProductType {

        @XmlElement(name = "WirelessAccessories")
        protected WirelessAccessories wirelessAccessories;
        @XmlElement(name = "WirelessDownloads")
        protected WirelessDownloads wirelessDownloads;

        /**
         * Gets the value of the wirelessAccessories property.
         * 
         * @return
         *     possible object is
         *     {@link WirelessAccessories }
         *     
         */
        public WirelessAccessories getWirelessAccessories() {
            return wirelessAccessories;
        }

        /**
         * Sets the value of the wirelessAccessories property.
         * 
         * @param value
         *     allowed object is
         *     {@link WirelessAccessories }
         *     
         */
        public void setWirelessAccessories(WirelessAccessories value) {
            this.wirelessAccessories = value;
        }

        /**
         * Gets the value of the wirelessDownloads property.
         * 
         * @return
         *     possible object is
         *     {@link WirelessDownloads }
         *     
         */
        public WirelessDownloads getWirelessDownloads() {
            return wirelessDownloads;
        }

        /**
         * Sets the value of the wirelessDownloads property.
         * 
         * @param value
         *     allowed object is
         *     {@link WirelessDownloads }
         *     
         */
        public void setWirelessDownloads(WirelessDownloads value) {
            this.wirelessDownloads = value;
        }

    }

}
