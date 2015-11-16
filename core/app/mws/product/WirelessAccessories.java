
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
 *         &lt;element name="Color" type="{}StringNotNull" minOccurs="0"/>
 *         &lt;element ref="{}ColorMap" minOccurs="0"/>
 *         &lt;element name="AdditionalFeatures" type="{}LongStringNotNull" minOccurs="0"/>
 *         &lt;element name="TalkTime" type="{}TimeDimension" minOccurs="0"/>
 *         &lt;element name="StandbyTime" type="{}TimeDimension" minOccurs="0"/>
 *         &lt;element name="ChargingTime" type="{}TimeDimension" minOccurs="0"/>
 *         &lt;element name="BatteryPower" type="{}BatteryPowerIntegerDimension" minOccurs="0"/>
 *         &lt;element name="Solar" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Refillable" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Extended" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Slim" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Auxiliary" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="BatteryType" type="{}StringNotNull" minOccurs="0"/>
 *         &lt;element name="AntennaType" type="{}StringNotNull" minOccurs="0"/>
 *         &lt;element name="CompatiblePhoneModels" type="{}StringNotNull" maxOccurs="18" minOccurs="0"/>
 *         &lt;element name="ManufacturerName" type="{}StringNotNull" minOccurs="0"/>
 *         &lt;element name="Keywords" type="{}StringNotNull" maxOccurs="5" minOccurs="0"/>
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
    "color",
    "colorMap",
    "additionalFeatures",
    "talkTime",
    "standbyTime",
    "chargingTime",
    "batteryPower",
    "solar",
    "refillable",
    "extended",
    "slim",
    "auxiliary",
    "batteryType",
    "antennaType",
    "compatiblePhoneModels",
    "manufacturerName",
    "keywords",
    "itemPackageQuantity"
})
@XmlRootElement(name = "WirelessAccessories")
public class WirelessAccessories {

    @XmlElement(name = "Color")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String color;
    @XmlElement(name = "ColorMap")
    protected String colorMap;
    @XmlElement(name = "AdditionalFeatures")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String additionalFeatures;
    @XmlElement(name = "TalkTime")
    protected TimeDimension talkTime;
    @XmlElement(name = "StandbyTime")
    protected TimeDimension standbyTime;
    @XmlElement(name = "ChargingTime")
    protected TimeDimension chargingTime;
    @XmlElement(name = "BatteryPower")
    protected BatteryPowerIntegerDimension batteryPower;
    @XmlElement(name = "Solar")
    protected Boolean solar;
    @XmlElement(name = "Refillable")
    protected Boolean refillable;
    @XmlElement(name = "Extended")
    protected Boolean extended;
    @XmlElement(name = "Slim")
    protected Boolean slim;
    @XmlElement(name = "Auxiliary")
    protected Boolean auxiliary;
    @XmlElement(name = "BatteryType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String batteryType;
    @XmlElement(name = "AntennaType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String antennaType;
    @XmlElement(name = "CompatiblePhoneModels")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> compatiblePhoneModels;
    @XmlElement(name = "ManufacturerName")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String manufacturerName;
    @XmlElement(name = "Keywords")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> keywords;
    @XmlElement(name = "ItemPackageQuantity")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger itemPackageQuantity;

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
     * Gets the value of the talkTime property.
     * 
     * @return
     *     possible object is
     *     {@link TimeDimension }
     *     
     */
    public TimeDimension getTalkTime() {
        return talkTime;
    }

    /**
     * Sets the value of the talkTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeDimension }
     *     
     */
    public void setTalkTime(TimeDimension value) {
        this.talkTime = value;
    }

    /**
     * Gets the value of the standbyTime property.
     * 
     * @return
     *     possible object is
     *     {@link TimeDimension }
     *     
     */
    public TimeDimension getStandbyTime() {
        return standbyTime;
    }

    /**
     * Sets the value of the standbyTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeDimension }
     *     
     */
    public void setStandbyTime(TimeDimension value) {
        this.standbyTime = value;
    }

    /**
     * Gets the value of the chargingTime property.
     * 
     * @return
     *     possible object is
     *     {@link TimeDimension }
     *     
     */
    public TimeDimension getChargingTime() {
        return chargingTime;
    }

    /**
     * Sets the value of the chargingTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeDimension }
     *     
     */
    public void setChargingTime(TimeDimension value) {
        this.chargingTime = value;
    }

    /**
     * Gets the value of the batteryPower property.
     * 
     * @return
     *     possible object is
     *     {@link BatteryPowerIntegerDimension }
     *     
     */
    public BatteryPowerIntegerDimension getBatteryPower() {
        return batteryPower;
    }

    /**
     * Sets the value of the batteryPower property.
     * 
     * @param value
     *     allowed object is
     *     {@link BatteryPowerIntegerDimension }
     *     
     */
    public void setBatteryPower(BatteryPowerIntegerDimension value) {
        this.batteryPower = value;
    }

    /**
     * Gets the value of the solar property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSolar() {
        return solar;
    }

    /**
     * Sets the value of the solar property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSolar(Boolean value) {
        this.solar = value;
    }

    /**
     * Gets the value of the refillable property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRefillable() {
        return refillable;
    }

    /**
     * Sets the value of the refillable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRefillable(Boolean value) {
        this.refillable = value;
    }

    /**
     * Gets the value of the extended property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isExtended() {
        return extended;
    }

    /**
     * Sets the value of the extended property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setExtended(Boolean value) {
        this.extended = value;
    }

    /**
     * Gets the value of the slim property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSlim() {
        return slim;
    }

    /**
     * Sets the value of the slim property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSlim(Boolean value) {
        this.slim = value;
    }

    /**
     * Gets the value of the auxiliary property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAuxiliary() {
        return auxiliary;
    }

    /**
     * Sets the value of the auxiliary property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAuxiliary(Boolean value) {
        this.auxiliary = value;
    }

    /**
     * Gets the value of the batteryType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBatteryType() {
        return batteryType;
    }

    /**
     * Sets the value of the batteryType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBatteryType(String value) {
        this.batteryType = value;
    }

    /**
     * Gets the value of the antennaType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAntennaType() {
        return antennaType;
    }

    /**
     * Sets the value of the antennaType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAntennaType(String value) {
        this.antennaType = value;
    }

    /**
     * Gets the value of the compatiblePhoneModels property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the compatiblePhoneModels property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCompatiblePhoneModels().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getCompatiblePhoneModels() {
        if (compatiblePhoneModels == null) {
            compatiblePhoneModels = new ArrayList<String>();
        }
        return this.compatiblePhoneModels;
    }

    /**
     * Gets the value of the manufacturerName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManufacturerName() {
        return manufacturerName;
    }

    /**
     * Sets the value of the manufacturerName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManufacturerName(String value) {
        this.manufacturerName = value;
    }

    /**
     * Gets the value of the keywords property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the keywords property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKeywords().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getKeywords() {
        if (keywords == null) {
            keywords = new ArrayList<String>();
        }
        return this.keywords;
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
