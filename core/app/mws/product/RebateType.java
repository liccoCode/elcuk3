
package mws.product;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for RebateType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RebateType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RebateStartDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="RebateEndDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="RebateMessage" type="{}TwoFiftyStringNotNull"/>
 *         &lt;element name="RebateName" type="{}FortyStringNotNull"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RebateType", propOrder = {
    "rebateStartDate",
    "rebateEndDate",
    "rebateMessage",
    "rebateName"
})
public class RebateType {

    @XmlElement(name = "RebateStartDate", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar rebateStartDate;
    @XmlElement(name = "RebateEndDate", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar rebateEndDate;
    @XmlElement(name = "RebateMessage", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String rebateMessage;
    @XmlElement(name = "RebateName", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String rebateName;

    /**
     * Gets the value of the rebateStartDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getRebateStartDate() {
        return rebateStartDate;
    }

    /**
     * Sets the value of the rebateStartDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setRebateStartDate(XMLGregorianCalendar value) {
        this.rebateStartDate = value;
    }

    /**
     * Gets the value of the rebateEndDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getRebateEndDate() {
        return rebateEndDate;
    }

    /**
     * Sets the value of the rebateEndDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setRebateEndDate(XMLGregorianCalendar value) {
        this.rebateEndDate = value;
    }

    /**
     * Gets the value of the rebateMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRebateMessage() {
        return rebateMessage;
    }

    /**
     * Sets the value of the rebateMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRebateMessage(String value) {
        this.rebateMessage = value;
    }

    /**
     * Gets the value of the rebateName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRebateName() {
        return rebateName;
    }

    /**
     * Sets the value of the rebateName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRebateName(String value) {
        this.rebateName = value;
    }

}
