package models.whouse;

import com.google.gson.annotations.Expose;
import models.market.M;
import models.procure.Shipment;
import play.db.jpa.Model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.util.Date;

/**
 * 出货计划
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/1/16
 * Time: 2:55 PM
 */
@Entity
public class ShipPlan extends Model {
    /**
     * 运输单
     */
    @OneToOne
    public Shipment shipment;

    /**
     * 出货对象(SKU or 物料)
     */
    @Embedded
    @Expose
    public StockObj stockObj;

    @Expose
    public M market;

    /**
     * 出货数量
     */
    public Integer qty;

    /**
     * 预计出货时间
     */
    @Expose
    public Date planDate;

    @Expose
    public Date createDate = new Date();

    @Expose
    public Date updateDate = new Date();
}
