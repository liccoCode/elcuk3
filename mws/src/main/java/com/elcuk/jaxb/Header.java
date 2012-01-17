
package com.elcuk.jaxb;

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
 *         &lt;element name="DocumentVersion">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;pattern value="\d{1,2}\.\d{1,2}"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="MerchantIdentifier" type="{}String"/>
 *         &lt;element name="OverrideReleaseId" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;pattern value="\d{1,4}\.\d{1,4}"/>
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
    "documentVersion",
    "merchantIdentifier",
    "overrideReleaseId"
})
@XmlRootElement(name = "Header")
public class Header {

    @XmlElement(name = "DocumentVersion", required = true)
    protected String documentVersion;
    @XmlElement(name = "MerchantIdentifier", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String merchantIdentifier;
    @XmlElement(name = "OverrideReleaseId")
    protected String overrideReleaseId;

    /**
     * Gets the value of the documentVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentVersion() {
        return documentVersion;
    }

    /**
     * Sets the value of the documentVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentVersion(String value) {
        this.documentVersion = value;
    }

    /**
     * Gets the value of the merchantIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMerchantIdentifier() {
        return merchantIdentifier;
    }

    /**
     * Sets the value of the merchantIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMerchantIdentifier(String value) {
        this.merchantIdentifier = value;
    }

    /**
     * Gets the value of the overrideReleaseId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOverrideReleaseId() {
        return overrideReleaseId;
    }

    /**
     * Sets the value of the overrideReleaseId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOverrideReleaseId(String value) {
        this.overrideReleaseId = value;
    }

}
