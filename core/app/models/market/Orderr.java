package models.market;

import com.google.gson.annotations.Expose;
import helper.Cached;
import helper.DBUtils;
import helper.Dates;
import helper.Promises;
import models.finance.SaleFee;
import models.view.dto.DashBoard;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Email;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import play.templates.JavaExtensions;
import query.OrderrQuery;
import query.vo.OrderrVO;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * 系统内的核心订单
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:18 AM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Orderr extends GenericModel {
    public static final String FRONT_TABLE = "Orderr.frontPageOrderTable";
    public static final Pattern AMAZON_ORDERID = Pattern.compile("^\\d{3}-\\d{7}-\\d{7}$");

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
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public List<OrderItem> items = new ArrayList<OrderItem>();

    /**
     * 订单所属的市场
     */
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) DEFAULT 'AMAZON_UK'")
    public M market;

    @OneToOne(mappedBy = "orderr", fetch = FetchType.LAZY)
    public Feedback feedback;

    /**
     * 订单是通过某一个账户下产生的; 这是一个单向的关系, 不需要 Account 知道. 需要的时候直接使用 SQL 语句进行反向查询
     */
    @OneToOne(fetch = FetchType.LAZY)
    public Account account;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @OrderBy("date ASC,cost DESC")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public List<SaleFee> fees = new ArrayList<SaleFee>();
    //-------------- Basic ----------------

    /**
     * 是否发送了 Review Mail
     */
    public boolean reviewMailed = false;


    /**
     * 订单的编码
     */
    @Id
    @Expose
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
     * 用来关联其他 Amazon 消息的用户的标识符
     */
    public String userid;

    /**
     * 联系地址(街道, 等等)
     */
    public String address;

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
    @Lob
    public String memo;

    /**
     * 用来记录此订单使用抓取更新的次数
     */
    public Integer crawlUpdateTimes = 0;

    /**
     * 是否为有问题的订单.
     */
    public boolean warnning = false;


    /**
     * 在进行解析的 Order XML 文件的时候, 每次需要将更新的数据记录到数据库, 此方法将从 XML 解析出来的 Order 的信息更新到被托管的对象身上.
     */
    public void updateAttrs(Orderr newOrderr) {
        if(newOrderr.address != null) this.address = newOrderr.address;
        if(newOrderr.address1 != null) this.address1 = newOrderr.address1;
        if(newOrderr.arriveDate != null) this.arriveDate = newOrderr.arriveDate;
        if(newOrderr.buyer != null) this.buyer = newOrderr.buyer;
        if(newOrderr.city != null) this.city = newOrderr.city;
        if(newOrderr.country != null) this.country = newOrderr.country;
        if(newOrderr.createDate != null) this.createDate = newOrderr.createDate;
        if(newOrderr.email != null) this.email = newOrderr.email;
        if(newOrderr.market != null) this.market = newOrderr.market;
        if(newOrderr.memo != null) this.memo = newOrderr.memo;
        if(newOrderr.paymentDate != null) this.paymentDate = newOrderr.paymentDate;
        if(newOrderr.phone != null) this.phone = newOrderr.phone;
        if(newOrderr.postalCode != null) this.postalCode = newOrderr.postalCode;
        if(newOrderr.province != null) this.province = newOrderr.province;
        if(newOrderr.reciver != null) this.reciver = newOrderr.reciver;
        if(newOrderr.shipDate != null) this.shipDate = newOrderr.shipDate;
        if(newOrderr.shipLevel != null) this.shipLevel = newOrderr.shipLevel;
        if(newOrderr.shippingAmount != null) this.shippingAmount = newOrderr.shippingAmount;
        if(newOrderr.shippingService != null) this.shippingService = newOrderr.shippingService;
        if(newOrderr.totalAmount != null) this.totalAmount = newOrderr.totalAmount;
        if(newOrderr.trackNo != null) this.trackNo = newOrderr.trackNo;
        if(newOrderr.state != null) {
            // 这几个状态是不可逆的, 如果有其他地方将订单更新成这几个状态, 那么此订单的状态不允许再进行更改!
            if(newOrderr.state == S.REFUNDED || newOrderr.state == S.CANCEL ||
                    newOrderr.state == S.RETURNNEW) return;
            this.state = newOrderr.state;
        }

        if(newOrderr.items.size() > 0) {
            // 比较两个 OrderItems, 首先将相同的 OrderItems 更新回来, 然后将 New OrderItem 集合中出现的系统中不存在的给添加进来
            Set<OrderItem> newlyOi = new HashSet<OrderItem>();

            // 如果原始的 Order 中一个 OrderItem 都没有
            if(this.items.size() == 0) {
                newlyOi.addAll(newOrderr.items);
            } else if(this.items.size() != newOrderr.items.size()) { // 如果原始的 Order 中有 OrderItem
                for(OrderItem noi : newOrderr.items) {
                    for(OrderItem oi : this.items) {
                        if(oi.equals(noi)) {
                            oi.updateAttr(noi);
                            // 表示一级缓存中没有, 那么才可以进入 newlyOrderItem 添加, 否则应该为更新
                        } else if(!JPA.em().contains(oi)) {
                            newlyOi.add(noi);
                        }
                    }
                }
            }
            // 如果有两个相同的 object_id 相同的对象添加进入 Orderr.items 进行级联保存或者更新, 那么会在 hibernate 进行保存更新
            // 检查唯一性的时候发生异常! 所以上面的 JPA.em().contains(oi) 判断很有必要!
            for(OrderItem noi : newlyOi) {
                noi.order = this;
                noi.save();
                this.items.add(noi);
            }
        }

        this.save();
    }


    public void setPostalCode(String postalCode1) {
        if(postalCode1 != null) this.postalCode = postalCode1.toUpperCase();
    }

    /**
     * 此订单总共卖出的产品数量
     * ps: 对于分页使用这个方法, 性能暂时不是需要考虑的问题
     *
     * @return
     */
    public Long itemCount() {
        BigDecimal qty = (BigDecimal) DBUtils
                .row("select sum(quantity) as qty from OrderItem where order_orderId=?",
                        this.orderId).get("qty");
        return qty == null ? 0l : qty.longValue();
    }

    /**
     * Review 邮件的 Title 生成
     *
     * @return
     */
    public String reviewMailTitle() {
        /**
         *  1. 首先寻找 Saner 或 Fencer, 找到第一个出现的产品即选择
         *  2. 再使用默认的 Title
         *
         *  ps: 所有的 Title 计算出来了直接返回, 不进行参数组装什么的.
         */
        switch(this.market) {
            case AMAZON_UK:
            case AMAZON_US:
                return "Thanks for purchasing EasyAcc Product on " +
                        JavaExtensions.capFirst(this.market.toString()) + " (Order: " +
                        this.orderId + ")";
            case AMAZON_DE:
                return "Vielen Dank für den Kauf EasyAcc Produkte auf Amazon.de (Bestellung: " +
                        this.orderId + ")";
            default:
                Logger.warn(String.format("MailTitle is not support [%s] right now.", this.market));
                return "";
        }
    }

    public boolean isHaveFeedback() {
        return Feedback.count("orderr=?", this) > 0;
    }

    /**
     * 这个订单的销售额.
     *
     * @return
     */
    public float totalUSDSales() {
        float totalSales = 0;
        if(this.market.name().startsWith("AMAZON")) {
            for(SaleFee fee : this.fees) {
                if(fee.type.parent != null && !"amazon".equals(fee.type.parent.name)) continue;
                if("principal".equals(fee.type.name) || "productcharges".equals(fee.type.name))
                    totalSales += fee.usdCost;
            }
        }
        return totalSales;
    }

    /**
     * 不同市场上所有收取的费用
     *
     * @return
     */
    public float totalMarketFee() {
        float totalMarketFee = 0;
        if(this.market.name().startsWith("AMAZON")) {
            for(SaleFee fee : this.fees) {
                if("principal".equals(fee.type.name) || "productcharges".equals(fee.type.name))
                    continue;
                if(fee.type.parent != null && !"amazon".equals(fee.type.parent.name)) continue;
                totalMarketFee += fee.usdCost;
            }
        }
        return Math.abs(totalMarketFee);
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

    // -------------------------

    /**
     * <pre>
     * 前台页面使用的, 查看最近 N 天内的订单情况分布的表格;
     * 把所有需要展示的数据做成 2 级 Map:
     *  - 第一级为时间
     *  - 第二级为数据行
     *   > 每一个数据行根据不同的 key 包含不维度的数据
     *   > all: 所有 state 的统计值
     *   > state: 根据日期, 所有 market, Account 的统计值
     *   > state_[market]: 根据状态 + 市场来区分的订单数据
     *   > all_[market]: 排除状态, 市场整天的订单数据
     *   > state_[account]: 根据状态 + 账户来区分的订单数据
     *   > all_[account]: 排除状态, 账户整天的订单数据
     * </pre>
     *
     * @param days
     * @return
     */
    @SuppressWarnings("unchecked")
    @Cached("1h")
    public static DashBoard frontPageOrderTable(int days) {
        DashBoard dashBoard = Cache.get(Orderr.FRONT_TABLE, DashBoard.class);
        if(dashBoard != null) return dashBoard;

        synchronized(Orderr.class) {
            dashBoard = Cache.get(Orderr.FRONT_TABLE, DashBoard.class);
            if(dashBoard != null) return dashBoard;
            dashBoard = new DashBoard();
            /**
             * 1. 将不同市场的 yyyy-MM-dd 日期的订单加载出来
             * 2. 对订单按照时间进行 group. 由于时间会调整, 不能使用 DB 的 group
             */

            final DateTime now = DateTime.parse(DateTime.now().toString("yyyy-MM-dd"));
            final Date pre7Day = now.minusDays(Math.abs(days)).toDate();

            List<OrderrVO> vos = new ArrayList<OrderrVO>();
            vos.addAll(Promises.forkJoin(new Promises.Callback<OrderrVO>() {
                @Override
                public List<OrderrVO> doJobWithResult(M m) {
                    return new OrderrQuery().dashBoardOrders(
                            m.withTimeZone(pre7Day).toDate(),
                            m.withTimeZone(now.toDate()).toDate(),
                            m);
                }

                @Override
                public String id() {
                    return "Orderr.frontPageOrderTable";
                }
            }));

            for(OrderrVO vo : vos) {
                String key = Dates.date2Date(vo.market.toTimeZone(vo.createDate));
                if(vo.state == S.PENDING)
                    dashBoard.pending(key, vo);
                else if(vo.state == S.PAYMENT)
                    dashBoard.payments(key, vo);
                else if(vo.state == S.CANCEL)
                    dashBoard.cancels(key, vo);
                else if(vo.state == S.REFUNDED)
                    dashBoard.refundeds(key, vo);
                else if(vo.state == S.RETURNNEW)
                    dashBoard.returnNews(key, vo);
                else if(vo.state == S.SHIPPED)
                    dashBoard.shippeds(key, vo);
            }
            dashBoard.sort();
            Cache.add(Orderr.FRONT_TABLE, dashBoard, "1h");
        }
        return Cache.get(Orderr.FRONT_TABLE, DashBoard.class);
    }


    /**
     * 查看某一天订单的饼图数据
     *
     * @param msku
     * @param date
     * @return
     */
    public static Map<String, AtomicInteger> orderPieChart(String msku, Date date) {
        DateTime day = Instant.parse(new DateTime(date.getTime()).toString("yyyy-MM-dd"))
                .toDateTime();
        Date dayBegin = day.toDate();
        Date dayEnd = day.plusDays(1).toDate();

        List<Orderr> orderrs = Orderr.ordersInRange(dayBegin, dayEnd);

        Map<String, AtomicInteger> rtMap = new LinkedHashMap<String, AtomicInteger>();
        for(Long begin = dayBegin.getTime(); begin < dayEnd.getTime();
            begin += TimeUnit.HOURS.toMillis(1)) {
            rtMap.put(begin.toString(), new AtomicInteger(0));
            for(Orderr or : orderrs) {
                for(OrderItem oi : or.items) {
                    if(oi.selling.merchantSKU.equals(msku) ||
                            "all".equalsIgnoreCase(msku)) {//如果搜索的 MerchantSKU 为 all 也进行计算
                        if(or.createDate.getTime() < begin ||
                                or.createDate.getTime() > begin + TimeUnit.HOURS.toMillis(1))
                            continue;

                        if(rtMap.containsKey(begin.toString())) {
                            rtMap.get(begin.toString()).incrementAndGet(); //因为是统计的订单, 所以数量都是以 1 递增
                        }
                    }
                }
            }
        }

        return rtMap;
    }


    /**
     * 此订单所关联的所有 OrderItem 的 SKU
     *
     * @return
     */
    public static String itemSkus(Orderr ord) {
        StringBuilder sbd = new StringBuilder();
        if(ord == null) return sbd.toString();
        for(OrderItem itm : ord.items) {
            sbd.append(itm.product.sku).append(" ");
        }
        return sbd.toString();
    }

    /**
     * 查找到此 userid 对应的所有订单
     *
     * @param userId
     * @return
     */
    public static List<Orderr> findByUserId(String userId) {
        return Orderr.find("userid=?", userId).fetch();
    }

    /**
     * 简单抽取常用的根据时间加载订单的方法
     *
     * @param from
     * @param to
     * @return
     */
    public static List<Orderr> ordersInRange(Date from, Date to) {
        return Orderr.find("createDate>=? AND createDate<=?", from, to).fetch();
    }

    /**
     * 返回数组集合的 orderIds
     *
     * @param orderrs
     * @return
     */
    public static List<String> ids(List<Orderr> orderrs) {
        if(orderrs == null) orderrs = new ArrayList<Orderr>();
        List<String> orderIds = new ArrayList<String>();
        for(Orderr o : orderrs) orderIds.add(o.orderId);
        return orderIds;
    }

    /**
     * 将 Orders 集合映射成为 Map, 方便遍历(减少两层循环的多次遍历循环)
     *
     * @param orderrs
     * @return
     */
    public static Map<String, Orderr> list2Map(List<Orderr> orderrs) {
        Map<String, Orderr> orderMap = new HashMap<String, Orderr>();
        for(Orderr or : orderrs) {
            orderMap.put(or.orderId, or);
        }
        return orderMap;
    }

    public static void warnningToDeal(Date from, Date to, M market) {
        DBUtils.row("UPDATE Orderr set warnning=false WHERE warnning=true AND market=?" +
                " AND createDate>=? AND createDate<=?",
                market.name(), market.withTimeZone(from), market.withTimeZone(to));
    }
}
