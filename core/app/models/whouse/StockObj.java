package models.whouse;

import com.google.gson.annotations.Expose;
import models.product.Product;
import play.data.validation.Validation;
import play.utils.FastRuntimeException;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

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

    public String name() {
        if(this.stockObjType == null) return "";

        switch(this.stockObjType) {
            case SKU:
                return getProduct().productName;
            case PRODUCT_MATERIEL:
                //TODO
            case PACKAGE_MATERIEL:
                //TODO
            default:
                return "";
        }
    }

    public void valid() {
        Validation.required("StockObj.stockObjId", this.stockObjId);
        Validation.required("StockObj.stockObjType", this.stockObjType);
    }

    public static SOT guessType(String id) {
        return SOT.SKU;
        //产品物料与包材物料需要注意 ID 的统一
    }
}
