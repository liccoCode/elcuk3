package models.market;

import com.google.gson.annotations.Expose;
import models.finance.EbayFee;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/8/7
 * Time: 下午2:24
 */
@Entity
@DynamicUpdate
public class EbayOrder extends GenericModel {

    private static final long serialVersionUID = -329095108326285783L;
    /**
     * 订单的编码
     */
    @Id
    @Expose
    public String orderId;

    /**
     * 自己系统定义ID
     */
    @Expose
    public String merchantOrderId;

    /**
     * 订单所属的市场
     */
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) DEFAULT 'AMAZON_UK'")
    public M market;

    @OneToOne(fetch = FetchType.LAZY)
    public Account account;

    /**
     * 订单的状态
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public Orderr.S state;

    public enum T {
        EBAY,
        MANUAL
    }

    @Required
    @Expose
    @Enumerated(EnumType.STRING)
    public T type;

    /**
     * 订单的付款时间
     */
    public Date paymentDate;

    /**
     * 是否为补发订单
     */
    public boolean replacementOrder = false;

    /**
     * 订单里面包含的产品个数
     */
    public int numberOfItemsShipped;


    /**
     * 订单里面的没运输产品个数
     */
    public int numberOfItemsUnShipped;

    /**
     *
     */
    public String shipServiceLevel;

    public String salesChannel;

    public boolean businessOrder = false;

    public String marketplaceId;

    public String fulfillmentChannel;

    /**
     * 订单创建时间
     */
    public Date createDate;


    public String stateOrRegion;

    /**
     * 快递接收人姓名
     */
    public String reciver;

    public String phone;

    /**
     * 联系地址(街道, 等等)
     */
    public String address2;

    /**
     * 备注地址 1
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    public String address1;

    /**
     * 城市,一级地名
     */
    public String city;

    /**
     * 州/省一类的,二级地名
     */
    public String province;

    /**
     * 邮编
     */
    public String postalCode;

    /**
     * 国家
     */
    public String country;
    /**
     * 是否使用 MWS 同步过 SaleFee 数据
     */
    public boolean synced = false;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    public List<EbayOrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    public List<EbayFee> fees = new ArrayList<>();

    public double totalFbaFee() {
        return new BigDecimal(this.fees.stream().mapToDouble(fee -> fee.cost).sum())
                .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

}
