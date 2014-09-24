package models.market;

import com.google.gson.annotations.Expose;
import helper.Cached;
import helper.DBUtils;
import helper.Dates;
import helper.Promises;
import models.finance.SaleFee;
import models.view.dto.DashBoard;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Email;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.templates.JavaExtensions;
import query.OrderrQuery;
import query.vo.OrderrVO;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
     * 是否为有问题的订单.
     */
    public boolean warnning = false;


    /**
     * 如果导入salefee则feeflag=2
     */
    public int feeflag;


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
                return "Thanks for purchasing from EasyAcc on " +
                        JavaExtensions.capFirst(this.market.toString()) + " (Order: " +
                        this.orderId + ")";
            case AMAZON_US:
                return "Thanks for purchasing from EasyAcc on " +
                        JavaExtensions.capFirst(this.market.toString()) + " (Order: " +
                        this.orderId + ")";
            case AMAZON_DE:
                return "Vielen Dank für den Kauf unseres EasyAcc Produkts auf Amazon.de (Bestellung: " +
                        this.orderId + ")";
            default:
                Logger.warn(String.format("MailTitle is not support [%s] right now.", this.market));
                return "";
        }
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

        dashBoard = new DashBoard();
        /**
         * 1. 将不同市场的 yyyy-MM-dd 日期的订单加载出来
         * 2. 对订单按照时间进行 group. 由于时间会调整, 不能使用 DB 的 group
         */

        final DateTime now = DateTime.parse(DateTime.now().toString("yyyy-MM-dd"));
        final Date pre7Day = now.minusDays(Math.abs(days)).toDate();

        List<OrderrVO> vos = new ArrayList<OrderrVO>();
        List<List<OrderrVO>> results = Promises.forkJoin(new Promises.DBCallback<List<OrderrVO>>() {
            @Override
            public List<OrderrVO> doJobWithResult(M m) {
                return new OrderrQuery().dashBoardOrders(
                        m.withTimeZone(pre7Day).toDate(),
                        m.withTimeZone(now.toDate()).toDate(),
                        m,
                        getConnection());
            }

            @Override
            public String id() {
                return "Orderr.frontPageOrderTable";
            }
        });
        for(List<OrderrVO> result : results) {
            vos.addAll(result);
        }

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
        return dashBoard;
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


    public F.T3<Float, Float, Float> amount() {
        Float totalamount = 0f;
        OrderInvoice invoice = OrderInvoice.findById(this.orderId);
        for(OrderItem item : this.items) {
            totalamount = totalamount + new BigDecimal(item.price).setScale(2, 4).floatValue();
        }
        totalamount = new BigDecimal(totalamount).setScale(2, 4).floatValue()

        Float notaxamount = 0f;
        if(invoice != null && invoice.europevat == OrderInvoice.VAT.EUROPE) {
            notaxamount = new BigDecimal(totalamount).divide(new BigDecimal(OrderInvoice.buyervat), 2,
                    java.math.RoundingMode.HALF_DOWN).floatValue();
        } else {
            notaxamount = new BigDecimal(totalamount).divide(new BigDecimal(this.orderrate()), 2,
                    java.math.RoundingMode.HALF_DOWN).floatValue();
        }
        Float tax = new BigDecimal(totalamount).subtract(new BigDecimal(notaxamount)).setScale(2, 4).floatValue();
        return new F.T3<Float, Float, Float>(totalamount, notaxamount, tax);
    }


    public String formataddress() {
        String editaddress = "";
        if(!StringUtils.isBlank(this.reciver)) {
            editaddress = editaddress + "," + this.reciver;
        }
        if(!StringUtils.isBlank(this.address)) {
            editaddress = editaddress + "," + this.address;
        }
        if(!StringUtils.isBlank(this.city)) {
            editaddress = editaddress + "," + this.city;
        }
        if(!StringUtils.isBlank(this.province)) {
            editaddress = editaddress + "," + this.province;
        }
        if(!StringUtils.isBlank(this.postalCode)) {
            editaddress = editaddress + "," + this.postalCode;
        }
        editaddress = editaddress + ",Deutschland";
        if(!StringUtils.isBlank(this.phone)) {
            editaddress = editaddress + ",phone:" + this.phone;
        }
        if(editaddress != null && editaddress.indexOf(",") == 0) {
            editaddress = editaddress.substring(1, editaddress.length());
        }
        return editaddress;
    }

    public boolean refundmoney() {
        SqlSelect itemsql = new SqlSelect()
                .select("sum(cost) as cost")
                .from("SaleFee s")
                .where(" s.order_orderid='" + this.orderId + "' and type_name='productcharges' ");
        List<Map<String, Object>> rows = DBUtils.rows(itemsql.toString());
        float cost = 0f;
        for(Map<String, Object> row : rows) {
            Object rowobject = row.get("cost");
            if(rowobject != null) {
                cost = cost + new Float(rowobject.toString());
            }
        }
        if(rows.size() <= 0 || cost > 0) {
            return false;
        } else {
            return true;
        }
    }

    public float orderrate() {
        if(market == M.AMAZON_DE) {
            return OrderInvoice.devat;
        }
        if(market == M.AMAZON_UK) {
            return OrderInvoice.ukvat;
        }
        if(market == M.AMAZON_FR) {
            return OrderInvoice.frvat;
        }
        if(market == M.AMAZON_IT) {
            return OrderInvoice.itvat;
        }
        return 0;
    }
}
