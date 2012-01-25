package models.market;

import com.elcuk.mws.jaxb.ordertracking.*;
import helper.Currency;
import helper.Patterns;
import models.finance.SaleFee;
import models.product.Product;
import play.Logger;
import play.data.validation.Email;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import javax.xml.bind.JAXB;
import java.io.File;
import java.util.*;

/**
 * 系统内的核心订单
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:18 AM
 */
@Entity
public class Orderr extends GenericModel {
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

    /**
     * 订单是通过某一个账户下产生的; 这是一个单向的关系, 不需要 Account 知道. 需要的时候直接使用 SQL 语句进行反向查询
     */
    @OneToOne(fetch = FetchType.LAZY)
    public Account account;
    //-------------- Basic ----------------

    /**
     * 发送邮件到达了什么阶段. 默认从 0 开始;
     * TODO 具体的 0,1,2,3... 还需要进行讨论
     */
    public int emailed = 0;


    /**
     * 订单的编码
     */
    @Id
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
    public String buyer;

    /**
     * 快递接收人姓名
     */
    public String reciver;

    /**
     * 联系地址(街道, 等等)
     */
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

    /**
     * 在进行解析的 Order XML 文件的时候, 每次需要将更新的数据记录到数据库, 此方法将从 XML 解析出来的 Order 的信息更新到被托管的对象身上.
     */
    public void updateOrderInfo(Orderr orderr) {
        if(orderr.address != null) this.address = orderr.address;
        if(orderr.address1 != null) this.address1 = orderr.address1;
        if(orderr.arriveDate != null) this.arriveDate = orderr.arriveDate;
        if(orderr.buyer != null) this.buyer = orderr.buyer;
        if(orderr.city != null) this.city = orderr.city;
        if(orderr.country != null) this.country = orderr.country;
        if(orderr.createDate != null) this.createDate = orderr.createDate;
        if(orderr.email != null) this.email = orderr.email;
        if(orderr.market != null) this.market = orderr.market;
        if(orderr.memo != null) this.memo = orderr.memo;
        if(orderr.paymentDate != null) this.paymentDate = orderr.paymentDate;
        if(orderr.phone != null) this.phone = orderr.phone;
        if(orderr.postalCode != null) this.postalCode = orderr.postalCode;
        if(orderr.province != null) this.province = orderr.province;
        if(orderr.reciver != null) this.reciver = orderr.reciver;
        if(orderr.shipDate != null) this.shipDate = orderr.shipDate;
        if(orderr.shipLevel != null) this.shipLevel = orderr.shipLevel;
        if(orderr.shippingAmount != null) this.shippingAmount = orderr.shippingAmount;
        if(orderr.shippingService != null) this.shippingService = orderr.shippingService;
        if(orderr.state != null) this.state = orderr.state;
        if(orderr.totalAmount != null) this.totalAmount = orderr.totalAmount;
        if(orderr.trackNo != null) this.trackNo = orderr.trackNo;

        // 比较两个 OrderItems, 首先将相同的 OrderItems 更新回来, 然后将 New OrderItem 集合中出现的系统中不存在的给添加进来
        Set<OrderItem> newlyOi = new HashSet<OrderItem>();
        for(OrderItem noi : orderr.items) {
            for(OrderItem oi : this.items) {
                if(oi.equals(noi)) {
                    if(noi.createDate != null) oi.createDate = noi.createDate;
                    if(noi.discountPrice != null) oi.discountPrice = noi.discountPrice;
                    if(noi.feesAmaount != null) oi.feesAmaount = noi.feesAmaount;
                    if(noi.memo != null) oi.memo = noi.memo;
                    if(noi.price != null) oi.price = noi.price;
                    if(noi.productName != null) oi.productName = noi.productName;
                    if(noi.quantity != null) oi.quantity = noi.quantity;
                    if(noi.shippingPrice != null) oi.shippingPrice = noi.shippingPrice;
                    if(noi.product != null) oi.product = noi.product;
                    if(noi.selling != null) oi.selling = noi.selling;
                } else {
                    newlyOi.add(noi);
                }
            }
        }
        for(OrderItem noi : newlyOi) this.items.add(noi);

        this.save();
    }

