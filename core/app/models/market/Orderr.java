package models.market;

import com.elcuk.mws.jaxb.ordertracking.AmazonEnvelopeType;
import com.elcuk.mws.jaxb.ordertracking.MessageType;
import com.elcuk.mws.jaxb.ordertracking.OrderType;
import models.finance.SaleFee;
import play.Logger;
import play.data.validation.Email;
import play.data.validation.Valid;
import play.db.jpa.Model;

import javax.persistence.*;
import javax.xml.bind.JAXB;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 系统内的核心订单
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:18 AM
 */
@Entity
public class Orderr extends Model {
    /**
     * 订单的状态 State
     */
    public enum S {
        /**
         * 新创建的, 也就是预定的
         */
        PENDING,

        /**
         * 已经付款了的
         */
        PAYMENT,

        /**
         * 发货了的
         */
        SHIPPED,

        /**
         * 成功执行了的
         */
        SUCCESS,

        /**
         * 做了 refund 返款的的
         */
        REFUNDED,

        /**
         * 重新补货发送了的
         */
        RETURNNEW,

        /**
         * 取消了的
         */
        CANCEL
    }

    //-------------- Object ----------------
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    public List<OrderItem> items;

    /**
     * 此订单关联的费用, 如果订单删除, 费用也删除.
     */
    @OneToMany(cascade = CascadeType.ALL)
    public List<SaleFee> saleFees;

    /**
     * 订单所属的市场
     */
    @Enumerated(EnumType.STRING)
    public Account.M market;
    //-------------- Basic ----------------


    /**
     * 订单的编码
     */
    @Column(unique = true, nullable = false)
    public String orderId;

    /**
     * 订单的状态
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public S state;

    /**
     * 订单创建时间
     */
    public Date createDate;

    /**
     * 订单的付款时间
     */
    public Date paymentDate;

    /**
     * 订单的发送时间
     */
    public Date shipDate;

    /**
     * 订单的到达时间
     */
    public Date arriveDate;

    /**
     * 买家姓名
     */
    @Column(nullable = false)
    public String buyer;

    /**
     * 快递接收人姓名
     */
    public String reciver;

    /**
     * 联系地址(街道, 等等)
     */
    @Column(nullable = false)
    public String address;

    /**
     * 备注地址 1
     */
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
     * 联系的邮箱地址
     */
    @Column(nullable = false)
    @Email
    public String email;

    /**
     * 联系的电话号码
     */
    public String phone;

    /**
     * 订单所对应的快递单的 tracking Number
     */
    public String trackNo;

    /**
     * 快递方式
     */
    public String shippingService;

    /**
     * 快递发送的等级, 表示着订单发送的紧急程度
     */
    public String shipLevel;

    /**
     * 订单的总共价格(为了防止批发性质)
     */
    public Float totalAmount;

    /**
     * 订单的总共快递费用(主要是为了防止批发性质的购买订单)
     */
    public Float shippingAmount;

    /**
     * 订单备用信息
     */
    public String memo;

    // -------------------------
    private static final Pattern A2Z = Pattern.compile("[a-zA-Z]*");

    public static List<Orderr> parseALLOrderXML(File file) {
        AmazonEnvelopeType envelopeType = JAXB.unmarshal(file, AmazonEnvelopeType.class);
        List<Orderr> orders = new ArrayList<Orderr>();
        for(MessageType message : envelopeType.getMessage()) {
            OrderType odt = message.getOrder();

            String amazonOrderId = odt.getAmazonOrderID().toUpperCase();
            if(amazonOrderId.startsWith("S02") || A2Z.matcher(amazonOrderId).matches()) {
                Logger.info("OrderId {} Can Not Be Add to Normal Order", amazonOrderId);
                continue;
            }

            Orderr orderr = new Orderr();
            orderr.orderId = amazonOrderId;
            orderr.market = Account.M.val(odt.getSalesChannel());
            orderr.createDate = odt.getPurchaseDate().toGregorianCalendar().getTime();
        }
        return orders;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Orderr");
        sb.append("{market=").append(market);
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", state=").append(state);
        sb.append(", createDate=").append(createDate);
        sb.append(", paymentDate=").append(paymentDate);
        sb.append(", shipDate=").append(shipDate);
        sb.append(", arriveDate=").append(arriveDate);
        sb.append(", buyer='").append(buyer).append('\'');
        sb.append(", reciver='").append(reciver).append('\'');
        sb.append(", address='").append(address).append('\'');
        sb.append(", address1='").append(address1).append('\'');
        sb.append(", city='").append(city).append('\'');
        sb.append(", province='").append(province).append('\'');
        sb.append(", postalCode='").append(postalCode).append('\'');
        sb.append(", country='").append(country).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", phone='").append(phone).append('\'');
        sb.append(", trackNo='").append(trackNo).append('\'');
        sb.append(", shippingService='").append(shippingService).append('\'');
        sb.append(", shipLevel='").append(shipLevel).append('\'');
        sb.append(", totalAmount=").append(totalAmount);
        sb.append(", shippingAmount=").append(shippingAmount);
        sb.append(", memo='").append(memo).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
