package models.view.dto;

import models.market.M;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-6-9
 * Time: AM11:48
 */
public class SaleReportDTO implements Serializable {
    private static final long serialVersionUID = -5659439515110844774L;

    /**
     * 产品线
     */
    public String categoryId;

    /**
     * sku
     */
    public String sku;

    /**
     * sellingId
     */
    public String sellingId;

    /**
     * 市场
     */
    public M market;

    /**
     * 销量
     */
    public Float sales;

    /**
     * 销售额
     */
    public Float salesAmount;


}
