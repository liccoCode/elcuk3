package models.whouse;

import com.alibaba.fastjson.JSON;
import com.google.gson.annotations.Expose;
import helper.J;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.product.Product;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 抽象出来的仓库实际存储的货物, 可以是 Product Or 物料等
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/1/16
 * Time: 2:19 PM
 */
@Embeddable
public class StockObj implements Serializable {

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
        } else {
            return Product.find("sku=?", stockObjId).first();
        }
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
                return getProduct().abbreviation;
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
        if(this.attrs == null || this.attrs.isEmpty()) {
            this.attrs = JSON.parseObject(StringUtils.isNotBlank(this.attributes) ? this.attributes : "{}", Map.class);
        }
        return this.attrs;
    }

    public void setAttributes() {
        this.attributes = J.json(this.attrs);
    }

    public void setAttributes(ProcureUnit unit) {
        //把采购计划一些自身属性存入到 DB,方便后期查询
        if(unit != null) {
            this.attrs.put("procureunitId", unit.id);
            if(unit.fba != null) this.attrs.put("fba", unit.fba.shipmentId);
            if(unit.shipType != null) this.attrs.put("shipType", unit.shipType.name());
            if(unit.whouse != null) {
                this.attrs.put("whouseId", unit.whouse.id);
                this.attrs.put("whouseName", unit.whouse.name);
            }
        }
        this.setAttributes();
    }

    public void setAttributes(ShipItem item) {
        if(item != null) {
            if(item.unit != null) this.setAttributes(item.unit);
            this.attrs.put("shipItemId", item.id);
            this.attrs.put("planBeginDate", item.shipment.dates.planBeginDate);
        }
        this.setAttributes();
    }
}
