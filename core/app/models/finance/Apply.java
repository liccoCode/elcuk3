package models.finance;

import models.procure.Cooperator;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 3/26/13
 * Time: 10:07 AM
 */
@MappedSuperclass
public abstract class Apply extends Model {
    /**
     * 请款单的编号
     */
    @Column(unique = true)
    public String serialNumber;

    /**
     * 请款的时间
     */
    public Date createdAt;

    /**
     * 最后更新时间
     */
    public Date updateAt;

    /**
     * 实际支付时间
     */
    public Date paymentDate;

    /**
     * NOTE:
     * 1. 请款人 User
     * 2. 涉及的付款单 Payments
     * 由于 Hibernate 限制, 无法在 MappedSuperclass 中处理关系, 所以只能
     * 在每个子类中自行设置
     */

    public abstract String generateSerialNumber(Cooperator cooper);
}
