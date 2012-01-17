
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
 *         &lt;element name="DocumentTransactionID" type="{}IDNumber"/>
 *         &lt;element name="StatusCode">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Complete"/>
 *               &lt;enumeration value="Processing"/>
 *               &lt;enumeration value="Rejected"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="ProcessingSummary" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="MessagesProcessed" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *                   &lt;element name="MessagesSuccessful" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *                   &lt;element name="MessagesWithError" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *                   &lt;element name="MessagesWithWarning" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Result" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="MessageID">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger">
 *                         &lt;pattern value="\d{1,20}"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="ResultCode">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="Error"/>
 *                         &lt;enumeration value="Warning"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="ResultMessageCode" type="{}IDNumber"/>
 *                   &lt;element name="ResultDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="AdditionalInfo" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}SKU" minOccurs="0"/>
 *                             &lt;element ref="{}FulfillmentCenterID" minOccurs="0"/>
 *                             &lt;element ref="{}AmazonOrderID" minOccurs="0"/>
 *                             &lt;element ref="{}AmazonOrderItemCode" minOccurs="0"/>
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
    "documentTransactionID",
    "statusCode",
    "processingSummary",
    "result"
})
@XmlRootElement(name = "ProcessingReport")
public class ProcessingReport {

    @XmlElement(name = "DocumentTransactionID", required = true)
    protected BigInteger documentTransactionID;
    @XmlElement(name = "StatusCode", required = true)
    protected String statusCode;
    @XmlElement(name = "ProcessingSummary")
    protected ProcessingReport.ProcessingSummary processingSummary;
    @XmlElement(name = "Result")
    protected List<ProcessingReport.Result> result;

    /**
     * Gets the value of the documentTransactionID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDocumentTransactionID() {
        return documentTransactionID;
    }

    /**
     * Sets the value of the documentTransactionID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDocumentTransactionID(BigInteger value) {
        this.documentTransactionID = value;
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
     * Gets the value of the processingSummary property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessingReport.ProcessingSummary }
     *     
     */
    public ProcessingReport.ProcessingSummary getProcessingSummary() {
        return processingSummary;
    }

    /**
     * Sets the value of the processingSummary property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessingReport.ProcessingSummary }
     *     
     */
    public void setProcessingSummary(ProcessingReport.ProcessingSummary value) {
        this.processingSummary = value;
    }

