
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
 *         &lt;element name="HardwarePlatform" type="{}HardwarePlatformType"/>
 *         &lt;element name="SystemRequirements" type="{}LongStringNotNull" minOccurs="0"/>
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
    "hardwarePlatform",
    "systemRequirements"
})
@XmlRootElement(name = "SoftwarePlatform")
public class SoftwarePlatform {

    @XmlElement(name = "HardwarePlatform", required = true)
    protected HardwarePlatformType hardwarePlatform;
    @XmlElement(name = "SystemRequirements", defaultValue = "N/A")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String systemRequirements;

    /**
     * Gets the value of the hardwarePlatform property.
     * 
     * @return
     *     possible object is
     *     {@link HardwarePlatformType }
     *     
     */
    public HardwarePlatformType getHardwarePlatform() {
        return hardwarePlatform;
    }

    /**
     * Sets the value of the hardwarePlatform property.
     * 
     * @param value
     *     allowed object is
     *     {@link HardwarePlatformType }
     *     
     */
    public void setHardwarePlatform(HardwarePlatformType value) {
        this.hardwarePlatform = value;
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

}
