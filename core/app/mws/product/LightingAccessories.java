
package mws.product;

import java.math.BigDecimal;
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
 *         &lt;element name="BaseDiameter" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element ref="{}Battery" minOccurs="0"/>
 *         &lt;element name="CircuitBreakerInstallationType" type="{}String" minOccurs="0"/>
 *         &lt;element name="CircuitBreakerType" type="{}String" minOccurs="0"/>
 *         &lt;element name="Color" type="{}String" minOccurs="0"/>
 *         &lt;element name="ColorMap" type="{}String" minOccurs="0"/>
 *         &lt;element name="CountryOfOrigin" type="{}CountryOfOriginType" minOccurs="0"/>
 *         &lt;element name="DisplayDepth" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="DisplayDiameter" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="DisplayHeight" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="DisplayLength" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="DisplayWeight" type="{}WeightDimension" minOccurs="0"/>
 *         &lt;element name="DisplayWidth" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="IncludedComponent" type="{}String" maxOccurs="5" minOccurs="0"/>
 *         &lt;element name="MaximumCurrent" type="{}AmperageDimension" minOccurs="0"/>
 *         &lt;element name="NumberOfStrands" type="{}String" minOccurs="0"/>
 *         &lt;element name="PlugInstallationType" type="{}String" minOccurs="0"/>
 *         &lt;element name="PlugType" type="{}HundredString" minOccurs="0"/>
 *         &lt;element name="PowerSource" type="{}HundredString" minOccurs="0"/>
 *         &lt;element name="SpecificUses" type="{}String" minOccurs="0"/>
 *         &lt;element name="StrandDiameter" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="SwitchStyle" type="{}String" minOccurs="0"/>
 *         &lt;element name="SwitchType" type="{}String" minOccurs="0"/>
 *         &lt;element name="Voltage" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="Volume" type="{}VolumeDimension" minOccurs="0"/>
 *         &lt;element name="Wattage" type="{}Dimension" minOccurs="0"/>
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
    "baseDiameter",
    "battery",
    "circuitBreakerInstallationType",
    "circuitBreakerType",
    "color",
    "colorMap",
    "countryOfOrigin",
    "displayDepth",
    "displayDiameter",
    "displayHeight",
    "displayLength",
    "displayWeight",
    "displayWidth",
    "includedComponent",
    "maximumCurrent",
    "numberOfStrands",
    "plugInstallationType",
    "plugType",
    "powerSource",
    "specificUses",
    "strandDiameter",
    "switchStyle",
    "switchType",
    "voltage",
    "volume",
    "wattage"
})
@XmlRootElement(name = "LightingAccessories")
public class LightingAccessories {

