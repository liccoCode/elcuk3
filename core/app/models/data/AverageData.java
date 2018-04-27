package models.data;

import models.market.M;
import models.market.Selling;
import models.product.Product;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 平均采购价，运输费用，vat费用的实体
 * 每天计算一次
 * <p>
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2018/4/23
 * Time: 下午5:17
 */
@Entity
public class AverageData extends Model {

    private static final long serialVersionUID = 7749844390238105548L;

    @ManyToOne
    public Selling selling;

    @OneToOne
    public Product product;

    @Enumerated(EnumType.STRING)
    public M market;

    public BigDecimal averageProcurePrice;

    public BigDecimal averageShipPrice;

    public BigDecimal averageVATPrice;

    /**
     * 仓库库存（制作中+已交货）
     */
    public Long warehouseInventory;

    /**
     * amazon库存 （已入库+在库）
     */
    public Long amazonInventory;

    /**
     * 在途数量
     */
    public Long wayQty;

    /**
     * (制作中+已交货)库存占用资金总金额(USD)
     */
    public BigDecimal warehousePrice;

    /**
     * 在途库存占用资金总金额
     */
    public BigDecimal wayPrice;

    /**
     * (入库+在库)库存占用资金总金额(USD)
     */
    public BigDecimal amazonPrice;

    public Date updateDate;

}
