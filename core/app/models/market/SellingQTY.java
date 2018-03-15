package models.market;

import exception.SellingQTYAttachNoWhouseException;
import models.ElcukRecord;
import models.product.Product;
import models.whouse.Whouse;
import org.apache.commons.lang.Validate;
import play.db.jpa.GenericModel;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 将库存绑定到 Selling 级别, 不再统一到 Product 身上;
 * <p/>
 * <pre>
 * 需要考虑两个问题:
 * 1. 如果是 Amazon 的 Selling, 那么可能同一个账户在 UK,DE 上都开设了 FBA 销售, 但是对于 FBA 仅仅只有一份库存.
 * 2. 需要考虑到其他 Selling 借用某一个 FBA 的 Selling 进行发货, 但自己并不拥有库存. [以上两个问题使用同一个解决方案]
 *  > SellingQTY 的定位为 MerchantSKU + Whouse(1-1 Account) 来进行唯一标识
 *  > SellingQTY 所跟踪的 Selling 对应到此 Selling Account 所在的市场上去. (可能导致此 Account 其他市场的 Selling 无 SellingQTY 对象)
 *  </pre>
 * User: wyattpan
 * Date: 4/10/12
 * Time: 7:30 PM
 */
@Entity
public class SellingQTY extends GenericModel implements ElcukRecord.Log {

    @ManyToOne(cascade = CascadeType.PERSIST)
    public Selling selling;

    @OneToOne
    public Product product;

    @OneToOne
    public Whouse whouse;


    /**
     * Selling 库存的 Id, 组成为  [Msku]_[Whouse_id]
     */
    @Id
    public String id;


    /**
     * 库存
     */
    public Integer qty = 0;

    /**
     * 仓库中被预定走的
     */
    public Integer pending = 0;

    /**
     * 正在入库的库存; 还是需要记录的
     */
    public Integer inbound = 0;

    /**
     * 仓库中不可销售的
     */
    public Integer unsellable = 0;

    public Date updateDate;
    public SellingQTY() {
    }

    public SellingQTY(String sqtyId) {
        this.id = sqtyId;
    }

    public String msku() {
        return this.id.split("_")[0].toUpperCase();
    }

    @Override
    public String toLog() {
        return String.format("SellingQTY[%s] [Inbound:%s] [Qty:%s] [Unsellable:%s] [Pending:%s]",
                this.id, this.inbound, this.qty, this.unsellable, this.pending);
    }


    /**
     * 将一个全新的 SellingQTY[qty,pending,inbound,unsellable] 关联到 Selling 上, 包括 Selling, Product, 组成 id
     *
     * @param merchantSKU
     * @throws SellingQTYAttachNoWhouseException SellingQTY 无法绑定 Whouse 的错误
     */
    public void attach2Selling(String merchantSKU, Whouse whouse) {
        Validate.notNull(merchantSKU);
        Validate.notNull(whouse);
        this.selling = Selling
                .findById(Selling.sid(Selling.getMappingSKU(merchantSKU), whouse.account.type, whouse.account));
        this.product = Product.findByMerchantSKU(merchantSKU);
        if(this.selling == null || this.product == null) throw new SellingQTYAttachNoWhouseException();
        // 虽然可以通过 Whouse.find("account=?", sell.account).first(); 找到 Whouse, 但是这是此 Selling 对应的 FBA 仓库, 此方法除了能够绑定 FBA 库存还需要能够绑定其他的库存
        this.whouse = whouse;
        this.id = String.format("%s_%s", merchantSKU.toUpperCase(), whouse.id);
        this.save();
    }

    public static List<SellingQTY> qtysAccodingSKU(Product prod) {
        return SellingQTY.find("product=?", prod).fetch();
    }

    public static List<SellingQTY> qtysAccodingSKU(String sku) {
        if(!Product.validSKU(sku)) throw new FastRuntimeException(sku + " 不是合法的 SKU");
        return SellingQTY.find("product.sku=?", sku).fetch();
    }

    public static List<SellingQTY> qtysAccodingMSKU(Selling selling) {
        Whouse wh = Whouse.find("account=?", selling.account).first();
        return find("id=?", String.format("%s_%s", selling.merchantSKU, wh.id)).fetch();
    }

    public static boolean exist(String id) {
        return SellingQTY.count("id=?", id) > 0;
    }

}
