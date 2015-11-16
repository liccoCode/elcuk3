
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
 *         &lt;element name="VariationData" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Parentage">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="parent"/>
 *                         &lt;enumeration value="child"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="VariationTheme" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="Color"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="AirFlowCapacity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="BaseDiameter" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element ref="{}Battery" minOccurs="0"/>
 *         &lt;element name="BulbDiameter" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="BulbLength" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="BulbLifeSpan" type="{}TimeDimension" minOccurs="0"/>
 *         &lt;element name="BulbPowerFactor" type="{}Dimension" minOccurs="0"/>
 *         &lt;element name="BulbSpecialFeatures" type="{}String" maxOccurs="5" minOccurs="0"/>
 *         &lt;element name="BulbSwitchingCycles" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="BulbType" type="{}String" minOccurs="0"/>
 *         &lt;element name="BulbWattage" type="{}Dimension" minOccurs="0"/>
 *         &lt;element name="CapType" type="{}String" minOccurs="0"/>
 *         &lt;element name="Certification" type="{}String" maxOccurs="4" minOccurs="0"/>
 *         &lt;element name="Collection" type="{}String" minOccurs="0"/>
 *         &lt;element name="Color" type="{}String" minOccurs="0"/>
 *         &lt;element name="ColorMap" type="{}String" minOccurs="0"/>
 *         &lt;element name="ColorRenderingIndex" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="ColorTemperature" type="{}TemperatureRatingDimension" minOccurs="0"/>
 *         &lt;element name="CountryOfOrigin" type="{}CountryOfOriginType" minOccurs="0"/>
 *         &lt;element name="DisplayDepth" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="DisplayDiameter" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="DisplayHeight" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="DisplayLength" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="DisplayWeight" type="{}WeightDimension" minOccurs="0"/>
 *         &lt;element name="DisplayWidth" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="FanBladeColor" type="{}String" minOccurs="0"/>
 *         &lt;element name="FinishType" type="{}String" minOccurs="0"/>
 *         &lt;element name="IncandescentEquivalentWattage" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="IncludedComponent" type="{}String" maxOccurs="5" minOccurs="0"/>
 *         &lt;element name="InternationalProtectionRating" type="{}String" minOccurs="0"/>
 *         &lt;element name="ItemDiameter" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="LampStartupTime" type="{}MediumStringNotNull" minOccurs="0"/>
 *         &lt;element name="LampWarmupTime" type="{}MediumStringNotNull" minOccurs="0"/>
 *         &lt;element name="LightingMethod" type="{}String" minOccurs="0"/>
 *         &lt;element name="LightOutputLuminance" type="{}LuminanceDimension" minOccurs="0"/>
 *         &lt;element name="LithiumBatteryEnergyContent" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="LithiumBatteryPackaging" type="{}LithiumBatteryPackagingType" minOccurs="0"/>
 *         &lt;element name="LithiumBatteryVoltage" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="LithiumBatteryWeight" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="LumenMaintenanceFactor" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="LuminousIntensity" type="{}LuminiousIntensityDimension" minOccurs="0"/>
 *         &lt;element name="Material" type="{}String" minOccurs="0"/>
 *         &lt;element name="MaximumCurrent" type="{}AmperageDimension" minOccurs="0"/>
 *         &lt;element name="MaximumSupportedWattage" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="MercuryContent" type="{}WeightDimension" minOccurs="0"/>
 *         &lt;element name="NumberOfBlades" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="NumberOfBulbSockets" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="NumberOfLights" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="NumberOfLithiumIonCells" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="NumberOfLithiumMetalCells" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="PlugType" type="{}HundredString" minOccurs="0"/>
 *         &lt;element name="PowerSource" type="{}HundredString" minOccurs="0"/>
 *         &lt;element name="PPUCount" type="{}Dimension" minOccurs="0"/>
 *         &lt;element name="PPUCountType" type="{}String" minOccurs="0"/>
 *         &lt;element name="ShadeColor" type="{}String" minOccurs="0"/>
 *         &lt;element name="ShadeDiameter" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="ShadeMaterial" type="{}String" minOccurs="0"/>
 *         &lt;element name="SpecialFeatures" type="{}String" maxOccurs="5" minOccurs="0"/>
 *         &lt;element name="SpecificUses" type="{}String" maxOccurs="2" minOccurs="0"/>
 *         &lt;element name="StyleName" type="{}String" minOccurs="0"/>
 *         &lt;element name="SwitchStyle" type="{}String" minOccurs="0"/>
 *         &lt;element name="Voltage" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
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
    "variationData",
    "airFlowCapacity",
    "baseDiameter",
    "battery",
    "bulbDiameter",
    "bulbLength",
    "bulbLifeSpan",
    "bulbPowerFactor",
    "bulbSpecialFeatures",
    "bulbSwitchingCycles",
    "bulbType",
    "bulbWattage",
    "capType",
    "certification",
    "collection",
    "color",
    "colorMap",
    "colorRenderingIndex",
    "colorTemperature",
    "countryOfOrigin",
    "displayDepth",
    "displayDiameter",
    "displayHeight",
    "displayLength",
    "displayWeight",
    "displayWidth",
    "fanBladeColor",
    "finishType",
    "incandescentEquivalentWattage",
    "includedComponent",
    "internationalProtectionRating",
    "itemDiameter",
    "lampStartupTime",
    "lampWarmupTime",
    "lightingMethod",
    "lightOutputLuminance",
    "lithiumBatteryEnergyContent",
    "lithiumBatteryPackaging",
    "lithiumBatteryVoltage",
    "lithiumBatteryWeight",
    "lumenMaintenanceFactor",
    "luminousIntensity",
    "material",
    "maximumCurrent",
    "maximumSupportedWattage",
    "mercuryContent",
    "numberOfBlades",
    "numberOfBulbSockets",
    "numberOfLights",
    "numberOfLithiumIonCells",
    "numberOfLithiumMetalCells",
    "plugType",
    "powerSource",
    "ppuCount",
    "ppuCountType",
    "shadeColor",
    "shadeDiameter",
    "shadeMaterial",
    "specialFeatures",
    "specificUses",
    "styleName",
    "switchStyle",
    "voltage",
    "wattage"
})
@XmlRootElement(name = "LightsAndFixtures")
public class LightsAndFixtures {

