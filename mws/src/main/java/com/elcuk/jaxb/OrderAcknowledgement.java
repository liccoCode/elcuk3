
package com.elcuk.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
 *         &lt;element ref="{}AmazonOrderID"/>
 *         &lt;element ref="{}MerchantOrderID" minOccurs="0"/>
 *         &lt;element name="StatusCode">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Success"/>
 *               &lt;enumeration value="Failure"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Item" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{}AmazonOrderItemCode"/>
 *                   &lt;element ref="{}MerchantOrderItemID" minOccurs="0"/>
 *                   &lt;element name="CancelReason" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="NoInventory"/>
 *                         &lt;enumeration value="ShippingAddressUndeliverable"/>
 *                         &lt;enumeration value="CustomerExchange"/>
 *                         &lt;enumeration value="BuyerCanceled"/>
 *                         &lt;enumeration value="GeneralAdjustment"/>
 *                         &lt;enumeration value="CarrierCreditDecision"/>
 *                         &lt;enumeration value="RiskAssessmentInformationNotValid"/>
 *                         &lt;enumeration value="CarrierCoverageFailure"/>
 *                         &lt;enumeration value="CustomerReturn"/>
 *                         &lt;enumeration value="MerchandiseNotReceived"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
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
    "statusCode",
    "item"
})
@XmlRootElement(name = "OrderAcknowledgement")
public class OrderAcknowledgement {

    @XmlElement(name = "AmazonOrderID", required = true)
    protected String amazonOrderID;
    @XmlElement(name = "MerchantOrderID")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String merchantOrderID;
    @XmlElement(name = "StatusCode", required = true)
    protected String statusCode;
    @XmlElement(name = "Item")
    protected List<OrderAcknowledgement.Item> item;

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
     * Gets the value of the statusCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the value of the statusCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatusCode(String value) {
        this.statusCode = value;
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
     * {@link OrderAcknowledgement.Item }
     * 
     * 
     */
    public List<OrderAcknowledgement.Item> getItem() {
        if (item == null) {
            item = new ArrayList<OrderAcknowledgement.Item>();
        }
        return this.item;
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
     *         &lt;element name="CancelReason" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="NoInventory"/>
     *               &lt;enumeration value="ShippingAddressUndeliverable"/>
     *               &lt;enumeration value="CustomerExchange"/>
     *               &lt;enumeration value="BuyerCanceled"/>
     *               &lt;enumeration value="GeneralAdjustment"/>
     *               &lt;enumeration value="CarrierCreditDecision"/>
     *               &lt;enumeration value="RiskAssessmentInformationNotValid"/>
     *               &lt;enumeration value="CarrierCoverageFailure"/>
     *               &lt;enumeration value="CustomerReturn"/>
     *               &lt;enumeration value="MerchandiseNotReceived"/>
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
        "amazonOrderItemCode",
        "merchantOrderItemID",
        "cancelReason"
    })
    public static class Item {

        @XmlElement(name = "AmazonOrderItemCode", required = true)
        protected String amazonOrderItemCode;
        @XmlElement(name = "MerchantOrderItemID")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String merchantOrderItemID;
        @XmlElement(name = "CancelReason")
        protected String cancelReason;

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
         * Gets the value of the cancelReason property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCancelReason() {
            return cancelReason;
        }

        /**
         * Sets the value of the cancelReason property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCancelReason(String value) {
            this.cancelReason = value;
        }

    }

}
