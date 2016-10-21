package models.finance;

import models.procure.Cooperator;
import models.procure.Deliveryment;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/24/13
 * Time: 3:13 PM
 */
@Entity
public class PaymentTarget extends Model {
    public PaymentTarget() {
    }

    public PaymentTarget(Deliveryment dmt) {
        this.name = dmt.cooperator.name;
    }

    @OneToMany(mappedBy = "target")
    public List<Payment> payments = new ArrayList<>();

    @ManyToOne
    public Cooperator cooper;

    @Required
    @Column(nullable = false)
    public String name;

    /**
     * 银行账号
     */
    @Required
    @Column(unique = true, nullable = false)
    public String accountNumber;

    /**
     * 银行账户
     */
    @Required
    @Column(nullable = false)
    public String accountUser;

    /**
     * 银行地址
     */
    @Required
    @Column(nullable = false)
    public String accountAddress;

    public String memo;

    public Date createdAt;

    @PrePersist
    public void beforeSave() {
        this.createdAt = new Date();
    }

    public void newPaymentTarget(Cooperator coper) {
        this.name = coper.fullName;
        this.cooper = coper;
        // accountNumber 已经保存
        if(!Validation.current().valid(this).ok) return;
        this.save();
    }

    public void updateAttr(PaymentTarget target) {
        if(StringUtils.isNotBlank(target.name))
            this.name = target.name;
        if(StringUtils.isNotBlank(target.accountNumber))
            this.accountNumber = target.accountNumber;
        if(StringUtils.isNotBlank(target.accountUser))
            this.accountUser = target.accountUser;

        if(!Validation.current().valid(this).ok) return;
        Cooperator cooper = Cooperator.findById(target.cooper.id);
        if(cooper != null)
            this.cooper = cooper;
        this.save();
    }

    /**
     * 删除这个支付目标.
     * 1. 如果这个支付目标有过支付, 则不允许删除.
     */
    public void destroy() {
        if(this.payments.size() > 0) {
            Validation.addError("",
                    String.format("此支付目标拥有 %s 个支付, 这些记录不允许删除.", this.payments.size()));
            return;
        }
        this.delete();
    }

    @Override
    public String toString() {
        return String.format("[%s %s]%s", this.accountUser, this.accountNumber, this.name);
    }
}
