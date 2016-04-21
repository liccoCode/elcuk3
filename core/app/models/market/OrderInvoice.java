package models.market;

import com.google.gson.annotations.Expose;
import helper.OrderInvoiceFormat;
import models.finance.SaleFee;
import play.db.jpa.GenericModel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 订单发票
 * User: cary
 * Date: 21/9/14
 * Time: 10:18 AM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class OrderInvoice extends GenericModel {
    /**
     * 订单的编码
     */
    @Id
    @Expose
    public String orderid;

    /**
     * 发票更新日期
     */
    public Date updateDate;

    /**
     * 发票更新人
     */
    public String updator;


    /**
     * 发票地址
     */
    public String invoiceto;

    /**
     * 用户地址
     */
    public String address;

    /**
     * 未含税金额
     */
    public float notaxamount;

    /**
     * 含税金额
     */
    public float taxamount;

    /**
     * 总金额
     */
    public float totalamount;


    /**
     * 价格
     */
    @Transient
    public List<Float> price;

    /**
     * 修改的价格
     */
    public String editprice;


    /**
     * 是否欧盟税号
     */
    public VAT europevat;

    @Transient
    public int isreturn;


    public enum VAT {
        /**
         * 普通发票
         */
        NORMAL {
            @Override
            public String label() {
                return "普通税号";
            }
        },
        /**
         * 欧盟发票
         */
        EUROPE {
            @Override
            public String label() {
                return "欧盟税号";
            }
        };

        public abstract String label();
    }


    @Transient
    public static float devat = 1.19f;

    @Transient
    public static float buyervat = 1f;

    @Transient
    public static float ukvat = 1.20f;

    @Transient
    public static float frvat = 1.20f;

    @Transient
    public static float itvat = 1.22f;

    @Transient
    public static float esvat = 1.20f;


    /**
     * 价格信息转化给前台显示
     */
    public void setprice() {
        if(editprice != null) {
            String[] prices = editprice.split(",");
            this.price = new ArrayList<Float>();
            for(String p : prices) {
                this.price.add(new Float(p));
            }
        }
    }


    /**
     * 保存价格信息
     */
    public void saveprice() {
        this.editprice = "";
        for(float p : this.price) {
            this.editprice = this.editprice + p + ",";
        }
    }


    /**
     * 判断invoice是否有效
     */
    public boolean checkInvoice(Orderr ord) {
        int pricecount = ord.items.size();
        for(int i = 0; i < ord.fees.size(); i++) {
            SaleFee fee = ord.fees.get(i);
            if(Arrays.asList("shipping", "shippingcharge", "giftwrap").contains(fee.type.name)) {
                pricecount++;
            }
        }
        if(pricecount > this.price.size()) {
            this.delete();
            return false;
        }
        return true;
    }


    /**
     * 格式化发票的信息
     *
     * @param m
     * @return
     */
    public static OrderInvoiceFormat invoiceformat(M m) {
        if(m == M.AMAZON_DE) {
            return OrderInvoiceFormat.newDe();
        } else if(m == M.AMAZON_UK) {
            return OrderInvoiceFormat.newUk();
        } else if(m == M.AMAZON_IT) {
            return OrderInvoiceFormat.newIt();
        } else if(m == M.AMAZON_FR) {
            return OrderInvoiceFormat.newFr();
        } else if(m == M.AMAZON_ES) {
            return OrderInvoiceFormat.newEs();
        }
        return null;
    }

}