package models.procure;

import com.google.gson.annotations.Expose;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2018/3/1
 * Time: 上午11:06
 */
@Entity
@DynamicUpdate
public class ShipmentMonthly extends Model {

    private static final long serialVersionUID = -4511960340937102240L;

    public ShipmentMonthly() {
        this.createDate = new Date();
    }

    @Expose
    @OneToOne
    public ProcureUnit unit;

    @Expose
    @OneToOne
    public ShipItem shipItem;

    public int year;

    public int month;

    public Date createDate;

    /**
     * 货运类型
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Required
    public Shipment.T type;

    /**
     * 实重
     */
    public Double realWeight;

    /**
     * 体积重
     */
    public Double volumeWeight;

    /**
     * 运费单价
     */
    public String price;

    /**
     * 关税预付手续费
     */
    public String customsPrepaidFee;

    /**
     * 自报关费
     */
    public String declarationFee;

    /**
     * 杂费
     */
    public String otherFee;

    /**
     * 总运费
     */
    public String totalShippingFee;

    /**
     * 货物申报价值
     */
    public String declaredValue;

    /**
     * 关税
     */
    public String tariff;

    /**
     * 丢货
     */
    public String lost;

}
