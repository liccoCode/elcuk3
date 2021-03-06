
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
 *         &lt;choice>
 *           &lt;element ref="{}AmazonOrderID"/>
 *           &lt;element ref="{}MerchantOrderID"/>
 *         &lt;/choice>
 *         &lt;element name="AdjustedItem" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;choice>
 *                     &lt;element ref="{}AmazonOrderItemCode"/>
 *                     &lt;element ref="{}MerchantOrderItemID"/>
 *                   &lt;/choice>
 *                   &lt;element name="MerchantAdjustmentItemID" type="{}StringNotNull" minOccurs="0"/>
 *                   &lt;element name="AdjustmentReason">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="NoInventory"/>
 *                         &lt;enumeration value="CustomerReturn"/>
 *                         &lt;enumeration value="GeneralAdjustment"/>
 *                         &lt;enumeration value="CouldNotShip"/>
 *                         &lt;enumeration value="DifferentItem"/>
 *                         &lt;enumeration value="Abandoned"/>
 *                         &lt;enumeration value="CustomerCancel"/>
 *                         &lt;enumeration value="PriceError"/>
 *                         &lt;enumeration value="ProductOutofStock"/>
 *                         &lt;enumeration value="CustomerAddressIncorrect"/>
 *                         &lt;enumeration value="Exchange"/>
 *                         &lt;enumeration value="Other"/>
 *                         &lt;enumeration value="CarrierCreditDecision"/>
 *                         &lt;enumeration value="RiskAssessmentInformationNotValid"/>
 *                         &lt;enumeration value="CarrierCoverageFailure"/>
 *                         &lt;enumeration value="TransactionRecord"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="ItemPriceAdjustments" type="{}BuyerPrice"/>
 *                   &lt;element name="PromotionAdjustments" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}PromotionClaimCode" minOccurs="0"/>
 *                             &lt;element ref="{}MerchantPromotionID" minOccurs="0"/>
 *                             &lt;element name="Component" maxOccurs="unbounded">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="Type" type="{}PromotionApplicationType"/>
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
 *                   &lt;element name="DirectPaymentAdjustments" type="{}DirectPaymentType" minOccurs="0"/>
 *                   &lt;element name="QuantityCancelled" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
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
    "amazonOrderID",
    "merchantOrderID",
    "adjustedItem"
})
@XmlRootElement(name = "OrderAdjustment")
public class OrderAdjustment {

