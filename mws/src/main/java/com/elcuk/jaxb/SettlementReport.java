
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
 *         &lt;element name="SettlementData">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="AmazonSettlementID" type="{}IDNumber"/>
 *                   &lt;element name="TotalAmount" type="{}CurrencyAmount"/>
 *                   &lt;element name="StartDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                   &lt;element name="EndDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                   &lt;element name="DepositDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Order" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="AmazonOrderID" type="{}AmazonAlphaOrderID"/>
 *                   &lt;element ref="{}MerchantOrderID" minOccurs="0"/>
 *                   &lt;element name="ShipmentID" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *                   &lt;element name="ShipmentFees" type="{}AmazonFees" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element ref="{}MarketplaceName"/>
 *                   &lt;element name="Fulfillment">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}MerchantFulfillmentID" minOccurs="0"/>
 *                             &lt;element name="PostedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                             &lt;element name="Item" maxOccurs="unbounded">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element ref="{}AmazonOrderItemCode"/>
 *                                       &lt;element ref="{}MerchantOrderItemID" minOccurs="0"/>
 *                                       &lt;element ref="{}SKU"/>
 *                                       &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *                                       &lt;element name="ItemPrice" type="{}BuyerPrice"/>
 *                                       &lt;element name="ItemFees" type="{}AmazonFees" minOccurs="0"/>
 *                                       &lt;element name="Promotion" maxOccurs="unbounded" minOccurs="0">
 *                                         &lt;complexType>
 *                                           &lt;complexContent>
 *                                             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                               &lt;sequence>
 *                                                 &lt;element ref="{}MerchantPromotionID"/>
 *                                                 &lt;element name="Type" type="{}StringNotNull"/>
 *                                                 &lt;element name="Amount" type="{}CurrencyAmount"/>
 *                                               &lt;/sequence>
 *                                             &lt;/restriction>
 *                                           &lt;/complexContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="DirectPayment" type="{}DirectPaymentType" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="OrderFee" type="{}AmazonFees" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Adjustment" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="AmazonOrderID" type="{}AmazonAlphaOrderID"/>
 *                   &lt;element ref="{}MerchantOrderID" minOccurs="0"/>
 *                   &lt;element name="AdjustmentID" type="{}StringNotNull" minOccurs="0"/>
 *                   &lt;element ref="{}MarketplaceName"/>
 *                   &lt;element name="Fulfillment">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}MerchantFulfillmentID" minOccurs="0"/>
 *                             &lt;element name="PostedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                             &lt;element name="AdjustedItem" maxOccurs="unbounded">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element ref="{}AmazonOrderItemCode"/>
 *                                       &lt;element ref="{}MerchantOrderItemID" minOccurs="0"/>
 *                                       &lt;element name="MerchantAdjustmentItemID" type="{}StringNotNull" minOccurs="0"/>
 *                                       &lt;element ref="{}SKU"/>
 *                                       &lt;element name="ItemPriceAdjustments" type="{}BuyerPrice"/>
 *                                       &lt;element name="ItemFeeAdjustments" type="{}AmazonFees" minOccurs="0"/>
 *                                       &lt;element name="PromotionAdjustment" maxOccurs="unbounded" minOccurs="0">
 *                                         &lt;complexType>
 *                                           &lt;complexContent>
 *                                             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                               &lt;sequence>
 *                                                 &lt;element ref="{}MerchantPromotionID"/>
 *                                                 &lt;element name="Type" type="{}StringNotNull"/>
 *                                                 &lt;element name="Amount" type="{}CurrencyAmount"/>
 *                                               &lt;/sequence>
 *                                             &lt;/restriction>
 *                                           &lt;/complexContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="DirectPayment" type="{}DirectPaymentType" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="OrderFeeAdjustment" type="{}AmazonFees" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="OtherFee" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="AmazonOrderID" type="{}AmazonAlphaOrderID" minOccurs="0"/>
 *                   &lt;element ref="{}MerchantOrderID" minOccurs="0"/>
 *                   &lt;element ref="{}MarketplaceName"/>
 *                   &lt;element ref="{}MerchantFulfillmentID" minOccurs="0"/>
 *                   &lt;element name="PostedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                   &lt;element name="Amount" type="{}CurrencyAmount"/>
 *                   &lt;element name="ReasonDescription" type="{}StringNotNull"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="OtherTransaction" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="AmazonOrderID" type="{}AmazonAlphaOrderID" minOccurs="0"/>
 *                   &lt;element ref="{}MerchantOrderID" minOccurs="0"/>
 *                   &lt;element ref="{}MarketplaceName" minOccurs="0"/>
 *                   &lt;element ref="{}MerchantFulfillmentID" minOccurs="0"/>
 *                   &lt;element name="TransactionType" type="{}StringNotNull"/>
 *                   &lt;element name="TransactionID" type="{}StringNotNull" minOccurs="0"/>
 *                   &lt;element name="PostedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                   &lt;element name="Amount" type="{}CurrencyAmount"/>
 *                   &lt;element name="ReasonDescription" type="{}StringNotNull" minOccurs="0"/>
 *                   &lt;element name="Fees" type="{}AmazonFees" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="MiscEvent" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="PostedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                   &lt;element name="Amount" type="{}CurrencyAmount"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
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
    "settlementData",
    "order",
    "adjustment",
    "otherFee",
    "otherTransaction",
    "miscEvent"
})
@XmlRootElement(name = "SettlementReport")
public class SettlementReport {

    @XmlElement(name = "SettlementData", required = true)
    protected SettlementReport.SettlementData settlementData;
    @XmlElement(name = "Order")
    protected List<SettlementReport.Order> order;
    @XmlElement(name = "Adjustment")
    protected List<SettlementReport.Adjustment> adjustment;
    @XmlElement(name = "OtherFee")
    protected List<SettlementReport.OtherFee> otherFee;
    @XmlElement(name = "OtherTransaction")
    protected List<SettlementReport.OtherTransaction> otherTransaction;
    @XmlElement(name = "MiscEvent")
    protected List<SettlementReport.MiscEvent> miscEvent;

