package models.procure;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import helper.J;
import models.ElcukRecord;
import models.product.Product;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
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
    @Min(5)
    @Expose
    public String sku;

    /**
     * 当前的采购价格
     */
    @Required
    @Expose
    public Float price;

    /**
     * 平均交货周期
     */
    @Required
    @Expose
    public Integer period;

    /**
     * 最低订货量
     */
    @Required
    @Expose
    public Integer lowestOrderNum;

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
        List<ElcukRecord.FromTo> changes = ElcukRecord.changes(this);
        if(changes.size() <= 0) return;
        ElcukRecord record = new ElcukRecord("CooperItem.update");
        record.jsonRecord = J.json(changes);
        record.s();
    }

    public CooperItem checkAndUpdate() {
        if(!this.product.sku.equals(this.sku)) throw new FastRuntimeException("不允许如此修改 SKU!");
        if(this.lowestOrderNum < 0) throw new FastRuntimeException("最低采货量不允许小于 0 ");
        if(this.period < 0) throw new FastRuntimeException("生产周期不允许小于  0");
        return this.save();
    }
}
