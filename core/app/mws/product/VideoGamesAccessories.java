
package mws.product;

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
 *         &lt;element name="HardwarePlatform" type="{}MediumStringNotNull" maxOccurs="5"/>
 *         &lt;element name="Bundles" type="{}ThirtyStringNotNull" minOccurs="0"/>
 *         &lt;element name="Color" type="{}StringNotNull" minOccurs="0"/>
 *         &lt;element ref="{}ColorMap" minOccurs="0"/>
 *         &lt;element name="AdditionalFeatures" type="{}LongStringNotNull" minOccurs="0"/>
 *         &lt;element name="ItemPackageQuantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
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
    "bundles",
    "color",
    "colorMap",
    "additionalFeatures",
    "itemPackageQuantity"
})
@XmlRootElement(name = "VideoGamesAccessories")
public class VideoGamesAccessories {

    @XmlElement(name = "HardwarePlatform", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> hardwarePlatform;
    @XmlElement(name = "Bundles")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String bundles;
    @XmlElement(name = "Color")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String color;
    @XmlElement(name = "ColorMap")
    protected String colorMap;
    @XmlElement(name = "AdditionalFeatures")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String additionalFeatures;
    @XmlElement(name = "ItemPackageQuantity")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger itemPackageQuantity;

    /**
     * Gets the value of the hardwarePlatform property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hardwarePlatform property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHardwarePlatform().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getHardwarePlatform() {
        if (hardwarePlatform == null) {
            hardwarePlatform = new ArrayList<String>();
        }
        return this.hardwarePlatform;
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
     * Gets the value of the color property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the value of the color property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColor(String value) {
        this.color = value;
    }

    /**
     * Gets the value of the colorMap property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColorMap() {
        return colorMap;
    }

    /**
     * Sets the value of the colorMap property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColorMap(String value) {
        this.colorMap = value;
    }

    /**
     * Gets the value of the additionalFeatures property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdditionalFeatures() {
        return additionalFeatures;
    }

    /**
     * Sets the value of the additionalFeatures property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdditionalFeatures(String value) {
        this.additionalFeatures = value;
    }

    /**
     * Gets the value of the itemPackageQuantity property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getItemPackageQuantity() {
        return itemPackageQuantity;
    }

    /**
     * Sets the value of the itemPackageQuantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setItemPackageQuantity(BigInteger value) {
        this.itemPackageQuantity = value;
    }

}
