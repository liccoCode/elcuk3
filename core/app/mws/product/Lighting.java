
package mws.product;

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
 *         &lt;element name="ProductType">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element ref="{}LightsAndFixtures"/>
 *                   &lt;element ref="{}LightingAccessories"/>
 *                   &lt;element ref="{}LightBulbs"/>
 *                 &lt;/choice>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ManufacturerWarrantyDescription" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}normalizedString">
 *               &lt;maxLength value="1500"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="SellerWarrantyDescription" type="{}SuperLongStringNotNull" minOccurs="0"/>
 *         &lt;element name="WarrantyType" type="{}String" minOccurs="0"/>
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
    "productType",
    "manufacturerWarrantyDescription",
    "sellerWarrantyDescription",
    "warrantyType"
})
@XmlRootElement(name = "Lighting")
public class Lighting {

    @XmlElement(name = "ProductType", required = true)
    protected Lighting.ProductType productType;
    @XmlElement(name = "ManufacturerWarrantyDescription")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String manufacturerWarrantyDescription;
    @XmlElement(name = "SellerWarrantyDescription")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String sellerWarrantyDescription;
    @XmlElement(name = "WarrantyType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String warrantyType;

    /**
     * Gets the value of the productType property.
     * 
     * @return
     *     possible object is
     *     {@link Lighting.ProductType }
     *     
     */
    public Lighting.ProductType getProductType() {
        return productType;
    }

    /**
     * Sets the value of the productType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Lighting.ProductType }
     *     
     */
    public void setProductType(Lighting.ProductType value) {
        this.productType = value;
    }

    /**
     * Gets the value of the manufacturerWarrantyDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManufacturerWarrantyDescription() {
        return manufacturerWarrantyDescription;
    }

    /**
     * Sets the value of the manufacturerWarrantyDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManufacturerWarrantyDescription(String value) {
        this.manufacturerWarrantyDescription = value;
    }

    /**
     * Gets the value of the sellerWarrantyDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSellerWarrantyDescription() {
        return sellerWarrantyDescription;
    }

    /**
     * Sets the value of the sellerWarrantyDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSellerWarrantyDescription(String value) {
        this.sellerWarrantyDescription = value;
    }

    /**
     * Gets the value of the warrantyType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWarrantyType() {
        return warrantyType;
    }

    /**
     * Sets the value of the warrantyType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWarrantyType(String value) {
        this.warrantyType = value;
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
     *         &lt;element ref="{}LightsAndFixtures"/>
     *         &lt;element ref="{}LightingAccessories"/>
     *         &lt;element ref="{}LightBulbs"/>
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
        "lightsAndFixtures",
        "lightingAccessories",
        "lightBulbs"
    })
    public static class ProductType {

        @XmlElement(name = "LightsAndFixtures")
        protected LightsAndFixtures lightsAndFixtures;
        @XmlElement(name = "LightingAccessories")
        protected LightingAccessories lightingAccessories;
        @XmlElement(name = "LightBulbs")
        protected LightBulbs lightBulbs;

        /**
         * Gets the value of the lightsAndFixtures property.
         * 
         * @return
         *     possible object is
         *     {@link LightsAndFixtures }
         *     
         */
        public LightsAndFixtures getLightsAndFixtures() {
            return lightsAndFixtures;
        }

        /**
         * Sets the value of the lightsAndFixtures property.
         * 
         * @param value
         *     allowed object is
         *     {@link LightsAndFixtures }
         *     
         */
        public void setLightsAndFixtures(LightsAndFixtures value) {
            this.lightsAndFixtures = value;
        }

        /**
         * Gets the value of the lightingAccessories property.
         * 
         * @return
         *     possible object is
         *     {@link LightingAccessories }
         *     
         */
        public LightingAccessories getLightingAccessories() {
            return lightingAccessories;
        }

        /**
         * Sets the value of the lightingAccessories property.
         * 
         * @param value
         *     allowed object is
         *     {@link LightingAccessories }
         *     
         */
        public void setLightingAccessories(LightingAccessories value) {
            this.lightingAccessories = value;
        }

        /**
         * Gets the value of the lightBulbs property.
         * 
         * @return
         *     possible object is
         *     {@link LightBulbs }
         *     
         */
        public LightBulbs getLightBulbs() {
            return lightBulbs;
        }

        /**
         * Sets the value of the lightBulbs property.
         * 
         * @param value
         *     allowed object is
         *     {@link LightBulbs }
         *     
         */
        public void setLightBulbs(LightBulbs value) {
            this.lightBulbs = value;
        }

    }

}
