package models.procure;

import com.google.gson.annotations.Expose;
import helper.Currency;
import models.User;
import org.joda.time.DateTime;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 采购单, 用来记录所采购的 ProcureUnit
 * User: wyattpan
 * Date: 6/18/12
 * Time: 4:50 PM
 */
@Entity
public class Deliveryment extends GenericModel implements Payment.ClosePayment {

    public enum S {
        /**
         * 预定
         */
        PENDING {
            @Override
            public String to_h() {
                return String.format("<span style='color:#5CB85C'>%s</span>", this);
            }
        },
        /**
         * 部分付款
         */
        PARTPAY {
            @Override
            public String to_h() {
                return String.format("<span style='color:#FAA52C'>%s</span>", this);
            }
        },
        /**
         * 全部付款
         */
        FULPAY {
            @Override
            public String to_h() {
                return String.format("<span style='color:#007BCC'>%s</span>", this);
            }
        },
        /**
         * 完成, 交货
         */
        DELIVERY {
            @Override
            public String to_h() {
                return String.format("<span style='color:#4DB2D0'>%s</span>", this);
            }
        };

        /**
         * 转换为 html
         *
         * @return
         */
        public abstract String to_h();
    }

    @OneToMany(mappedBy = "deliveryment")
    @OrderBy("state DESC")
    public List<Payment> payments = new ArrayList<Payment>();


    @OneToMany(mappedBy = "deliveryment")
    public List<ProcureUnit> units = new ArrayList<ProcureUnit>();

    @OneToOne
    public User handler;

    @Expose
    public Date createDate = new Date();

    /**
     * 此采购单的状态
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Column(nullable = false)
    public S state;


    @Id
    @Column(length = 30)
    @Expose
    public String id;

    @Lob
    public String memo = " ";

    public static String id() {
        DateTime dt = DateTime.now();
        String count = Deliveryment.count("createDate>=? AND createDate<=?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-30", dt.getYear(), dt.getMonthOfYear())).toDate()) + "";
        return String.format("DL|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
    }

    public static List<Deliveryment> openDeliveryments() {
        //TODO 需要将 Deliveryment 添加 supplier
        return Deliveryment.find("state!=?", S.DELIVERY).fetch();
    }

    public static Deliveryment checkAndCreate(User user) {
        if(user == null) throw new FastRuntimeException("必须拥有创建者.");
        Deliveryment deliveryment = new Deliveryment();
        deliveryment.id = Deliveryment.id();
        deliveryment.state = S.PENDING;
        deliveryment.handler = user;
        return deliveryment.save();
    }

    public Payment payForDeliveryment(Payment payment) {
        if(payment == null) throw new FastRuntimeException("没有付款数据, 无法付款.");
        if(this.isPaymentComplete()) throw new FastRuntimeException("款项已经全部付清, 无需再付款.");
        payment.paymentCheckItSelf();

        payment.deliveryment = this; //由 Payment 添加关联
        payment.deliveryment.state = S.PARTPAY;
        payment.deliveryment.save();
        return payment.save();
    }

    /**
     * 判断此采购单是否付款完全
     *
     * @return
     */
    public boolean isPaymentComplete() {
        switch(this.state) {
            case PENDING:
            case PARTPAY:
                return false;
            case FULPAY:
            case DELIVERY:
            default:
                return true;
        }
    }