    @XmlElement(name = "AmazonOrderID")
    protected String amazonOrderID;
    @XmlElement(name = "MerchantOrderID")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String merchantOrderID;
    @XmlElement(name = "AdjustedItem", required = true)
    protected List<OrderAdjustment.AdjustedItem> adjustedItem;

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
     * {@link OrderAdjustment.AdjustedItem }
     * 
     * 
     */
    public List<OrderAdjustment.AdjustedItem> getAdjustedItem() {
        if (adjustedItem == null) {
            adjustedItem = new ArrayList<OrderAdjustment.AdjustedItem>();
        }
        return this.adjustedItem;
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
     *         &lt;choice>
     *           &lt;element ref="{}AmazonOrderItemCode"/>
     *           &lt;element ref="{}MerchantOrderItemID"/>
     *         &lt;/choice>
     *         &lt;element name="MerchantAdjustmentItemID" type="{}StringNotNull" minOccurs="0"/>
     *         &lt;element name="AdjustmentReason">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="NoInventory"/>
     *               &lt;enumeration value="CustomerReturn"/>
     *               &lt;enumeration value="GeneralAdjustment"/>
     *               &lt;enumeration value="CouldNotShip"/>
     *               &lt;enumeration value="DifferentItem"/>
     *               &lt;enumeration value="Abandoned"/>
     *               &lt;enumeration value="CustomerCancel"/>
     *               &lt;enumeration value="PriceError"/>
     *               &lt;enumeration value="ProductOutofStock"/>
     *               &lt;enumeration value="CustomerAddressIncorrect"/>
     *               &lt;enumeration value="Exchange"/>
     *               &lt;enumeration value="Other"/>
     *               &lt;enumeration value="CarrierCreditDecision"/>
     *               &lt;enumeration value="RiskAssessmentInformationNotValid"/>
     *               &lt;enumeration value="CarrierCoverageFailure"/>
     *               &lt;enumeration value="TransactionRecord"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="ItemPriceAdjustments" type="{}BuyerPrice"/>
     *         &lt;element name="PromotionAdjustments" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}PromotionClaimCode" minOccurs="0"/>
     *                   &lt;element ref="{}MerchantPromotionID" minOccurs="0"/>
     *                   &lt;element name="Component" maxOccurs="unbounded">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="Type" type="{}PromotionApplicationType"/>
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
     *         &lt;element name="DirectPaymentAdjustments" type="{}DirectPaymentType" minOccurs="0"/>
     *         &lt;element name="QuantityCancelled" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
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
        "adjustmentReason",
        "itemPriceAdjustments",
        "promotionAdjustments",
        "directPaymentAdjustments",
        "quantityCancelled"
    })
    public static class AdjustedItem {

        @XmlElement(name = "AmazonOrderItemCode")
        protected String amazonOrderItemCode;
        @XmlElement(name = "MerchantOrderItemID")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String merchantOrderItemID;
        @XmlElement(name = "MerchantAdjustmentItemID")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String merchantAdjustmentItemID;
        @XmlElement(name = "AdjustmentReason", required = true)
        protected String adjustmentReason;
        @XmlElement(name = "ItemPriceAdjustments", required = true)
        protected BuyerPrice itemPriceAdjustments;
        @XmlElement(name = "PromotionAdjustments")
        protected List<OrderAdjustment.AdjustedItem.PromotionAdjustments> promotionAdjustments;
        @XmlElement(name = "DirectPaymentAdjustments")
        protected DirectPaymentType directPaymentAdjustments;
        @XmlElement(name = "QuantityCancelled")
        @XmlSchemaType(name = "positiveInteger")
        protected BigInteger quantityCancelled;

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
         * Gets the value of the adjustmentReason property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getAdjustmentReason() {
            return adjustmentReason;
        }

        /**
         * Sets the value of the adjustmentReason property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAdjustmentReason(String value) {
            this.adjustmentReason = value;
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
         * Gets the value of the promotionAdjustments property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the promotionAdjustments property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getPromotionAdjustments().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link OrderAdjustment.AdjustedItem.PromotionAdjustments }
         * 
         * 
         */
        public List<OrderAdjustment.AdjustedItem.PromotionAdjustments> getPromotionAdjustments() {
            if (promotionAdjustments == null) {
                promotionAdjustments = new ArrayList<OrderAdjustment.AdjustedItem.PromotionAdjustments>();
            }
            return this.promotionAdjustments;
        }

        /**
         * Gets the value of the directPaymentAdjustments property.
         * 
         * @return
         *     possible object is
         *     {@link DirectPaymentType }
         *     
         */
        public DirectPaymentType getDirectPaymentAdjustments() {
            return directPaymentAdjustments;
        }

        /**
         * Sets the value of the directPaymentAdjustments property.
         * 
         * @param value
         *     allowed object is
         *     {@link DirectPaymentType }
         *     
         */
        public void setDirectPaymentAdjustments(DirectPaymentType value) {
            this.directPaymentAdjustments = value;
        }

        /**
         * Gets the value of the quantityCancelled property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getQuantityCancelled() {
            return quantityCancelled;
        }

        /**
         * Sets the value of the quantityCancelled property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setQuantityCancelled(BigInteger value) {
            this.quantityCancelled = value;
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
         *         &lt;element ref="{}PromotionClaimCode" minOccurs="0"/>
         *         &lt;element ref="{}MerchantPromotionID" minOccurs="0"/>
         *         &lt;element name="Component" maxOccurs="unbounded">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="Type" type="{}PromotionApplicationType"/>
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
            "promotionClaimCode",
            "merchantPromotionID",
            "component"
        })
        public static class PromotionAdjustments {

            @XmlElement(name = "PromotionClaimCode")
            protected String promotionClaimCode;
            @XmlElement(name = "MerchantPromotionID")
            @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
            protected String merchantPromotionID;
            @XmlElement(name = "Component", required = true)
            protected List<OrderAdjustment.AdjustedItem.PromotionAdjustments.Component> component;

            /**
             * Gets the value of the promotionClaimCode property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getPromotionClaimCode() {
                return promotionClaimCode;
            }

            /**
             * Sets the value of the promotionClaimCode property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setPromotionClaimCode(String value) {
                this.promotionClaimCode = value;
            }

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
             * Gets the value of the component property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the component property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getComponent().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link OrderAdjustment.AdjustedItem.PromotionAdjustments.Component }
             * 
             * 
             */
            public List<OrderAdjustment.AdjustedItem.PromotionAdjustments.Component> getComponent() {
                if (component == null) {
                    component = new ArrayList<OrderAdjustment.AdjustedItem.PromotionAdjustments.Component>();
                }
                return this.component;
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
             *         &lt;element name="Type" type="{}PromotionApplicationType"/>
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
                "type",
                "amount"
            })
            public static class Component {

                @XmlElement(name = "Type", required = true)
                protected PromotionApplicationType type;
                @XmlElement(name = "Amount", required = true)
                protected CurrencyAmount amount;

                /**
                 * Gets the value of the type property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link PromotionApplicationType }
                 *     
                 */
                public PromotionApplicationType getType() {
                    return type;
                }

                /**
                 * Sets the value of the type property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link PromotionApplicationType }
                 *     
                 */
                public void setType(PromotionApplicationType value) {
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
