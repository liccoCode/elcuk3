package models.view.dto;


import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 8/2/15
 * Time: 6:03 PM
 */
public class ProfitDto implements Serializable {

    private static final long serialVersionUID = -6932525943590728789L;

    public String sku;
    public int qty;
    public float fee;
    public float amazon_fee;
    public float fba_fee;
    public float purchase_price;
    public float ship_price;
    public float vat_price;
    public float total_profit;
    public float profit_rate;

}
