
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
import javax.xml.datatype.XMLGregorianCalendar;


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
 *         &lt;element ref="{}SKU"/>
 *         &lt;element ref="{}StandardProductID" minOccurs="0"/>
 *         &lt;element ref="{}ProductTaxCode" minOccurs="0"/>
 *         &lt;element name="LaunchDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="DiscontinueDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="ReleaseDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="ExternalProductUrl" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="OffAmazonChannel" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="advertise"/>
 *               &lt;enumeration value="exclude"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="OnAmazonChannel" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="sell"/>
 *               &lt;enumeration value="advertise"/>
 *               &lt;enumeration value="exclude"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Condition" type="{}ConditionInfo" minOccurs="0"/>
 *         &lt;element name="Rebate" type="{}RebateType" maxOccurs="2" minOccurs="0"/>
 *         &lt;element name="ItemPackageQuantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="NumberOfItems" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="DescriptionData" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Title" type="{}LongStringNotNull"/>
 *                   &lt;element name="Brand" type="{}StringNotNull" minOccurs="0"/>
 *                   &lt;element name="Designer" type="{}StringNotNull" minOccurs="0"/>
 *                   &lt;element name="Description" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}normalizedString">
 *                         &lt;maxLength value="2000"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="BulletPoint" type="{}LongStringNotNull" maxOccurs="5" minOccurs="0"/>
 *                   &lt;element name="ItemDimensions" type="{}Dimensions" minOccurs="0"/>
 *                   &lt;element name="PackageDimensions" type="{}SpatialDimensions" minOccurs="0"/>
 *                   &lt;element name="PackageWeight" type="{}PositiveNonZeroWeightDimension" minOccurs="0"/>
 *                   &lt;element name="ShippingWeight" type="{}PositiveNonZeroWeightDimension" minOccurs="0"/>
 *                   &lt;element name="MerchantCatalogNumber" type="{}FortyStringNotNull" minOccurs="0"/>
 *                   &lt;element name="MSRP" type="{}CurrencyAmount" minOccurs="0"/>
 *                   &lt;element name="MaxOrderQuantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *                   &lt;element name="SerialNumberRequired" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *                   &lt;element name="Prop65" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *                   &lt;element name="CPSIAWarning" maxOccurs="4" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="choking_hazard_balloon"/>
 *                         &lt;enumeration value="choking_hazard_contains_a_marble"/>
 *                         &lt;enumeration value="choking_hazard_contains_small_ball"/>
 *                         &lt;enumeration value="choking_hazard_is_a_marble"/>
 *                         &lt;enumeration value="choking_hazard_is_a_small_ball"/>
 *                         &lt;enumeration value="choking_hazard_small_parts"/>
 *                         &lt;enumeration value="no_warning_applicable"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="CPSIAWarningDescription" type="{}TwoFiftyStringNotNull" minOccurs="0"/>
 *                   &lt;element name="LegalDisclaimer" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}normalizedString">
 *                         &lt;maxLength value="1000"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="Manufacturer" type="{}StringNotNull" minOccurs="0"/>
 *                   &lt;element name="MfrPartNumber" type="{}FortyStringNotNull" minOccurs="0"/>
 *                   &lt;element name="SearchTerms" type="{}StringNotNull" maxOccurs="5" minOccurs="0"/>
 *                   &lt;element name="PlatinumKeywords" type="{}StringNotNull" maxOccurs="20" minOccurs="0"/>
 *                   &lt;element name="Memorabilia" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *                   &lt;element name="Autographed" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *                   &lt;element name="UsedFor" type="{}StringNotNull" maxOccurs="5" minOccurs="0"/>
 *                   &lt;element name="ItemType" type="{}LongStringNotNull" minOccurs="0"/>
 *                   &lt;element name="OtherItemAttributes" type="{}LongStringNotNull" maxOccurs="5" minOccurs="0"/>
 *                   &lt;element name="TargetAudience" type="{}StringNotNull" maxOccurs="4" minOccurs="0"/>
 *                   &lt;element name="SubjectContent" type="{}StringNotNull" maxOccurs="5" minOccurs="0"/>
 *                   &lt;element name="IsGiftWrapAvailable" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *                   &lt;element name="IsGiftMessageAvailable" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *                   &lt;element name="PromotionKeywords" type="{}StringNotNull" maxOccurs="10" minOccurs="0"/>
 *                   &lt;element name="IsDiscontinuedByManufacturer" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *                   &lt;element ref="{}DeliveryChannel" maxOccurs="2" minOccurs="0"/>
 *                   &lt;element name="MaxAggregateShipQuantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *                   &lt;element name="RecommendedBrowseNode" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" maxOccurs="2" minOccurs="0"/>
 *                   &lt;element name="FEDAS_ID" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}normalizedString">
 *                         &lt;length value="6"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="TSDAgeWarning" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="not_suitable_under_36_months"/>
 *                         &lt;enumeration value="not_suitable_under_3_years_supervision"/>
 *                         &lt;enumeration value="not_suitable_under_4_years_supervision"/>
 *                         &lt;enumeration value="not_suitable_under_5_years_supervision"/>
 *                         &lt;enumeration value="not_suitable_under_6_years_supervision"/>
 *                         &lt;enumeration value="not_suitable_under_7_years_supervision"/>
 *                         &lt;enumeration value="not_suitable_under_8_years_supervision"/>
 *                         &lt;enumeration value="not_suitable_under_9_years_supervision"/>
 *                         &lt;enumeration value="not_suitable_under_10_years_supervision"/>
 *                         &lt;enumeration value="not_suitable_under_11_years_supervision"/>
 *                         &lt;enumeration value="not_suitable_under_12_years_supervision"/>
 *                         &lt;enumeration value="not_suitable_under_13_years_supervision"/>
 *                         &lt;enumeration value="not_suitable_under_14_years_supervision"/>
 *                         &lt;enumeration value="no_warning_applicable"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="TSDWarning" maxOccurs="8" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="only_domestic_use"/>
 *                         &lt;enumeration value="adult_supervision_required"/>
 *                         &lt;enumeration value="protective_equipment_required"/>
 *                         &lt;enumeration value="water_adult_supervision_required"/>
 *                         &lt;enumeration value="toy_inside"/>
 *                         &lt;enumeration value="no_protective_equipment"/>
 *                         &lt;enumeration value="risk_of_entanglement"/>
 *                         &lt;enumeration value="fragrances_allergy_risk"/>
 *                         &lt;enumeration value="no_warning_applicable"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="TSDLanguage" maxOccurs="21" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="English"/>
 *                         &lt;enumeration value="French"/>
 *                         &lt;enumeration value="German"/>
 *                         &lt;enumeration value="Italian"/>
 *                         &lt;enumeration value="Spanish"/>
 *                         &lt;enumeration value="Dutch"/>
 *                         &lt;enumeration value="Polish"/>
 *                         &lt;enumeration value="Bulgarian"/>
 *                         &lt;enumeration value="Czech"/>
 *                         &lt;enumeration value="Danish"/>
 *                         &lt;enumeration value="Estonian"/>
 *                         &lt;enumeration value="Finnish"/>
 *                         &lt;enumeration value="Greek"/>
 *                         &lt;enumeration value="Hungarian"/>
 *                         &lt;enumeration value="Latvian"/>
 *                         &lt;enumeration value="Lithuanian"/>
 *                         &lt;enumeration value="Portuguese"/>
 *                         &lt;enumeration value="Romanian"/>
 *                         &lt;enumeration value="Slovak"/>
 *                         &lt;enumeration value="Slovene"/>
 *                         &lt;enumeration value="Swedish"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="OptionalPaymentTypeExclusion" maxOccurs="2" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="cash_on_delivery"/>
 *                         &lt;enumeration value="cvs"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="DiscoveryData" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Priority" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}positiveInteger">
 *                         &lt;minInclusive value="1"/>
 *                         &lt;maxInclusive value="10"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="BrowseExclusion" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *                   &lt;element name="RecommendationExclusion" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ProductData" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element ref="{}Tools"/>
 *                   &lt;element ref="{}SoftwareVideoGames"/>
 *                   &lt;element ref="{}Wireless"/>
 *                   &lt;element ref="{}Lighting"/>
 *                 &lt;/choice>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="RegisteredParameter" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="PrivateLabel"/>
 *               &lt;enumeration value="Specialized"/>
 *               &lt;enumeration value="NonConsumer"/>
 *               &lt;enumeration value="PreConfigured"/>
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
    "sku",
    "standardProductID",
    "productTaxCode",
    "launchDate",
    "discontinueDate",
    "releaseDate",
    "externalProductUrl",
    "offAmazonChannel",
    "onAmazonChannel",
    "condition",
    "rebate",
    "itemPackageQuantity",
    "numberOfItems",
    "descriptionData",
    "discoveryData",
    "productData",
    "registeredParameter"
})
@XmlRootElement(name = "Product")
public class Product {

