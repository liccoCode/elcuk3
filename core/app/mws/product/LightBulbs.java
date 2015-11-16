
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
 *                         &lt;enumeration value="Wattage"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="BeamAngle" type="{}DegreeDimension" minOccurs="0"/>
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
 *         &lt;element name="Color" type="{}String" minOccurs="0"/>
 *         &lt;element name="ColorMap" type="{}String" minOccurs="0"/>
 *         &lt;element name="ColorRenderingIndex" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="ColorTemperature" type="{}TemperatureRatingDimension" minOccurs="0"/>
 *         &lt;element name="CountryOfOrigin" type="{}CountryOfOriginType" minOccurs="0"/>
 *         &lt;element name="DisplayDepth" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="EnergyEfficiencyRating" type="{}String" minOccurs="0"/>
 *         &lt;element name="IncandescentEquivalentWattage" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="ItemShape" type="{}String" minOccurs="0"/>
 *         &lt;element name="LampStartupTime" type="{}MediumStringNotNull" minOccurs="0"/>
 *         &lt;element name="LampWarmupTime" type="{}MediumStringNotNull" minOccurs="0"/>
 *         &lt;element name="LightOutputLuminance" type="{}LuminanceDimension" minOccurs="0"/>
 *         &lt;element name="LumenMaintenanceFactor" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="LuminousIntensity" type="{}LuminiousIntensityDimension" minOccurs="0"/>
 *         &lt;element name="MercuryContent" type="{}WeightDimension" minOccurs="0"/>
 *         &lt;element name="PPUCount" type="{}Dimension" minOccurs="0"/>
 *         &lt;element name="PPUCountType" type="{}String" minOccurs="0"/>
 *         &lt;element name="SpecificUses" type="{}String" maxOccurs="2" minOccurs="0"/>
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
    "beamAngle",
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
    "color",
    "colorMap",
    "colorRenderingIndex",
    "colorTemperature",
    "countryOfOrigin",
    "displayDepth",
    "energyEfficiencyRating",
    "incandescentEquivalentWattage",
    "itemShape",
    "lampStartupTime",
    "lampWarmupTime",
    "lightOutputLuminance",
    "lumenMaintenanceFactor",
    "luminousIntensity",
    "mercuryContent",
    "ppuCount",
    "ppuCountType",
    "specificUses",
    "voltage",
    "wattage"
})
@XmlRootElement(name = "LightBulbs")
public class LightBulbs {

    @XmlElement(name = "VariationData")
    protected LightBulbs.VariationData variationData;
    @XmlElement(name = "BeamAngle")
    protected DegreeDimension beamAngle;
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
    @XmlElement(name = "EnergyEfficiencyRating")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String energyEfficiencyRating;
    @XmlElement(name = "IncandescentEquivalentWattage")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger incandescentEquivalentWattage;
    @XmlElement(name = "ItemShape")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String itemShape;
    @XmlElement(name = "LampStartupTime")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String lampStartupTime;
    @XmlElement(name = "LampWarmupTime")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String lampWarmupTime;
    @XmlElement(name = "LightOutputLuminance")
    protected LuminanceDimension lightOutputLuminance;
    @XmlElement(name = "LumenMaintenanceFactor")
    protected BigDecimal lumenMaintenanceFactor;
    @XmlElement(name = "LuminousIntensity")
    protected LuminiousIntensityDimension luminousIntensity;
    @XmlElement(name = "MercuryContent")
    protected WeightDimension mercuryContent;
    @XmlElement(name = "PPUCount")
    protected BigDecimal ppuCount;
    @XmlElement(name = "PPUCountType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String ppuCountType;
    @XmlElement(name = "SpecificUses")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected List<String> specificUses;
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
     *     {@link LightBulbs.VariationData }
     *     
     */
    public LightBulbs.VariationData getVariationData() {
        return variationData;
    }

    /**
     * Sets the value of the variationData property.
     * 
     * @param value
     *     allowed object is
     *     {@link LightBulbs.VariationData }
     *     
     */
    public void setVariationData(LightBulbs.VariationData value) {
        this.variationData = value;
    }

    /**
     * Gets the value of the beamAngle property.
     * 
     * @return
     *     possible object is
     *     {@link DegreeDimension }
     *     
     */
    public DegreeDimension getBeamAngle() {
        return beamAngle;
    }

    /**
     * Sets the value of the beamAngle property.
     * 
     * @param value
     *     allowed object is
     *     {@link DegreeDimension }
     *     
     */
    public void setBeamAngle(DegreeDimension value) {
        this.beamAngle = value;
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
     * Gets the value of the energyEfficiencyRating property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnergyEfficiencyRating() {
        return energyEfficiencyRating;
    }

    /**
     * Sets the value of the energyEfficiencyRating property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnergyEfficiencyRating(String value) {
        this.energyEfficiencyRating = value;
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
     * Gets the value of the itemShape property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getItemShape() {
        return itemShape;
    }

    /**
     * Sets the value of the itemShape property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setItemShape(String value) {
        this.itemShape = value;
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
     *               &lt;enumeration value="Wattage"/>
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
