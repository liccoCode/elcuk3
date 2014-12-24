package models.view.dto;

import helper.Currency;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-12-2
 * Time: 下午4:04
 */
public class ApplyPaymentDTO {
    private static final long serialVersionUID = -6922564943590728789L;

    public ApplyPaymentDTO() {
        this.total_fee = new BigDecimal(0);
        this.approval_fee = new BigDecimal(0);
        this.noapproval_fee = new BigDecimal(0);
    }

    /**
     * 货币
     */
    public Currency currency;
    /**
     * 总金额
     */
    public BigDecimal total_fee;
    /**
     * 批准总金额
     */
    public BigDecimal approval_fee;
    /**
     * 未批准总金额
     */
    public BigDecimal noapproval_fee;
}
