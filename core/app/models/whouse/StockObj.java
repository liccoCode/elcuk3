package models.whouse;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Optional;
import com.google.gson.annotations.Expose;
import helper.J;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.product.Product;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.data.validation.Validation;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽象出来的仓库实际存储的货物, 可以是 Product Or 物料等
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/1/16
 * Time: 2:19 PM
 */
@Embeddable
public class StockObj implements Serializable, Cloneable {

    /**
     * 存货条目 ID
     * (关联的对象的 ID -> SKU or 物料)
     */
    @Expose
    public String stockObjId;

    /**
     * 类型
     */
    @Enumerated(EnumType.STRING)
    @Expose
    public SOT stockObjType;

    public enum SOT {
        SKU {
            @Override
            public String label() {
                return "SKU";
            }
        },
        PRODUCT_MATERIEL {
            @Override
            public String label() {
                return "产品物料";
            }
        },
        PACKAGE_MATERIEL {
            @Override
            public String label() {
                return "包材物料";
            }
        };

        public abstract String label();
    }

    /**
     * 冗余属性(存成 JSON), 只为了把采购计划相关信息带过来方便查询
     */
    @Expose
    @Lob
    public String attributes = "{}";

    @Transient
    public Map<String, Object> attrs = new HashMap<>();

    public Product getProduct() {
        if(this.stockObjType != SOT.SKU) {
            throw new FastRuntimeException("货物类型(stockObjType)错误, 无法找到对应 Product!");
        }
        return Product.find("sku=?", stockObjId).first();
    }

    public StockObj() {
    }

    public StockObj(String sku) {
        this.stockObjType = SOT.SKU;
        this.stockObjId = sku;
    }

    public StockObj(String stockObjId, SOT stockObjType) {
        this.stockObjId = stockObjId;
        this.stockObjType = stockObjType;
    }

    public StockObj(Map<String, Object> attrs) {
        if(attrs != null) {
            if(attrs.containsKey("stockObjId") && attrs.containsKey("stockObjType")) {
                this.stockObjId = attrs.get("stockObjId").toString();
                this.stockObjType = SOT.valueOf(attrs.get("stockObjType").toString());
            }
            if(attrs.containsKey("attributes")) this.attributes = attrs.get("attributes").toString();
        }
    }

    public String name() {
        if(this.stockObjType == null) return "";

        switch(this.stockObjType) {
            case SKU:
                Product product = getProduct();
                if(product != null) return product.abbreviation;
            case PRODUCT_MATERIEL:
                //TODO
            case PACKAGE_MATERIEL:
                //TODO
            default:
                return "";
        }
    }

    public void valid() {
        Validation.required("物料编码", this.stockObjId);
        Validation.required("物料编码类型", this.stockObjType);
    }

    public static SOT guessType(String id) {
        return SOT.SKU;
        //产品物料与包材物料需要注意 ID 的统一
    }

    public Map attributes() {
        if(this.attrs == null || this.attrs.isEmpty()) this.unmarshalAtts();
        return this.attrs;
    }

    public void marshalAtts() {
        this.attributes = J.json(this.attrs);
    }

    public void unmarshalAtts() {
        this.attrs = JSON.parseObject(StringUtils.isNotBlank(this.attributes) ? this.attributes : "{}", Map.class);
        if(this.attrs.get("procureunitId") != null && StringUtils.isNotBlank(attrs.get("procureunitId").toString())) {
            ProcureUnit unit = ProcureUnit.findById(Long.parseLong(attrs.get("procureunitId").toString()));
            this.attrs.put("unit", unit);
            if(attrs.get("procureunitId") != null && StringUtils.isNotBlank(attrs.get("procureunitId").toString())) {
                List<InboundRecord> list = InboundRecord.find("stockObj.attributes like ? ",
                        "%\"procureunitId\":" + attrs.get("procureunitId").toString() + "%").fetch();
                if(list != null && list.size() > 0) {
                    InboundRecord inboundRecord = list.get(0);
                    this.attrs.put("inboundRecord", inboundRecord);
                }
            }
        }
    }

