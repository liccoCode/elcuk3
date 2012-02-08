package models.product;

import exception.VErrorRuntimeException;
import play.Logger;
import play.db.jpa.Model;

import javax.persistence.*;

/**
 * 将产品的库存单独拿出来进行记录
 * User: Wyatt
 * Date: 12-1-8
 * Time: 上午6:00
 */
@Entity
public class ProductQTY extends Model {

    @ManyToOne
    public Product product;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    public Whouse whouse;

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

    /**
     * 给前端使用的, 判断这个 ProductQTY 是为保存还是更新
     */
    @Transient
    public boolean save = false;

    @PrePersist
    public void prePersist() {//检查保证 QTY 的默认值
        if(this.qty == null) this.qty = 0;
        if(this.unsellable == null) this.unsellable = 0;
        if(this.pending == null) this.pending = 0;
        if(this.product == null)
            throw new VErrorRuntimeException("ProductQTY.product", "ProductQTY Product can not be null.");
        if(this.whouse == null)
            throw new VErrorRuntimeException("ProductQTY.whouse", "ProductQTY Whouse can not be null.");
    }

    /**
     * 如果是新的 ProductQTY 则为对于的 SKU + Whouse 添加一个库存量, 如果这个 SKU + Whouse 存在了库存, 那么则累加
     * 这个库存的量
     */
    public void saveAndUpdate() {
        /**
         * 1. 判断是否为新创建的 QTY. 如果是新创建则加载对应的 QTY, Whouse 进行关联
         * 2. 如果是更新老的数据, 则 Product, Whouse 为 null 直接加载进行更能;
         * 3. 如果为累加新数据, 那么则 Product, Whouse 不能为 null, 重新加载后进行累加保存
         */
        ProductQTY pqty = ProductQTY.find("FROM ProductQTY pt WHERE pt.whouse.id=? AND pt.product.sku=?", this.whouse.id, this.product.sku).first();
        if(save) {
            // 如果是保存, 判断是否有已经存在的 ProductQTY
            if(pqty != null) {// 如果有已经存在的 ProductQTY, 那么则累加数据
                if(this.qty == null) throw new VErrorRuntimeException("ProductQty.qty", "Must input value");
                pqty.qty += this.qty;
                if(this.inbound != null) pqty.inbound += this.inbound;
                if(this.pending != null) pqty.pending += this.pending;
                if(this.unsellable != null) pqty.unsellable += this.unsellable;
                pqty.save();
            } else {
                Product prod = Product.find("sku=?", this.product.sku).first();
                if(prod == null) throw new VErrorRuntimeException("product.sku", "No valid SKU");
                Whouse wh = Whouse.findById(this.whouse.id);
                if(wh == null) throw new VErrorRuntimeException("whouse.id", "No valid Whouse");
                this.product = prod;
                this.whouse = wh;
                this.save();
            }
        } else {// 如果是更新, 那么则加载出老的 ProductQTY 然后将新的数据完全更新到老数据上即可.
            if(pqty == null) { // 如果没有,则标示这个 ProductQTY 不存在, 那么可以保存
                throw new VErrorRuntimeException("ProductQTY", "Not Valid ProductQTY, may be current ProductQTY is not exsit.");
            }
            pqty.qty = this.qty;
            pqty.pending = this.pending;
            pqty.unsellable = this.unsellable;
            pqty.save();
        }
    }

    /**
     * 将 New ProductQTY 中的数据同步回当前被管理的 ProductQTY 中, 如果同步成功, 则会将 this.save 设置为 true;
     *
     * @param nqty
     */
    public void updateAttrs(ProductQTY nqty) {
        if(!nqty.product.sku.equals(this.product.sku)) {
            Logger.warn("ProductQTY.product[" + this.product.sku + "/" + nqty.product.sku + "] Is Not the same, can not be update!");
            return;
        }
        if(!nqty.whouse.equals(this.whouse)) {
            Logger.warn("ProductQTY.whouse[" + this.whouse.name + "/" + nqty.whouse.name + "] Is Not the same,, can not be update!");
            return;
        }
        if(nqty.inbound != null) this.inbound = nqty.inbound;
        if(nqty.pending != null) this.pending = nqty.pending;
        if(nqty.qty != null) this.qty = nqty.qty;
        if(nqty.unsellable != null) this.unsellable = nqty.unsellable;

        this.save = true;
        nqty.save = true;

        this.save();
    }

}
