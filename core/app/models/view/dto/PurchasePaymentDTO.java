package models.view.dto;

import models.procure.Cooperator;
import models.procure.ProcureUnit;

import java.io.Serializable;

/**
 * Created by licco on 16/5/13.
 */
public class PurchasePaymentDTO implements Serializable {


    public String cooperator;

    public String deliverymentId;

    public String unitId;

    public String sku;

    public ProcureUnit.STAGE stage;

    public int qty;

    public float price;

    public String currency;

    /**
     * 采购总额
     */
    public float totalPurchases;

    /**
     * 已请款金额
     */
    public float paymentAmount;

    /**
     * 未支付金额
     */
    public float notPayAmount;

    public String payment;

    public String url = "https://e.easyacc.com/";



}