    @XmlElement(name = "BaseDiameter")
    protected LengthDimension baseDiameter;
    @XmlElement(name = "Battery")
    protected Battery battery;
    @XmlElement(name = "CircuitBreakerInstallationType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String circuitBreakerInstallationType;
    @XmlElement(name = "CircuitBreakerType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String circuitBreakerType;
    @XmlElement(name = "Color")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String color;
    @XmlElement(name = "ColorMap")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String colorMap;
    @XmlElement(name = "CountryOfOrigin")
    protected String countryOfOrigin;
    @XmlElement(name = "DisplayDepth")
    protected LengthDimension displayDepth;
    @XmlElement(name = "DisplayDiameter")
    protected LengthDimension displayDiameter;
    @XmlElement(name = "DisplayHeight")
    protected LengthDimension displayHeight;
    @XmlElement(name = "DisplayLength")
    protected LengthDimension displayLength;
    @XmlElement(name = "DisplayWeight")
    protected WeightDimension displayWeight;
    @XmlElement(name = "DisplayWidth")
    protected LengthDimension displayWidth;
    @XmlElement(name = "IncludedComponent")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> includedComponent;
    @XmlElement(name = "MaximumCurrent")
    protected AmperageDimension maximumCurrent;
    @XmlElement(name = "NumberOfStrands")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String numberOfStrands;
    @XmlElement(name = "PlugInstallationType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String plugInstallationType;
    @XmlElement(name = "PlugType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String plugType;
    @XmlElement(name = "PowerSource")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String powerSource;
    @XmlElement(name = "SpecificUses")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String specificUses;
    @XmlElement(name = "StrandDiameter")
    protected LengthDimension strandDiameter;
    @XmlElement(name = "SwitchStyle")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String switchStyle;
    @XmlElement(name = "SwitchType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String switchType;
    @XmlElement(name = "Voltage")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger voltage;
    @XmlElement(name = "Volume")
    protected VolumeDimension volume;
    @XmlElement(name = "Wattage")
    protected BigDecimal wattage;

    /**
     * Gets the value of the baseDiameter property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDimension }
     *     
     */
    public LengthDimension getBaseDiameter() {
        return baseDiameter;
    }

    /**
     * Sets the value of the baseDiameter property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDimension }
     *     
     */
    public void setBaseDiameter(LengthDimension value) {
        this.baseDiameter = value;
    }

    /**
     * Gets the value of the battery property.
     * 
     * @return
     *     possible object is
     *     {@link Battery }
     *     
     */
    public Battery getBattery() {
        return battery;
    }

    /**
     * Sets the value of the battery property.
     * 
     * @param value
     *     allowed object is
     *     {@link Battery }
     *     
     */
    public void setBattery(Battery value) {
        this.battery = value;
    }

    /**
     * Gets the value of the circuitBreakerInstallationType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCircuitBreakerInstallationType() {
        return circuitBreakerInstallationType;
    }

    /**
     * Sets the value of the circuitBreakerInstallationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCircuitBreakerInstallationType(String value) {
        this.circuitBreakerInstallationType = value;
    }

    /**
     * Gets the value of the circuitBreakerType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCircuitBreakerType() {
        return circuitBreakerType;
    }

    /**
     * Sets the value of the circuitBreakerType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCircuitBreakerType(String value) {
        this.circuitBreakerType = value;
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
     * Gets the value of the countryOfOrigin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    /**
     * Sets the value of the countryOfOrigin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountryOfOrigin(String value) {
        this.countryOfOrigin = value;
    }

    /**
     * Gets the value of the displayDepth property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDimension }
     *     
     */
    public LengthDimension getDisplayDepth() {
        return displayDepth;
    }

    /**
     * Sets the value of the displayDepth property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDimension }
     *     
     */
    public void setDisplayDepth(LengthDimension value) {
        this.displayDepth = value;
    }

    /**
     * Gets the value of the displayDiameter property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDimension }
     *     
     */
    public LengthDimension getDisplayDiameter() {
        return displayDiameter;
    }

    /**
     * Sets the value of the displayDiameter property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDimension }
     *     
     */
    public void setDisplayDiameter(LengthDimension value) {
        this.displayDiameter = value;
    }

    /**
     * Gets the value of the displayHeight property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDimension }
     *     
     */
    public LengthDimension getDisplayHeight() {
        return displayHeight;
    }

    /**
     * Sets the value of the displayHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDimension }
     *     
     */
    public void setDisplayHeight(LengthDimension value) {
        this.displayHeight = value;
    }

    /**
     * Gets the value of the displayLength property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDimension }
     *     
     */
    public LengthDimension getDisplayLength() {
        return displayLength;
    }

    /**
     * Sets the value of the displayLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDimension }
     *     
     */
    public void setDisplayLength(LengthDimension value) {
        this.displayLength = value;
    }

    /**
     * Gets the value of the displayWeight property.
     * 
     * @return
     *     possible object is
     *     {@link WeightDimension }
     *     
     */
    public WeightDimension getDisplayWeight() {
        return displayWeight;
    }

    /**
     * Sets the value of the displayWeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link WeightDimension }
     *     
     */
    public void setDisplayWeight(WeightDimension value) {
        this.displayWeight = value;
    }

    /**
     * Gets the value of the displayWidth property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDimension }
     *     
     */
    public LengthDimension getDisplayWidth() {
        return displayWidth;
    }

    /**
     * Sets the value of the displayWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDimension }
     *     
     */
    public void setDisplayWidth(LengthDimension value) {
        this.displayWidth = value;
    }

    /**
     * Gets the value of the includedComponent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the includedComponent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIncludedComponent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getIncludedComponent() {
        if (includedComponent == null) {
            includedComponent = new ArrayList<String>();
        }
        return this.includedComponent;
    }

    /**
     * Gets the value of the maximumCurrent property.
     * 
     * @return
     *     possible object is
     *     {@link AmperageDimension }
     *     
     */
    public AmperageDimension getMaximumCurrent() {
        return maximumCurrent;
    }

    /**
     * Sets the value of the maximumCurrent property.
     * 
     * @param value
     *     allowed object is
     *     {@link AmperageDimension }
     *     
     */
    public void setMaximumCurrent(AmperageDimension value) {
        this.maximumCurrent = value;
    }

    /**
     * Gets the value of the numberOfStrands property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumberOfStrands() {
        return numberOfStrands;
    }

    /**
     * Sets the value of the numberOfStrands property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumberOfStrands(String value) {
        this.numberOfStrands = value;
    }

    /**
     * Gets the value of the plugInstallationType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlugInstallationType() {
        return plugInstallationType;
    }

    /**
     * Sets the value of the plugInstallationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlugInstallationType(String value) {
        this.plugInstallationType = value;
    }

    /**
     * Gets the value of the plugType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlugType() {
        return plugType;
    }

    /**
     * Sets the value of the plugType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlugType(String value) {
        this.plugType = value;
    }

    /**
     * Gets the value of the powerSource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPowerSource() {
        return powerSource;
    }

    /**
     * Sets the value of the powerSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPowerSource(String value) {
        this.powerSource = value;
    }

    /**
     * Gets the value of the specificUses property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecificUses() {
        return specificUses;
    }

    /**
     * Sets the value of the specificUses property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecificUses(String value) {
        this.specificUses = value;
    }

    /**
     * Gets the value of the strandDiameter property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDimension }
     *     
     */
    public LengthDimension getStrandDiameter() {
        return strandDiameter;
    }

    /**
     * Sets the value of the strandDiameter property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDimension }
     *     
     */
    public void setStrandDiameter(LengthDimension value) {
        this.strandDiameter = value;
    }

    /**
     * Gets the value of the switchStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSwitchStyle() {
        return switchStyle;
    }

    /**
     * Sets the value of the switchStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSwitchStyle(String value) {
        this.switchStyle = value;
    }

    /**
     * Gets the value of the switchType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSwitchType() {
        return switchType;
    }

    /**
     * Sets the value of the switchType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSwitchType(String value) {
        this.switchType = value;
    }

    /**
     * Gets the value of the voltage property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getVoltage() {
        return voltage;
    }

    /**
     * Sets the value of the voltage property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setVoltage(BigInteger value) {
        this.voltage = value;
    }

    /**
     * Gets the value of the volume property.
     * 
     * @return
     *     possible object is
     *     {@link VolumeDimension }
     *     
     */
    public VolumeDimension getVolume() {
        return volume;
    }

    /**
     * Sets the value of the volume property.
     * 
     * @param value
     *     allowed object is
     *     {@link VolumeDimension }
     *     
     */
    public void setVolume(VolumeDimension value) {
        this.volume = value;
    }

    /**
     * Gets the value of the wattage property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getWattage() {
        return wattage;
    }

    /**
     * Sets the value of the wattage property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setWattage(BigDecimal value) {
        this.wattage = value;
    }

}
