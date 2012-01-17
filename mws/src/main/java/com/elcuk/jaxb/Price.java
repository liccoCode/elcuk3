
package com.elcuk.jaxb;

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
 *         &lt;element name="StandardPrice" type="{}OverrideCurrencyAmount"/>
 *         &lt;element name="MAP" type="{}OverrideCurrencyAmount" minOccurs="0"/>
 *         &lt;element name="DepositAmount" type="{}CurrencyAmountWithDefault" minOccurs="0"/>
 *         &lt;element name="Sale" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="StartDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                   &lt;element name="EndDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                   &lt;element name="SalePrice" type="{}OverrideCurrencyAmount"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Previous" type="{}DatedPrice" minOccurs="0"/>
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
    "standardPrice",
    "map",
    "depositAmount",
    "sale",
    "previous"
})
@XmlRootElement(name = "Price")
public class Price {

    @XmlElement(name = "SKU", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String sku;
    @XmlElement(name = "StandardPrice", required = true)
    protected OverrideCurrencyAmount standardPrice;
    @XmlElement(name = "MAP")
    protected OverrideCurrencyAmount map;
    @XmlElement(name = "DepositAmount")
    protected CurrencyAmountWithDefault depositAmount;
    @XmlElement(name = "Sale")
    protected Price.Sale sale;
    @XmlElement(name = "Previous")
    protected DatedPrice previous;

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
     * Gets the value of the standardPrice property.
     * 
     * @return
     *     possible object is
     *     {@link OverrideCurrencyAmount }
     *     
     */
    public OverrideCurrencyAmount getStandardPrice() {
        return standardPrice;
    }

    /**
     * Sets the value of the standardPrice property.
     * 
     * @param value
     *     allowed object is
     *     {@link OverrideCurrencyAmount }
     *     
     */
    public void setStandardPrice(OverrideCurrencyAmount value) {
        this.standardPrice = value;
    }

    /**
     * Gets the value of the map property.
     * 
     * @return
     *     possible object is
     *     {@link OverrideCurrencyAmount }
     *     
     */
    public OverrideCurrencyAmount getMAP() {
        return map;
    }

    /**
     * Sets the value of the map property.
     * 
     * @param value
     *     allowed object is
     *     {@link OverrideCurrencyAmount }
     *     
     */
    public void setMAP(OverrideCurrencyAmount value) {
        this.map = value;
    }

    /**
     * Gets the value of the depositAmount property.
     * 
     * @return
     *     possible object is
     *     {@link CurrencyAmountWithDefault }
     *     
     */
    public CurrencyAmountWithDefault getDepositAmount() {
        return depositAmount;
    }

    /**
     * Sets the value of the depositAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link CurrencyAmountWithDefault }
     *     
     */
    public void setDepositAmount(CurrencyAmountWithDefault value) {
        this.depositAmount = value;
    }

    /**
     * Gets the value of the sale property.
     * 
     * @return
     *     possible object is
     *     {@link Price.Sale }
     *     
     */
    public Price.Sale getSale() {
        return sale;
    }

    /**
     * Sets the value of the sale property.
     * 
     * @param value
     *     allowed object is
     *     {@link Price.Sale }
     *     
     */
    public void setSale(Price.Sale value) {
        this.sale = value;
    }

    /**
     * Gets the value of the previous property.
     * 
     * @return
     *     possible object is
     *     {@link DatedPrice }
     *     
     */
    public DatedPrice getPrevious() {
        return previous;
    }

    /**
     * Sets the value of the previous property.
     * 
     * @param value
     *     allowed object is
     *     {@link DatedPrice }
     *     
     */
    public void setPrevious(DatedPrice value) {
        this.previous = value;
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
     *         &lt;element name="StartDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *         &lt;element name="EndDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *         &lt;element name="SalePrice" type="{}OverrideCurrencyAmount"/>
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
        "startDate",
        "endDate",
        "salePrice"
    })
    public static class Sale {

        @XmlElement(name = "StartDate", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar startDate;
        @XmlElement(name = "EndDate", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar endDate;
        @XmlElement(name = "SalePrice", required = true)
        protected OverrideCurrencyAmount salePrice;

        /**
         * Gets the value of the startDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getStartDate() {
            return startDate;
        }

        /**
         * Sets the value of the startDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setStartDate(XMLGregorianCalendar value) {
            this.startDate = value;
        }

        /**
         * Gets the value of the endDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getEndDate() {
            return endDate;
        }

        /**
         * Sets the value of the endDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setEndDate(XMLGregorianCalendar value) {
            this.endDate = value;
        }

        /**
         * Gets the value of the salePrice property.
         * 
         * @return
         *     possible object is
         *     {@link OverrideCurrencyAmount }
         *     
         */
        public OverrideCurrencyAmount getSalePrice() {
            return salePrice;
        }

        /**
         * Sets the value of the salePrice property.
         * 
         * @param value
         *     allowed object is
         *     {@link OverrideCurrencyAmount }
         *     
         */
        public void setSalePrice(OverrideCurrencyAmount value) {
            this.salePrice = value;
        }

    }

}
