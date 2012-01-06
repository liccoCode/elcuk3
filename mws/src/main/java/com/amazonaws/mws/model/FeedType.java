
package com.amazonaws.mws.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FeedType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="FeedType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="_POST_PRODUCT_DATA_"/>
 *     &lt;enumeration value="_POST_PRODUCT_RELATIONSHIP_DATA_"/>
 *     &lt;enumeration value="_POST_ITEM_DATA_"/>
 *     &lt;enumeration value="_POST_PRODUCT_OVERRIDES_DATA_"/>
 *     &lt;enumeration value="_POST_PRODUCT_IMAGE_DATA_"/>
 *     &lt;enumeration value="_POST_PRODUCT_PRICING_DATA_"/>
 *     &lt;enumeration value="_POST_INVENTORY_AVAILABILITY_DATA_"/>
 *     &lt;enumeration value="_POST_ACKNOWLEDGEMENT_DATA_"/>
 *     &lt;enumeration value="_POST_ORDER_FULFILLMENT_DATA_"/>
 *     &lt;enumeration value="_POST_PAYMENT_ADJUSTMENT_DATA_"/>
 *     &lt;enumeration value="_POST_FLAT_FILE_LISTINGS_DATA_"/>
 *     &lt;enumeration value="_POST_FLAT_FILE_ORDER_ACKNOWLEDGEMENT_DATA_"/>
 *     &lt;enumeration value="_POST_FLAT_FILE_FULFILLMENT_DATA_"/>
 *     &lt;enumeration value="_POST_FLAT_FILE_PAYMENT_ADJUSTMENT_DATA_"/>
 *     &lt;enumeration value="_POST_FLAT_FILE_INVLOADER_DATA_"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "FeedType")
@XmlEnum
public enum FeedType {

    @XmlEnumValue("_POST_PRODUCT_DATA_")
    POST_PRODUCT_DATA("_POST_PRODUCT_DATA_"),
    @XmlEnumValue("_POST_PRODUCT_RELATIONSHIP_DATA_")
    POST_PRODUCT_RELATIONSHIP_DATA("_POST_PRODUCT_RELATIONSHIP_DATA_"),
    @XmlEnumValue("_POST_ITEM_DATA_")
    POST_ITEM_DATA("_POST_ITEM_DATA_"),
    @XmlEnumValue("_POST_PRODUCT_OVERRIDES_DATA_")
    POST_PRODUCT_OVERRIDES_DATA("_POST_PRODUCT_OVERRIDES_DATA_"),
    @XmlEnumValue("_POST_PRODUCT_IMAGE_DATA_")
    POST_PRODUCT_IMAGE_DATA("_POST_PRODUCT_IMAGE_DATA_"),
    @XmlEnumValue("_POST_PRODUCT_PRICING_DATA_")
    POST_PRODUCT_PRICING_DATA("_POST_PRODUCT_PRICING_DATA_"),
    @XmlEnumValue("_POST_INVENTORY_AVAILABILITY_DATA_")
    POST_INVENTORY_AVAILABILITY_DATA("_POST_INVENTORY_AVAILABILITY_DATA_"),
    @XmlEnumValue("_POST_ACKNOWLEDGEMENT_DATA_")
    POST_ACKNOWLEDGEMENT_DATA("_POST_ACKNOWLEDGEMENT_DATA_"),
    @XmlEnumValue("_POST_ORDER_FULFILLMENT_DATA_")
    POST_ORDER_FULFILLMENT_DATA("_POST_ORDER_FULFILLMENT_DATA_"),
    @XmlEnumValue("_POST_PAYMENT_ADJUSTMENT_DATA_")
    POST_PAYMENT_ADJUSTMENT_DATA("_POST_PAYMENT_ADJUSTMENT_DATA_"),
    @XmlEnumValue("_POST_FLAT_FILE_LISTINGS_DATA_")
    POST_FLAT_FILE_LISTINGS_DATA("_POST_FLAT_FILE_LISTINGS_DATA_"),
    @XmlEnumValue("_POST_FLAT_FILE_ORDER_ACKNOWLEDGEMENT_DATA_")
    POST_FLAT_FILE_ORDER_ACKNOWLEDGEMENT_DATA("_POST_FLAT_FILE_ORDER_ACKNOWLEDGEMENT_DATA_"),
    @XmlEnumValue("_POST_FLAT_FILE_FULFILLMENT_DATA_")
    POST_FLAT_FILE_FULFILLMENT_DATA("_POST_FLAT_FILE_FULFILLMENT_DATA_"),
    @XmlEnumValue("_POST_FLAT_FILE_PAYMENT_ADJUSTMENT_DATA_")
    POST_FLAT_FILE_PAYMENT_ADJUSTMENT_DATA("_POST_FLAT_FILE_PAYMENT_ADJUSTMENT_DATA_"),
    @XmlEnumValue("_POST_FLAT_FILE_INVLOADER_DATA_")
    POST_FLAT_FILE_INVLOADER_DATA("_POST_FLAT_FILE_INVLOADER_DATA_");
    private final String value;

    FeedType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FeedType fromValue(String v) {
        for (FeedType c: FeedType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
