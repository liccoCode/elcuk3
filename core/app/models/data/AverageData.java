package models.data;

import models.market.M;
import models.market.Selling;
import models.product.Product;
import play.db.jpa.Model;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
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

    public Date update;

}
