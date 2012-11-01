package models.view.dto;

import models.procure.Deliveryment;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来生成 采购单 Excel 文档的 Model
 * User: wyattpan
 * Date: 10/31/12
 * Time: 4:48 PM
 */
public class DeliveryExcel {

    /**
     * 交货地址
     */
    public String deliveryAddress;

    /**
     * 收货人
     */
    public String receiver;

    /**
     * 收货人移动电话
     */
    public String receiverPhone;

    /**
     * 收货人固定电话
     */
    public String receiverTel;

    /**
     * 交易条款
     */
    public String tradeTerms;

    /**
     * 产品要求
     */
    public String productTerms;

    /**
     * 供货方
     */
    public String supplierCompany;

    /**
     * 供货方经办人
     */
    public String supplier;

    public String supplierPhone;

    public String supplierTel;

    public String supplierFax;

    public String supplierAddress;

    /**
     * 买方
     */
    public String buyerCompany;

    /**
     * 买方经办人
     */
    public String buyer;

    public String buyerPhone;

    public String buyerTel;

    public String buyerFax;

    public String buyerAddress;

    public Deliveryment dmt;

    /**
     * 生成 Product Terms
     *
     * @return
     */
    public String productTerms() {
        if(StringUtils.isBlank(this.productTerms)) return "产品要求:\n";
        StringBuilder terms = new StringBuilder("产品要求:\n");
        String[] lines = StringUtils.split(this.productTerms, "\n");
        for(int i = 0; i < lines.length; i++)
            terms.append(i + 1).append(".").append(lines[i]).append("\n");
        return terms.toString();
    }

    public List<String> tradeTerms() {
        List<String> termLines = new ArrayList<String>();
        if(StringUtils.isBlank(this.tradeTerms)) return termLines;
        String[] lines = StringUtils.split(this.tradeTerms);
        for(int i = 0; i < lines.length; i++)
            termLines.add(String.format("%s.%s", i + 1, lines[i]));
        return termLines;
    }
}
