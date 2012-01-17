
package com.elcuk.jaxb;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


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
 *         &lt;element ref="{}FulfillmentCenterID" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="Available" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *           &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *           &lt;element name="Lookup">
 *             &lt;simpleType>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 &lt;enumeration value="FulfillmentNetwork"/>
 *               &lt;/restriction>
 *             &lt;/simpleType>
 *           &lt;/element>
 *         &lt;/choice>
 *         &lt;element name="RestockDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="FulfillmentLatency" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="SwitchFulfillmentTo" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="MFN"/>
 *               &lt;enumeration value="AFN"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
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
    "fulfillmentCenterID",
    "available",
    "quantity",
    "lookup",
    "restockDate",
    "fulfillmentLatency",
    "switchFulfillmentTo"
})
@XmlRootElement(name = "Inventory")
public class Inventory {

    @XmlElement(name = "SKU", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String sku;
    @XmlElement(name = "FulfillmentCenterID")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String fulfillmentCenterID;
    @XmlElement(name = "Available")
    protected Boolean available;
    @XmlElement(name = "Quantity")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger quantity;
    @XmlElement(name = "Lookup")
    protected String lookup;
    @XmlElement(name = "RestockDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar restockDate;
    @XmlElement(name = "FulfillmentLatency")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger fulfillmentLatency;
    @XmlElement(name = "SwitchFulfillmentTo")
    protected String switchFulfillmentTo;

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
     * Gets the value of the fulfillmentCenterID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFulfillmentCenterID() {
        return fulfillmentCenterID;
    }

    /**
     * Sets the value of the fulfillmentCenterID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFulfillmentCenterID(String value) {
        this.fulfillmentCenterID = value;
    }

    /**
     * Gets the value of the available property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAvailable() {
        return available;
    }

    /**
     * Sets the value of the available property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAvailable(Boolean value) {
        this.available = value;
    }

    /**
     * Gets the value of the quantity property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getQuantity() {
        return quantity;
    }

    /**
     * Sets the value of the quantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setQuantity(BigInteger value) {
        this.quantity = value;
    }

    /**
     * Gets the value of the lookup property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLookup() {
        return lookup;
    }

    /**
     * Sets the value of the lookup property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLookup(String value) {
        this.lookup = value;
    }

    /**
     * Gets the value of the restockDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getRestockDate() {
        return restockDate;
    }

    /**
     * Sets the value of the restockDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setRestockDate(XMLGregorianCalendar value) {
        this.restockDate = value;
    }

    /**
     * Gets the value of the fulfillmentLatency property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFulfillmentLatency() {
        return fulfillmentLatency;
    }

    /**
     * Sets the value of the fulfillmentLatency property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setFulfillmentLatency(BigInteger value) {
        this.fulfillmentLatency = value;
    }

    /**
     * Gets the value of the switchFulfillmentTo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSwitchFulfillmentTo() {
        return switchFulfillmentTo;
    }

    /**
     * Sets the value of the switchFulfillmentTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSwitchFulfillmentTo(String value) {
        this.switchFulfillmentTo = value;
    }

}