    /**
     * 订单抓取第一步, 获取订单(FBA 为主)
     * 解析的文件中的所有订单; 需要区别市场
     *
     * @param file
     * @param market 确认是哪一个市场的, Amazon 还是 Ebay
     * @return
     */
    public static List<Orderr> parseAllOrderXML(File file, Account.M market) {
        switch(market) {
            case AMAZON_US:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_ES:
            case AMAZON_IT:
                return allOrderXML_Amazon(file);
            case EBAY_UK:
                return allOrderXML_Ebay(file);
            default:
                return new ArrayList<Orderr>();
        }
    }

    /**
     * 抓取订单第二步
     * 更新相信的订单信息, 比如 buyer, 地址等.
     *
     * @param file
     * @param market
     * @return
     */
    public static List<Orderr> parseUpdateOrderXML(File file, Account.M market) {
        return new ArrayList<Orderr>();
    }

    public static List<Orderr> allOrderXML_Ebay(File file) {
        //TODO 暂时还没有实现.
        return new ArrayList<Orderr>();
    }

    /**
     * 解析产生的订单, 这一部分主要提供的是 Amazon 的
     *
     * @param file
     * @return
     */
    public static List<Orderr> allOrderXML_Amazon(File file) {
        AmazonEnvelopeType envelopeType = JAXB.unmarshal(file, AmazonEnvelopeType.class);
        List<Orderr> orders = new ArrayList<Orderr>();
        for(MessageType message : envelopeType.getMessage()) {
            OrderType odt = message.getOrder();

            String amazonOrderId = odt.getAmazonOrderID().toUpperCase();
            if(amazonOrderId.startsWith("S02") || Patterns.A2Z.matcher(amazonOrderId).matches()) {
                Logger.info("OrderId {} Can Not Be Add to Normal Order", amazonOrderId);
                continue;
            }

            Orderr orderr = new Orderr();
            orderr.orderId = amazonOrderId;
            orderr.market = Account.M.val(odt.getSalesChannel());
            orderr.createDate = odt.getPurchaseDate().toGregorianCalendar().getTime();
            orderr.state = parseOrderState(odt.getOrderStatus());

            if(orderr.state.ordinal() >= S.PAYMENT.ordinal()) {
                Date lastUpdateTime = odt.getLastUpdatedDate().toGregorianCalendar().getTime();
                orderr.paymentDate = lastUpdateTime;
                orderr.shipDate = lastUpdateTime;


                FulfillmentDataType ffdt = odt.getFulfillmentData();
                orderr.shipLevel = ffdt.getShipServiceLevel();

                AddressType addtype = ffdt.getAddress();
                orderr.city = addtype.getCity(); // 在国外, 一般情况下只需要 City, State(Province), PostalCode 就可以定位具体地址了
                orderr.province = addtype.getState();
                orderr.postalCode = addtype.getPostalCode();
                orderr.country = addtype.getCountry();
            }

            List<OrderItemType> oits = odt.getOrderItem();
            List<OrderItem> orderitems = new ArrayList<OrderItem>();

            Float totalAmount = 0f;
            Float shippingAmount = 0f;
            for(OrderItemType oid : oits) {
                /**
                 * 0. 检查这个 order 是否需要进行补充 orderitem
                 * 1. 将 orderitem 的基本信息补充完全
                 * 2. 检查 orderitems List 中时候已经存在相同的产品了, 如果有这修改已经存在的产品的数量否则才添加新的
                 */
                if(oid.getQuantity() <= 0) continue;//只有数量为 0 这没必要记录, 但如果订单为 Cancel 还是有必要记录的

                OrderItem oi = new OrderItem();
                oi.order = orderr;
                oi.productName = oid.getProductName();
                oi.quantity = oid.getQuantity();
                oi.createDate = orderr.createDate; // 这个字段是从 Order 转移到 OrderItem 上的一个冗余字段, 方便统计使用

                Product product = Product.find("sku=?", oid.getSKU().split(",")[0].toUpperCase()).first();
                Selling selling = Selling.find("asin=? AND market=?", oid.getASIN().toUpperCase(), orderr.market).first();
                if(product != null) oi.product = product;
                else {
                    // TODO 发送邮件提醒自己有产品不存在!
                    Logger.error("SKU[%s] is not in PRODUCT, it can not be happed!!", oid.getSKU().split(",")[0].toUpperCase());
                    continue; // 发生了这个错误, 这跳过这个 orderitem 
                }
                if(selling != null) oi.selling = selling;
                else {
                    // TODO 发送邮件提醒自己有产品不存在!
                    Logger.error("Selling[%s %s] is not in SELLING, it can not be happed!!", oid.getASIN().toUpperCase(), orderr.market.toString());
                    continue;
                }
                oi.id = String.format("%s_%s", orderr.orderId, product.sku);

                // price calculate
                oi.price = oi.discountPrice = oi.feesAmaount = oi.shippingPrice = 0f; // 初始化值
                if(orderr.state == S.CANCEL) { //基本信息完成后, 如果订单是取消的, 那么价格等都设置为 0 , 不计入计算并
                    addIntoOrderItemList(orderitems, oi);
                    continue;
                }

                ItemPriceType ipt = oid.getItemPrice();
                if(ipt == null) { //如果价格还没有出来, 表示在 Amazon 上数据还没有及时到位, 暂时不记录价格数据
                    Logger.warn("Order[%s] orderitem don`t have price yet.", orderr.orderId);
                } else {
                    List<ComponentType> costs = oid.getItemPrice().getComponent();
                    for(ComponentType ct : costs) { // 价格在这个都要统一成为 GBP (英镑), 注意不是 EUR 欧元
                        AmountType at = ct.getAmount();
                        String compType = ct.getType().toLowerCase();
                        Currency currency = Currency.valueOf(at.getCurrency());
                        if("principal".equals(compType)) {
                            oi.price = currency.toGBP(at.getValue());
                            totalAmount += oi.price;
                        } else if("shipping".equals(compType)) {
                            oi.shippingPrice = currency.toGBP(at.getValue());
                            shippingAmount += oi.shippingPrice;
                        } else if("giftwrap".equals(compType)) {
                            oi.memo += "\nGiftWrap: " + currency.toGBP(at.getValue()) + " GBP."; //这个价格暂时不知道如何处理, 所以就直接记录到中性字段中
                        }
                    }

                    // 计算折扣了多少钱
                    PromotionType promotionType = oid.getPromotion();
                    if(promotionType != null) {
                        if(promotionType.getShipPromotionDiscount() != null) {
                            oi.shippingPrice = oi.shippingPrice - promotionType.getShipPromotionDiscount();
                        }
                        if(promotionType.getItemPromotionDiscount() != null) {
                            oi.discountPrice = promotionType.getItemPromotionDiscount();
                        }
                    }
                }

                addIntoOrderItemList(orderitems, oi);
            }

            orderr.totalAmount = totalAmount;
            orderr.shippingAmount = shippingAmount;
            orderr.items = orderitems;
            orders.add(orderr);
        }
        return orders;
    }

