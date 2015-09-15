package models.finance;

import helper.Currency;
import models.market.Account;
import models.market.M;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * 记录在销售过程中, 不同市场产生的服务费用;
 * 非订单费用
 * Created by licco on 15/9/7.
 */
@Entity
public class ServiceFee extends Model{

    public String groupId;

    public String feeType;

    public String feeReason;

    @OneToOne
    public Account account;

    @Enumerated(EnumType.STRING)
    public M market;

    /**
     * group开始时间
     */
    public Date startDate;

    /**
     * group结束时间
     */
    public Date endDate;

    /***
     * 创建时间
     */
    public Date createDate;

    /**
     * 费用, 系统内的费用使用 USD 结算
     */
    @Column(nullable = false)
    public Float cost;

    @Enumerated(EnumType.STRING)
    public Currency currency;

    /**
     * 最终统计成 USD 的价格
     */
    public Float usdCost;

    /**
     * 成功则为1
     */
    public int feeflag;

    public String sku;

    public String memo;



}