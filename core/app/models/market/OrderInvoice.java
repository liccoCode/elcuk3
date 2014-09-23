package models.market;

import com.google.gson.annotations.Expose;
import helper.Cached;
import helper.DBUtils;
import helper.Dates;
import helper.Promises;
import models.embedded.ERecordBuilder;
import models.finance.SaleFee;
import models.view.dto.DashBoard;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Email;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.templates.JavaExtensions;
import query.OrderrQuery;
import query.vo.OrderrVO;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

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


    public enum VAT {
        /**
         * 普通发票
         */
        NORMAL {
            @Override
            public String label() {
                return "普通发票";
            }
        },
        /**
         * 欧盟发票
         */
        EUROPE {
            @Override
            public String label() {
                return "欧盟发票";
            }
        };

        public abstract String label();
    }


    @Transient
    public static float devat = 1.19f;


    public void setprice() {
        if(editprice != null) {
            String[] prices = editprice.split(",");
            this.price = new ArrayList<Float>();
            for(String p : prices) {
                this.price.add(new Float(p));
            }
        }
    }


    public void saveprice() {
        this.editprice = "";
        for(float p : this.price) {
            this.editprice = this.editprice + p + ",";
        }
    }


}