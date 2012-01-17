
package com.elcuk.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
 *         &lt;element name="ProductType">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element ref="{}Software"/>
 *                   &lt;element ref="{}HandheldSoftwareDownloads"/>
 *                   &lt;element ref="{}SoftwareGames"/>
 *                   &lt;element ref="{}VideoGames"/>
 *                   &lt;element ref="{}VideoGamesAccessories"/>
 *                   &lt;element ref="{}VideoGamesHardware"/>
 *                 &lt;/choice>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="IsAdultProduct" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
    "productType",
    "isAdultProduct"
})
@XmlRootElement(name = "SoftwareVideoGames")
public class SoftwareVideoGames {

    @XmlElement(name = "ProductType", required = true)
    protected SoftwareVideoGames.ProductType productType;
    @XmlElement(name = "IsAdultProduct")
    protected Boolean isAdultProduct;

    /**
     * Gets the value of the productType property.
     * 
     * @return
     *     possible object is
     *     {@link SoftwareVideoGames.ProductType }
     *     
     */
    public SoftwareVideoGames.ProductType getProductType() {
        return productType;
    }

    /**
     * Sets the value of the productType property.
     * 
     * @param value
     *     allowed object is
     *     {@link SoftwareVideoGames.ProductType }
     *     
     */
    public void setProductType(SoftwareVideoGames.ProductType value) {
        this.productType = value;
    }

    /**
     * Gets the value of the isAdultProduct property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsAdultProduct() {
        return isAdultProduct;
    }

    /**
     * Sets the value of the isAdultProduct property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsAdultProduct(Boolean value) {
        this.isAdultProduct = value;
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
     *         &lt;element ref="{}Software"/>
     *         &lt;element ref="{}HandheldSoftwareDownloads"/>
     *         &lt;element ref="{}SoftwareGames"/>
     *         &lt;element ref="{}VideoGames"/>
     *         &lt;element ref="{}VideoGamesAccessories"/>
     *         &lt;element ref="{}VideoGamesHardware"/>
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
        "software",
        "handheldSoftwareDownloads",
        "softwareGames",
        "videoGames",
        "videoGamesAccessories",
        "videoGamesHardware"
    })
    public static class ProductType {

        @XmlElement(name = "Software")
        protected Software software;
        @XmlElement(name = "HandheldSoftwareDownloads")
        protected HandheldSoftwareDownloads handheldSoftwareDownloads;
        @XmlElement(name = "SoftwareGames")
        protected SoftwareGames softwareGames;
        @XmlElement(name = "VideoGames")
        protected VideoGames videoGames;
        @XmlElement(name = "VideoGamesAccessories")
        protected VideoGamesAccessories videoGamesAccessories;
        @XmlElement(name = "VideoGamesHardware")
        protected VideoGamesHardware videoGamesHardware;

        /**
         * Gets the value of the software property.
         * 
         * @return
         *     possible object is
         *     {@link Software }
         *     
         */
        public Software getSoftware() {
            return software;
        }

        /**
         * Sets the value of the software property.
         * 
         * @param value
         *     allowed object is
         *     {@link Software }
         *     
         */
        public void setSoftware(Software value) {
            this.software = value;
        }

        /**
         * Gets the value of the handheldSoftwareDownloads property.
         * 
         * @return
         *     possible object is
         *     {@link HandheldSoftwareDownloads }
         *     
         */
        public HandheldSoftwareDownloads getHandheldSoftwareDownloads() {
            return handheldSoftwareDownloads;
        }

        /**
         * Sets the value of the handheldSoftwareDownloads property.
         * 
         * @param value
         *     allowed object is
         *     {@link HandheldSoftwareDownloads }
         *     
         */
        public void setHandheldSoftwareDownloads(HandheldSoftwareDownloads value) {
            this.handheldSoftwareDownloads = value;
        }

        /**
         * Gets the value of the softwareGames property.
         * 
         * @return
         *     possible object is
         *     {@link SoftwareGames }
         *     
         */
        public SoftwareGames getSoftwareGames() {
            return softwareGames;
        }

        /**
         * Sets the value of the softwareGames property.
         * 
         * @param value
         *     allowed object is
         *     {@link SoftwareGames }
         *     
         */
        public void setSoftwareGames(SoftwareGames value) {
            this.softwareGames = value;
        }

        /**
         * Gets the value of the videoGames property.
         * 
         * @return
         *     possible object is
         *     {@link VideoGames }
         *     
         */
        public VideoGames getVideoGames() {
            return videoGames;
        }

        /**
         * Sets the value of the videoGames property.
         * 
         * @param value
         *     allowed object is
         *     {@link VideoGames }
         *     
         */
        public void setVideoGames(VideoGames value) {
            this.videoGames = value;
        }

        /**
         * Gets the value of the videoGamesAccessories property.
         * 
         * @return
         *     possible object is
         *     {@link VideoGamesAccessories }
         *     
         */
        public VideoGamesAccessories getVideoGamesAccessories() {
            return videoGamesAccessories;
        }

        /**
         * Sets the value of the videoGamesAccessories property.
         * 
         * @param value
         *     allowed object is
         *     {@link VideoGamesAccessories }
         *     
         */
        public void setVideoGamesAccessories(VideoGamesAccessories value) {
            this.videoGamesAccessories = value;
        }

        /**
         * Gets the value of the videoGamesHardware property.
         * 
         * @return
         *     possible object is
         *     {@link VideoGamesHardware }
         *     
         */
        public VideoGamesHardware getVideoGamesHardware() {
            return videoGamesHardware;
        }

        /**
         * Sets the value of the videoGamesHardware property.
         * 
         * @param value
         *     allowed object is
         *     {@link VideoGamesHardware }
         *     
         */
        public void setVideoGamesHardware(VideoGamesHardware value) {
            this.videoGamesHardware = value;
        }

    }

}
