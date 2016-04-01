package models.whouse;

import com.google.gson.annotations.Expose;
import models.product.Product;
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
    private String stockObjId;

    /**
     * 类型
     */
    @Enumerated(EnumType.STRING)
    @Required
    @Expose
    private SOT stockObjType;

    private enum SOT {
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
            throw new FastRuntimeException("货物不能为空!");
        }
    }

    //TODO 支持产品物料与包材物料
}
