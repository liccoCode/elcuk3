package models.procure;

import com.alibaba.fastjson.JSON;
import com.google.gson.annotations.Expose;
import helper.Currency;
import helper.J;
import helper.Reflects;
import models.ElcukRecord;
import models.embedded.ERecordBuilder;
import models.material.Material;
import models.product.Product;
import models.view.dto.CooperItemDTO;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Min;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.i18n.Messages;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    @OneToOne
    public Material material;

    /**
     * 冗余字段
     */
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

    /***
     * 其他价格（包含包材配件价格）
     */
    @Required
    @Expose
    @Min(0)
    public Float otherPrice = 0f;

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

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public T type;

    public enum T {

        SKU {
            @Override
            public String label() {
                return "SKU";
            }
        },
        MATERIAL {
            @Override
            public String label() {
                return "包材物料";
            }
        };

        public abstract String label();

    }

    public enum S {
        Pending {
            @Override
            public String label() {
                return "待审核";
            }
        },
        Agree {
            @Override
            public String label() {
                return "审核通过";
            }
        },
        Disagree {
            @Override
            public String label() {
                return "审核不通过";
            }
        };

        public abstract String label();
    }

    @Enumerated(EnumType.STRING)
    public S status;

    /**
     * 方案Json串
     */
    public String attributes;

    public Date createDate;

    public CooperItem checkAndUpdate(CooperItem entity) {
        entity.check();
        entity.setAttributes();
        entity.setDefaultValue();
        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(this, "price", entity.price));
        logs.addAll(Reflects.logFieldFade(this, "currency", entity.currency));
        logs.addAll(Reflects.logFieldFade(this, "otherPrice", entity.otherPrice));
        logs.addAll(Reflects.logFieldFade(this, "period", entity.period));
        logs.addAll(Reflects.logFieldFade(this, "lowestOrderNum", entity.lowestOrderNum));

        this.productTerms = entity.productTerms;
        this.memo = entity.memo;
        this.items = entity.items;
        this.setDefaultValue();
        this.save();
        if(logs.size() > 0) {
            new ElcukRecord(Messages.get("cooperators.updatecooperitem"),
                    Messages.get("cooperators.updatecooperitem.msg", this.id, StringUtils.join(logs, "<br>")),
                    this.id.toString()).save();
        }
        return this;
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

    public void setDefaultValue() {
        if(this.items != null && this.items.size() > 0) {
            CooperItemDTO dto = this.items.get(0);
            this.height = dto.height;
            this.width = dto.width;
            this.length = dto.length;
            this.singleBoxWeight = dto.singleBoxWeight;
            this.boxSize = dto.boxSize;
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
        this.createDate = new Date();
        this.check();
        if(cooperator == null || !cooperator.isPersistent())
            throw new FastRuntimeException("CooperItem 必须有关联的 Cooperator");
        this.cooperator = cooperator;
        this.type = T.SKU;
        this.status = S.Pending;

        if(this.cooperator.cooperItems.stream().filter(item -> Objects.equals(item.type, T.SKU))
                .anyMatch(item -> Objects.equals(item.sku, this.sku)))
            throw new FastRuntimeException(this.sku + " 已经绑定了, 不需要重复绑定.");
        cooperator.cooperItems.add(this);
        this.setAttributes();
        this.setDefaultValue();
        CooperItem item = this.save();
        new ERecordBuilder("copItem.save")
                .msgArgs(this.cooperator.name, this.sku, this.currency.symbol() + this.price).fid(item.id).save();
        return item;
    }

    public void saveMaterialItem(Cooperator cop) {
        Material m = Material.findById(this.material.id);
        List<CooperItem> cooperItems = CooperItem.find("type =?", T.MATERIAL).fetch();
        if(cooperItems.stream().anyMatch(item -> Objects.equals(item.material, m))) {
            Validation.addError("", "供应商下已经存在该物料，请选择其他物料!");
            return;
        }
        this.type = T.MATERIAL;
        this.cooperator = cop;
        this.createDate = new Date();
        this.setAttributes();
        this.setDefaultValue();
        this.save();
        new ElcukRecord(Messages.get("cooperators.savecooperitem"),
                Messages.get("cooperators.savecooperitem.msg", this.id), this.id.toString());
    }

    public void updateMaterialItem(Cooperator cooperator, CooperItem db) {
        Material m = Material.findById(this.material.id);
        List<CooperItem> cooperItems = CooperItem.find("type =?", T.MATERIAL).fetch();
        if(db.id != null && cooperItems.stream()
                .anyMatch(item -> Objects.equals(item.material, m) && item.cooperator != cooperator)) {
            Validation.addError("", " 其他供应商下已经存在该物料，请重新选择物料!");
            return;
        }
        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(db, "price", this.price));
        logs.addAll(Reflects.logFieldFade(db, "currency", this.currency));
        logs.addAll(Reflects.logFieldFade(db, "otherPrice", this.otherPrice));
        logs.addAll(Reflects.logFieldFade(db, "period", this.period));
        logs.addAll(Reflects.logFieldFade(db, "lowestOrderNum", this.lowestOrderNum));
        this.setAttributes();
        this.setDefaultValue();

        db.productTerms = this.productTerms;
        db.memo = this.memo;
        db.items = this.items;
        db.setAttributes();
        db.setDefaultValue();
        db.save();
        if(logs.size() > 0) {
            new ElcukRecord(Messages.get("cooperators.updatecooperitem"),
                    Messages.get("cooperators.updatecooperitem.msg", db.id, StringUtils.join(logs, "<br>")),
                    db.id.toString()).save();
        }
    }

    /**
     * 基础的检查
     */
    private void check() {
        if(this.sku == null) throw new FastRuntimeException("没有关联产品, 不允许.");
        if(this.price <= 0) throw new FastRuntimeException("采购价格能小于 0 ?");
        if(this.lowestOrderNum < 0) throw new FastRuntimeException("最低采货量不允许小于 0 ");
        if(this.period < 0) throw new FastRuntimeException("生产周期不允许小于  0");

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