    /**
     * 判断将 OrderItem 是否能够添加如已经存在的 List[OrderItem]
     *
     * @param list
     * @param oi
     * @return
     */
    private static boolean addIntoOrderItemList(List<OrderItem> list, OrderItem oi) {
        if(list.contains(oi)) {
            for(OrderItem item : list) {
                if(!item.equals(oi)) continue;
                item.quantity = item.quantity + oi.quantity;
                Logger.info("merge one orderItem[%s] belong to order %s, see the details goto %s",
                        oi.product.sku,
                        oi.order.orderId,
                        "https://sellercentral.amazon.co.uk/gp/orders-v2/details?ie=UTF8&orderID=" + oi.order.orderId
                );
            }
            return false;
        } else {
            list.add(oi);
            return true;
        }
    }

    private static S parseOrderState(String orderState) {
        // {Shipped=7729, Cancelled=339, Shipping=1, Pending=1723}, 一个 11MB 的文件中的类型
        String orderSt = orderState.toLowerCase();
        if("pending".equals(orderSt)) {
            return S.PENDING;
        } else if("shipped".equals(orderSt)) {
            return S.SHIPPED;
        } else if("unshipped".equals(orderSt)) {
            return S.PAYMENT;
        } else if("shipping".equals(orderSt)) {
            return S.PAYMENT;
        } else if("cancelled".equals(orderSt)) {
            return S.CANCEL;
        } else {
            return S.PENDING;
        }
    }

    public void setPostalCode(String postalCode1) {
        if(postalCode1 != null) this.postalCode = postalCode1.toUpperCase();
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

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Orderr orderr = (Orderr) o;

        if(!orderId.equals(orderr.orderId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + orderId.hashCode();
        return result;
    }
}
