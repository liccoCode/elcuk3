package models.material;

import com.google.gson.annotations.Expose;
import models.User;
import models.finance.ProcureApply;
import models.procure.Cooperator;
import models.procure.Deliveryment;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 物料采购单
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/5/31
 * Time: AM10:19
 */
public class MaterialPurchase extends GenericModel {


    public MaterialPurchase() {

    }

    @Id
    @Column(length = 30)
    @Expose
    public String id;

    /**
     * 此采购单的状态
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Column(nullable = false)
    @Required
    public Deliveryment.S state;

    /**
     * 采购计划
     */
    @OneToMany(mappedBy = "materialPurchase", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    public List<MaterialUnit> units = new ArrayList<>();

    @ManyToOne
    public ProcureApply apply;

    @OneToOne
    public User handler;

    /**
     * 供应商
     * 一个采购单只能拥有一个供应商
     */
    @ManyToOne
    public Cooperator cooperator;

    @Expose
    @Required
    public Date createDate = new Date();

    /**
     * 下单时间
     */
    public Date orderTime;

    /**
     * 交货时间
     */
    public Date deliveryTime;

    public Date confirmDate;
    /**
     * 采购单类型
     */
    @Enumerated(EnumType.STRING)
    public Deliveryment.T deliveryType;

    /**
     * 所属公司
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public User.COR projectName;

}
