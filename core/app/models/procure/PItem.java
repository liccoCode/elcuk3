package models.procure;

import models.market.Account;
import models.market.Selling;
import models.product.Product;
import models.product.Whouse;
import play.db.jpa.Model;

import javax.persistence.*;

/**
 * 采购模块使用的基础单元, 采购分析页面由此对象承载信息, 采购的流动由此对象承载信息.
 * User: wyattpan
 * Date: 3/2/12
 * Time: 12:01 PM
 */
@Entity
public class PItem extends Model {
    @OneToOne
    public Product product;//sku, category , 所对应的具体产品

    @OneToOne
    public Selling selling; // asin, 去往的 Selling/Listing

    @OneToOne
    public Account.M market; // market, 去往的市场

    @OneToOne
    public Whouse whouse; // whouse, 自己的存放仓库

    @OneToOne
    public Supplier supplier; // 采购的工厂

    /**
     * PItem 的几个状态
     */
    public enum S {
        NONE,//分析状态
        PLAN,
        PROCURE,
        SHIPMENT,
        INOUND
    }


    // ------------ For Normal Procure --------------------
    public String title; // 采购的商品名称
    public Integer qty; // 采购的数量
    public Float price; // 采购的价格

    @Enumerated(EnumType.STRING)
    public S state;

    @Lob
    public String memo;


    // ------------ For Procure Analyze -------------------
    @Transient
    public Integer in; // 在库
    @Transient
    public Float inDay; // qty / selling.ps

    @Transient
    public Integer onWay; //在途
    @Transient
    public Float onWayDay; // onWay / selling.ps

    @Transient
    public Integer onWork; // 在工厂
    @Transient
    public Float onWorkDay; // onWork / selling.ps

    @Transient
    public Integer airPatch; // 空运补货
    @Transient
    public Integer airBuy;  // 空运采购
    @Transient
    public Integer seaPatch; // 海运补货
    @Transient
    public Integer seaBuy; // 海运采购
}
