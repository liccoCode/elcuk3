package models.material;
import com.google.gson.annotations.Expose;
import helper.Currency;
import models.User;
import models.embedded.ERecordBuilder;
import models.finance.FeeType;
import models.finance.PaymentUnit;
import models.procure.CooperItem;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.whouse.Whouse;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 物料采购计划
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/5/31
 * Time: AM10:18
 */
@Entity
@DynamicUpdate
public class MaterialUnit extends Model {

    /**
     * 物料采购单
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    public MaterialPurchase materialPurchase;

    /**
     * 物料信息
     */
    @OneToOne(fetch = FetchType.LAZY)
    public Material material;

    /**
     * 当前仓库（深圳仓库）
     */
    @OneToOne
    public Whouse currWhouse;

    /**
     * 供应商
     * 一个采购单只能拥有一个供应商
     */
    @ManyToOne
    public Cooperator cooperator;

    /**
     * 计划采购数量
     */
    @Expose
    @Required
    public int planQty;

    /**
     * 实际交货数量
     * 目前版本对应 收货数量
     */
    public int qty;

    /**
     * 可用库存数量
     */
    public int availableQty;

    /**
     * 入库数
     */
    public int inboundQty;

    /**
     * 预计交货时间
     */
    @Expose
    @Temporal(TemporalType.DATE)
    @Required
    public Date planDeliveryDate;

    /**
     * 实际交货时间
     */
    @Expose
    @Temporal(TemporalType.DATE)
    public Date deliveryDate;


    /**
     * 物料计划状态
     */
    @Expose
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public STAGE stage;

    /**
     * 阶段
     */
    public enum STAGE {

        /**
         * 已取消 解绑的物料计划变为“已取消”阶段
         */
        CANCEL {
            @Override
            public String label() {
                return "已取消";
            }
        },
        /**
         * 采购中 下单成功即为“采购中”
         */
        DELIVERY {
            @Override
            public String label() {
                return "采购中";
            }
        },
        /**
         * 结束：（待定，留位置）
         */
        CLOSE {
            @Override
            public String label() {
                return "结束";
            }
        };

        public abstract String label();
    }

    /**
     * 项目名称(所属公司)
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public User.COR projectName;
    /**
     * 操作人员
     */
    @OneToOne
    public User handler;

    /**
     * 创建时间
     */
    @Expose
    @Required
    public Date createDate = new Date();


    /**
     * 预计单价
     */
    @Expose
    @Required
    @Min(0)
    public float planPrice;

    @Expose
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Required
    public Currency planCurrency;


    /**
     * 实际单价
     */
    @Expose
    @Required
    @Min(0)
    public float price;

    @Expose
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Required
    public Currency currency;

    @OneToMany(mappedBy = "materialUnit", fetch = FetchType.LAZY)
    public List<PaymentUnit> fees = new ArrayList<>();
    /**
     * 是否需要付款
     */
    public boolean isNeedPay = true;


    /**
     * 手动单采购计划数据验证
     */
    public void validateManual() {
        Validation.required("物料编码", this.material.id);
        Validation.required("采购数量", this.planQty);
        Validation.required("预计单价", this.planPrice);
    }

    public float totalAmountToCNY() {
        return this.planCurrency.toCNY(new BigDecimal(this.planCurrency.toString())
                .multiply(new BigDecimal(this.planQty)).setScale(2, 4).floatValue());

    }

    /**
     * 总共需要申请的金额
     *
     * @return
     */
    public float totalAmount() {
        return new BigDecimal(this.planPrice)
                .multiply(new BigDecimal(this.planQty))
                .setScale(2, 4)
                .floatValue();
    }

    public float formatPrice() {
        return new BigDecimal(this.planPrice).setScale(2, 4).floatValue();
    }

    /**
     * 将 MaterialUnit 添加到/移出 采购单,状态改变
     *
     * @param materialPurchase
     */
    public void toggleAssignTodeliveryment(MaterialPurchase materialPurchase, boolean assign) {
        if(assign) {
            this.materialPurchase = materialPurchase;
            this.stage = this.stage.DELIVERY;
        } else {
            this.materialPurchase = null;
            this.stage = this.stage.CANCEL;
        }
    }


    /**
     * 总共需要申请的金额
     *
     * @return
     */
    public float totallanPrice() {
        return this.planPrice*this.planQty;
    }

    /**
     * 格式化产品要求，前台 popover 使用
     */
    public String formatProductTerms() {
        StringBuilder message = new StringBuilder();
        if(StringUtils.isNotBlank(material.specification)) {
            message.append("<span class='label label-info'>规格:</span><br>");
            String[] messageArray = StringUtils.split(material.specification, "\n");
            for(String text : messageArray) {
                message.append("<p>").append(text).append("<p>");
            }
        }
        if(StringUtils.isNotEmpty(material.texture)) {
            message.append("<span class='label label-info'>材质:</span><br>");
            String[] messageArray = StringUtils.split(material.texture, "\n");
            for(String text : messageArray) {
                message.append("<p>").append(text).append("<p>");
            }
        }
        if(StringUtils.isNotEmpty(material.technology)) {
            message.append("<span class='label label-info'>工艺:</span><br>");
            String[] messageArray = StringUtils.split(material.technology, "\n");
            for(String text : messageArray) {
                message.append("<p>").append(text).append("<p>");
            }
        }
        if(material.cooperItems != null && material.cooperItems.size() > 0
                && StringUtils.isNotEmpty(material.cooperItems.get(0).productTerms)) {
            message.append("<span class='label label-info'>产品要求:</span><br>");
            String[] messageArray = StringUtils.split(material.cooperItems.get(0).productTerms, "\n");
            for(String text : messageArray) {
                message.append("<p>").append(text).append("<p>");
            }
        }
        return message.toString();
    }


