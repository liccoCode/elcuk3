package models.view.dto;

import models.procure.ProcureUnit;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 16/5/13
 * Time: 上午11:37
 */
public class PurchasePaymentDTO implements Serializable {


    public String cooperator;

    public String deliverymentId;

    public String unitId;

    public String sku;

    public ProcureUnit.STAGE stage;

    public int qty;
    public int containTax;

    public float price;

    public String currency;

    /**
     * 采购总额
     */
    public float totalPurchases;

    /**
     * 请款总额
     */
    public float totalPayment;

    /**
     * 已请款金额(已支付)
     */
    public float paidAmount;

    /**
     * 已请款未支付金额
     */
    public float notPayAmount;

    /**
     * 未请款未支付金额
     */
    public float leftAmount;

    public String payment;

    public String url = "https://e.easyacc.com/";



}
