package models.procure;

import com.google.gson.annotations.Expose;
import helper.Currency;
import models.User;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.db.jpa.GenericModel;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.Date;

/**
 * 采购所使用的付款
 * User: wyattpan
 * Date: 6/20/12
 * Time: 9:46 AM
 */
@Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Payment extends GenericModel {
    public static interface ClosePayment {
        /**
         * 在 Payment 进行 Close 的时候回掉的进行不同 Model 自行检查的方法
         *
         * @param thisPayment
         */
        public void close(Payment thisPayment);
    }

    public enum T {
        DELIVERY,

        SHIP
    }

    public enum S {
        NORMAL,
        CLOSE
    }

    @Id
    @GeneratedValue
    @Expose
    public Long id;

    /**
     * 付款的类型
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Column(length = 12)
    public T type;

    /**
     * 此付款的状态
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Column(length = 12)
    public S state;

    @ManyToOne
    public Deliveryment deliveryment;

    @ManyToOne
    public Shipment shipment;

    @OneToOne
    public User payer;


    /**
     * 付了多少钱
     */
    @Column(nullable = false)
    @Expose
    public Float price;

    /**
     * 付款的单位是什么
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    @Expose
    public Currency currency;

    /**
     * 什么时候付款的
     */
    @Column(nullable = false)
    @Expose
    public Date payDate = new Date();

    /**
     * 记录做了什么
     */
    @Lob
    @Expose
    public String memo = " ";

    public Payment close(String msg) {
        if(StringUtils.isBlank(msg)) throw new FastRuntimeException("关闭 Payment 必须留有理由!");
        this.state = Payment.S.CLOSE;
        this.memo += String.format("\r\n(CLOSE|%s)", msg);
        ClosePayment closePayment = null;
        if(this.type == T.DELIVERY)
            closePayment = new Deliveryment();
        else if(this.type == T.SHIP)
            closePayment = new Shipment();
        if(closePayment == null) throw new FastRuntimeException("正在关闭位置的 Payment 类型!");
        closePayment.close(this);
        return this.save();
    }

    /**
     * Payment 自己的检查, 付款金额, 类型, 付款时间,类型
     */
    public void paymentCheckItSelf() {
        if(this.price < 0) throw new FastRuntimeException("付款价格小于 0");
        if(this.type == null) throw new FastRuntimeException("没有指定付款类型.");
        if(this.payer == null) throw new FastRuntimeException("系统无法确认是谁进行的付款.");
        if(this.payDate == null) this.payDate = new Date();
        if(this.payDate.getTime() > System.currentTimeMillis()) throw new FastRuntimeException("你付款时间穿越了吗?");
        if(this.state == null) this.state = Payment.S.NORMAL;
    }
}
