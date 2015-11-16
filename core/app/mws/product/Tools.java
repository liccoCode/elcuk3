
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
 *         &lt;element name="GritRating" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="Horsepower" type="{}ToolsHorsepower" minOccurs="0"/>
 *         &lt;element name="Diameter" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="Length" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="Width" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="Height" type="{}LengthDimension" minOccurs="0"/>
 *         &lt;element name="Weight" type="{}WeightDimension" minOccurs="0"/>
 *         &lt;element name="PowerSource" maxOccurs="2" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="battery-powered"/>
 *               &lt;enumeration value="gas-powered"/>
 *               &lt;enumeration value="hydraulic-powered"/>
 *               &lt;enumeration value="air-powered"/>
 *               &lt;enumeration value="corded-electric"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Wattage" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="Voltage" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="NumberOfItemsInPackage" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
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
    "gritRating",
    "horsepower",
    "diameter",
    "length",
    "width",
    "height",
    "weight",
    "powerSource",
    "wattage",
    "voltage",
    "numberOfItemsInPackage"
})
@XmlRootElement(name = "Tools")
public class Tools {

    @XmlElement(name = "GritRating")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger gritRating;
    @XmlElement(name = "Horsepower")
    protected BigDecimal horsepower;
    @XmlElement(name = "Diameter")
    protected LengthDimension diameter;
    @XmlElement(name = "Length")
    protected LengthDimension length;
    @XmlElement(name = "Width")
    protected LengthDimension width;
    @XmlElement(name = "Height")
    protected LengthDimension height;
    @XmlElement(name = "Weight")
    protected WeightDimension weight;
    @XmlElement(name = "PowerSource")
    protected List<String> powerSource;
    @XmlElement(name = "Wattage")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger wattage;
    @XmlElement(name = "Voltage")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger voltage;
    @XmlElement(name = "NumberOfItemsInPackage")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger numberOfItemsInPackage;

    /**
     * Gets the value of the gritRating property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getGritRating() {
        return gritRating;
    }

    /**
     * Sets the value of the gritRating property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setGritRating(BigInteger value) {
        this.gritRating = value;
    }

    /**
     * Gets the value of the horsepower property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getHorsepower() {
        return horsepower;
    }

    /**
     * Sets the value of the horsepower property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setHorsepower(BigDecimal value) {
        this.horsepower = value;
    }

    /**
     * Gets the value of the diameter property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDimension }
     *     
     */
    public LengthDimension getDiameter() {
        return diameter;
    }

    /**
     * Sets the value of the diameter property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDimension }
     *     
     */
    public void setDiameter(LengthDimension value) {
        this.diameter = value;
    }

    /**
     * Gets the value of the length property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDimension }
     *     
     */
    public LengthDimension getLength() {
        return length;
    }

    /**
     * Sets the value of the length property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDimension }
     *     
     */
    public void setLength(LengthDimension value) {
        this.length = value;
    }

    /**
     * Gets the value of the width property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDimension }
     *     
     */
    public LengthDimension getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDimension }
     *     
     */
    public void setWidth(LengthDimension value) {
        this.width = value;
    }

    /**
     * Gets the value of the height property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDimension }
     *     
     */
    public LengthDimension getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDimension }
     *     
     */
    public void setHeight(LengthDimension value) {
        this.height = value;
    }

    /**
     * Gets the value of the weight property.
     * 
     * @return
     *     possible object is
     *     {@link WeightDimension }
     *     
     */
    public WeightDimension getWeight() {
        return weight;
    }

    /**
     * Sets the value of the weight property.
     * 
     * @param value
     *     allowed object is
     *     {@link WeightDimension }
     *     
     */
    public void setWeight(WeightDimension value) {
        this.weight = value;
    }

    /**
     * Gets the value of the powerSource property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the powerSource property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPowerSource().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getPowerSource() {
        if (powerSource == null) {
            powerSource = new ArrayList<String>();
        }
        return this.powerSource;
    }

    /**
     * Gets the value of the wattage property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getWattage() {
        return wattage;
    }

    /**
     * Sets the value of the wattage property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setWattage(BigInteger value) {
        this.wattage = value;
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
     * Gets the value of the numberOfItemsInPackage property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumberOfItemsInPackage() {
        return numberOfItemsInPackage;
    }

    /**
     * Sets the value of the numberOfItemsInPackage property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumberOfItemsInPackage(BigInteger value) {
        this.numberOfItemsInPackage = value;
    }

}
