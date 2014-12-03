package models.view.dto;

import helper.Currency;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-12-2
 * Time: 下午4:04
 */
public class ApplyPaymentDTO {
    private static final long serialVersionUID = -6922564943590728789L;

    public ApplyPaymentDTO() {
        this.total_fee = 0f;
        this.approval_fee = 0f;
        this.noapproval_fee = 0f;
    }

    /**
     * 货币
     */
    public Currency currency;
    /**
     * 总金额
     */
    public float total_fee;
    /**
     * 批准总金额
     */
    public float approval_fee;
    /**
     * 未批准总金额
     */
    public float noapproval_fee;
}
