package models.finance;

import models.market.Account;
import models.market.M;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import java.util.Date;

/**
 * 通过ServiceFee API方式获得的订单信息而在Salefee表中找不到记录的 问题订单
 *
 * Created by licco on 15/9/11.
 */
@Entity
public class QuestionOrder extends Model {

    public String orderId;

    /**
     * 市场
     */
    @Enumerated(EnumType.STRING)
    public M market;

    public String memo;

    /**
     * 记录此订单的信息属于哪个GroupId,用于后期的统计分析
     */
    public String groupId;

    @OneToOne
    public Account account;

    public Date createDate;

    /**
     * 未处理为0
     * 处理完毕为1
     */
    public int flag;





}
