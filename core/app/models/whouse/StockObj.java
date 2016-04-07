package models.whouse;

import com.google.gson.annotations.Expose;
import models.product.Product;
import models.qc.CheckTask;
import org.apache.commons.lang3.StringUtils;
import play.data.validation.Required;
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
    @Required
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

    public void setProduct(Product product) {
        if(product != null && StringUtils.isNotBlank(product.sku)) {
            this.stockObjType = SOT.SKU;
            this.stockObjId = product.sku;
        } else {
            throw new FastRuntimeException("Product 不能为空!");
        }
    }

    public String name() {
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

    /**
     * 根据实际存储的货物和质检结果来挑选一个仓库项, 用来接收货物
     *
     * @param st
     * @return
     */
    public WhouseItem pickWhouseItem(CheckTask.ShipType st) {
        WhouseItem whouseItem = WhouseItem.find("whouse.style=? AND stockObjId=? AND stockObjType=?",
                Whouse.selectStyle(st, this.stockObjType).name(), this.stockObjId, this.stockObjType).first();
        return whouseItem != null ? whouseItem : new WhouseItem(this, st).<WhouseItem>save();
    }
}
