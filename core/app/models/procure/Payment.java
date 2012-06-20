package models.procure;

import com.google.gson.annotations.Expose;
import helper.Currency;
import models.User;
import org.apache.commons.lang.StringUtils;
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
public class Payment extends GenericModel {

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
        try {
            this.deliveryment.complatePayment();
        } catch(FastRuntimeException e) {
            this.deliveryment.state = Deliveryment.S.PARTPAY;
            this.deliveryment.save();
        }
        return this.save();
    }
}
