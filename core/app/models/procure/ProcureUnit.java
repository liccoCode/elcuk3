package models.procure;

import com.google.gson.annotations.Expose;
import helper.JPAs;
import models.User;
import models.embedded.UnitDelivery;
import models.embedded.UnitPlan;
import models.market.Selling;
import models.product.Product;
import models.product.Whouse;
import org.apache.commons.lang.StringUtils;
import play.db.helper.JpqlSelect;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 每一个采购单元
 * User: wyattpan
 * Date: 6/11/12
 * Time: 5:23 PM
 */
@Entity
public class ProcureUnit extends Model {

    /**
     * 阶段
     */
    public enum STAGE {
        /**
         * 计划阶段
         */
        PLAN,
        /**
         * 采购阶段
         */
        DELIVERY,
        /**
         * 运输阶段
         */
        SHIP,
        /**
         * 完成了
         */
        DONE,
        /**
         * 关闭阶段, 不处理了
         */
        CLOSE
    }

    public enum S {
        /**
         * 计划
         */
        PLAN,
        /**
         * 已经下单
         */
        ORDERED,
        /**
         * 部分付款
         */
        PARTPAY,
        /**
         * 全部付款
         */
        FULPAY,
        /**
         * 已交货
         */
        DELIVERIED,
        /**
         * 运输中
         */
        SHIPPING,
        /**
         * 清关
         */
        CLEARGATE,
        /**
         * 入库中
         */
        RECIVING,
        /**
         * 采购完成
         */
        DONE,
        /**
         * 没有经过正常流程,进行关闭了的
         */
        CLOSE
    }

    /**
     * 运输单
     */
    @ManyToOne
    public Shipment shipment;

    /**
     * 采购单
     */
    @ManyToOne
    public Deliveryment deliveryment;

    @OneToOne(fetch = FetchType.LAZY)
    public Selling selling;
    @Expose
    public String sid; // 一个 SellingId 字段

    @OneToOne(fetch = FetchType.LAZY)
    public Product product;
    @Expose
    public String sku;// 冗余 sku 字段
    /**
     * 操作人员
     */
    @OneToOne
    public User handler;

    /**
     * 送往的仓库
     */
    @OneToOne
    public Whouse whouse;

    // ----------- 将不同阶段的数据封装到不同的对象当中去.

    /**
     * 计算阶段
     */
    @Expose
    public UnitPlan plan;

    /**
     * 采购|交付阶段
     */
    @Expose
    public UnitDelivery delivery;

    /**
     * 此 Unit 的状态
     */
    @Expose
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public S state;


    @Lob
    @Expose
    public String comment = " ";

    public STAGE stage() {
        switch(this.state) {
            case PLAN:
                return STAGE.PLAN;
            case ORDERED:
            case PARTPAY:
            case FULPAY:
            case DELIVERIED:
                return STAGE.DELIVERY;
            case SHIPPING:
            case CLEARGATE:
            case RECIVING:
                return STAGE.SHIP;
            default:
                return null;
        }
    }

    /**
     * 检查参数并且创建新 ProcureUnit
     */
    public ProcureUnit checkAndCreate() {
        /**
         *
         *
         *
         *
         *
         * 6. whouse 必须存在
         */
        // 1. 预期日期不允许为空
        if(this.plan.planArrivDate == null) throw new FastRuntimeException("预期日期不允许为空");
        // 2. 检查预计日期不允许比当前日期小
        if(this.plan.planArrivDate.getTime() - System.currentTimeMillis() < 0)
            throw new FastRuntimeException("预期日期已经过期!");
        // 3. 采购单价不允许 < 0
        if(this.plan.unitPrice < 0) throw new FastRuntimeException("采购单价不允许小于 0");
        // 4. 采购量不允许 < 0
        if(this.plan.planQty < 0) throw new FastRuntimeException("采购量不允许小于 0");
        // 5. 采购价格单位不允许为空
        if(this.plan.currency == null) throw new FastRuntimeException("采购的货币单位不允许为空.");
        // 6. 采购商不允许为空
        if(StringUtils.isBlank(this.plan.supplier)) throw new FastRuntimeException("供应商不能为空.");
        // 7. selling 必须存在
        if(StringUtils.isBlank(this.sid)) throw new FastRuntimeException("采购对应的 Selling 不能为空.");
        this.selling = Selling.findById(this.sid);
        if(this.selling == null) throw new FastRuntimeException(String.format("Selling %s 不存在", this.sid));
        // 8. 目标仓库必须拥有
        if(this.whouse == null) throw new FastRuntimeException("目的仓库不能为空");
        // 9. 采购人必须记录
        if(this.handler == null) throw new FastRuntimeException("必须拥有一个处理人.");
        this.product = Product.findById(this.sku);
        if(this.product == null) throw new FastRuntimeException("没有关联 Product.");

        return this.save();
    }

    public ProcureUnit fromPlanToDelivery() {
        /**
         * 1. 检查是否合法
         * 2. 进行转换
         */
        return this.save();
    }

    @SuppressWarnings("unchecked")
    public static List<String> suppliers() {
        Query query = JPAs.createQuery(new JpqlSelect().select("plan.supplier").from("ProcureUnit").groupBy("plan.supplier"));
        return query.getResultList();
    }

    public static List<ProcureUnit> findByState(STAGE stage, int page, int size) {
        switch(stage) {
            case PLAN:
                return ProcureUnit.find("state IN (?) ORDER BY plan.planArrivDate", S.PLAN).fetch(page, size);
            case DELIVERY:
                return ProcureUnit.find("state IN (?,?,?,?)", S.ORDERED, S.PARTPAY, S.FULPAY, S.DELIVERIED).fetch(page, size);
            case SHIP:
                return ProcureUnit.find("state IN (?,?,?)", S.SHIPPING, S.CLEARGATE, S.RECIVING).fetch(page, size);
            default:
                return new ArrayList<ProcureUnit>();
        }
    }
}
