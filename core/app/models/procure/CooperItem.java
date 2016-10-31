package models.procure;

import com.alibaba.fastjson.JSON;
import com.google.gson.annotations.Expose;
import helper.Currency;
import helper.J;
import models.product.Product;
import models.view.dto.CooperItemDTO;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Min;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
     * 一箱的数量
     */
    @Min(0)
    @Expose
    @Required
    public Integer boxSize;

    /**
     * 单箱重量
     */
    public double singleBoxWeight;

    /**
     * 单箱长
     */
    public double length;

    /**
     * 单箱宽
     */
    public double width;

    /**
     * 单箱高
     */
    public double height;

    /**
     * 最低订货量
     */
    @Required
    @Expose
    @Min(0)
    public Integer lowestOrderNum;

    @Lob
    public String memo;

    /**
     * 要求工厂 对该产品的要求
     */
    @Lob
    public String productTerms;

    @Transient
    public List<CooperItemDTO> items = new ArrayList<>();

    /**
     * 方案Json串
     */
    public String attributes;

    public CooperItem checkAndUpdate() {
        this.check();
        this.setAttributes();
        return this.save();
    }

    public void setAttributes() {
        this.attributes = J.json(this.items);
    }

    public void getAttributes() {
        if(this.items == null || this.items.isEmpty()) {
             this.items = JSON.parseArray(StringUtils.isNotBlank(this.attributes) ? this.attributes : "[]",
                     CooperItemDTO.class);
        }
    }

    /**
     * 通过 Box 计算数量
     *
     * @return
     */
    public int boxToSize(int size) {
        if(this.boxSize == null) return size;
        return this.boxSize * size;
    }

    /**
     * 检查并且创建新的 CooperItem
     *
     * @return
     */
    public CooperItem checkAndSave(Cooperator cooperator) {
        this.sku = this.sku.trim();
        this.product = Product.findById(this.sku);
        this.check();
        if(cooperator == null || !cooperator.isPersistent())
            throw new FastRuntimeException("CooperItem 必须有关联的 Cooperator");
        this.cooperator = cooperator;
        for(CooperItem copitm : this.cooperator.cooperItems) {
            if(copitm.sku.equals(this.sku))
                throw new FastRuntimeException(this.sku + " 已经绑定了, 不需要重复绑定.");
        }
        cooperator.cooperItems.add(this);
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
        if(!this.product.sku.equals(this.sku))
            throw new FastRuntimeException("不允许使 this.product.sku 与 this.sku 不一样!");
    }

    /**
     * 检查并且删除这个 CooperatorItem
     */
    public CooperItem checkAndRemove() {
        return this.delete();
    }

    /**
     * 主箱箱数
     *
     * @param shipedQty
     * @return
     */
    public int boxNum(int shipedQty) {
        if(this.boxSize == null) return 0;
        float boxNum = shipedQty / (float) this.boxSize;
        if(boxNum < 1) {
            return 1;
        } else {
            return (int) Math.floor(boxNum);
        }
    }

    /**
     * 尾箱内的产品数量
     *
     * @return
     */
    public int lastCartonNum(int shipedQty) {
        if(this.boxSize == null) return 0;
        int boxNum = this.boxNum(shipedQty);
        int lastCartonNum = shipedQty - boxNum * this.boxSize;
        if(lastCartonNum <= 0) {
            return 0;
        } else {
            return lastCartonNum;
        }
    }
}
