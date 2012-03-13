package models.procure;

import models.market.Account;
import models.market.Selling;
import models.product.Product;
import models.product.Whouse;
import play.db.jpa.Model;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Transient;

/**
 * 采购模块使用的基础单元, 采购分析页面由此对象承载信息, 采购的流动由此对象承载信息.
 * User: wyattpan
 * Date: 3/2/12
 * Time: 12:01 PM
 */
//@Entity
public class PItem extends Model {
    //    @OneToOne
    public Product product;//sku, category , 所对应的具体产品

    //    @OneToOne
    public Selling selling; // asin, 去往的 Selling/Listing

    //    @OneToOne
    public Whouse whouse; // whouse, 自己的存放仓库

    //    @OneToOne
    public Supplier supplier; // 采购的工厂


    //    @ManyToOne
    public Plan plan;

    //    @ManyToOne
    public Procure procure;

    //    @ManyToOne
    public Shipment shipment;

    public Account.M market; // market, 去往的市场

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


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PItem{");
//        sb.append("whouse=").append(whouse);
//        sb.append(", supplier=").append(supplier);
//        sb.append(", plan=").append(plan);
//        sb.append(", procure=").append(procure);
//        sb.append(", shipment=").append(shipment);
//        sb.append(", market=").append(market);
        sb.append(" title='").append(title).append('\'');
        sb.append(", qty=").append(qty);
        sb.append(", price=").append(price);
        sb.append(", state=").append(state);
        sb.append(", memo='").append(memo).append('\'');
        sb.append(", in=").append(in);
        sb.append(", inDay=").append(inDay);
        sb.append(", onWay=").append(onWay);
        sb.append(", onWayDay=").append(onWayDay);
        sb.append(", onWork=").append(onWork);
        sb.append(", onWorkDay=").append(onWorkDay);
        sb.append(", airPatch=").append(airPatch);
        sb.append(", airBuy=").append(airBuy);
        sb.append(", seaPatch=").append(seaPatch);
        sb.append(", seaBuy=").append(seaBuy);
        sb.append(", selling=").append(selling);
        sb.append(", product=").append(product);
        sb.append('}');
        return sb.toString();
    }
}
