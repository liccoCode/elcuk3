package models.procure;

import com.google.gson.annotations.Expose;
import helper.J;
import models.ElcukRecord;
import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 所有与公司一起的合作者;
 * (使用 Play 1 内置的 validate annotation 做一个测试 model)
 * User: wyattpan
 * Date: 7/16/12
 * Time: 10:58 AM
 */
@Entity
public class Cooperator extends Model {

    public enum T {
        /**
         * 供应商
         */
        SUPPLIER {
            @Override
            public String to_s() {
                return "供应商";
            }
        },
        /**
         * 货代
         */
        SHIPPER {
            @Override
            public String to_s() {
                return "货代";
            }
        };

        /**
         * 简单的名称
         *
         * @return
         */
        public abstract String to_s();
    }

    /**
     * 只有 SUPPLIER 会使用到此字段, 表示这个 SUPPLIER 可以生产的 Item.
     */
    @OneToMany(mappedBy = "cooperator")
    public List<CooperItem> cooperItems = new ArrayList<CooperItem>();

    /**
     * 全称
     */
    @Required
    @Expose
    public String fullName;

    /**
     * 简称
     */
    @Required
    @Unique
    @Expose
    public String name;

    /**
     * 地址
     */
    @Required
    @Expose
    public String address;

    /**
     * 联系人
     */
    @Column(length = 32)
    @Required
    @Expose
    public String contacter;

    /**
     * 主要联系电话
     */
    @Column(length = 32)
    @Required
    @Min(7)
    @Expose
    public String phone;

    /**
     * 备注信息
     */
    @Lob
    public String memo;

    /**
     * 是什么类型的供应商
     */
    @Enumerated(EnumType.STRING)
    @Required
    @Expose
    public T type;


    @Transient
    public Cooperator mirror;

    @PostLoad
    public void preLoad() {
        this.mirror = J.from(J.G(this), Cooperator.class);
    }

    /**
     * 为了将 CooperItem 的值的记录全部记录下来, 当成功保存以后再进行记录
     */
    @PostUpdate
    public void postUpdate() {
        ElcukRecord.postUpdate(this, "Cooperator.update");
    }

    public Cooperator checkAndUpdate() {
        if(this.type == T.SHIPPER && this.cooperItems.size() > 0)
            throw new FastRuntimeException("货物运输商不允许拥有[可生产的商品项目]");
        return this.save();
    }


}