    /**
     * Gets the value of the settlementData property.
     * 
     * @return
     *     possible object is
     *     {@link SettlementReport.SettlementData }
     *     
     */
    public SettlementReport.SettlementData getSettlementData() {
        return settlementData;
    }

    /**
     * Sets the value of the settlementData property.
     * 
     * @param value
     *     allowed object is
     *     {@link SettlementReport.SettlementData }
     *     
     */
    public void setSettlementData(SettlementReport.SettlementData value) {
        this.settlementData = value;
    }

    /**
     * Gets the value of the order property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the order property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOrder().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SettlementReport.Order }
     * 
     * 
     */
    public List<SettlementReport.Order> getOrder() {
        if (order == null) {
            order = new ArrayList<SettlementReport.Order>();
        }
        return this.order;
    }

    /**
     * Gets the value of the adjustment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the adjustment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAdjustment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SettlementReport.Adjustment }
     * 
     * 
     */
    public List<SettlementReport.Adjustment> getAdjustment() {
        if (adjustment == null) {
            adjustment = new ArrayList<SettlementReport.Adjustment>();
        }
        return this.adjustment;
    }

    /**
     * Gets the value of the otherFee property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the otherFee property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOtherFee().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SettlementReport.OtherFee }
     * 
     * 
     */
    public List<SettlementReport.OtherFee> getOtherFee() {
        if (otherFee == null) {
            otherFee = new ArrayList<SettlementReport.OtherFee>();
        }
        return this.otherFee;
    }

    /**
     * Gets the value of the otherTransaction property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the otherTransaction property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOtherTransaction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SettlementReport.OtherTransaction }
     * 
     * 
     */
    public List<SettlementReport.OtherTransaction> getOtherTransaction() {
        if (otherTransaction == null) {
            otherTransaction = new ArrayList<SettlementReport.OtherTransaction>();
        }
        return this.otherTransaction;
    }

