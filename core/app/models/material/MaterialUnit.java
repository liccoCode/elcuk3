package models.material;

import com.alibaba.fastjson.JSON;
import com.google.gson.annotations.Expose;
import helper.Currency;
import models.User;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.qc.CheckTaskDTO;
import models.whouse.InboundUnit;
import models.whouse.Whouse;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;

import javax.persistence.*;
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
     * 入库数量
     */
    public int inboundQty;

    /**
     * 可用库存数量
     */
    public int availableQty;


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
    public ProcureUnit.STAGE stage;


    /**
     * 质检状态 and 质检结果
     */
    @Enumerated(EnumType.STRING)
    public InboundUnit.R result;


    /**
     * 包装信息：主箱信息
     */
    @Lob
    public String mainBoxInfo;
    /**
     * 包装信息：尾箱信息
     */
    @Lob
    public String lastBoxInfo;

    @Transient
    public CheckTaskDTO mainBox = new CheckTaskDTO();

    @Transient
    public CheckTaskDTO lastBox = new CheckTaskDTO();


    /**
     * 项目名称(所属公司)
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public User.COR projectName;

    /**
     * 创建人
     */
    @OneToOne
    public User creator;

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


    @PostLoad
    public void postPersist() {
        this.mainBox = JSON.parseObject(this.mainBoxInfo, CheckTaskDTO.class);
        this.lastBox = JSON.parseObject(this.lastBoxInfo, CheckTaskDTO.class);
    }

    /**
     * 手动单采购计划数据验证
     */
    public void validateManual() {
        Validation.required("物料编码", this.material.code);
        Validation.required("采购数量", this.planQty);
        Validation.required("预计单价", this.planPrice);
    }
}
