
package com.elcuk.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PEGIDetailsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PEGIDetailsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="BadLanguage" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Fear" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Violence" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Discrimination" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Drugs" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="SexualContent" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PEGIDetailsType", propOrder = {
    "badLanguage",
    "fear",
    "violence",
    "discrimination",
    "drugs",
    "sexualContent"
})
public class PEGIDetailsType {

    @XmlElement(name = "BadLanguage")
    protected Boolean badLanguage;
    @XmlElement(name = "Fear")
    protected Boolean fear;
    @XmlElement(name = "Violence")
    protected Boolean violence;
    @XmlElement(name = "Discrimination")
    protected Boolean discrimination;
    @XmlElement(name = "Drugs")
    protected Boolean drugs;
    @XmlElement(name = "SexualContent")
    protected Boolean sexualContent;

    /**
     * Gets the value of the badLanguage property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isBadLanguage() {
        return badLanguage;
    }

    /**
     * Sets the value of the badLanguage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBadLanguage(Boolean value) {
        this.badLanguage = value;
    }

    /**
     * Gets the value of the fear property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isFear() {
        return fear;
    }

    /**
     * Sets the value of the fear property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFear(Boolean value) {
        this.fear = value;
    }

    /**
     * Gets the value of the violence property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isViolence() {
        return violence;
    }

    /**
     * Sets the value of the violence property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setViolence(Boolean value) {
        this.violence = value;
    }

    /**
     * Gets the value of the discrimination property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDiscrimination() {
        return discrimination;
    }

    /**
     * Sets the value of the discrimination property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDiscrimination(Boolean value) {
        this.discrimination = value;
    }

    /**
     * Gets the value of the drugs property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDrugs() {
        return drugs;
    }

    /**
     * Sets the value of the drugs property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDrugs(Boolean value) {
        this.drugs = value;
    }

    /**
     * Gets the value of the sexualContent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSexualContent() {
        return sexualContent;
    }

    /**
     * Sets the value of the sexualContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSexualContent(Boolean value) {
        this.sexualContent = value;
    }

}
