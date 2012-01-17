
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
 *         &lt;element name="MediaFormat" type="{}MediumStringNotNull" maxOccurs="3"/>
 *         &lt;element name="OperatingSystem" type="{}MediumStringNotNull" maxOccurs="7"/>
 *         &lt;element name="Bundles" type="{}ThirtyStringNotNull" minOccurs="0"/>
 *         &lt;element name="ChildrensSoftwareGenre" type="{}StringNotNull" maxOccurs="5" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="ESRBRating" type="{}FortyStringNotNull"/>
 *           &lt;element name="BBFCRating" type="{}BBFCRatingType"/>
 *           &lt;element name="PEGIRating" type="{}PEGIRatingType"/>
 *           &lt;element name="USKRating" type="{}USKRatingType"/>
 *         &lt;/choice>
 *         &lt;element name="ESRBDescriptors" type="{}StringNotNull" maxOccurs="5" minOccurs="0"/>
 *         &lt;element name="PEGIDetails" type="{}PEGIDetailsType" minOccurs="0"/>
 *         &lt;element name="MFGSuggestedAgeMin" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="MFGSuggestedAgeMax" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element ref="{}SoftwarePlatform" maxOccurs="4" minOccurs="0"/>
 *         &lt;element name="ApplicationVersion" type="{}StringNotNull" minOccurs="0"/>
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
    "mediaFormat",
    "operatingSystem",
    "bundles",
    "childrensSoftwareGenre",
    "esrbRating",
    "bbfcRating",
    "pegiRating",
    "uskRating",
    "esrbDescriptors",
    "pegiDetails",
    "mfgSuggestedAgeMin",
    "mfgSuggestedAgeMax",
    "softwarePlatform",
    "applicationVersion",
    "numberOfLicenses"
})
@XmlRootElement(name = "Software")
public class Software {

    @XmlElement(name = "MediaFormat", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> mediaFormat;
    @XmlElement(name = "OperatingSystem", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> operatingSystem;
    @XmlElement(name = "Bundles")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String bundles;
    @XmlElement(name = "ChildrensSoftwareGenre")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> childrensSoftwareGenre;
    @XmlElement(name = "ESRBRating")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String esrbRating;
    @XmlElement(name = "BBFCRating")
    protected BBFCRatingType bbfcRating;
    @XmlElement(name = "PEGIRating")
    protected PEGIRatingType pegiRating;
    @XmlElement(name = "USKRating")
    protected USKRatingType uskRating;
    @XmlElement(name = "ESRBDescriptors")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> esrbDescriptors;
    @XmlElement(name = "PEGIDetails")
    protected PEGIDetailsType pegiDetails;
    @XmlElement(name = "MFGSuggestedAgeMin")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger mfgSuggestedAgeMin;
    @XmlElement(name = "MFGSuggestedAgeMax")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger mfgSuggestedAgeMax;
    @XmlElement(name = "SoftwarePlatform")
    protected List<SoftwarePlatform> softwarePlatform;
    @XmlElement(name = "ApplicationVersion")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String applicationVersion;
    @XmlElement(name = "NumberOfLicenses")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger numberOfLicenses;

    /**
     * Gets the value of the mediaFormat property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mediaFormat property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMediaFormat().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getMediaFormat() {
        if (mediaFormat == null) {
            mediaFormat = new ArrayList<String>();
        }
        return this.mediaFormat;
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
     * Gets the value of the bundles property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBundles() {
        return bundles;
    }

    /**
     * Sets the value of the bundles property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBundles(String value) {
        this.bundles = value;
    }

    /**
     * Gets the value of the childrensSoftwareGenre property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the childrensSoftwareGenre property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChildrensSoftwareGenre().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getChildrensSoftwareGenre() {
        if (childrensSoftwareGenre == null) {
            childrensSoftwareGenre = new ArrayList<String>();
        }
        return this.childrensSoftwareGenre;
    }

    /**
     * Gets the value of the esrbRating property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getESRBRating() {
        return esrbRating;
    }

    /**
     * Sets the value of the esrbRating property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setESRBRating(String value) {
        this.esrbRating = value;
    }

    /**
     * Gets the value of the bbfcRating property.
     * 
     * @return
     *     possible object is
     *     {@link BBFCRatingType }
     *     
     */
    public BBFCRatingType getBBFCRating() {
        return bbfcRating;
    }

    /**
     * Sets the value of the bbfcRating property.
     * 
     * @param value
     *     allowed object is
     *     {@link BBFCRatingType }
     *     
     */
    public void setBBFCRating(BBFCRatingType value) {
        this.bbfcRating = value;
    }

    /**
     * Gets the value of the pegiRating property.
     * 
     * @return
     *     possible object is
     *     {@link PEGIRatingType }
     *     
     */
    public PEGIRatingType getPEGIRating() {
        return pegiRating;
    }

    /**
     * Sets the value of the pegiRating property.
     * 
     * @param value
     *     allowed object is
     *     {@link PEGIRatingType }
     *     
     */
    public void setPEGIRating(PEGIRatingType value) {
        this.pegiRating = value;
    }

    /**
     * Gets the value of the uskRating property.
     * 
     * @return
     *     possible object is
     *     {@link USKRatingType }
     *     
     */
    public USKRatingType getUSKRating() {
        return uskRating;
    }

    /**
     * Sets the value of the uskRating property.
     * 
     * @param value
     *     allowed object is
     *     {@link USKRatingType }
     *     
     */
    public void setUSKRating(USKRatingType value) {
        this.uskRating = value;
    }

    /**
     * Gets the value of the esrbDescriptors property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the esrbDescriptors property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getESRBDescriptors().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getESRBDescriptors() {
        if (esrbDescriptors == null) {
            esrbDescriptors = new ArrayList<String>();
        }
        return this.esrbDescriptors;
    }

    /**
     * Gets the value of the pegiDetails property.
     * 
     * @return
     *     possible object is
     *     {@link PEGIDetailsType }
     *     
     */
    public PEGIDetailsType getPEGIDetails() {
        return pegiDetails;
    }

    /**
     * Sets the value of the pegiDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link PEGIDetailsType }
     *     
     */
    public void setPEGIDetails(PEGIDetailsType value) {
        this.pegiDetails = value;
    }

    /**
     * Gets the value of the mfgSuggestedAgeMin property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMFGSuggestedAgeMin() {
        return mfgSuggestedAgeMin;
    }

    /**
     * Sets the value of the mfgSuggestedAgeMin property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMFGSuggestedAgeMin(BigInteger value) {
        this.mfgSuggestedAgeMin = value;
    }

    /**
     * Gets the value of the mfgSuggestedAgeMax property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMFGSuggestedAgeMax() {
        return mfgSuggestedAgeMax;
    }

    /**
     * Sets the value of the mfgSuggestedAgeMax property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMFGSuggestedAgeMax(BigInteger value) {
        this.mfgSuggestedAgeMax = value;
    }

    /**
     * Gets the value of the softwarePlatform property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the softwarePlatform property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSoftwarePlatform().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SoftwarePlatform }
     * 
     * 
     */
    public List<SoftwarePlatform> getSoftwarePlatform() {
        if (softwarePlatform == null) {
            softwarePlatform = new ArrayList<SoftwarePlatform>();
        }
        return this.softwarePlatform;
    }

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
