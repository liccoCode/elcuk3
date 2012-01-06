
package com.amazonservices.mws.sellers.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Marketplace complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Marketplace">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MarketplaceId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DefaultLanguageCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DefaultCountryCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DefaultCurrencyCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DomainName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * Generated by AWS Code Generator
 * <p/>
 * Fri Jun 24 20:08:14 GMT 2011
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Marketplace", propOrder = {
    "marketplaceId",
    "name",
    "defaultLanguageCode",
    "defaultCountryCode",
    "defaultCurrencyCode",
    "domainName"
})
public class Marketplace {

    @XmlElement(name = "MarketplaceId")
    protected String marketplaceId;
    @XmlElement(name = "Name")
    protected String name;
    @XmlElement(name = "DefaultLanguageCode")
    protected String defaultLanguageCode;
    @XmlElement(name = "DefaultCountryCode")
    protected String defaultCountryCode;
    @XmlElement(name = "DefaultCurrencyCode")
    protected String defaultCurrencyCode;
    @XmlElement(name = "DomainName")
    protected String domainName;

    /**
     * Default constructor
     * 
     */
    public Marketplace() {
        super();
    }

    /**
     * Value constructor
     * 
     */
    public Marketplace(final String marketplaceId, final String name, final String defaultLanguageCode, final String defaultCountryCode, final String defaultCurrencyCode, final String domainName) {
        this.marketplaceId = marketplaceId;
        this.name = name;
        this.defaultLanguageCode = defaultLanguageCode;
        this.defaultCountryCode = defaultCountryCode;
        this.defaultCurrencyCode = defaultCurrencyCode;
        this.domainName = domainName;
    }

    /**
     * Gets the value of the marketplaceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMarketplaceId() {
        return marketplaceId;
    }

    /**
     * Sets the value of the marketplaceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMarketplaceId(String value) {
        this.marketplaceId = value;
    }

    public boolean isSetMarketplaceId() {
        return (this.marketplaceId!= null);
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    public boolean isSetName() {
        return (this.name!= null);
    }

    /**
     * Gets the value of the defaultLanguageCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultLanguageCode() {
        return defaultLanguageCode;
    }

    /**
     * Sets the value of the defaultLanguageCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultLanguageCode(String value) {
        this.defaultLanguageCode = value;
    }

    public boolean isSetDefaultLanguageCode() {
        return (this.defaultLanguageCode!= null);
    }

    /**
     * Gets the value of the defaultCountryCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultCountryCode() {
        return defaultCountryCode;
    }

    /**
     * Sets the value of the defaultCountryCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultCountryCode(String value) {
        this.defaultCountryCode = value;
    }

    public boolean isSetDefaultCountryCode() {
        return (this.defaultCountryCode!= null);
    }

    /**
     * Gets the value of the defaultCurrencyCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultCurrencyCode() {
        return defaultCurrencyCode;
    }

    /**
     * Sets the value of the defaultCurrencyCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultCurrencyCode(String value) {
        this.defaultCurrencyCode = value;
    }

    public boolean isSetDefaultCurrencyCode() {
        return (this.defaultCurrencyCode!= null);
    }

    /**
     * Gets the value of the domainName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * Sets the value of the domainName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDomainName(String value) {
        this.domainName = value;
    }

    public boolean isSetDomainName() {
        return (this.domainName!= null);
    }

    /**
     * Sets the value of the MarketplaceId property.
     * 
     * @param value
     * @return
     *     this instance
     */
    public Marketplace withMarketplaceId(String value) {
        setMarketplaceId(value);
        return this;
    }

    /**
     * Sets the value of the Name property.
     * 
     * @param value
     * @return
     *     this instance
     */
    public Marketplace withName(String value) {
        setName(value);
        return this;
    }

    /**
     * Sets the value of the DefaultLanguageCode property.
     * 
     * @param value
     * @return
     *     this instance
     */
    public Marketplace withDefaultLanguageCode(String value) {
        setDefaultLanguageCode(value);
        return this;
    }

    /**
     * Sets the value of the DefaultCountryCode property.
     * 
     * @param value
     * @return
     *     this instance
     */
    public Marketplace withDefaultCountryCode(String value) {
        setDefaultCountryCode(value);
        return this;
    }

    /**
     * Sets the value of the DefaultCurrencyCode property.
     * 
     * @param value
     * @return
     *     this instance
     */
    public Marketplace withDefaultCurrencyCode(String value) {
        setDefaultCurrencyCode(value);
        return this;
    }