    @XmlElement(name = "VariationData")
    protected LightsAndFixtures.VariationData variationData;
    @XmlElement(name = "AirFlowCapacity")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger airFlowCapacity;
    @XmlElement(name = "BaseDiameter")
    protected LengthDimension baseDiameter;
    @XmlElement(name = "Battery")
    protected Battery battery;
    @XmlElement(name = "BulbDiameter")
    protected LengthDimension bulbDiameter;
    @XmlElement(name = "BulbLength")
    protected LengthDimension bulbLength;
    @XmlElement(name = "BulbLifeSpan")
    protected TimeDimension bulbLifeSpan;
    @XmlElement(name = "BulbPowerFactor")
    protected BigDecimal bulbPowerFactor;
    @XmlElement(name = "BulbSpecialFeatures")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> bulbSpecialFeatures;
    @XmlElement(name = "BulbSwitchingCycles")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger bulbSwitchingCycles;
    @XmlElement(name = "BulbType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String bulbType;
    @XmlElement(name = "BulbWattage")
    protected BigDecimal bulbWattage;
    @XmlElement(name = "CapType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String capType;
    @XmlElement(name = "Certification")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> certification;
    @XmlElement(name = "Collection")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String collection;
    @XmlElement(name = "Color")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String color;
    @XmlElement(name = "ColorMap")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String colorMap;
    @XmlElement(name = "ColorRenderingIndex")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger colorRenderingIndex;
    @XmlElement(name = "ColorTemperature")
    protected TemperatureRatingDimension colorTemperature;
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
    @XmlElement(name = "FanBladeColor")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String fanBladeColor;
    @XmlElement(name = "FinishType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String finishType;
    @XmlElement(name = "IncandescentEquivalentWattage")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger incandescentEquivalentWattage;
    @XmlElement(name = "IncludedComponent")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> includedComponent;
    @XmlElement(name = "InternationalProtectionRating")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String internationalProtectionRating;
    @XmlElement(name = "ItemDiameter")
    protected LengthDimension itemDiameter;
    @XmlElement(name = "LampStartupTime")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String lampStartupTime;
    @XmlElement(name = "LampWarmupTime")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String lampWarmupTime;
    @XmlElement(name = "LightingMethod")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String lightingMethod;
    @XmlElement(name = "LightOutputLuminance")
    protected LuminanceDimension lightOutputLuminance;
    @XmlElement(name = "LithiumBatteryEnergyContent")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger lithiumBatteryEnergyContent;
    @XmlElement(name = "LithiumBatteryPackaging")
    protected LithiumBatteryPackagingType lithiumBatteryPackaging;
    @XmlElement(name = "LithiumBatteryVoltage")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger lithiumBatteryVoltage;
    @XmlElement(name = "LithiumBatteryWeight")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger lithiumBatteryWeight;
    @XmlElement(name = "LumenMaintenanceFactor")
    protected BigDecimal lumenMaintenanceFactor;
    @XmlElement(name = "LuminousIntensity")
    protected LuminiousIntensityDimension luminousIntensity;
    @XmlElement(name = "Material")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String material;
    @XmlElement(name = "MaximumCurrent")
    protected AmperageDimension maximumCurrent;
    @XmlElement(name = "MaximumSupportedWattage")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger maximumSupportedWattage;
    @XmlElement(name = "MercuryContent")
    protected WeightDimension mercuryContent;
    @XmlElement(name = "NumberOfBlades")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger numberOfBlades;
    @XmlElement(name = "NumberOfBulbSockets")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger numberOfBulbSockets;
    @XmlElement(name = "NumberOfLights")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger numberOfLights;
    @XmlElement(name = "NumberOfLithiumIonCells")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger numberOfLithiumIonCells;
    @XmlElement(name = "NumberOfLithiumMetalCells")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger numberOfLithiumMetalCells;
    @XmlElement(name = "PlugType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String plugType;
    @XmlElement(name = "PowerSource")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String powerSource;
    @XmlElement(name = "PPUCount")
    protected BigDecimal ppuCount;
    @XmlElement(name = "PPUCountType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String ppuCountType;
    @XmlElement(name = "ShadeColor")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String shadeColor;
    @XmlElement(name = "ShadeDiameter")
    protected LengthDimension shadeDiameter;
    @XmlElement(name = "ShadeMaterial")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String shadeMaterial;
    @XmlElement(name = "SpecialFeatures")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> specialFeatures;
    @XmlElement(name = "SpecificUses")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> specificUses;
    @XmlElement(name = "StyleName")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String styleName;
    @XmlElement(name = "SwitchStyle")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String switchStyle;
    @XmlElement(name = "Voltage")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger voltage;
    @XmlElement(name = "Wattage")
    protected BigDecimal wattage;

    /**
     * Gets the value of the variationData property.
     * 
     * @return
     *     possible object is
     *     {@link LightsAndFixtures.VariationData }
     *     
     */
    public LightsAndFixtures.VariationData getVariationData() {
        return variationData;
    }

    /**
     * Sets the value of the variationData property.
     * 
     * @param value
     *     allowed object is
     *     {@link LightsAndFixtures.VariationData }
     *     
     */
    public void setVariationData(LightsAndFixtures.VariationData value) {
        this.variationData = value;
    }

    /**
     * Gets the value of the airFlowCapacity property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAirFlowCapacity() {
        return airFlowCapacity;
    }

    /**
     * Sets the value of the airFlowCapacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAirFlowCapacity(BigInteger value) {
        this.airFlowCapacity = value;
    }

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
     * Gets the value of the bulbDiameter property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDimension }
     *     
     */
    public LengthDimension getBulbDiameter() {
        return bulbDiameter;
    }

    /**
     * Sets the value of the bulbDiameter property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDimension }
     *     
     */
    public void setBulbDiameter(LengthDimension value) {
        this.bulbDiameter = value;
    }

    /**
     * Gets the value of the bulbLength property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDimension }
     *     
     */
    public LengthDimension getBulbLength() {
        return bulbLength;
    }

    /**
     * Sets the value of the bulbLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDimension }
     *     
     */
    public void setBulbLength(LengthDimension value) {
        this.bulbLength = value;
    }

    /**
     * Gets the value of the bulbLifeSpan property.
     * 
     * @return
     *     possible object is
     *     {@link TimeDimension }
     *     
     */
    public TimeDimension getBulbLifeSpan() {
        return bulbLifeSpan;
    }

    /**
     * Sets the value of the bulbLifeSpan property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeDimension }
     *     
     */
    public void setBulbLifeSpan(TimeDimension value) {
        this.bulbLifeSpan = value;
    }

    /**
     * Gets the value of the bulbPowerFactor property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getBulbPowerFactor() {
        return bulbPowerFactor;
    }

    /**
     * Sets the value of the bulbPowerFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setBulbPowerFactor(BigDecimal value) {
        this.bulbPowerFactor = value;
    }

    /**
     * Gets the value of the bulbSpecialFeatures property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bulbSpecialFeatures property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBulbSpecialFeatures().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getBulbSpecialFeatures() {
        if (bulbSpecialFeatures == null) {
            bulbSpecialFeatures = new ArrayList<String>();
        }
        return this.bulbSpecialFeatures;
    }

    /**
     * Gets the value of the bulbSwitchingCycles property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getBulbSwitchingCycles() {
        return bulbSwitchingCycles;
    }

    /**
     * Sets the value of the bulbSwitchingCycles property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setBulbSwitchingCycles(BigInteger value) {
        this.bulbSwitchingCycles = value;
    }

    /**
     * Gets the value of the bulbType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBulbType() {
        return bulbType;
    }

    /**
     * Sets the value of the bulbType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBulbType(String value) {
        this.bulbType = value;
    }

    /**
     * Gets the value of the bulbWattage property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getBulbWattage() {
        return bulbWattage;
    }

    /**
     * Sets the value of the bulbWattage property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setBulbWattage(BigDecimal value) {
        this.bulbWattage = value;
    }

    /**
     * Gets the value of the capType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCapType() {
        return capType;
    }

    /**
     * Sets the value of the capType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCapType(String value) {
        this.capType = value;
    }

    /**
     * Gets the value of the certification property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the certification property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCertification().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getCertification() {
        if (certification == null) {
            certification = new ArrayList<String>();
        }
        return this.certification;
    }

    /**
     * Gets the value of the collection property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollection() {
        return collection;
    }

    /**
     * Sets the value of the collection property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollection(String value) {
        this.collection = value;
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
     * Gets the value of the colorRenderingIndex property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getColorRenderingIndex() {
        return colorRenderingIndex;
    }

    /**
     * Sets the value of the colorRenderingIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setColorRenderingIndex(BigInteger value) {
        this.colorRenderingIndex = value;
    }

    /**
     * Gets the value of the colorTemperature property.
     * 
     * @return
     *     possible object is
     *     {@link TemperatureRatingDimension }
     *     
     */
    public TemperatureRatingDimension getColorTemperature() {
        return colorTemperature;
    }

    /**
     * Sets the value of the colorTemperature property.
     * 
     * @param value
     *     allowed object is
     *     {@link TemperatureRatingDimension }
     *     
     */
    public void setColorTemperature(TemperatureRatingDimension value) {
        this.colorTemperature = value;
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
     * Gets the value of the fanBladeColor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFanBladeColor() {
        return fanBladeColor;
    }

    /**
     * Sets the value of the fanBladeColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFanBladeColor(String value) {
        this.fanBladeColor = value;
    }

    /**
     * Gets the value of the finishType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinishType() {
        return finishType;
    }

    /**
     * Sets the value of the finishType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinishType(String value) {
        this.finishType = value;
    }

    /**
     * Gets the value of the incandescentEquivalentWattage property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getIncandescentEquivalentWattage() {
        return incandescentEquivalentWattage;
    }

    /**
     * Sets the value of the incandescentEquivalentWattage property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setIncandescentEquivalentWattage(BigInteger value) {
        this.incandescentEquivalentWattage = value;
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
     * Gets the value of the internationalProtectionRating property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInternationalProtectionRating() {
        return internationalProtectionRating;
    }

    /**
     * Sets the value of the internationalProtectionRating property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInternationalProtectionRating(String value) {
        this.internationalProtectionRating = value;
    }

    /**
     * Gets the value of the itemDiameter property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDimension }
     *     
     */
    public LengthDimension getItemDiameter() {
        return itemDiameter;
    }

    /**
     * Sets the value of the itemDiameter property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDimension }
     *     
     */
    public void setItemDiameter(LengthDimension value) {
        this.itemDiameter = value;
    }

    /**
     * Gets the value of the lampStartupTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLampStartupTime() {
        return lampStartupTime;
    }

    /**
     * Sets the value of the lampStartupTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLampStartupTime(String value) {
        this.lampStartupTime = value;
    }

    /**
     * Gets the value of the lampWarmupTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLampWarmupTime() {
        return lampWarmupTime;
    }

    /**
     * Sets the value of the lampWarmupTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLampWarmupTime(String value) {
        this.lampWarmupTime = value;
    }

    /**
     * Gets the value of the lightingMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLightingMethod() {
        return lightingMethod;
    }

    /**
     * Sets the value of the lightingMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLightingMethod(String value) {
        this.lightingMethod = value;
    }

    /**
     * Gets the value of the lightOutputLuminance property.
     * 
     * @return
     *     possible object is
     *     {@link LuminanceDimension }
     *     
     */
    public LuminanceDimension getLightOutputLuminance() {
        return lightOutputLuminance;
    }

    /**
     * Sets the value of the lightOutputLuminance property.
     * 
     * @param value
     *     allowed object is
     *     {@link LuminanceDimension }
     *     
     */
    public void setLightOutputLuminance(LuminanceDimension value) {
        this.lightOutputLuminance = value;
    }

    /**
     * Gets the value of the lithiumBatteryEnergyContent property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLithiumBatteryEnergyContent() {
        return lithiumBatteryEnergyContent;
    }

    /**
     * Sets the value of the lithiumBatteryEnergyContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLithiumBatteryEnergyContent(BigInteger value) {
        this.lithiumBatteryEnergyContent = value;
    }

    /**
     * Gets the value of the lithiumBatteryPackaging property.
     * 
     * @return
     *     possible object is
     *     {@link LithiumBatteryPackagingType }
     *     
     */
    public LithiumBatteryPackagingType getLithiumBatteryPackaging() {
        return lithiumBatteryPackaging;
    }

    /**
     * Sets the value of the lithiumBatteryPackaging property.
     * 
     * @param value
     *     allowed object is
     *     {@link LithiumBatteryPackagingType }
     *     
     */
    public void setLithiumBatteryPackaging(LithiumBatteryPackagingType value) {
        this.lithiumBatteryPackaging = value;
    }

    /**
     * Gets the value of the lithiumBatteryVoltage property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLithiumBatteryVoltage() {
        return lithiumBatteryVoltage;
    }

    /**
     * Sets the value of the lithiumBatteryVoltage property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLithiumBatteryVoltage(BigInteger value) {
        this.lithiumBatteryVoltage = value;
    }

    /**
     * Gets the value of the lithiumBatteryWeight property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLithiumBatteryWeight() {
        return lithiumBatteryWeight;
    }

    /**
     * Sets the value of the lithiumBatteryWeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLithiumBatteryWeight(BigInteger value) {
        this.lithiumBatteryWeight = value;
    }

    /**
     * Gets the value of the lumenMaintenanceFactor property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLumenMaintenanceFactor() {
        return lumenMaintenanceFactor;
    }

    /**
     * Sets the value of the lumenMaintenanceFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLumenMaintenanceFactor(BigDecimal value) {
        this.lumenMaintenanceFactor = value;
    }

    /**
     * Gets the value of the luminousIntensity property.
     * 
     * @return
     *     possible object is
     *     {@link LuminiousIntensityDimension }
     *     
     */
    public LuminiousIntensityDimension getLuminousIntensity() {
        return luminousIntensity;
    }

    /**
     * Sets the value of the luminousIntensity property.
     * 
     * @param value
     *     allowed object is
     *     {@link LuminiousIntensityDimension }
     *     
     */
    public void setLuminousIntensity(LuminiousIntensityDimension value) {
        this.luminousIntensity = value;
    }

    /**
     * Gets the value of the material property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaterial() {
        return material;
    }

    /**
     * Sets the value of the material property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaterial(String value) {
        this.material = value;
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
     * Gets the value of the maximumSupportedWattage property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaximumSupportedWattage() {
        return maximumSupportedWattage;
    }

    /**
     * Sets the value of the maximumSupportedWattage property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaximumSupportedWattage(BigInteger value) {
        this.maximumSupportedWattage = value;
    }

    /**
     * Gets the value of the mercuryContent property.
     * 
     * @return
     *     possible object is
     *     {@link WeightDimension }
     *     
     */
    public WeightDimension getMercuryContent() {
        return mercuryContent;
    }

    /**
     * Sets the value of the mercuryContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link WeightDimension }
     *     
     */
    public void setMercuryContent(WeightDimension value) {
        this.mercuryContent = value;
    }

    /**
     * Gets the value of the numberOfBlades property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumberOfBlades() {
        return numberOfBlades;
    }

    /**
     * Sets the value of the numberOfBlades property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumberOfBlades(BigInteger value) {
        this.numberOfBlades = value;
    }

    /**
     * Gets the value of the numberOfBulbSockets property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumberOfBulbSockets() {
        return numberOfBulbSockets;
    }

    /**
     * Sets the value of the numberOfBulbSockets property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumberOfBulbSockets(BigInteger value) {
        this.numberOfBulbSockets = value;
    }

    /**
     * Gets the value of the numberOfLights property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumberOfLights() {
        return numberOfLights;
    }

    /**
     * Sets the value of the numberOfLights property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumberOfLights(BigInteger value) {
        this.numberOfLights = value;
    }

    /**
     * Gets the value of the numberOfLithiumIonCells property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumberOfLithiumIonCells() {
        return numberOfLithiumIonCells;
    }

    /**
     * Sets the value of the numberOfLithiumIonCells property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumberOfLithiumIonCells(BigInteger value) {
        this.numberOfLithiumIonCells = value;
    }

    /**
     * Gets the value of the numberOfLithiumMetalCells property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumberOfLithiumMetalCells() {
        return numberOfLithiumMetalCells;
    }

    /**
     * Sets the value of the numberOfLithiumMetalCells property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumberOfLithiumMetalCells(BigInteger value) {
        this.numberOfLithiumMetalCells = value;
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
     * Gets the value of the ppuCount property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getPPUCount() {
        return ppuCount;
    }

    /**
     * Sets the value of the ppuCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setPPUCount(BigDecimal value) {
        this.ppuCount = value;
    }

    /**
     * Gets the value of the ppuCountType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPPUCountType() {
        return ppuCountType;
    }

    /**
     * Sets the value of the ppuCountType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPPUCountType(String value) {
        this.ppuCountType = value;
    }

    /**
     * Gets the value of the shadeColor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShadeColor() {
        return shadeColor;
    }

    /**
     * Sets the value of the shadeColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShadeColor(String value) {
        this.shadeColor = value;
    }

    /**
     * Gets the value of the shadeDiameter property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDimension }
     *     
     */
    public LengthDimension getShadeDiameter() {
        return shadeDiameter;
    }

    /**
     * Sets the value of the shadeDiameter property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDimension }
     *     
     */
    public void setShadeDiameter(LengthDimension value) {
        this.shadeDiameter = value;
    }

    /**
     * Gets the value of the shadeMaterial property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShadeMaterial() {
        return shadeMaterial;
    }

    /**
     * Sets the value of the shadeMaterial property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShadeMaterial(String value) {
        this.shadeMaterial = value;
    }

    /**
     * Gets the value of the specialFeatures property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the specialFeatures property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpecialFeatures().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSpecialFeatures() {
        if (specialFeatures == null) {
            specialFeatures = new ArrayList<String>();
        }
        return this.specialFeatures;
    }

    /**
     * Gets the value of the specificUses property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the specificUses property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpecificUses().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSpecificUses() {
        if (specificUses == null) {
            specificUses = new ArrayList<String>();
        }
        return this.specificUses;
    }

    /**
     * Gets the value of the styleName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyleName() {
        return styleName;
    }

    /**
     * Sets the value of the styleName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyleName(String value) {
        this.styleName = value;
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
     *         &lt;element name="Parentage">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="parent"/>
     *               &lt;enumeration value="child"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="VariationTheme" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="Color"/>
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
        "parentage",
        "variationTheme"
    })
    public static class VariationData {

        @XmlElement(name = "Parentage", required = true)
        protected String parentage;
        @XmlElement(name = "VariationTheme")
        protected String variationTheme;

        /**
         * Gets the value of the parentage property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getParentage() {
            return parentage;
        }

        /**
         * Sets the value of the parentage property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setParentage(String value) {
            this.parentage = value;
        }

        /**
         * Gets the value of the variationTheme property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getVariationTheme() {
            return variationTheme;
        }

        /**
         * Sets the value of the variationTheme property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setVariationTheme(String value) {
            this.variationTheme = value;
        }

    }

}
