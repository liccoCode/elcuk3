package com.amazonservices.mws.FulfillmentInventory._2010_10_01.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for InventorySupplyList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InventorySupplyList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="member" type="{http://mws.amazonaws.com/FulfillmentInventory/2010-10-01/}InventorySupply" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * Generated by AWS Code Generator
 * <p/>
 * Fri Oct 22 09:47:28 UTC 2010
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InventorySupplyList", propOrder = {
    "member"
})
public class InventorySupplyList {

    @XmlElement(required = true)
    protected List<InventorySupply> member;

    /**
     * Default constructor
     * 
     */
    public InventorySupplyList() {
        super();
    }

    /**
     * Value constructor
     * 
     */
    public InventorySupplyList(final List<InventorySupply> member) {
        this.member = member;
    }

    /**
     * Gets the value of the member property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the member property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMember().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.InventorySupply }
     * 
     * 
     */
    public List<InventorySupply> getMember() {
        if (member == null) {
            member = new ArrayList<InventorySupply>();
        }
        return this.member;
    }

    public boolean isSetMember() {
        return ((this.member!= null)&&(!this.member.isEmpty()));
    }

    public void unsetMember() {
        this.member = null;
    }

    /**
     * Sets the value of the Member property.
     * 
     * @param values
     * @return
     *     this instance
     */
    public InventorySupplyList withMember(InventorySupply... values) {
        for (InventorySupply value: values) {
            getMember().add(value);
        }
        return this;
    }

    /**
     * Sets the value of the member property.
     * 
     * @param member
     *     allowed object is
     *     {@link com.amazonservices.mws.FulfillmentInventory._2010_10_01.model.InventorySupply }
     *     
     */
    public void setMember(List<InventorySupply> member) {
        this.member = member;
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
        List<InventorySupply> memberList = getMember();
        for (InventorySupply member : memberList) {
            xml.append("<member>");
            xml.append(member.toXMLFragment());
            xml.append("</member>");
        }
        return xml.toString();
    }

    /**
     * 
     * Escape XML special characters
     */
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
        if (isSetMember()) {
            if (!first) json.append(", ");
            json.append("\"member\" : [");
            List<InventorySupply> memberList = getMember();
            int memberListIndex = 0;
            for (InventorySupply member : memberList) {
                if (memberListIndex > 0) json.append(", ");
                json.append("{");
                json.append("");
                json.append(member.toJSONFragment());
                json.append("}");
                first = false;
                ++memberListIndex;
            }
            json.append("]");
        }
        return json.toString();
    }

    /**
     *
     * Quote JSON string
     */
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
