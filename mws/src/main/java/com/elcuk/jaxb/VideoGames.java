
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
 *         &lt;element name="ConsoleVideoGamesGenre" type="{}StringNotNull" maxOccurs="5"/>
 *         &lt;choice>
 *           &lt;element name="ESRBRating" type="{}FortyStringNotNull"/>
 *           &lt;element name="BBFCRating" type="{}BBFCRatingType"/>
 *           &lt;element name="PEGIRating" type="{}PEGIRatingType"/>
 *           &lt;element name="USKRating" type="{}USKRatingType"/>
 *         &lt;/choice>
 *         &lt;element name="HardwarePlatform" type="{}MediumStringNotNull"/>
 *         &lt;element name="Bundles" type="{}ThirtyStringNotNull" minOccurs="0"/>
 *         &lt;element name="ESRBDescriptors" type="{}StringNotNull" maxOccurs="5" minOccurs="0"/>
 *         &lt;element name="PEGIDetails" type="{}PEGIDetailsType" minOccurs="0"/>
 *         &lt;element name="MaxNumberOfPlayers" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
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
    "consoleVideoGamesGenre",
    "esrbRating",
    "bbfcRating",
    "pegiRating",
    "uskRating",
    "hardwarePlatform",
    "bundles",
    "esrbDescriptors",
    "pegiDetails",
    "maxNumberOfPlayers"
})
@XmlRootElement(name = "VideoGames")
public class VideoGames {

    @XmlElement(name = "ConsoleVideoGamesGenre", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> consoleVideoGamesGenre;
    @XmlElement(name = "ESRBRating")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String esrbRating;
    @XmlElement(name = "BBFCRating")
    protected BBFCRatingType bbfcRating;
    @XmlElement(name = "PEGIRating")
    protected PEGIRatingType pegiRating;
    @XmlElement(name = "USKRating")
    protected USKRatingType uskRating;
    @XmlElement(name = "HardwarePlatform", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String hardwarePlatform;
    @XmlElement(name = "Bundles")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String bundles;
    @XmlElement(name = "ESRBDescriptors")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> esrbDescriptors;
    @XmlElement(name = "PEGIDetails")
    protected PEGIDetailsType pegiDetails;
    @XmlElement(name = "MaxNumberOfPlayers")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger maxNumberOfPlayers;

    /**
     * Gets the value of the consoleVideoGamesGenre property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the consoleVideoGamesGenre property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConsoleVideoGamesGenre().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getConsoleVideoGamesGenre() {
        if (consoleVideoGamesGenre == null) {
            consoleVideoGamesGenre = new ArrayList<String>();
        }
        return this.consoleVideoGamesGenre;
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
     * Gets the value of the hardwarePlatform property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHardwarePlatform() {
        return hardwarePlatform;
    }

    /**
     * Sets the value of the hardwarePlatform property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHardwarePlatform(String value) {
        this.hardwarePlatform = value;
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
     * Gets the value of the maxNumberOfPlayers property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxNumberOfPlayers() {
        return maxNumberOfPlayers;
    }

    /**
     * Sets the value of the maxNumberOfPlayers property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxNumberOfPlayers(BigInteger value) {
        this.maxNumberOfPlayers = value;
    }

}