    /**
     * 将采购单付清全款
     */
    public void complatePayment() {
        /**
         * 1. 检查全款金额
         */
        float totalPayedCNY = 0;
        float totalPayedGBP = 0;
        float totalPayedUSD = 0;
        float totalPayedEUR = 0;
        for(Payment pay : this.payments) {
            if(pay.state == Payment.S.CLOSE) continue;
            if(pay.currency == Currency.CNY)
                totalPayedCNY += pay.price;
            else if(pay.currency == Currency.USD)
                totalPayedUSD += pay.price;
            else if(pay.currency == Currency.GBP)
                totalPayedGBP += pay.price;
            else if(pay.currency == Currency.EUR)
                totalPayedEUR += pay.price;
        }

        float totalNeedPayCNY = 0;
        float totalNeedPayGBP = 0;
        float totalNeedPayUSD = 0;
        float totalNeedPayEUR = 0;
        for(ProcureUnit unit : this.units) {
            if(unit.plan.currency == Currency.CNY)
                totalNeedPayCNY += unit.plan.unitPrice * unit.delivery.deliveryQty;
            else if(unit.plan.currency == Currency.USD)
                totalNeedPayUSD += unit.plan.unitPrice * unit.delivery.deliveryQty;
            else if(unit.plan.currency == Currency.GBP)
                totalNeedPayGBP += unit.plan.unitPrice * unit.delivery.deliveryQty;
            else if(unit.plan.currency == Currency.EUR)
                totalNeedPayEUR += unit.plan.unitPrice * unit.delivery.deliveryQty;
        }

        boolean fullPayCNY = totalNeedPayCNY == totalPayedCNY;
        boolean fullPayUSD = totalNeedPayUSD == totalPayedUSD;
        boolean fullPayGBP = totalNeedPayGBP == totalPayedGBP;
        boolean fullPayEUR = totalNeedPayEUR == totalPayedEUR;
        if(fullPayCNY && fullPayGBP && fullPayUSD && fullPayEUR) {
            this.state = S.FULPAY;
            this.save();
        } else {
            StringBuilder sbd = new StringBuilder("款项还没有付清,请检查!\r\n");
            sbd.append("CNY: ").append(String.format("%s(%s) / %s ", totalPayedCNY, totalPayedCNY - totalNeedPayCNY, totalNeedPayCNY)).append("\r\n")
                    .append("USD: ").append(String.format("%s(%s) / %s ", totalPayedUSD, totalPayedUSD - totalNeedPayUSD, totalNeedPayUSD)).append("\r\n")
                    .append("GBP: ").append(String.format("%s(%s) / %s ", totalPayedGBP, totalPayedGBP - totalNeedPayGBP, totalNeedPayGBP)).append("\r\n")
                    .append("EUR: ").append(String.format("%s(%s) / %s ", totalPayedEUR, totalPayedEUR - totalNeedPayEUR, totalNeedPayEUR)).append("\r\n");
            throw new FastRuntimeException(sbd.toString());
        }
    }

    public String supplier() {
        if(this.units != null && this.units.size() > 0)
            return this.units.get(0).plan.supplier;
        else
            return "Empty";
    }

    @Override
    public void close(Payment thisPayment) {
        if(this.state == S.DELIVERY) throw new FastRuntimeException("采购单已经全部交货, 不允许删除付款信息.");
        try {
            thisPayment.deliveryment.complatePayment();
        } catch(FastRuntimeException e) {
            thisPayment.deliveryment.state = Deliveryment.S.PARTPAY;
            thisPayment.deliveryment.save();
        }
    }

    /**
     * 检查此 Deliveryment 是否可以标记为 DELIVERY;
     * (其所有的 ProcureUnit 全部交货, DONE)
     */
    public boolean canBeDelivery() {
        if(this.state == S.PENDING) return false;
        for(ProcureUnit pu : this.units) {
            if(pu.stage != ProcureUnit.STAGE.DONE) return false;
        }
        return true;
    }

    /**
     * 交货的进度
     *
     * @return
     */
    public F.T2<Integer, Integer> deliveryProgress() {
        int ensureQty = 0;
        int deliveriedQty = 0;
        for(ProcureUnit unit : this.units) {
            ensureQty += unit.delivery.ensureQty;
            if(unit.stage == ProcureUnit.STAGE.DONE)
                deliveriedQty += unit.delivery.deliveryQty;
        }
        return new F.T2<Integer, Integer>(deliveriedQty, ensureQty);
    }
}