    @XmlElement(name = "SKU", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String sku;
    @XmlElement(name = "StandardProductID")
    protected StandardProductID standardProductID;
    @XmlElement(name = "ProductTaxCode")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String productTaxCode;
    @XmlElement(name = "LaunchDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar launchDate;
    @XmlElement(name = "DiscontinueDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar discontinueDate;
    @XmlElement(name = "ReleaseDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDate;
    @XmlElement(name = "ExternalProductUrl")
    @XmlSchemaType(name = "anyURI")
    protected String externalProductUrl;
    @XmlElement(name = "OffAmazonChannel")
    protected String offAmazonChannel;
    @XmlElement(name = "OnAmazonChannel")
    protected String onAmazonChannel;
    @XmlElement(name = "Condition")
    protected ConditionInfo condition;
    @XmlElement(name = "Rebate")
    protected List<RebateType> rebate;
    @XmlElement(name = "ItemPackageQuantity")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger itemPackageQuantity;
    @XmlElement(name = "NumberOfItems")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger numberOfItems;
    @XmlElement(name = "DescriptionData")
    protected Product.DescriptionData descriptionData;
    @XmlElement(name = "DiscoveryData")
    protected Product.DiscoveryData discoveryData;
    @XmlElement(name = "ProductData")
    protected Product.ProductData productData;
    @XmlElement(name = "RegisteredParameter")
    protected String registeredParameter;

    /**
     * Gets the value of the sku property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSKU() {
        return sku;
    }

    /**
     * Sets the value of the sku property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSKU(String value) {
        this.sku = value;
    }

    /**
     * Gets the value of the standardProductID property.
     * 
     * @return
     *     possible object is
     *     {@link StandardProductID }
     *     
     */
    public StandardProductID getStandardProductID() {
        return standardProductID;
    }

    /**
     * Sets the value of the standardProductID property.
     * 
     * @param value
     *     allowed object is
     *     {@link StandardProductID }
     *     
     */
    public void setStandardProductID(StandardProductID value) {
        this.standardProductID = value;
    }

    /**
     * Gets the value of the productTaxCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductTaxCode() {
        return productTaxCode;
    }

    /**
     * Sets the value of the productTaxCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductTaxCode(String value) {
        this.productTaxCode = value;
    }

    /**
     * Gets the value of the launchDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLaunchDate() {
        return launchDate;
    }

    /**
     * Sets the value of the launchDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLaunchDate(XMLGregorianCalendar value) {
        this.launchDate = value;
    }

    /**
     * Gets the value of the discontinueDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDiscontinueDate() {
        return discontinueDate;
    }

    /**
     * Sets the value of the discontinueDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDiscontinueDate(XMLGregorianCalendar value) {
        this.discontinueDate = value;
    }

    /**
     * Gets the value of the releaseDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getReleaseDate() {
        return releaseDate;
    }

    /**
     * Sets the value of the releaseDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setReleaseDate(XMLGregorianCalendar value) {
        this.releaseDate = value;
    }

    /**
     * Gets the value of the externalProductUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalProductUrl() {
        return externalProductUrl;
    }

    /**
     * Sets the value of the externalProductUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalProductUrl(String value) {
        this.externalProductUrl = value;
    }

    /**
     * Gets the value of the offAmazonChannel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOffAmazonChannel() {
        return offAmazonChannel;
    }

    /**
     * Sets the value of the offAmazonChannel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOffAmazonChannel(String value) {
        this.offAmazonChannel = value;
    }

    /**
     * Gets the value of the onAmazonChannel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnAmazonChannel() {
        return onAmazonChannel;
    }

    /**
     * Sets the value of the onAmazonChannel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnAmazonChannel(String value) {
        this.onAmazonChannel = value;
    }

    /**
     * Gets the value of the condition property.
     * 
     * @return
     *     possible object is
     *     {@link ConditionInfo }
     *     
     */
    public ConditionInfo getCondition() {
        return condition;
    }

    /**
     * Sets the value of the condition property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConditionInfo }
     *     
     */
    public void setCondition(ConditionInfo value) {
        this.condition = value;
    }

    /**
     * Gets the value of the rebate property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rebate property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRebate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RebateType }
     * 
     * 
     */
    public List<RebateType> getRebate() {
        if (rebate == null) {
            rebate = new ArrayList<RebateType>();
        }
        return this.rebate;
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

    /**
     * Gets the value of the numberOfItems property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumberOfItems() {
        return numberOfItems;
    }

    /**
     * Sets the value of the numberOfItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumberOfItems(BigInteger value) {
        this.numberOfItems = value;
    }

    /**
     * Gets the value of the descriptionData property.
     * 
     * @return
     *     possible object is
     *     {@link Product.DescriptionData }
     *     
     */
    public Product.DescriptionData getDescriptionData() {
        return descriptionData;
    }

    /**
     * Sets the value of the descriptionData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Product.DescriptionData }
     *     
     */
    public void setDescriptionData(Product.DescriptionData value) {
        this.descriptionData = value;
    }

    /**
     * Gets the value of the discoveryData property.
     * 
     * @return
     *     possible object is
     *     {@link Product.DiscoveryData }
     *     
     */
    public Product.DiscoveryData getDiscoveryData() {
        return discoveryData;
    }

    /**
     * Sets the value of the discoveryData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Product.DiscoveryData }
     *     
     */
    public void setDiscoveryData(Product.DiscoveryData value) {
        this.discoveryData = value;
    }

    /**
     * Gets the value of the productData property.
     * 
     * @return
     *     possible object is
     *     {@link Product.ProductData }
     *     
     */
    public Product.ProductData getProductData() {
        return productData;
    }

    /**
     * Sets the value of the productData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Product.ProductData }
     *     
     */
    public void setProductData(Product.ProductData value) {
        this.productData = value;
    }

    /**
     * Gets the value of the registeredParameter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegisteredParameter() {
        return registeredParameter;
    }

    /**
     * Sets the value of the registeredParameter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegisteredParameter(String value) {
        this.registeredParameter = value;
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
     *         &lt;element name="Title" type="{}LongStringNotNull"/>
     *         &lt;element name="Brand" type="{}StringNotNull" minOccurs="0"/>
     *         &lt;element name="Designer" type="{}StringNotNull" minOccurs="0"/>
     *         &lt;element name="Description" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}normalizedString">
     *               &lt;maxLength value="2000"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="BulletPoint" type="{}LongStringNotNull" maxOccurs="5" minOccurs="0"/>
     *         &lt;element name="ItemDimensions" type="{}Dimensions" minOccurs="0"/>
     *         &lt;element name="PackageDimensions" type="{}SpatialDimensions" minOccurs="0"/>
     *         &lt;element name="PackageWeight" type="{}PositiveNonZeroWeightDimension" minOccurs="0"/>
     *         &lt;element name="ShippingWeight" type="{}PositiveNonZeroWeightDimension" minOccurs="0"/>
     *         &lt;element name="MerchantCatalogNumber" type="{}FortyStringNotNull" minOccurs="0"/>
     *         &lt;element name="MSRP" type="{}CurrencyAmount" minOccurs="0"/>
     *         &lt;element name="MaxOrderQuantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
     *         &lt;element name="SerialNumberRequired" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
     *         &lt;element name="Prop65" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
     *         &lt;element name="CPSIAWarning" maxOccurs="4" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="choking_hazard_balloon"/>
     *               &lt;enumeration value="choking_hazard_contains_a_marble"/>
     *               &lt;enumeration value="choking_hazard_contains_small_ball"/>
     *               &lt;enumeration value="choking_hazard_is_a_marble"/>
     *               &lt;enumeration value="choking_hazard_is_a_small_ball"/>
     *               &lt;enumeration value="choking_hazard_small_parts"/>
     *               &lt;enumeration value="no_warning_applicable"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="CPSIAWarningDescription" type="{}TwoFiftyStringNotNull" minOccurs="0"/>
     *         &lt;element name="LegalDisclaimer" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}normalizedString">
     *               &lt;maxLength value="1000"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="Manufacturer" type="{}StringNotNull" minOccurs="0"/>
     *         &lt;element name="MfrPartNumber" type="{}FortyStringNotNull" minOccurs="0"/>
     *         &lt;element name="SearchTerms" type="{}StringNotNull" maxOccurs="5" minOccurs="0"/>
     *         &lt;element name="PlatinumKeywords" type="{}StringNotNull" maxOccurs="20" minOccurs="0"/>
     *         &lt;element name="Memorabilia" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
     *         &lt;element name="Autographed" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
     *         &lt;element name="UsedFor" type="{}StringNotNull" maxOccurs="5" minOccurs="0"/>
     *         &lt;element name="ItemType" type="{}LongStringNotNull" minOccurs="0"/>
     *         &lt;element name="OtherItemAttributes" type="{}LongStringNotNull" maxOccurs="5" minOccurs="0"/>
     *         &lt;element name="TargetAudience" type="{}StringNotNull" maxOccurs="4" minOccurs="0"/>
     *         &lt;element name="SubjectContent" type="{}StringNotNull" maxOccurs="5" minOccurs="0"/>
     *         &lt;element name="IsGiftWrapAvailable" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
     *         &lt;element name="IsGiftMessageAvailable" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
     *         &lt;element name="PromotionKeywords" type="{}StringNotNull" maxOccurs="10" minOccurs="0"/>
     *         &lt;element name="IsDiscontinuedByManufacturer" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
     *         &lt;element ref="{}DeliveryChannel" maxOccurs="2" minOccurs="0"/>
     *         &lt;element name="MaxAggregateShipQuantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
     *         &lt;element name="RecommendedBrowseNode" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" maxOccurs="2" minOccurs="0"/>
     *         &lt;element name="FEDAS_ID" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}normalizedString">
     *               &lt;length value="6"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="TSDAgeWarning" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="not_suitable_under_36_months"/>
     *               &lt;enumeration value="not_suitable_under_3_years_supervision"/>
     *               &lt;enumeration value="not_suitable_under_4_years_supervision"/>
     *               &lt;enumeration value="not_suitable_under_5_years_supervision"/>
     *               &lt;enumeration value="not_suitable_under_6_years_supervision"/>
     *               &lt;enumeration value="not_suitable_under_7_years_supervision"/>
     *               &lt;enumeration value="not_suitable_under_8_years_supervision"/>
     *               &lt;enumeration value="not_suitable_under_9_years_supervision"/>
     *               &lt;enumeration value="not_suitable_under_10_years_supervision"/>
     *               &lt;enumeration value="not_suitable_under_11_years_supervision"/>
     *               &lt;enumeration value="not_suitable_under_12_years_supervision"/>
     *               &lt;enumeration value="not_suitable_under_13_years_supervision"/>
     *               &lt;enumeration value="not_suitable_under_14_years_supervision"/>
     *               &lt;enumeration value="no_warning_applicable"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="TSDWarning" maxOccurs="8" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="only_domestic_use"/>
     *               &lt;enumeration value="adult_supervision_required"/>
     *               &lt;enumeration value="protective_equipment_required"/>
     *               &lt;enumeration value="water_adult_supervision_required"/>
     *               &lt;enumeration value="toy_inside"/>
     *               &lt;enumeration value="no_protective_equipment"/>
     *               &lt;enumeration value="risk_of_entanglement"/>
     *               &lt;enumeration value="fragrances_allergy_risk"/>
     *               &lt;enumeration value="no_warning_applicable"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="TSDLanguage" maxOccurs="21" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="English"/>
     *               &lt;enumeration value="French"/>
     *               &lt;enumeration value="German"/>
     *               &lt;enumeration value="Italian"/>
     *               &lt;enumeration value="Spanish"/>
     *               &lt;enumeration value="Dutch"/>
     *               &lt;enumeration value="Polish"/>
     *               &lt;enumeration value="Bulgarian"/>
     *               &lt;enumeration value="Czech"/>
     *               &lt;enumeration value="Danish"/>
     *               &lt;enumeration value="Estonian"/>
     *               &lt;enumeration value="Finnish"/>
     *               &lt;enumeration value="Greek"/>
     *               &lt;enumeration value="Hungarian"/>
     *               &lt;enumeration value="Latvian"/>
     *               &lt;enumeration value="Lithuanian"/>
     *               &lt;enumeration value="Portuguese"/>
     *               &lt;enumeration value="Romanian"/>
     *               &lt;enumeration value="Slovak"/>
     *               &lt;enumeration value="Slovene"/>
     *               &lt;enumeration value="Swedish"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="OptionalPaymentTypeExclusion" maxOccurs="2" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="cash_on_delivery"/>
     *               &lt;enumeration value="cvs"/>
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
        "title",
        "brand",
        "designer",
        "description",
        "bulletPoint",
        "itemDimensions",
        "packageDimensions",
        "packageWeight",
        "shippingWeight",
        "merchantCatalogNumber",
        "msrp",
        "maxOrderQuantity",
        "serialNumberRequired",
        "prop65",
        "cpsiaWarning",
        "cpsiaWarningDescription",
        "legalDisclaimer",
        "manufacturer",
        "mfrPartNumber",
        "searchTerms",
        "platinumKeywords",
        "memorabilia",
        "autographed",
        "usedFor",
        "itemType",
        "otherItemAttributes",
        "targetAudience",
        "subjectContent",
        "isGiftWrapAvailable",
        "isGiftMessageAvailable",
        "promotionKeywords",
        "isDiscontinuedByManufacturer",
        "deliveryChannel",
        "maxAggregateShipQuantity",
        "recommendedBrowseNode",
        "fedasid",
        "tsdAgeWarning",
        "tsdWarning",
        "tsdLanguage",
        "optionalPaymentTypeExclusion"
    })
    public static class DescriptionData {

        @XmlElement(name = "Title", required = true)
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String title;
        @XmlElement(name = "Brand")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String brand;
        @XmlElement(name = "Designer")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String designer;
        @XmlElement(name = "Description")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String description;
        @XmlElement(name = "BulletPoint")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected List<String> bulletPoint;
        @XmlElement(name = "ItemDimensions")
        protected Dimensions itemDimensions;
        @XmlElement(name = "PackageDimensions")
        protected SpatialDimensions packageDimensions;
        @XmlElement(name = "PackageWeight")
        protected PositiveNonZeroWeightDimension packageWeight;
        @XmlElement(name = "ShippingWeight")
        protected PositiveNonZeroWeightDimension shippingWeight;
        @XmlElement(name = "MerchantCatalogNumber")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String merchantCatalogNumber;
        @XmlElement(name = "MSRP")
        protected CurrencyAmount msrp;
        @XmlElement(name = "MaxOrderQuantity")
        @XmlSchemaType(name = "positiveInteger")
        protected BigInteger maxOrderQuantity;
        @XmlElement(name = "SerialNumberRequired")
        protected Boolean serialNumberRequired;
        @XmlElement(name = "Prop65")
        protected Boolean prop65;
        @XmlElement(name = "CPSIAWarning")
        protected List<String> cpsiaWarning;
        @XmlElement(name = "CPSIAWarningDescription")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String cpsiaWarningDescription;
        @XmlElement(name = "LegalDisclaimer")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String legalDisclaimer;
        @XmlElement(name = "Manufacturer")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String manufacturer;
        @XmlElement(name = "MfrPartNumber")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String mfrPartNumber;
        @XmlElement(name = "SearchTerms")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected List<String> searchTerms;
        @XmlElement(name = "PlatinumKeywords")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected List<String> platinumKeywords;
        @XmlElement(name = "Memorabilia")
        protected Boolean memorabilia;
        @XmlElement(name = "Autographed")
        protected Boolean autographed;
        @XmlElement(name = "UsedFor")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected List<String> usedFor;
        @XmlElement(name = "ItemType")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String itemType;
        @XmlElement(name = "OtherItemAttributes")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected List<String> otherItemAttributes;
        @XmlElement(name = "TargetAudience")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected List<String> targetAudience;
        @XmlElement(name = "SubjectContent")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected List<String> subjectContent;
        @XmlElement(name = "IsGiftWrapAvailable")
        protected Boolean isGiftWrapAvailable;
        @XmlElement(name = "IsGiftMessageAvailable")
        protected Boolean isGiftMessageAvailable;
        @XmlElement(name = "PromotionKeywords")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected List<String> promotionKeywords;
        @XmlElement(name = "IsDiscontinuedByManufacturer")
        protected Boolean isDiscontinuedByManufacturer;
        @XmlElement(name = "DeliveryChannel")
        protected List<String> deliveryChannel;
        @XmlElement(name = "MaxAggregateShipQuantity")
        @XmlSchemaType(name = "positiveInteger")
        protected BigInteger maxAggregateShipQuantity;
        @XmlElement(name = "RecommendedBrowseNode")
        @XmlSchemaType(name = "positiveInteger")
        protected List<BigInteger> recommendedBrowseNode;
        @XmlElement(name = "FEDAS_ID")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String fedasid;
        @XmlElement(name = "TSDAgeWarning")
        protected String tsdAgeWarning;
        @XmlElement(name = "TSDWarning")
        protected List<String> tsdWarning;
        @XmlElement(name = "TSDLanguage")
        protected List<String> tsdLanguage;
        @XmlElement(name = "OptionalPaymentTypeExclusion")
        protected List<String> optionalPaymentTypeExclusion;

        /**
         * Gets the value of the title property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTitle() {
            return title;
        }

        /**
         * Sets the value of the title property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTitle(String value) {
            this.title = value;
        }

        /**
         * Gets the value of the brand property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getBrand() {
            return brand;
        }

        /**
         * Sets the value of the brand property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setBrand(String value) {
            this.brand = value;
        }

        /**
         * Gets the value of the designer property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDesigner() {
            return designer;
        }

        /**
         * Sets the value of the designer property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDesigner(String value) {
            this.designer = value;
        }

        /**
         * Gets the value of the description property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets the value of the description property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDescription(String value) {
            this.description = value;
        }

        /**
         * Gets the value of the bulletPoint property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the bulletPoint property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getBulletPoint().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getBulletPoint() {
            if (bulletPoint == null) {
                bulletPoint = new ArrayList<String>();
            }
            return this.bulletPoint;
        }

        /**
         * Gets the value of the itemDimensions property.
         * 
         * @return
         *     possible object is
         *     {@link Dimensions }
         *     
         */
        public Dimensions getItemDimensions() {
            return itemDimensions;
        }

        /**
         * Sets the value of the itemDimensions property.
         * 
         * @param value
         *     allowed object is
         *     {@link Dimensions }
         *     
         */
        public void setItemDimensions(Dimensions value) {
            this.itemDimensions = value;
        }

        /**
         * Gets the value of the packageDimensions property.
         * 
         * @return
         *     possible object is
         *     {@link SpatialDimensions }
         *     
         */
        public SpatialDimensions getPackageDimensions() {
            return packageDimensions;
        }

        /**
         * Sets the value of the packageDimensions property.
         * 
         * @param value
         *     allowed object is
         *     {@link SpatialDimensions }
         *     
         */
        public void setPackageDimensions(SpatialDimensions value) {
            this.packageDimensions = value;
        }

        /**
         * Gets the value of the packageWeight property.
         * 
         * @return
         *     possible object is
         *     {@link PositiveNonZeroWeightDimension }
         *     
         */
        public PositiveNonZeroWeightDimension getPackageWeight() {
            return packageWeight;
        }

        /**
         * Sets the value of the packageWeight property.
         * 
         * @param value
         *     allowed object is
         *     {@link PositiveNonZeroWeightDimension }
         *     
         */
        public void setPackageWeight(PositiveNonZeroWeightDimension value) {
            this.packageWeight = value;
        }

        /**
         * Gets the value of the shippingWeight property.
         * 
         * @return
         *     possible object is
         *     {@link PositiveNonZeroWeightDimension }
         *     
         */
        public PositiveNonZeroWeightDimension getShippingWeight() {
            return shippingWeight;
        }

        /**
         * Sets the value of the shippingWeight property.
         * 
         * @param value
         *     allowed object is
         *     {@link PositiveNonZeroWeightDimension }
         *     
         */
        public void setShippingWeight(PositiveNonZeroWeightDimension value) {
            this.shippingWeight = value;
        }

        /**
         * Gets the value of the merchantCatalogNumber property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMerchantCatalogNumber() {
            return merchantCatalogNumber;
        }

        /**
         * Sets the value of the merchantCatalogNumber property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMerchantCatalogNumber(String value) {
            this.merchantCatalogNumber = value;
        }

        /**
         * Gets the value of the msrp property.
         * 
         * @return
         *     possible object is
         *     {@link CurrencyAmount }
         *     
         */
        public CurrencyAmount getMSRP() {
            return msrp;
        }

        /**
         * Sets the value of the msrp property.
         * 
         * @param value
         *     allowed object is
         *     {@link CurrencyAmount }
         *     
         */
        public void setMSRP(CurrencyAmount value) {
            this.msrp = value;
        }

        /**
         * Gets the value of the maxOrderQuantity property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getMaxOrderQuantity() {
            return maxOrderQuantity;
        }

        /**
         * Sets the value of the maxOrderQuantity property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setMaxOrderQuantity(BigInteger value) {
            this.maxOrderQuantity = value;
        }

        /**
         * Gets the value of the serialNumberRequired property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isSerialNumberRequired() {
            return serialNumberRequired;
        }

        /**
         * Sets the value of the serialNumberRequired property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setSerialNumberRequired(Boolean value) {
            this.serialNumberRequired = value;
        }

        /**
         * Gets the value of the prop65 property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isProp65() {
            return prop65;
        }

        /**
         * Sets the value of the prop65 property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setProp65(Boolean value) {
            this.prop65 = value;
        }

        /**
         * Gets the value of the cpsiaWarning property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the cpsiaWarning property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCPSIAWarning().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getCPSIAWarning() {
            if (cpsiaWarning == null) {
                cpsiaWarning = new ArrayList<String>();
            }
            return this.cpsiaWarning;
        }

        /**
         * Gets the value of the cpsiaWarningDescription property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCPSIAWarningDescription() {
            return cpsiaWarningDescription;
        }

        /**
         * Sets the value of the cpsiaWarningDescription property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCPSIAWarningDescription(String value) {
            this.cpsiaWarningDescription = value;
        }

        /**
         * Gets the value of the legalDisclaimer property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLegalDisclaimer() {
            return legalDisclaimer;
        }

        /**
         * Sets the value of the legalDisclaimer property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLegalDisclaimer(String value) {
            this.legalDisclaimer = value;
        }

        /**
         * Gets the value of the manufacturer property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getManufacturer() {
            return manufacturer;
        }

        /**
         * Sets the value of the manufacturer property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setManufacturer(String value) {
            this.manufacturer = value;
        }

        /**
         * Gets the value of the mfrPartNumber property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMfrPartNumber() {
            return mfrPartNumber;
        }

        /**
         * Sets the value of the mfrPartNumber property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMfrPartNumber(String value) {
            this.mfrPartNumber = value;
        }

        /**
         * Gets the value of the searchTerms property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the searchTerms property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSearchTerms().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getSearchTerms() {
            if (searchTerms == null) {
                searchTerms = new ArrayList<String>();
            }
            return this.searchTerms;
        }

        /**
         * Gets the value of the platinumKeywords property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the platinumKeywords property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getPlatinumKeywords().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getPlatinumKeywords() {
            if (platinumKeywords == null) {
                platinumKeywords = new ArrayList<String>();
            }
            return this.platinumKeywords;
        }

        /**
         * Gets the value of the memorabilia property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isMemorabilia() {
            return memorabilia;
        }

        /**
         * Sets the value of the memorabilia property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setMemorabilia(Boolean value) {
            this.memorabilia = value;
        }

        /**
         * Gets the value of the autographed property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isAutographed() {
            return autographed;
        }

        /**
         * Sets the value of the autographed property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setAutographed(Boolean value) {
            this.autographed = value;
        }

        /**
         * Gets the value of the usedFor property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the usedFor property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getUsedFor().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getUsedFor() {
            if (usedFor == null) {
                usedFor = new ArrayList<String>();
            }
            return this.usedFor;
        }

        /**
         * Gets the value of the itemType property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getItemType() {
            return itemType;
        }

        /**
         * Sets the value of the itemType property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setItemType(String value) {
            this.itemType = value;
        }

        /**
         * Gets the value of the otherItemAttributes property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the otherItemAttributes property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getOtherItemAttributes().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getOtherItemAttributes() {
            if (otherItemAttributes == null) {
                otherItemAttributes = new ArrayList<String>();
            }
            return this.otherItemAttributes;
        }

        /**
         * Gets the value of the targetAudience property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the targetAudience property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTargetAudience().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getTargetAudience() {
            if (targetAudience == null) {
                targetAudience = new ArrayList<String>();
            }
            return this.targetAudience;
        }

        /**
         * Gets the value of the subjectContent property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the subjectContent property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSubjectContent().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getSubjectContent() {
            if (subjectContent == null) {
                subjectContent = new ArrayList<String>();
            }
            return this.subjectContent;
        }

        /**
         * Gets the value of the isGiftWrapAvailable property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isIsGiftWrapAvailable() {
            return isGiftWrapAvailable;
        }

        /**
         * Sets the value of the isGiftWrapAvailable property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setIsGiftWrapAvailable(Boolean value) {
            this.isGiftWrapAvailable = value;
        }

        /**
         * Gets the value of the isGiftMessageAvailable property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isIsGiftMessageAvailable() {
            return isGiftMessageAvailable;
        }

        /**
         * Sets the value of the isGiftMessageAvailable property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setIsGiftMessageAvailable(Boolean value) {
            this.isGiftMessageAvailable = value;
        }

        /**
         * Gets the value of the promotionKeywords property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the promotionKeywords property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getPromotionKeywords().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getPromotionKeywords() {
            if (promotionKeywords == null) {
                promotionKeywords = new ArrayList<String>();
            }
            return this.promotionKeywords;
        }

        /**
         * Gets the value of the isDiscontinuedByManufacturer property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isIsDiscontinuedByManufacturer() {
            return isDiscontinuedByManufacturer;
        }

        /**
         * Sets the value of the isDiscontinuedByManufacturer property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setIsDiscontinuedByManufacturer(Boolean value) {
            this.isDiscontinuedByManufacturer = value;
        }

        /**
         * Gets the value of the deliveryChannel property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the deliveryChannel property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getDeliveryChannel().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getDeliveryChannel() {
            if (deliveryChannel == null) {
                deliveryChannel = new ArrayList<String>();
            }
            return this.deliveryChannel;
        }

        /**
         * Gets the value of the maxAggregateShipQuantity property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getMaxAggregateShipQuantity() {
            return maxAggregateShipQuantity;
        }

        /**
         * Sets the value of the maxAggregateShipQuantity property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setMaxAggregateShipQuantity(BigInteger value) {
            this.maxAggregateShipQuantity = value;
        }

        /**
         * Gets the value of the recommendedBrowseNode property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the recommendedBrowseNode property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getRecommendedBrowseNode().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link BigInteger }
         * 
         * 
         */
        public List<BigInteger> getRecommendedBrowseNode() {
            if (recommendedBrowseNode == null) {
                recommendedBrowseNode = new ArrayList<BigInteger>();
            }
            return this.recommendedBrowseNode;
        }

        /**
         * Gets the value of the fedasid property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFEDASID() {
            return fedasid;
        }

        /**
         * Sets the value of the fedasid property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFEDASID(String value) {
            this.fedasid = value;
        }

        /**
         * Gets the value of the tsdAgeWarning property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTSDAgeWarning() {
            return tsdAgeWarning;
        }

        /**
         * Sets the value of the tsdAgeWarning property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTSDAgeWarning(String value) {
            this.tsdAgeWarning = value;
        }

        /**
         * Gets the value of the tsdWarning property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the tsdWarning property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTSDWarning().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getTSDWarning() {
            if (tsdWarning == null) {
                tsdWarning = new ArrayList<String>();
            }
            return this.tsdWarning;
        }

        /**
         * Gets the value of the tsdLanguage property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the tsdLanguage property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTSDLanguage().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getTSDLanguage() {
            if (tsdLanguage == null) {
                tsdLanguage = new ArrayList<String>();
            }
            return this.tsdLanguage;
        }

        /**
         * Gets the value of the optionalPaymentTypeExclusion property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the optionalPaymentTypeExclusion property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getOptionalPaymentTypeExclusion().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getOptionalPaymentTypeExclusion() {
            if (optionalPaymentTypeExclusion == null) {
                optionalPaymentTypeExclusion = new ArrayList<String>();
            }
            return this.optionalPaymentTypeExclusion;
        }

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
     *         &lt;element name="Priority" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}positiveInteger">
     *               &lt;minInclusive value="1"/>
     *               &lt;maxInclusive value="10"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="BrowseExclusion" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
     *         &lt;element name="RecommendationExclusion" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
        "priority",
        "browseExclusion",
        "recommendationExclusion"
    })
    public static class DiscoveryData {

        @XmlElement(name = "Priority")
        protected Integer priority;
        @XmlElement(name = "BrowseExclusion")
        protected Boolean browseExclusion;
        @XmlElement(name = "RecommendationExclusion")
        protected Boolean recommendationExclusion;

        /**
         * Gets the value of the priority property.
         * 
         * @return
         *     possible object is
         *     {@link Integer }
         *     
         */
        public Integer getPriority() {
            return priority;
        }

        /**
         * Sets the value of the priority property.
         * 
         * @param value
         *     allowed object is
         *     {@link Integer }
         *     
         */
        public void setPriority(Integer value) {
            this.priority = value;
        }

        /**
         * Gets the value of the browseExclusion property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isBrowseExclusion() {
            return browseExclusion;
        }

        /**
         * Sets the value of the browseExclusion property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setBrowseExclusion(Boolean value) {
            this.browseExclusion = value;
        }

        /**
         * Gets the value of the recommendationExclusion property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isRecommendationExclusion() {
            return recommendationExclusion;
        }

        /**
         * Sets the value of the recommendationExclusion property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setRecommendationExclusion(Boolean value) {
            this.recommendationExclusion = value;
        }

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
     *       &lt;choice>
     *         &lt;element ref="{}Tools"/>
     *         &lt;element ref="{}SoftwareVideoGames"/>
     *         &lt;element ref="{}Wireless"/>
     *         &lt;element ref="{}Lighting"/>
     *       &lt;/choice>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "tools",
        "softwareVideoGames",
        "wireless",
        "lighting"
    })
    public static class ProductData {

        @XmlElement(name = "Tools")
        protected Tools tools;
        @XmlElement(name = "SoftwareVideoGames")
        protected SoftwareVideoGames softwareVideoGames;
        @XmlElement(name = "Wireless")
        protected Wireless wireless;
        @XmlElement(name = "Lighting")
        protected Lighting lighting;

        /**
         * Gets the value of the tools property.
         * 
         * @return
         *     possible object is
         *     {@link Tools }
         *     
         */
        public Tools getTools() {
            return tools;
        }

        /**
         * Sets the value of the tools property.
         * 
         * @param value
         *     allowed object is
         *     {@link Tools }
         *     
         */
        public void setTools(Tools value) {
            this.tools = value;
        }

        /**
         * Gets the value of the softwareVideoGames property.
         * 
         * @return
         *     possible object is
         *     {@link SoftwareVideoGames }
         *     
         */
        public SoftwareVideoGames getSoftwareVideoGames() {
            return softwareVideoGames;
        }

        /**
         * Sets the value of the softwareVideoGames property.
         * 
         * @param value
         *     allowed object is
         *     {@link SoftwareVideoGames }
         *     
         */
        public void setSoftwareVideoGames(SoftwareVideoGames value) {
            this.softwareVideoGames = value;
        }

        /**
         * Gets the value of the wireless property.
         * 
         * @return
         *     possible object is
         *     {@link Wireless }
         *     
         */
        public Wireless getWireless() {
            return wireless;
        }

        /**
         * Sets the value of the wireless property.
         * 
         * @param value
         *     allowed object is
         *     {@link Wireless }
         *     
         */
        public void setWireless(Wireless value) {
            this.wireless = value;
        }

        /**
         * Gets the value of the lighting property.
         * 
         * @return
         *     possible object is
         *     {@link Lighting }
         *     
         */
        public Lighting getLighting() {
            return lighting;
        }

        /**
         * Sets the value of the lighting property.
         * 
         * @param value
         *     allowed object is
         *     {@link Lighting }
         *     
         */
        public void setLighting(Lighting value) {
            this.lighting = value;
        }

    }

}
