
package com.elcuk.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
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
 *         &lt;element name="ImageType">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Main"/>
 *               &lt;enumeration value="Swatch"/>
 *               &lt;enumeration value="PT1"/>
 *               &lt;enumeration value="PT2"/>
 *               &lt;enumeration value="PT3"/>
 *               &lt;enumeration value="PT4"/>
 *               &lt;enumeration value="PT5"/>
 *               &lt;enumeration value="PT6"/>
 *               &lt;enumeration value="PT7"/>
 *               &lt;enumeration value="PT8"/>
 *               &lt;enumeration value="Search"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="ImageLocation" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
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
    "imageType",
    "imageLocation"
})
@XmlRootElement(name = "ProductImage")
public class ProductImage {

    @XmlElement(name = "SKU", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String sku;
    @XmlElement(name = "ImageType", required = true)
    protected String imageType;
    @XmlElement(name = "ImageLocation")
    @XmlSchemaType(name = "anyURI")
    protected String imageLocation;

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
     * Gets the value of the imageType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImageType() {
        return imageType;
    }

    /**
     * Sets the value of the imageType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImageType(String value) {
        this.imageType = value;
    }

    /**
     * Gets the value of the imageLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImageLocation() {
        return imageLocation;
    }

    /**
     * Sets the value of the imageLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImageLocation(String value) {
        this.imageLocation = value;
    }

}
