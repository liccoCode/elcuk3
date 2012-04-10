package models.product;

import helper.Patterns;
import models.market.Listing;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:55 AM
 */
@Entity
public class Product extends GenericModel {
    /**
     * 此产品所能够符合的上架的货架, 不能够集联删除, 删除 Product 是一个很严重的事情!
     * 需要检测 Product 相关的数据
     */
    @OneToMany(mappedBy = "product", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    public List<Listing> listings;

    @ManyToOne
    public Category category;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<Product> relates;

    /**
     * Product 的所有库存;
     * 将产品删除了, 库存不允许删除, 库存记录变为"孤儿"
     */
    @OneToMany(mappedBy = "product", cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<ProductQTY> qtys;

    /**
     * 唯一的标示
     */
    @Id
    public String sku;

    @Required
    @Lob
    public String productName;

    public Float lengths;

    public Float heigh;

    public Float width;

    public Float weight;

    public Product() {
    }

    public Product(String sku) {
        this.sku = sku;
    }

    /**
     * 删除 Product 前需要检查与 Product 有直接关系的各种对象.
     */
    @PreRemove
    public void checkDelete() {
        if(this.listings != null && this.listings.size() > 0) {
            throw new FastRuntimeException("Product [" + this.sku + "] have relate Listing, cannot be delete.");
        }
        for(ProductQTY qty : this.qtys) {
            if(qty.pending + qty.unsellable + qty.qty > 0) {
                throw new FastRuntimeException("Product [" + this.sku + "] hava quantity, cannot be delete.");
            }
        }

    }

    /**
     * 验证这个 SKU 是否合法
     *
     * @param sku
     * @return
     */
    public static boolean validSKU(String sku) {
        //71SNS1-B2PE, 71-SNS1-B2PE
        if(sku == null || sku.trim().isEmpty()) return false;
        String[] parts = sku.split("-");
        if(parts.length == 2) { //做一次兼容
            String part0 = parts[0].substring(0, 2);
            parts = new String[]{part0, parts[0].substring(2), parts[1]};
        }
        if(parts.length != 3) return false;
        if(!Patterns.Nub.matcher(parts[0]).matches()) return false;
        return true;
    }

    /**
     * 根据 Amazon 输入的 MerchantSKU 来查找 Product, 会自动将 MerchantSKU 转换成系统内使用的 SKU;
     * 这里面拥有两个兼容性判断.
     *
     * @param msku
     * @return
     */
    public static Product findByMerchantSKU(String msku) {
        return Product.findById(merchantSKUtoSKU(msku));
    }

    /**
     * 将 Amazon 上的 MerchantSKU 转换成 SKU, 并且对 Amazon 上几个错误的 MerchantSKU 进行处理
     *
     * @param merchantSKU
     * @return
     */
    public static String merchantSKUtoSKU(String merchantSKU) {
        Validate.notNull(merchantSKU);
        // ------ fix -----------
//                if("609132508189".equals(t_msku)) t_msku = "71-HPTOUCH-B2PG"; //对历史错误数据的修复 @_@
//                if("8Z-0JR3-1BHG".equals(t_msku.toUpperCase())) t_msku = "80-QW1A56-BE"; // Power Bank 的销售还是需要囊括进来的
        String sku = StringUtils.split(merchantSKU, ",")[0].toUpperCase();
        if("609132508189".equals(sku)) sku = "71-HPTOUCH-B2PG";
        else if("8Z-0JR3-1BHG".equals(sku)) sku = "80-QW1A56-BE";
        return sku;
    }
}