    /**
     * Sets the value of the DomainName property.
     * 
     * @param value
     * @return
     *     this instance
     */
    public Marketplace withDomainName(String value) {
        setDomainName(value);
        return this;
    }
    

    /**
     * 
     * XML fragment representation of this object
     * 
     * @return XML fragment for this object. Name for outer
     * tag expected to be set by calling method. This fragment
     * returns inner properties representation only
     */
    protected String toXMLFragment() {
        StringBuffer xml = new StringBuffer();
        if (isSetMarketplaceId()) {
            xml.append("<MarketplaceId>");
            xml.append(escapeXML(getMarketplaceId()));
            xml.append("</MarketplaceId>");
        }
        if (isSetName()) {
            xml.append("<Name>");
            xml.append(escapeXML(getName()));
            xml.append("</Name>");
        }
        if (isSetDefaultLanguageCode()) {
            xml.append("<DefaultLanguageCode>");
            xml.append(escapeXML(getDefaultLanguageCode()));
            xml.append("</DefaultLanguageCode>");
        }
        if (isSetDefaultCountryCode()) {
            xml.append("<DefaultCountryCode>");
            xml.append(escapeXML(getDefaultCountryCode()));
            xml.append("</DefaultCountryCode>");
        }
        if (isSetDefaultCurrencyCode()) {
            xml.append("<DefaultCurrencyCode>");
            xml.append(escapeXML(getDefaultCurrencyCode()));
            xml.append("</DefaultCurrencyCode>");
        }
        if (isSetDomainName()) {
            xml.append("<DomainName>");
            xml.append(escapeXML(getDomainName()));
            xml.append("</DomainName>");
        }
        return xml.toString();
    }

    /**
     * 
     * Escape XML special characters
     */
    private String escapeXML(String string) {
        StringBuffer sb = new StringBuffer();
        int length = string.length();
        for (int i = 0; i < length; ++i) {
            char c = string.charAt(i);
            switch (c) {
            case '&':
                sb.append("&amp;");
                break;
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            case '\'':
                sb.append("&#039;");
                break;
            case '"':
                sb.append("&quot;");
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }



    /**
     *
     * JSON fragment representation of this object
     *
     * @return JSON fragment for this object. Name for outer
     * object expected to be set by calling method. This fragment
     * returns inner properties representation only
     *
     */
    protected String toJSONFragment() {
        StringBuffer json = new StringBuffer();
        boolean first = true;
        if (isSetMarketplaceId()) {
            if (!first) json.append(", ");
            json.append(quoteJSON("MarketplaceId"));
            json.append(" : ");
            json.append(quoteJSON(getMarketplaceId()));
            first = false;
        }
        if (isSetName()) {
            if (!first) json.append(", ");
            json.append(quoteJSON("Name"));
            json.append(" : ");
            json.append(quoteJSON(getName()));
            first = false;
        }
        if (isSetDefaultLanguageCode()) {
            if (!first) json.append(", ");
            json.append(quoteJSON("DefaultLanguageCode"));
            json.append(" : ");
            json.append(quoteJSON(getDefaultLanguageCode()));
            first = false;
        }
        if (isSetDefaultCountryCode()) {
            if (!first) json.append(", ");
            json.append(quoteJSON("DefaultCountryCode"));
            json.append(" : ");
            json.append(quoteJSON(getDefaultCountryCode()));
            first = false;
        }
        if (isSetDefaultCurrencyCode()) {
            if (!first) json.append(", ");
            json.append(quoteJSON("DefaultCurrencyCode"));
            json.append(" : ");
            json.append(quoteJSON(getDefaultCurrencyCode()));
            first = false;
        }
        if (isSetDomainName()) {
            if (!first) json.append(", ");
            json.append(quoteJSON("DomainName"));
            json.append(" : ");
            json.append(quoteJSON(getDomainName()));
            first = false;
        }
        return json.toString();
    }

    /**
     *
     * Quote JSON string
     */
    private String quoteJSON(String string) {
        StringBuffer sb = new StringBuffer();
        sb.append("\"");
        int length = string.length();
        for (int i = 0; i < length; ++i) {
            char c = string.charAt(i);
            switch (c) {
            case '"':
                sb.append("\\\"");
                break;
            case '\\':
                sb.append("\\\\");
                break;
            case '/':
                sb.append("\\/");
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\t':
                sb.append("\\t");
                break;
            default:
                if (c <  ' ') {
                    sb.append("\\u" + String.format("%03x", Integer.valueOf(c)));
                } else {
                sb.append(c);
            }
        }
        }
        sb.append("\"");
        return sb.toString();
    }


}