    /**
     * Gets the value of the miscEvent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the miscEvent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMiscEvent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SettlementReport.MiscEvent }
     * 
     * 
     */
    public List<SettlementReport.MiscEvent> getMiscEvent() {
        if (miscEvent == null) {
            miscEvent = new ArrayList<SettlementReport.MiscEvent>();
        }
        return this.miscEvent;
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
     *         &lt;element name="AmazonOrderID" type="{}AmazonAlphaOrderID"/>
     *         &lt;element ref="{}MerchantOrderID" minOccurs="0"/>
     *         &lt;element name="AdjustmentID" type="{}StringNotNull" minOccurs="0"/>
     *         &lt;element ref="{}MarketplaceName"/>
     *         &lt;element name="Fulfillment">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}MerchantFulfillmentID" minOccurs="0"/>
     *                   &lt;element name="PostedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *                   &lt;element name="AdjustedItem" maxOccurs="unbounded">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element ref="{}AmazonOrderItemCode"/>
     *                             &lt;element ref="{}MerchantOrderItemID" minOccurs="0"/>
     *                             &lt;element name="MerchantAdjustmentItemID" type="{}StringNotNull" minOccurs="0"/>
     *                             &lt;element ref="{}SKU"/>
     *                             &lt;element name="ItemPriceAdjustments" type="{}BuyerPrice"/>
     *                             &lt;element name="ItemFeeAdjustments" type="{}AmazonFees" minOccurs="0"/>
     *                             &lt;element name="PromotionAdjustment" maxOccurs="unbounded" minOccurs="0">
     *                               &lt;complexType>
     *                                 &lt;complexContent>
     *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                                     &lt;sequence>
     *                                       &lt;element ref="{}MerchantPromotionID"/>
     *                                       &lt;element name="Type" type="{}StringNotNull"/>
     *                                       &lt;element name="Amount" type="{}CurrencyAmount"/>
     *                                     &lt;/sequence>
     *                                   &lt;/restriction>
     *                                 &lt;/complexContent>
     *                               &lt;/complexType>
     *                             &lt;/element>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                   &lt;element name="DirectPayment" type="{}DirectPaymentType" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="OrderFeeAdjustment" type="{}AmazonFees" maxOccurs="unbounded" minOccurs="0"/>
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
        "amazonOrderID",
        "merchantOrderID",
        "adjustmentID",
        "marketplaceName",
        "fulfillment",
        "orderFeeAdjustment"
    })
    public static class Adjustment {

        @XmlElement(name = "AmazonOrderID", required = true)
        protected String amazonOrderID;
        @XmlElement(name = "MerchantOrderID")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String merchantOrderID;
        @XmlElement(name = "AdjustmentID")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String adjustmentID;
        @XmlElement(name = "MarketplaceName", required = true)
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String marketplaceName;
        @XmlElement(name = "Fulfillment", required = true)
        protected SettlementReport.Adjustment.Fulfillment fulfillment;
        @XmlElement(name = "OrderFeeAdjustment")
        protected List<AmazonFees> orderFeeAdjustment;

        /**
         * Gets the value of the amazonOrderID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getAmazonOrderID() {
            return amazonOrderID;
        }

        /**
         * Sets the value of the amazonOrderID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAmazonOrderID(String value) {
            this.amazonOrderID = value;
        }

        /**
         * Gets the value of the merchantOrderID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMerchantOrderID() {
            return merchantOrderID;
        }

        /**
         * Sets the value of the merchantOrderID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMerchantOrderID(String value) {
            this.merchantOrderID = value;
        }

        /**
         * Gets the value of the adjustmentID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getAdjustmentID() {
            return adjustmentID;
        }

        /**
         * Sets the value of the adjustmentID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAdjustmentID(String value) {
            this.adjustmentID = value;
        }

        /**
         * Gets the value of the marketplaceName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMarketplaceName() {
            return marketplaceName;
        }

        /**
         * Sets the value of the marketplaceName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMarketplaceName(String value) {
            this.marketplaceName = value;
        }

        /**
         * Gets the value of the fulfillment property.
         * 
         * @return
         *     possible object is
         *     {@link SettlementReport.Adjustment.Fulfillment }
         *     
         */
        public SettlementReport.Adjustment.Fulfillment getFulfillment() {
            return fulfillment;
        }

        /**
         * Sets the value of the fulfillment property.
         * 
         * @param value
         *     allowed object is
         *     {@link SettlementReport.Adjustment.Fulfillment }
         *     
         */
        public void setFulfillment(SettlementReport.Adjustment.Fulfillment value) {
            this.fulfillment = value;
        }

        /**
         * Gets the value of the orderFeeAdjustment property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the orderFeeAdjustment property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getOrderFeeAdjustment().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link AmazonFees }
         * 
         * 
         */
        public List<AmazonFees> getOrderFeeAdjustment() {
            if (orderFeeAdjustment == null) {
                orderFeeAdjustment = new ArrayList<AmazonFees>();
            }
            return this.orderFeeAdjustment;
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
         *         &lt;element ref="{}MerchantFulfillmentID" minOccurs="0"/>
         *         &lt;element name="PostedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
         *         &lt;element name="AdjustedItem" maxOccurs="unbounded">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element ref="{}AmazonOrderItemCode"/>
         *                   &lt;element ref="{}MerchantOrderItemID" minOccurs="0"/>
         *                   &lt;element name="MerchantAdjustmentItemID" type="{}StringNotNull" minOccurs="0"/>
         *                   &lt;element ref="{}SKU"/>
         *                   &lt;element name="ItemPriceAdjustments" type="{}BuyerPrice"/>
         *                   &lt;element name="ItemFeeAdjustments" type="{}AmazonFees" minOccurs="0"/>
         *                   &lt;element name="PromotionAdjustment" maxOccurs="unbounded" minOccurs="0">
         *                     &lt;complexType>
         *                       &lt;complexContent>
         *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                           &lt;sequence>
         *                             &lt;element ref="{}MerchantPromotionID"/>
         *                             &lt;element name="Type" type="{}StringNotNull"/>
         *                             &lt;element name="Amount" type="{}CurrencyAmount"/>
         *                           &lt;/sequence>
         *                         &lt;/restriction>
         *                       &lt;/complexContent>
         *                     &lt;/complexType>
         *                   &lt;/element>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *         &lt;element name="DirectPayment" type="{}DirectPaymentType" minOccurs="0"/>
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
            "merchantFulfillmentID",
            "postedDate",
            "adjustedItem",
            "directPayment"
        })
        public static class Fulfillment {

            @XmlElement(name = "MerchantFulfillmentID")
            @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
            protected String merchantFulfillmentID;
            @XmlElement(name = "PostedDate", required = true)
            @XmlSchemaType(name = "dateTime")
            protected XMLGregorianCalendar postedDate;
            @XmlElement(name = "AdjustedItem", required = true)
            protected List<SettlementReport.Adjustment.Fulfillment.AdjustedItem> adjustedItem;
            @XmlElement(name = "DirectPayment")
            protected DirectPaymentType directPayment;

            /**
             * Gets the value of the merchantFulfillmentID property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getMerchantFulfillmentID() {
                return merchantFulfillmentID;
            }

            /**
             * Sets the value of the merchantFulfillmentID property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setMerchantFulfillmentID(String value) {
                this.merchantFulfillmentID = value;
            }

            /**
             * Gets the value of the postedDate property.
             * 
             * @return
             *     possible object is
             *     {@link XMLGregorianCalendar }
             *     
             */
            public XMLGregorianCalendar getPostedDate() {
                return postedDate;
            }

            /**
             * Sets the value of the postedDate property.
             * 
             * @param value
             *     allowed object is
             *     {@link XMLGregorianCalendar }
             *     
             */
            public void setPostedDate(XMLGregorianCalendar value) {
                this.postedDate = value;
            }

            /**
             * Gets the value of the adjustedItem property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the adjustedItem property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getAdjustedItem().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link SettlementReport.Adjustment.Fulfillment.AdjustedItem }
             * 
             * 
             */
            public List<SettlementReport.Adjustment.Fulfillment.AdjustedItem> getAdjustedItem() {
                if (adjustedItem == null) {
                    adjustedItem = new ArrayList<SettlementReport.Adjustment.Fulfillment.AdjustedItem>();
                }
                return this.adjustedItem;
            }

            /**
             * Gets the value of the directPayment property.
             * 
             * @return
             *     possible object is
             *     {@link DirectPaymentType }
             *     
             */
            public DirectPaymentType getDirectPayment() {
                return directPayment;
            }

            /**
             * Sets the value of the directPayment property.
             * 
             * @param value
             *     allowed object is
             *     {@link DirectPaymentType }
             *     
             */
            public void setDirectPayment(DirectPaymentType value) {
                this.directPayment = value;
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
             *         &lt;element ref="{}AmazonOrderItemCode"/>
             *         &lt;element ref="{}MerchantOrderItemID" minOccurs="0"/>
             *         &lt;element name="MerchantAdjustmentItemID" type="{}StringNotNull" minOccurs="0"/>
             *         &lt;element ref="{}SKU"/>
             *         &lt;element name="ItemPriceAdjustments" type="{}BuyerPrice"/>
             *         &lt;element name="ItemFeeAdjustments" type="{}AmazonFees" minOccurs="0"/>
             *         &lt;element name="PromotionAdjustment" maxOccurs="unbounded" minOccurs="0">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;sequence>
             *                   &lt;element ref="{}MerchantPromotionID"/>
             *                   &lt;element name="Type" type="{}StringNotNull"/>
             *                   &lt;element name="Amount" type="{}CurrencyAmount"/>
             *                 &lt;/sequence>
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
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
                "amazonOrderItemCode",
                "merchantOrderItemID",
                "merchantAdjustmentItemID",
                "sku",
                "itemPriceAdjustments",
                "itemFeeAdjustments",
                "promotionAdjustment"
            })
            public static class AdjustedItem {

                @XmlElement(name = "AmazonOrderItemCode", required = true)
                protected String amazonOrderItemCode;
                @XmlElement(name = "MerchantOrderItemID")
                @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
                protected String merchantOrderItemID;
                @XmlElement(name = "MerchantAdjustmentItemID")
                @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
                protected String merchantAdjustmentItemID;
                @XmlElement(name = "SKU", required = true)
                @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
                protected String sku;
                @XmlElement(name = "ItemPriceAdjustments", required = true)
                protected BuyerPrice itemPriceAdjustments;
                @XmlElement(name = "ItemFeeAdjustments")
                protected AmazonFees itemFeeAdjustments;
                @XmlElement(name = "PromotionAdjustment")
                protected List<SettlementReport.Adjustment.Fulfillment.AdjustedItem.PromotionAdjustment> promotionAdjustment;

                /**
                 * Gets the value of the amazonOrderItemCode property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getAmazonOrderItemCode() {
                    return amazonOrderItemCode;
                }

                /**
                 * Sets the value of the amazonOrderItemCode property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setAmazonOrderItemCode(String value) {
                    this.amazonOrderItemCode = value;
                }

                /**
                 * Gets the value of the merchantOrderItemID property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getMerchantOrderItemID() {
                    return merchantOrderItemID;
                }

                /**
                 * Sets the value of the merchantOrderItemID property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setMerchantOrderItemID(String value) {
                    this.merchantOrderItemID = value;
                }

                /**
                 * Gets the value of the merchantAdjustmentItemID property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getMerchantAdjustmentItemID() {
                    return merchantAdjustmentItemID;
                }

                /**
                 * Sets the value of the merchantAdjustmentItemID property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setMerchantAdjustmentItemID(String value) {
                    this.merchantAdjustmentItemID = value;
                }

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
                 * Gets the value of the itemPriceAdjustments property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BuyerPrice }
                 *     
                 */
                public BuyerPrice getItemPriceAdjustments() {
                    return itemPriceAdjustments;
                }

                /**
                 * Sets the value of the itemPriceAdjustments property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BuyerPrice }
                 *     
                 */
                public void setItemPriceAdjustments(BuyerPrice value) {
                    this.itemPriceAdjustments = value;
                }

                /**
                 * Gets the value of the itemFeeAdjustments property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link AmazonFees }
                 *     
                 */
                public AmazonFees getItemFeeAdjustments() {
                    return itemFeeAdjustments;
                }

                /**
                 * Sets the value of the itemFeeAdjustments property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link AmazonFees }
                 *     
                 */
                public void setItemFeeAdjustments(AmazonFees value) {
                    this.itemFeeAdjustments = value;
                }

                /**
                 * Gets the value of the promotionAdjustment property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the promotionAdjustment property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getPromotionAdjustment().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link SettlementReport.Adjustment.Fulfillment.AdjustedItem.PromotionAdjustment }
                 * 
                 * 
                 */
                public List<SettlementReport.Adjustment.Fulfillment.AdjustedItem.PromotionAdjustment> getPromotionAdjustment() {
                    if (promotionAdjustment == null) {
                        promotionAdjustment = new ArrayList<SettlementReport.Adjustment.Fulfillment.AdjustedItem.PromotionAdjustment>();
                    }
                    return this.promotionAdjustment;
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
                 *         &lt;element ref="{}MerchantPromotionID"/>
                 *         &lt;element name="Type" type="{}StringNotNull"/>
                 *         &lt;element name="Amount" type="{}CurrencyAmount"/>
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
                    "merchantPromotionID",
                    "type",
                    "amount"
                })
                public static class PromotionAdjustment {

                    @XmlElement(name = "MerchantPromotionID", required = true)
                    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
                    protected String merchantPromotionID;
                    @XmlElement(name = "Type", required = true)
                    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
                    protected String type;
                    @XmlElement(name = "Amount", required = true)
                    protected CurrencyAmount amount;

                    /**
                     * Gets the value of the merchantPromotionID property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getMerchantPromotionID() {
                        return merchantPromotionID;
                    }

                    /**
                     * Sets the value of the merchantPromotionID property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setMerchantPromotionID(String value) {
                        this.merchantPromotionID = value;
                    }

                    /**
                     * Gets the value of the type property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getType() {
                        return type;
                    }

                    /**
                     * Sets the value of the type property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setType(String value) {
                        this.type = value;
                    }

                    /**
                     * Gets the value of the amount property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link CurrencyAmount }
                     *     
                     */
                    public CurrencyAmount getAmount() {
                        return amount;
                    }

                    /**
                     * Sets the value of the amount property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link CurrencyAmount }
                     *     
                     */
                    public void setAmount(CurrencyAmount value) {
                        this.amount = value;
                    }

                }

            }

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
     *         &lt;element name="PostedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *         &lt;element name="Amount" type="{}CurrencyAmount"/>
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
        "postedDate",
        "amount"
    })
    public static class MiscEvent {

        @XmlElement(name = "PostedDate", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar postedDate;
        @XmlElement(name = "Amount", required = true)
        protected CurrencyAmount amount;

        /**
         * Gets the value of the postedDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getPostedDate() {
            return postedDate;
        }

        /**
         * Sets the value of the postedDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setPostedDate(XMLGregorianCalendar value) {
            this.postedDate = value;
        }

        /**
         * Gets the value of the amount property.
         * 
         * @return
         *     possible object is
         *     {@link CurrencyAmount }
         *     
         */
        public CurrencyAmount getAmount() {
            return amount;
        }

        /**
         * Sets the value of the amount property.
         * 
         * @param value
         *     allowed object is
         *     {@link CurrencyAmount }
         *     
         */
        public void setAmount(CurrencyAmount value) {
            this.amount = value;
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
     *         &lt;element name="AmazonOrderID" type="{}AmazonAlphaOrderID"/>
     *         &lt;element ref="{}MerchantOrderID" minOccurs="0"/>
     *         &lt;element name="ShipmentID" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
     *         &lt;element name="ShipmentFees" type="{}AmazonFees" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element ref="{}MarketplaceName"/>
     *         &lt;element name="Fulfillment">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}MerchantFulfillmentID" minOccurs="0"/>
     *                   &lt;element name="PostedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *                   &lt;element name="Item" maxOccurs="unbounded">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element ref="{}AmazonOrderItemCode"/>
     *                             &lt;element ref="{}MerchantOrderItemID" minOccurs="0"/>
     *                             &lt;element ref="{}SKU"/>
     *                             &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
     *                             &lt;element name="ItemPrice" type="{}BuyerPrice"/>
     *                             &lt;element name="ItemFees" type="{}AmazonFees" minOccurs="0"/>
     *                             &lt;element name="Promotion" maxOccurs="unbounded" minOccurs="0">
     *                               &lt;complexType>
     *                                 &lt;complexContent>
     *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                                     &lt;sequence>
     *                                       &lt;element ref="{}MerchantPromotionID"/>
     *                                       &lt;element name="Type" type="{}StringNotNull"/>
     *                                       &lt;element name="Amount" type="{}CurrencyAmount"/>
     *                                     &lt;/sequence>
     *                                   &lt;/restriction>
     *                                 &lt;/complexContent>
     *                               &lt;/complexType>
     *                             &lt;/element>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                   &lt;element name="DirectPayment" type="{}DirectPaymentType" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="OrderFee" type="{}AmazonFees" maxOccurs="unbounded" minOccurs="0"/>
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
        "amazonOrderID",
        "merchantOrderID",
        "shipmentID",
        "shipmentFees",
        "marketplaceName",
        "fulfillment",
        "orderFee"
    })
    public static class Order {

        @XmlElement(name = "AmazonOrderID", required = true)
        protected String amazonOrderID;
        @XmlElement(name = "MerchantOrderID")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String merchantOrderID;
        @XmlElement(name = "ShipmentID")
        protected Object shipmentID;
        @XmlElement(name = "ShipmentFees")
        protected List<AmazonFees> shipmentFees;
        @XmlElement(name = "MarketplaceName", required = true)
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String marketplaceName;
        @XmlElement(name = "Fulfillment", required = true)
        protected SettlementReport.Order.Fulfillment fulfillment;
        @XmlElement(name = "OrderFee")
        protected List<AmazonFees> orderFee;

        /**
         * Gets the value of the amazonOrderID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getAmazonOrderID() {
            return amazonOrderID;
        }

        /**
         * Sets the value of the amazonOrderID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAmazonOrderID(String value) {
            this.amazonOrderID = value;
        }

        /**
         * Gets the value of the merchantOrderID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMerchantOrderID() {
            return merchantOrderID;
        }

        /**
         * Sets the value of the merchantOrderID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMerchantOrderID(String value) {
            this.merchantOrderID = value;
        }

        /**
         * Gets the value of the shipmentID property.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getShipmentID() {
            return shipmentID;
        }

        /**
         * Sets the value of the shipmentID property.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setShipmentID(Object value) {
            this.shipmentID = value;
        }

        /**
         * Gets the value of the shipmentFees property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the shipmentFees property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getShipmentFees().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link AmazonFees }
         * 
         * 
         */
        public List<AmazonFees> getShipmentFees() {
            if (shipmentFees == null) {
                shipmentFees = new ArrayList<AmazonFees>();
            }
            return this.shipmentFees;
        }

        /**
         * Gets the value of the marketplaceName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMarketplaceName() {
            return marketplaceName;
        }

        /**
         * Sets the value of the marketplaceName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMarketplaceName(String value) {
            this.marketplaceName = value;
        }

        /**
         * Gets the value of the fulfillment property.
         * 
         * @return
         *     possible object is
         *     {@link SettlementReport.Order.Fulfillment }
         *     
         */
        public SettlementReport.Order.Fulfillment getFulfillment() {
            return fulfillment;
        }

        /**
         * Sets the value of the fulfillment property.
         * 
         * @param value
         *     allowed object is
         *     {@link SettlementReport.Order.Fulfillment }
         *     
         */
        public void setFulfillment(SettlementReport.Order.Fulfillment value) {
            this.fulfillment = value;
        }

        /**
         * Gets the value of the orderFee property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the orderFee property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getOrderFee().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link AmazonFees }
         * 
         * 
         */
        public List<AmazonFees> getOrderFee() {
            if (orderFee == null) {
                orderFee = new ArrayList<AmazonFees>();
            }
            return this.orderFee;
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
         *         &lt;element ref="{}MerchantFulfillmentID" minOccurs="0"/>
         *         &lt;element name="PostedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
         *         &lt;element name="Item" maxOccurs="unbounded">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element ref="{}AmazonOrderItemCode"/>
         *                   &lt;element ref="{}MerchantOrderItemID" minOccurs="0"/>
         *                   &lt;element ref="{}SKU"/>
         *                   &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
         *                   &lt;element name="ItemPrice" type="{}BuyerPrice"/>
         *                   &lt;element name="ItemFees" type="{}AmazonFees" minOccurs="0"/>
         *                   &lt;element name="Promotion" maxOccurs="unbounded" minOccurs="0">
         *                     &lt;complexType>
         *                       &lt;complexContent>
         *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                           &lt;sequence>
         *                             &lt;element ref="{}MerchantPromotionID"/>
         *                             &lt;element name="Type" type="{}StringNotNull"/>
         *                             &lt;element name="Amount" type="{}CurrencyAmount"/>
         *                           &lt;/sequence>
         *                         &lt;/restriction>
         *                       &lt;/complexContent>
         *                     &lt;/complexType>
         *                   &lt;/element>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *         &lt;element name="DirectPayment" type="{}DirectPaymentType" minOccurs="0"/>
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
            "merchantFulfillmentID",
            "postedDate",
            "item",
            "directPayment"
        })
        public static class Fulfillment {

            @XmlElement(name = "MerchantFulfillmentID")
            @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
            protected String merchantFulfillmentID;
            @XmlElement(name = "PostedDate", required = true)
            @XmlSchemaType(name = "dateTime")
            protected XMLGregorianCalendar postedDate;
            @XmlElement(name = "Item", required = true)
            protected List<SettlementReport.Order.Fulfillment.Item> item;
            @XmlElement(name = "DirectPayment")
            protected DirectPaymentType directPayment;

            /**
             * Gets the value of the merchantFulfillmentID property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getMerchantFulfillmentID() {
                return merchantFulfillmentID;
            }

            /**
             * Sets the value of the merchantFulfillmentID property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setMerchantFulfillmentID(String value) {
                this.merchantFulfillmentID = value;
            }

            /**
             * Gets the value of the postedDate property.
             * 
             * @return
             *     possible object is
             *     {@link XMLGregorianCalendar }
             *     
             */
            public XMLGregorianCalendar getPostedDate() {
                return postedDate;
            }

            /**
             * Sets the value of the postedDate property.
             * 
             * @param value
             *     allowed object is
             *     {@link XMLGregorianCalendar }
             *     
             */
            public void setPostedDate(XMLGregorianCalendar value) {
                this.postedDate = value;
            }

            /**
             * Gets the value of the item property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the item property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getItem().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link SettlementReport.Order.Fulfillment.Item }
             * 
             * 
             */
            public List<SettlementReport.Order.Fulfillment.Item> getItem() {
                if (item == null) {
                    item = new ArrayList<SettlementReport.Order.Fulfillment.Item>();
                }
                return this.item;
            }

            /**
             * Gets the value of the directPayment property.
             * 
             * @return
             *     possible object is
             *     {@link DirectPaymentType }
             *     
             */
            public DirectPaymentType getDirectPayment() {
                return directPayment;
            }

            /**
             * Sets the value of the directPayment property.
             * 
             * @param value
             *     allowed object is
             *     {@link DirectPaymentType }
             *     
             */
            public void setDirectPayment(DirectPaymentType value) {
                this.directPayment = value;
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
             *         &lt;element ref="{}AmazonOrderItemCode"/>
             *         &lt;element ref="{}MerchantOrderItemID" minOccurs="0"/>
             *         &lt;element ref="{}SKU"/>
             *         &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
             *         &lt;element name="ItemPrice" type="{}BuyerPrice"/>
             *         &lt;element name="ItemFees" type="{}AmazonFees" minOccurs="0"/>
             *         &lt;element name="Promotion" maxOccurs="unbounded" minOccurs="0">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;sequence>
             *                   &lt;element ref="{}MerchantPromotionID"/>
             *                   &lt;element name="Type" type="{}StringNotNull"/>
             *                   &lt;element name="Amount" type="{}CurrencyAmount"/>
             *                 &lt;/sequence>
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
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
                "amazonOrderItemCode",
                "merchantOrderItemID",
                "sku",
                "quantity",
                "itemPrice",
                "itemFees",
                "promotion"
            })
            public static class Item {

                @XmlElement(name = "AmazonOrderItemCode", required = true)
                protected String amazonOrderItemCode;
                @XmlElement(name = "MerchantOrderItemID")
                @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
                protected String merchantOrderItemID;
                @XmlElement(name = "SKU", required = true)
                @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
                protected String sku;
                @XmlElement(name = "Quantity", required = true)
                @XmlSchemaType(name = "positiveInteger")
                protected BigInteger quantity;
                @XmlElement(name = "ItemPrice", required = true)
                protected BuyerPrice itemPrice;
                @XmlElement(name = "ItemFees")
                protected AmazonFees itemFees;
                @XmlElement(name = "Promotion")
                protected List<SettlementReport.Order.Fulfillment.Item.Promotion> promotion;

                /**
                 * Gets the value of the amazonOrderItemCode property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getAmazonOrderItemCode() {
                    return amazonOrderItemCode;
                }

                /**
                 * Sets the value of the amazonOrderItemCode property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setAmazonOrderItemCode(String value) {
                    this.amazonOrderItemCode = value;
                }

                /**
                 * Gets the value of the merchantOrderItemID property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getMerchantOrderItemID() {
                    return merchantOrderItemID;
                }

                /**
                 * Sets the value of the merchantOrderItemID property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setMerchantOrderItemID(String value) {
                    this.merchantOrderItemID = value;
                }

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
                 * Gets the value of the quantity property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getQuantity() {
                    return quantity;
                }

                /**
                 * Sets the value of the quantity property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setQuantity(BigInteger value) {
                    this.quantity = value;
                }

                /**
                 * Gets the value of the itemPrice property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BuyerPrice }
                 *     
                 */
                public BuyerPrice getItemPrice() {
                    return itemPrice;
                }

                /**
                 * Sets the value of the itemPrice property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BuyerPrice }
                 *     
                 */
                public void setItemPrice(BuyerPrice value) {
                    this.itemPrice = value;
                }

                /**
                 * Gets the value of the itemFees property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link AmazonFees }
                 *     
                 */
                public AmazonFees getItemFees() {
                    return itemFees;
                }

                /**
                 * Sets the value of the itemFees property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link AmazonFees }
                 *     
                 */
                public void setItemFees(AmazonFees value) {
                    this.itemFees = value;
                }

                /**
                 * Gets the value of the promotion property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the promotion property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getPromotion().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link SettlementReport.Order.Fulfillment.Item.Promotion }
                 * 
                 * 
                 */
                public List<SettlementReport.Order.Fulfillment.Item.Promotion> getPromotion() {
                    if (promotion == null) {
                        promotion = new ArrayList<SettlementReport.Order.Fulfillment.Item.Promotion>();
                    }
                    return this.promotion;
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
                 *         &lt;element ref="{}MerchantPromotionID"/>
                 *         &lt;element name="Type" type="{}StringNotNull"/>
                 *         &lt;element name="Amount" type="{}CurrencyAmount"/>
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
                    "merchantPromotionID",
                    "type",
                    "amount"
                })
                public static class Promotion {

                    @XmlElement(name = "MerchantPromotionID", required = true)
                    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
                    protected String merchantPromotionID;
                    @XmlElement(name = "Type", required = true)
                    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
                    protected String type;
                    @XmlElement(name = "Amount", required = true)
                    protected CurrencyAmount amount;

                    /**
                     * Gets the value of the merchantPromotionID property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getMerchantPromotionID() {
                        return merchantPromotionID;
                    }

                    /**
                     * Sets the value of the merchantPromotionID property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setMerchantPromotionID(String value) {
                        this.merchantPromotionID = value;
                    }

                    /**
                     * Gets the value of the type property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getType() {
                        return type;
                    }

                    /**
                     * Sets the value of the type property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setType(String value) {
                        this.type = value;
                    }

                    /**
                     * Gets the value of the amount property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link CurrencyAmount }
                     *     
                     */
                    public CurrencyAmount getAmount() {
                        return amount;
                    }

                    /**
                     * Sets the value of the amount property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link CurrencyAmount }
                     *     
                     */
                    public void setAmount(CurrencyAmount value) {
                        this.amount = value;
                    }

                }

            }

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
     *         &lt;element name="AmazonOrderID" type="{}AmazonAlphaOrderID" minOccurs="0"/>
     *         &lt;element ref="{}MerchantOrderID" minOccurs="0"/>
     *         &lt;element ref="{}MarketplaceName"/>
     *         &lt;element ref="{}MerchantFulfillmentID" minOccurs="0"/>
     *         &lt;element name="PostedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *         &lt;element name="Amount" type="{}CurrencyAmount"/>
     *         &lt;element name="ReasonDescription" type="{}StringNotNull"/>
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
        "amazonOrderID",
        "merchantOrderID",
        "marketplaceName",
        "merchantFulfillmentID",
        "postedDate",
        "amount",
        "reasonDescription"
    })
    public static class OtherFee {

        @XmlElement(name = "AmazonOrderID")
        protected String amazonOrderID;
        @XmlElement(name = "MerchantOrderID")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String merchantOrderID;
        @XmlElement(name = "MarketplaceName", required = true)
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String marketplaceName;
        @XmlElement(name = "MerchantFulfillmentID")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String merchantFulfillmentID;
        @XmlElement(name = "PostedDate", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar postedDate;
        @XmlElement(name = "Amount", required = true)
        protected CurrencyAmount amount;
        @XmlElement(name = "ReasonDescription", required = true)
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String reasonDescription;

        /**
         * Gets the value of the amazonOrderID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getAmazonOrderID() {
            return amazonOrderID;
        }

        /**
         * Sets the value of the amazonOrderID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAmazonOrderID(String value) {
            this.amazonOrderID = value;
        }

        /**
         * Gets the value of the merchantOrderID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMerchantOrderID() {
            return merchantOrderID;
        }

        /**
         * Sets the value of the merchantOrderID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMerchantOrderID(String value) {
            this.merchantOrderID = value;
        }

        /**
         * Gets the value of the marketplaceName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMarketplaceName() {
            return marketplaceName;
        }

        /**
         * Sets the value of the marketplaceName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMarketplaceName(String value) {
            this.marketplaceName = value;
        }

        /**
         * Gets the value of the merchantFulfillmentID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMerchantFulfillmentID() {
            return merchantFulfillmentID;
        }

        /**
         * Sets the value of the merchantFulfillmentID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMerchantFulfillmentID(String value) {
            this.merchantFulfillmentID = value;
        }

        /**
         * Gets the value of the postedDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getPostedDate() {
            return postedDate;
        }

        /**
         * Sets the value of the postedDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setPostedDate(XMLGregorianCalendar value) {
            this.postedDate = value;
        }

        /**
         * Gets the value of the amount property.
         * 
         * @return
         *     possible object is
         *     {@link CurrencyAmount }
         *     
         */
        public CurrencyAmount getAmount() {
            return amount;
        }

        /**
         * Sets the value of the amount property.
         * 
         * @param value
         *     allowed object is
         *     {@link CurrencyAmount }
         *     
         */
        public void setAmount(CurrencyAmount value) {
            this.amount = value;
        }

        /**
         * Gets the value of the reasonDescription property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getReasonDescription() {
            return reasonDescription;
        }

        /**
         * Sets the value of the reasonDescription property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setReasonDescription(String value) {
            this.reasonDescription = value;
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
     *         &lt;element name="AmazonOrderID" type="{}AmazonAlphaOrderID" minOccurs="0"/>
     *         &lt;element ref="{}MerchantOrderID" minOccurs="0"/>
     *         &lt;element ref="{}MarketplaceName" minOccurs="0"/>
     *         &lt;element ref="{}MerchantFulfillmentID" minOccurs="0"/>
     *         &lt;element name="TransactionType" type="{}StringNotNull"/>
     *         &lt;element name="TransactionID" type="{}StringNotNull" minOccurs="0"/>
     *         &lt;element name="PostedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *         &lt;element name="Amount" type="{}CurrencyAmount"/>
     *         &lt;element name="ReasonDescription" type="{}StringNotNull" minOccurs="0"/>
     *         &lt;element name="Fees" type="{}AmazonFees" minOccurs="0"/>
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
        "amazonOrderID",
        "merchantOrderID",
        "marketplaceName",
        "merchantFulfillmentID",
        "transactionType",
        "transactionID",
        "postedDate",
        "amount",
        "reasonDescription",
        "fees"
    })
    public static class OtherTransaction {

        @XmlElement(name = "AmazonOrderID")
        protected String amazonOrderID;
        @XmlElement(name = "MerchantOrderID")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String merchantOrderID;
        @XmlElement(name = "MarketplaceName")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String marketplaceName;
        @XmlElement(name = "MerchantFulfillmentID")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String merchantFulfillmentID;
        @XmlElement(name = "TransactionType", required = true)
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String transactionType;
        @XmlElement(name = "TransactionID")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String transactionID;
        @XmlElement(name = "PostedDate", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar postedDate;
        @XmlElement(name = "Amount", required = true)
        protected CurrencyAmount amount;
        @XmlElement(name = "ReasonDescription")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String reasonDescription;
        @XmlElement(name = "Fees")
        protected AmazonFees fees;

        /**
         * Gets the value of the amazonOrderID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getAmazonOrderID() {
            return amazonOrderID;
        }

        /**
         * Sets the value of the amazonOrderID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAmazonOrderID(String value) {
            this.amazonOrderID = value;
        }

        /**
         * Gets the value of the merchantOrderID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMerchantOrderID() {
            return merchantOrderID;
        }

        /**
         * Sets the value of the merchantOrderID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMerchantOrderID(String value) {
            this.merchantOrderID = value;
        }

        /**
         * Gets the value of the marketplaceName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMarketplaceName() {
            return marketplaceName;
        }

        /**
         * Sets the value of the marketplaceName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMarketplaceName(String value) {
            this.marketplaceName = value;
        }

        /**
         * Gets the value of the merchantFulfillmentID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMerchantFulfillmentID() {
            return merchantFulfillmentID;
        }

        /**
         * Sets the value of the merchantFulfillmentID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMerchantFulfillmentID(String value) {
            this.merchantFulfillmentID = value;
        }

        /**
         * Gets the value of the transactionType property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTransactionType() {
            return transactionType;
        }

        /**
         * Sets the value of the transactionType property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTransactionType(String value) {
            this.transactionType = value;
        }

        /**
         * Gets the value of the transactionID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTransactionID() {
            return transactionID;
        }

        /**
         * Sets the value of the transactionID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTransactionID(String value) {
            this.transactionID = value;
        }

        /**
         * Gets the value of the postedDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getPostedDate() {
            return postedDate;
        }

        /**
         * Sets the value of the postedDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setPostedDate(XMLGregorianCalendar value) {
            this.postedDate = value;
        }

        /**
         * Gets the value of the amount property.
         * 
         * @return
         *     possible object is
         *     {@link CurrencyAmount }
         *     
         */
        public CurrencyAmount getAmount() {
            return amount;
        }

        /**
         * Sets the value of the amount property.
         * 
         * @param value
         *     allowed object is
         *     {@link CurrencyAmount }
         *     
         */
        public void setAmount(CurrencyAmount value) {
            this.amount = value;
        }

        /**
         * Gets the value of the reasonDescription property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getReasonDescription() {
            return reasonDescription;
        }

        /**
         * Sets the value of the reasonDescription property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setReasonDescription(String value) {
            this.reasonDescription = value;
        }

        /**
         * Gets the value of the fees property.
         * 
         * @return
         *     possible object is
         *     {@link AmazonFees }
         *     
         */
        public AmazonFees getFees() {
            return fees;
        }

        /**
         * Sets the value of the fees property.
         * 
         * @param value
         *     allowed object is
         *     {@link AmazonFees }
         *     
         */
        public void setFees(AmazonFees value) {
            this.fees = value;
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
     *         &lt;element name="AmazonSettlementID" type="{}IDNumber"/>
     *         &lt;element name="TotalAmount" type="{}CurrencyAmount"/>
     *         &lt;element name="StartDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *         &lt;element name="EndDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *         &lt;element name="DepositDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
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
        "amazonSettlementID",
        "totalAmount",
        "startDate",
        "endDate",
        "depositDate"
    })
    public static class SettlementData {

        @XmlElement(name = "AmazonSettlementID", required = true)
        protected BigInteger amazonSettlementID;
        @XmlElement(name = "TotalAmount", required = true)
        protected CurrencyAmount totalAmount;
        @XmlElement(name = "StartDate", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar startDate;
        @XmlElement(name = "EndDate", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar endDate;
        @XmlElement(name = "DepositDate", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar depositDate;

        /**
         * Gets the value of the amazonSettlementID property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getAmazonSettlementID() {
            return amazonSettlementID;
        }

        /**
         * Sets the value of the amazonSettlementID property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setAmazonSettlementID(BigInteger value) {
            this.amazonSettlementID = value;
        }

        /**
         * Gets the value of the totalAmount property.
         * 
         * @return
         *     possible object is
         *     {@link CurrencyAmount }
         *     
         */
        public CurrencyAmount getTotalAmount() {
            return totalAmount;
        }

        /**
         * Sets the value of the totalAmount property.
         * 
         * @param value
         *     allowed object is
         *     {@link CurrencyAmount }
         *     
         */
        public void setTotalAmount(CurrencyAmount value) {
            this.totalAmount = value;
        }

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
         * Gets the value of the depositDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getDepositDate() {
            return depositDate;
        }

        /**
         * Sets the value of the depositDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setDepositDate(XMLGregorianCalendar value) {
            this.depositDate = value;
        }

    }

}
