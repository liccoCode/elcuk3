package models.finance;

import models.market.Account;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;

/**
 * 记录在销售过程中, 不同市场产生的不同的费用
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:58 AM
 */
@Entity
public class SaleFee extends Model {

    /**
     * 费用的状态, 影响记录费用的情况
     */
    public enum S {
        NORMAL,
        NEEDCONFIRM,
        EXCLUDE
    }

    @OneToOne
    public Account account;

    /**
     * 费用的名称
     */
    public String name;

    /**
     * 费用的总花费
     */
    public Float amount;

    /**
     * 费用的状态
     */
    @Enumerated(EnumType.STRING)
    public S state;

    /**
     * 这项费用一个寄存临时信息的地方
     */
    public String memo;
}
