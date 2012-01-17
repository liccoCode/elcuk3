
package com.elcuk.jaxb;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element name="ApplicationVersion" type="{}StringNotNull"/>
 *         &lt;element ref="{}DownloadableFile"/>
 *         &lt;element name="OperatingSystem" type="{}MediumStringNotNull" maxOccurs="7"/>
 *         &lt;element name="SystemRequirements" type="{}LongStringNotNull" minOccurs="0"/>
 *         &lt;element name="NumberOfLicenses" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
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
    "applicationVersion",
    "downloadableFile",
    "operatingSystem",
    "systemRequirements",
    "numberOfLicenses"
})
@XmlRootElement(name = "HandheldSoftwareDownloads")
public class HandheldSoftwareDownloads {

    @XmlElement(name = "ApplicationVersion", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String applicationVersion;
    @XmlElement(name = "DownloadableFile", required = true)
    protected DownloadableFile downloadableFile;
    @XmlElement(name = "OperatingSystem", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> operatingSystem;
    @XmlElement(name = "SystemRequirements")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String systemRequirements;
    @XmlElement(name = "NumberOfLicenses")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger numberOfLicenses;

    /**
     * Gets the value of the applicationVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApplicationVersion() {
        return applicationVersion;
    }

    /**
     * Sets the value of the applicationVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApplicationVersion(String value) {
        this.applicationVersion = value;
    }

    /**
     * Gets the value of the downloadableFile property.
     * 
     * @return
     *     possible object is
     *     {@link DownloadableFile }
     *     
     */
    public DownloadableFile getDownloadableFile() {
        return downloadableFile;
    }

    /**
     * Sets the value of the downloadableFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link DownloadableFile }
     *     
     */
    public void setDownloadableFile(DownloadableFile value) {
        this.downloadableFile = value;
    }

    /**
     * Gets the value of the operatingSystem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the operatingSystem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOperatingSystem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getOperatingSystem() {
        if (operatingSystem == null) {
            operatingSystem = new ArrayList<String>();
        }
        return this.operatingSystem;
    }

    /**
     * Gets the value of the systemRequirements property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSystemRequirements() {
        return systemRequirements;
    }

    /**
     * Sets the value of the systemRequirements property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSystemRequirements(String value) {
        this.systemRequirements = value;
    }

    /**
     * Gets the value of the numberOfLicenses property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumberOfLicenses() {
        return numberOfLicenses;
    }

    /**
     * Sets the value of the numberOfLicenses property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumberOfLicenses(BigInteger value) {
        this.numberOfLicenses = value;
    }

}
