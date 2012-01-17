
package com.elcuk.jaxb;

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
 *         &lt;element name="DownloadableFileFormat" type="{}FortyStringNotNull"/>
 *         &lt;element name="FileSize" type="{}MemorySizeDimension"/>
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
    "downloadableFileFormat",
    "fileSize"
})
@XmlRootElement(name = "DownloadableFile")
public class DownloadableFile {

    @XmlElement(name = "DownloadableFileFormat", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String downloadableFileFormat;
    @XmlElement(name = "FileSize", required = true)
    protected MemorySizeDimension fileSize;

    /**
     * Gets the value of the downloadableFileFormat property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDownloadableFileFormat() {
        return downloadableFileFormat;
    }

    /**
     * Sets the value of the downloadableFileFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDownloadableFileFormat(String value) {
        this.downloadableFileFormat = value;
    }

    /**
     * Gets the value of the fileSize property.
     * 
     * @return
     *     possible object is
     *     {@link MemorySizeDimension }
     *     
     */
    public MemorySizeDimension getFileSize() {
        return fileSize;
    }

    /**
     * Sets the value of the fileSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link MemorySizeDimension }
     *     
     */
    public void setFileSize(MemorySizeDimension value) {
        this.fileSize = value;
    }

}
