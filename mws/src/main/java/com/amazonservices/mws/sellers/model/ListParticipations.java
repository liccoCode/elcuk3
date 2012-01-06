
package com.amazonservices.mws.sellers.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ListParticipations complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ListParticipations">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Participation" type="{https://mws.amazonservices.com/Sellers/2011-07-01}Participation" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * Generated by AWS Code Generator
 * <p/>
 * Fri Jun 24 20:08:14 GMT 2011
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListParticipations", propOrder = {
    "participation"
})
public class ListParticipations {

    @XmlElement(name = "Participation", required = true)
    protected List<Participation> participation;

    /**
     * Default constructor
     * 
     */
    public ListParticipations() {
        super();
    }

    /**
     * Value constructor
     * 
     */
    public ListParticipations(final List<Participation> participation) {
        this.participation = participation;
    }

    /**
     * Gets the value of the participation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the participation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParticipation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Participation }
     * 
     * 
     */
    public List<Participation> getParticipation() {
        if (participation == null) {
            participation = new ArrayList<Participation>();
        }
        return this.participation;
    }

    public boolean isSetParticipation() {
        return ((this.participation!= null)&&(!this.participation.isEmpty()));
    }

    public void unsetParticipation() {
        this.participation = null;
    }

    /**
     * Sets the value of the Participation property.
     * 
     * @param values
     * @return
     *     this instance
     */
    public ListParticipations withParticipation(Participation... values) {
        for (Participation value: values) {
            getParticipation().add(value);
        }
        return this;
    }

    /**
     * Sets the value of the participation property.
     * 
     * @param participation
     *     allowed object is
     *     {@link Participation }
     *     
     */
    public void setParticipation(List<Participation> participation) {
        this.participation = participation;
    }
    

    /**
     * 
     * XML fragment representation of this object
     * 
     * @return XML fragment for this object. Name for outer
     * tag expected to be set by calling method. This fragment
     * returns inner properties representation only
     */
    protected String toXMLFragment() {
        StringBuffer xml = new StringBuffer();
        java.util.List<Participation> participationList = getParticipation();
        for (Participation participation : participationList) {
            xml.append("<Participation>");
            xml.append(participation.toXMLFragment());
            xml.append("</Participation>");
        }
        return xml.toString();
    }

    /**
     * 
     * Escape XML special characters
     */
    @SuppressWarnings("unused")
	private String escapeXML(String string) {
        StringBuffer sb = new StringBuffer();
        int length = string.length();
        for (int i = 0; i < length; ++i) {
            char c = string.charAt(i);
            switch (c) {
            case '&':
                sb.append("&amp;");
                break;
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            case '\'':
                sb.append("&#039;");
                break;
            case '"':
                sb.append("&quot;");
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }


    /**
     *
     * JSON fragment representation of this object
     *
     * @return JSON fragment for this object. Name for outer
     * object expected to be set by calling method. This fragment
     * returns inner properties representation only
     *
     */
    protected String toJSONFragment() {
        StringBuffer json = new StringBuffer();
        boolean first = true;
        if (isSetParticipation()) {
            if (!first) json.append(", ");
            json.append("\"Participation\" : [");
            java.util.List<Participation> participationList = getParticipation();
            int participationListIndex = 0;
            for (Participation participation : participationList) {
                if (participationListIndex > 0) json.append(", ");
                json.append("{");
                json.append("");
                json.append(participation.toJSONFragment());
                json.append("}");
                first = false;
                ++participationListIndex;
            }
            json.append("]");
        }
        return json.toString();
    }

    /**
     *
     * Quote JSON string
     */
    @SuppressWarnings("unused")
	private String quoteJSON(String string) {
        StringBuffer sb = new StringBuffer();
        sb.append("\"");
        int length = string.length();
        for (int i = 0; i < length; ++i) {
            char c = string.charAt(i);
            switch (c) {
            case '"':
                sb.append("\\\"");
                break;
            case '\\':
                sb.append("\\\\");
                break;
            case '/':
                sb.append("\\/");
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\t':
                sb.append("\\t");
                break;
            default:
                if (c <  ' ') {
                    sb.append("\\u" + String.format("%03x", Integer.valueOf(c)));
                } else {
                sb.append(c);
            }
        }
        }
        sb.append("\"");
        return sb.toString();
    }


}