    public Currency getCurrency() {
        List<CooperItem> cooperItems = this.material.cooperItems;
        if(cooperItems != null && cooperItems.get(0) != null) {
            return cooperItems.get(0).currency;
        }
        return null;
    }


    public List<PaymentUnit> fees() {
        List<PaymentUnit> paymentUnits = new ArrayList<>();
        for(PaymentUnit fee : this.fees) {
            if(fee.remove) {
                continue;
            }
            paymentUnits.add(fee);
        }
        return paymentUnits;
    }

    /**
     * 已经申请的金额
     *
     * @return
     */
    public float appliedAmount() {
        float appliedAmount = 0;
        for(PaymentUnit fee : this.fees()) {
            appliedAmount += fee.amount();
        }
        return appliedAmount;
    }

    /**
     * 当前出货计划所有请款的修正总额
     *
     * @return
     */
    public float fixValueAmount() {
        float fixValueAmount = 0;
        for(PaymentUnit fee : this.fees()) {
            fixValueAmount += fee.fixValue;
        }
        return fixValueAmount;
    }

    /**
     * 修改付款状态
     */
    public void editPayStatus() {
        this.isNeedPay = !this.isNeedPay;
        this.save();
    }

    /**
     * 1. 出货计划所在的出货单单需要拥有一个请款单
     * 2. 出货计划需要已经交货
     */
    private void billingValid() {
        if(this.materialPurchase.applyPurchase == null) {
            Validation.addError("", String.format("采购计划所属的采购单[%s]还没有规划的请款单", this.materialPurchase.id));
        }
    }


    /**
     * 是否拥有了尾款
     *
     * @return
     */
    public boolean hasTailPay() {
        for(PaymentUnit fee : this.fees()) {
            if(fee.feeType == FeeType.procurement()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 剩余的请款金额
     *
     * @return
     */
    public float leftAmount() {
        return totalAmount() - appliedAmount();
    }

    /**
     * 预付款申请
     */
    public PaymentUnit billingPrePay() {
        /*
         * 0. 基本检查
         * 1. 检查是否此采购计划是否已经存在一个预付款
         * 2. 申请预付款
         */
        this.billingValid();
        if(this.hasPrePay()) {
            Validation.addError("", "不允许重复申请预付款.");
        }
        if(this.hasTailPay()) {
            Validation.addError("", "已经申请了尾款, 不需要再申请预付款.");
        }
        if(this.cooperator.materialFirst == 0) {
            Validation.addError("", "当前物料预付款比例为空，请先设置供应商物料预付款比例！");
        }
        if(Validation.hasErrors()) {
            return null;
        }
        PaymentUnit fee = new PaymentUnit(this);
        // 预付款的逻辑在这里实现, 总额的 30% 为预付款
        fee.feeType = FeeType.cashpledge();
        float pre = (float) this.cooperator.materialFirst / 100;
        fee.amount = fee.amount * pre;
        fee.save();

        this.materialPurchase.applyPurchase.updateAt = new Date();
        this.materialPurchase.applyPurchase.save();
        new ERecordBuilder("procureunit.prepay").msgArgs(this.id, String.format("%s %s", fee.currency.symbol(), fee.amount))
                .fid(this.id, ProcureUnit.class).save();
        return fee;
    }

    /**
     * 付款申请
     */
    public PaymentUnit billingTailPay() {
        /**
         * 0. 基本检查
         * 1. 申请付款
         */
        this.billingValid();
        if(Validation.hasErrors()) {
            return null;
        }
        if(this.hasTailPay()) {
            Validation.addError("", "存在重复申请尾款的物料，请查证！");
        }
        if(Validation.hasErrors()) {
            return null;
        }
        PaymentUnit fee = new PaymentUnit(this);
        fee.feeType = FeeType.procurement();
        fee.amount = this.leftAmount();
        fee.save();

        this.materialPurchase.applyPurchase.updateAt = new Date();
        this.materialPurchase.applyPurchase.save();
        new ERecordBuilder("materialPlanUnit.prepay")
                .msgArgs(this.id, String.format("%s %s", fee.currency.symbol(), fee.amount)).fid(this.id, ProcureUnit.class).save();
        return fee;
    }



    /**
     * 是否拥有了 预付款
     *
     * @return
     */
    public boolean hasPrePay() {
        return this.fees().stream().anyMatch(fee -> fee.feeType == FeeType.cashpledge());
    }
}