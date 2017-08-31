package models.material;

import com.google.gson.annotations.Expose;
import helper.Currency;
import models.User;
import models.procure.Cooperator;
import models.whouse.Whouse;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

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
                .multiply(new BigDecimal(this.paidQty())).setScale(2, 4).floatValue());

    }

    public int paidQty() {
        if(Arrays.asList("IN_STORAGE", "OUTBOUND", "SHIPPING", "SHIP_OVER", "INBOUND", "CLOSE")
                .contains(this.stage.name()))
            return qty;
        else
            return this.planQty;
    }

    /**
     * 总共需要申请的金额
     *
     * @return
     */
    public float totalAmount() {
        return new BigDecimal(this.planPrice)
                .multiply(new BigDecimal(this.paidQty()))
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
     * 预计单价金额格式化
     *
     * @return
     */
    public float formatPlanPrice() {
        return new BigDecimal(this.planPrice).setScale(2, 4).floatValue();
    }

    /**
     * 总共需要申请的金额
     *
     * @return
     */
    public float totallanPrice() {
        return new BigDecimal(this.planPrice)
                .multiply(new BigDecimal(planQty))
                .setScale(2, 4)
                .floatValue();
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
        if(material.cooperItems != null && material.cooperItems.size() > 0 &&
                StringUtils.isNotEmpty(material.cooperItems.get(0).productTerms)) {
            message.append("<span class='label label-info'>产品要求:</span><br>");
            String[] messageArray = StringUtils.split(material.cooperItems.get(0).productTerms, "\n");
            for(String text : messageArray) {
                message.append("<p>").append(text).append("<p>");
            }
        }
        return message.toString();
    }
}