    /**
     * Gets the value of the result property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the result property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProcessingReport.Result }
     * 
     * 
     */
    public List<ProcessingReport.Result> getResult() {
        if (result == null) {
            result = new ArrayList<ProcessingReport.Result>();
        }
        return this.result;
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
     *         &lt;element name="MessagesProcessed" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
     *         &lt;element name="MessagesSuccessful" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
     *         &lt;element name="MessagesWithError" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
     *         &lt;element name="MessagesWithWarning" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
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
        "messagesProcessed",
        "messagesSuccessful",
        "messagesWithError",
        "messagesWithWarning"
    })
    public static class ProcessingSummary {

        @XmlElement(name = "MessagesProcessed", required = true)
        @XmlSchemaType(name = "nonNegativeInteger")
        protected BigInteger messagesProcessed;
        @XmlElement(name = "MessagesSuccessful", required = true)
        @XmlSchemaType(name = "nonNegativeInteger")
        protected BigInteger messagesSuccessful;
        @XmlElement(name = "MessagesWithError", required = true)
        @XmlSchemaType(name = "nonNegativeInteger")
        protected BigInteger messagesWithError;
        @XmlElement(name = "MessagesWithWarning", required = true)
        @XmlSchemaType(name = "nonNegativeInteger")
        protected BigInteger messagesWithWarning;

        /**
         * Gets the value of the messagesProcessed property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getMessagesProcessed() {
            return messagesProcessed;
        }

        /**
         * Sets the value of the messagesProcessed property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setMessagesProcessed(BigInteger value) {
            this.messagesProcessed = value;
        }

        /**
         * Gets the value of the messagesSuccessful property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getMessagesSuccessful() {
            return messagesSuccessful;
        }

        /**
         * Sets the value of the messagesSuccessful property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setMessagesSuccessful(BigInteger value) {
            this.messagesSuccessful = value;
        }

        /**
         * Gets the value of the messagesWithError property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getMessagesWithError() {
            return messagesWithError;
        }

        /**
         * Sets the value of the messagesWithError property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setMessagesWithError(BigInteger value) {
            this.messagesWithError = value;
        }

        /**
         * Gets the value of the messagesWithWarning property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getMessagesWithWarning() {
            return messagesWithWarning;
        }

        /**
         * Sets the value of the messagesWithWarning property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setMessagesWithWarning(BigInteger value) {
            this.messagesWithWarning = value;
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
     *         &lt;element name="MessageID">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger">
     *               &lt;pattern value="\d{1,20}"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="ResultCode">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="Error"/>
     *               &lt;enumeration value="Warning"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="ResultMessageCode" type="{}IDNumber"/>
     *         &lt;element name="ResultDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="AdditionalInfo" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}SKU" minOccurs="0"/>
     *                   &lt;element ref="{}FulfillmentCenterID" minOccurs="0"/>
     *                   &lt;element ref="{}AmazonOrderID" minOccurs="0"/>
     *                   &lt;element ref="{}AmazonOrderItemCode" minOccurs="0"/>
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
        "messageID",
        "resultCode",
        "resultMessageCode",
        "resultDescription",
        "additionalInfo"
    })
    public static class Result {

        @XmlElement(name = "MessageID", required = true)
        protected BigInteger messageID;
        @XmlElement(name = "ResultCode", required = true)
        protected String resultCode;
        @XmlElement(name = "ResultMessageCode", required = true)
        protected BigInteger resultMessageCode;
        @XmlElement(name = "ResultDescription", required = true)
        protected String resultDescription;
        @XmlElement(name = "AdditionalInfo")
        protected ProcessingReport.Result.AdditionalInfo additionalInfo;

        /**
         * Gets the value of the messageID property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getMessageID() {
            return messageID;
        }

        /**
         * Sets the value of the messageID property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setMessageID(BigInteger value) {
            this.messageID = value;
        }

        /**
         * Gets the value of the resultCode property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getResultCode() {
            return resultCode;
        }

        /**
         * Sets the value of the resultCode property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setResultCode(String value) {
            this.resultCode = value;
        }

        /**
         * Gets the value of the resultMessageCode property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getResultMessageCode() {
            return resultMessageCode;
        }

        /**
         * Sets the value of the resultMessageCode property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setResultMessageCode(BigInteger value) {
            this.resultMessageCode = value;
        }

        /**
         * Gets the value of the resultDescription property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getResultDescription() {
            return resultDescription;
        }

        /**
         * Sets the value of the resultDescription property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setResultDescription(String value) {
            this.resultDescription = value;
        }

        /**
         * Gets the value of the additionalInfo property.
         * 
         * @return
         *     possible object is
         *     {@link ProcessingReport.Result.AdditionalInfo }
         *     
         */
        public ProcessingReport.Result.AdditionalInfo getAdditionalInfo() {
            return additionalInfo;
        }

        /**
         * Sets the value of the additionalInfo property.
         * 
         * @param value
         *     allowed object is
         *     {@link ProcessingReport.Result.AdditionalInfo }
         *     
         */
        public void setAdditionalInfo(ProcessingReport.Result.AdditionalInfo value) {
            this.additionalInfo = value;
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
         *         &lt;element ref="{}SKU" minOccurs="0"/>
         *         &lt;element ref="{}FulfillmentCenterID" minOccurs="0"/>
         *         &lt;element ref="{}AmazonOrderID" minOccurs="0"/>
         *         &lt;element ref="{}AmazonOrderItemCode" minOccurs="0"/>
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
            "fulfillmentCenterID",
            "amazonOrderID",
            "amazonOrderItemCode"
        })
        public static class AdditionalInfo {

            @XmlElement(name = "SKU")
            @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
            protected String sku;
            @XmlElement(name = "FulfillmentCenterID")
            @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
            protected String fulfillmentCenterID;
            @XmlElement(name = "AmazonOrderID")
            protected String amazonOrderID;
            @XmlElement(name = "AmazonOrderItemCode")
            protected String amazonOrderItemCode;

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
             * Gets the value of the fulfillmentCenterID property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getFulfillmentCenterID() {
                return fulfillmentCenterID;
            }

            /**
             * Sets the value of the fulfillmentCenterID property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFulfillmentCenterID(String value) {
                this.fulfillmentCenterID = value;
            }

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

        }

    }

}