    public void setAttributes(ProcureUnit unit) {
        //把采购计划一些自身属性存入到 DB,方便后期查询
        if(unit != null) {
            this.attrs.put("procureunitId", unit.id);
            if(unit.fba != null) this.attrs.put("fba", unit.fba.shipmentId);
            if(unit.shipType != null) this.attrs.put("shipType", unit.shipType.name());
            if(unit.selling != null && StringUtils.isNotBlank(unit.selling.fnSku)) {
                this.attrs.put("fnsku", unit.selling.fnSku);
            }
            if(unit.whouse != null) {
                this.attrs.put("whouseId", unit.whouse.id);
                this.attrs.put("whouseName", unit.whouse.name);
            }
            if(unit.cooperator != null) this.attrs.put("cooperatorId", unit.cooperator.id);
            if(unit.attrs.planShipDate != null) this.attrs.put("planBeginDate", unit.attrs.planShipDate);
            this.marshalAtts();
        }
    }

    public void setAttributes(ShipItem item) {
        if(item != null) {
            if(item.unit != null) this.setAttributes(item.unit);
            this.attrs.put("shipItemId", item.id);
            this.attrs.put("planBeginDate", item.shipment.dates.planBeginDate);
            this.marshalAtts();
        }
    }

    public void setAttributes(OutboundRecord outboundRecord) {
        if(outboundRecord != null) {
            if(outboundRecord.planBeginDate != null) this.attrs.put("planBeginDate", outboundRecord.planBeginDate);
            if(StringUtils.isNotBlank(outboundRecord.fba)) this.attrs.put("fba", outboundRecord.fba);
            if(outboundRecord.shipType != null) this.attrs.put("shipType", outboundRecord.shipType.name());
            if(StringUtils.isNotBlank(outboundRecord.market)) this.attrs.put("whouseName", outboundRecord.market);
            if(outboundRecord.productCode != null) this.attrs.put("productCode", outboundRecord.productCode);
            this.marshalAtts();
        }
    }

    public void setAttributes(InboundRecord inboundRecord) {
        if(inboundRecord != null) {
            if(StringUtils.isNotBlank(inboundRecord.procureunitId))
                this.attrs.put("procureunitId", inboundRecord.procureunitId);
            if(StringUtils.isNotBlank(inboundRecord.fba)) this.attrs.put("fba", inboundRecord.fba);
            if(inboundRecord.shipType != null) this.attrs.put("shipType", inboundRecord.shipType.name());
            if(inboundRecord.market != null) this.attrs.put("whouseName", inboundRecord.market
                    .marketAndWhouseMapping());
            if(inboundRecord.productCode != null) this.attrs.put("productCode", inboundRecord.productCode);
            this.marshalAtts();
        }
    }

    public StockObj dump() {
        try {
            return (StockObj) this.clone();
        } catch(CloneNotSupportedException e) {
            throw new FastRuntimeException(e);
        }
    }

    /**
     * 由于前期数据没有存储 fnsku 数据,所以在这里弥补一下
     *
     * @return
     */
    public String fnsku() {
        this.attributes();
        if(!this.attrs.containsKey("fnsku")) this.resetAttrs();
        Optional fnsku = Optional.fromNullable(this.attrs.get("fnsku"));
        if(fnsku.isPresent()) {
            return fnsku.get().toString();
        } else {
            return "";
        }
    }

    /**
     * 重新加载 attributes 属性到 attrs
     */
    public void resetAttrs() {
        if(this.attrs.containsKey("procureunitId")) {
            ProcureUnit procureUnit = ProcureUnit.findById(NumberUtils.toLong(this.attrs.get("procureunitId")
                    .toString()));
            if(procureUnit != null && procureUnit.selling != null) {
                this.attrs.put("fnsku", procureUnit.selling.fnSku);
                this.attrs.put("cooperatorId", procureUnit.cooperator.id);
                this.marshalAtts();
            }
        }
    }

    /**
     * 读取采购计划 ID
     *
     * @return
     */
    public Long procureunitId() {
        if(this.attributes() != null && !this.attributes().isEmpty()) {
            Optional<Object> procureunitId = Optional.fromNullable(this.attributes().get("procureunitId"));
            if(procureunitId.isPresent()) return NumberUtils.toLong(procureunitId.get().toString());
        }
        return null;
    }
}
