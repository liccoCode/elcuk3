package models.procure;

import com.google.gson.annotations.Expose;
import helper.Currency;
import models.product.Product;
import org.apache.commons.lang.StringUtils;
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
     * 一箱的数量
     */
    @Min(0)
    @Expose
    @Required
    public Integer boxSize;

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

    public CooperItem checkAndUpdate() {
        this.check();
        return this.save();
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
     *
     *格式化产品要求，前台 popover 使用
     */
    public String formatProductTerms() {
        StringBuffer message = new StringBuffer();
        message.append("<span class='label label-info'>产品要求:</span><br>");
        if( StringUtils.isNotEmpty(this.productTerms)) {
            String[] messageArray = this.productTerms.split("\\s");
            for(String text : messageArray) {
                message.append("<p>").append(text).append("<p>");
            }
        }
        message.append("<span class='label label-info'>Memo:</span><br>");
        if( StringUtils.isNotEmpty(this.memo)) {
            String[] messageArray = this.memo.split("\\s");
            for(String text : messageArray) {
                message.append("<p>").append(text).append("<p>");
            }
        }

        return message.toString();
    }
}
