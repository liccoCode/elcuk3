package models.procure;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import helper.Currency;
import helper.J;
import models.ElcukRecord;
import models.product.Product;
import play.data.validation.Min;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 7/16/12
 * Time: 11:42 AM
 */
@Entity
public class CooperItem extends Model {

    @ManyToOne
    public Cooperator cooperator;

    @OneToOne
    public Product product;

    /**
     * 冗余字段
     */
    @Required
    @MinSize(5)
    @Expose
    public String sku;

    /**
     * 当前的采购价格
     */
    @Required
    @Expose
    @Min(0)
    public Float price;

    @Required
    @Expose
    @Enumerated(EnumType.STRING)
    public Currency currency = Currency.CNY;

    /**
     * 平均交货周期(以 天 为单位)
     */
    @Required
    @Expose
    @Min(0)
    public Integer period;

    /**
     * 最低订货量
     */
    @Required
    @Expose
    @Min(0)
    public Integer lowestOrderNum;

    @Lob
    public String memo;

    @Transient
    public CooperItem mirror;

    /**
     * 由于需要就 CooperItem 的更新记录, 所以需要一个加载出来的时候的镜像对象
     */
    @PostLoad
    public void preLoad() {
        this.mirror = new Gson().fromJson(J.G(this), CooperItem.class);
    }


    /**
     * 为了将 CooperItem 的值的记录全部记录下来, 当成功保存以后再进行记录
     */
    @PostUpdate
    public void postUpdate() {
        ElcukRecord.postUpdate(this, "CooperItem.update");
    }

    public CooperItem checkAndUpdate() {
        this.check();
        return this.save();
    }

    /**
     * 检查并且创建新的 CooperItem
     *
     * @return
     */
    public CooperItem checkAndSave(Cooperator cooperator) {
        this.product = Product.findById(this.sku);
        this.check();
        if(cooperator == null || !cooperator.isPersistent())
            throw new FastRuntimeException("CooperItem 必须有关联的 Cooperator");
        this.cooperator = cooperator;
        for(CooperItem copitm : this.cooperator.cooperItems) {
            if(copitm.sku.equals(this.sku)) throw new FastRuntimeException(this.sku + " 已经绑定了, 不需要重复绑定.");
        }
        return this.save();
    }

    /**
     * 基础的检查
     */
    private void check() {
        if(this.product == null) throw new FastRuntimeException("没有关联产品, 不允许.");
        if(this.price <= 0) throw new FastRuntimeException("采购价格能小于 0 ?");
        if(this.lowestOrderNum < 0) throw new FastRuntimeException("最低采货量不允许小于 0 ");
        if(this.period < 0) throw new FastRuntimeException("生产周期不允许小于  0");
        if(!this.product.sku.equals(this.sku)) throw new FastRuntimeException("不允许使 this.product.sku 与 this.sku 不一样!");
    }

    /**
     * 检查并且删除这个 CooperatorItem
     */
    public CooperItem checkAndRemove() {
        return this.delete();
    }
}
