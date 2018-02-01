package models.market;

import com.amazonservices.mws.finances.MWSFinancesServiceClient;
import com.amazonservices.mws.finances.model.ListFinancialEventsRequest;
import com.amazonservices.mws.finances.model.ListFinancialEventsResponse;
import com.google.gson.annotations.Expose;
import helper.*;
import models.finance.SaleFee;
import models.view.dto.DashBoard;
import mws.MWSFinances;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
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
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * 系统内的核心订单
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:18 AM
 */
@Entity
@DynamicUpdate
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
         * 预售，未发货
         */
        UNSHIPPED,
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
    public List<OrderItem> items = new ArrayList<>();

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
    public List<SaleFee> fees = new ArrayList<>();
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
    public Date updateDate;

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
    @Column(columnDefinition = "int(11) DEFAULT '0'")
    public int feeflag;

    /**
     * 是否使用 MWS 同步过 SaleFee 数据
     */
    @Column(columnDefinition = "tinyint(1) DEFAULT '0'")
    public boolean synced = false;

    /**
     * 发票状态(已发送yes,未发送no) 默认no
     */
    @Column(columnDefinition = "varchar(10) DEFAULT 'no'")
    public String invoiceState;

    /**
     * osticket调用发票的时间
     */
    public Date invoiceDate;

    /**
     * 销售总额
     */
    public Float totalSale;

    /**
     * 是否使用 商务订单(用于JRockend 发送合同)
     */
    @Column(columnDefinition = "tinyint(1) DEFAULT '0'")
    public boolean businessOrder;
    /**
     * 是否发送商务合同(用于JRockend 发送合同)
     */
    public boolean sendBusiness;


    /**
     * 此订单总共卖出的产品数量
     * ps: 对于分页使用这个方法, 性能暂时不是需要考虑的问题
     *
     * @return
     */
    public Long itemCount() {
        BigDecimal qty = (BigDecimal) DBUtils
                .row("select sum(quantity) as qty from OrderItem where order_orderId=?", this.orderId)
                .get("qty");
        return qty == null ? 0L : qty.longValue();
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
                return "Thanks for purchasing from EasyAcc on " + JavaExtensions.capFirst(this.market.toString())
                        + " (Order: " + this.orderId + ")";
            case AMAZON_US:
                return "Thanks for purchasing from EasyAcc on " + JavaExtensions.capFirst(this.market.toString())
                        + " (Order: " + this.orderId + ")";
            case AMAZON_DE:
                return "Vielen Dank für den Kauf unseres EasyAcc Produkts auf Amazon.de (Bestellung: "
                        + this.orderId + ")";
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
     * 这个订单的当前币种销售额.
     */
    public float totalCurrencySales() {
        if(this.fees.size() == 0) {
            MWSFinancesServiceClient client = MWSFinances.client(this.account, this.account.type);
            ListFinancialEventsRequest request = new ListFinancialEventsRequest();
            request.setSellerId(this.account.merchantId);
            request.setMWSAuthToken(this.account.token);
            request.setAmazonOrderId(this.orderId);
            ListFinancialEventsResponse response = client.listFinancialEvents(request);
            SaleFee.parseFinancesApiResult(response, this.account);
            Orderr newOne = Orderr.findById(this.orderId);
            this.fees = newOne.fees;
        }
        float totalSales = 0;
        if(this.market.name().startsWith("AMAZON")) {
            for(SaleFee fee : this.fees) {
                if(fee.type.parent != null && !"amazon".equals(fee.type.parent.name)) continue;
                if("principal".equals(fee.type.name) || "productcharges".equals(fee.type.name))
                    totalSales += fee.cost;
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
        return orderId.equals(orderr.orderId);
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
    @Cached("2h")
    public static DashBoard frontPageOrderTable(int days) {
        DashBoard dashBoard = Cache.get(Orderr.FRONT_TABLE, DashBoard.class);
        if(dashBoard != null) return dashBoard;
        dashBoard = new DashBoard();
        /*
         * 1. 将不同市场的 yyyy-MM-dd 日期的订单加载出来
         * 2. 对订单按照时间进行 group. 由于时间会调整, 不能使用 DB 的 group
         */

        final DateTime now = DateTime.parse(DateTime.now().toString("yyyy-MM-dd"));
        final Date pre7Day = now.minusDays(Math.abs(days)).toDate();

        List<OrderrVO> vos = new ArrayList<>();
        List<List<OrderrVO>> results = Promises.forkJoin(new Promises.DBCallback<List<OrderrVO>>() {
            @Override
            public List<OrderrVO> doJobWithResult(Object param) {
                M m = (M) param;
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
        results.forEach(vos::addAll);

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
        Cache.add(Orderr.FRONT_TABLE, dashBoard, "2h");
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
        if(orderrs == null) orderrs = new ArrayList<>();
        List<String> orderIds = new ArrayList<>();
        for(Orderr o : orderrs) orderIds.add(o.orderId);
        return orderIds;
    }


    /**
     * 计算正常发票的总金额,未含税金额，税金额
     *
     * @return
     */
    public F.T3<Float, Float, Float> amount() {
        Float totalamount = 0f;
        OrderInvoice invoice = OrderInvoice.findById(this.orderId);
        //累计总金额
        float itemamount = 0f;
        for(OrderItem item : this.items) {
            if(item.discountPrice == null) item.discountPrice = 0f;
            if(item.price == null) item.price = 0f;
            if(item.quantity == null) item.quantity = 0;
            totalamount = totalamount + new BigDecimal(item.price - item.discountPrice).setScale(2, 4).floatValue();
            if(item.quantity != 0) {
                itemamount = itemamount
                        + new BigDecimal(item.quantity).multiply(new BigDecimal(item.price - item.discountPrice)
                        .divide(new BigDecimal(item.quantity), 2, 4)
                        .divide(new BigDecimal(this.orderrate()), 2, java.math.RoundingMode.HALF_DOWN))
                        .setScale(2, 4).floatValue();
            }
        }

        for(SaleFee fee : this.fees) {
            if((fee.type.name.equals("shipping") || fee.type.name.equals("shippingcharge")
                    || fee.type.name.equals("giftwrap")) && fee.cost > 0) {
                totalamount = totalamount + fee.cost;
                itemamount = itemamount + new BigDecimal(fee.cost).divide(
                        new BigDecimal(this.orderrate()), 2, java.math.RoundingMode.HALF_DOWN).setScale(2, 4)
                        .floatValue();
            }
        }

        totalamount = new BigDecimal(totalamount).setScale(2, 4).floatValue();
        itemamount = new BigDecimal(itemamount).setScale(2, 4).floatValue();

        Float notaxamount = 0f;
        //欧盟税号的税率为1
        if(invoice != null && invoice.europevat == OrderInvoice.VAT.EUROPE) {
            notaxamount = new BigDecimal(totalamount).divide(new BigDecimal(OrderInvoice.buyervat), 2,
                    java.math.RoundingMode.HALF_DOWN).floatValue();
        } else {
            //用各国的税率计算
            notaxamount = itemamount;
        }
        Float tax = new BigDecimal(totalamount).subtract(new BigDecimal(notaxamount)).setScale(2, 4).floatValue();
        return new F.T3<>(totalamount, notaxamount, tax);
    }


    /**
     * 格式化地址信息
     *
     * @param country
     * @return
     */
    public String formataddress(String country) {

        if(!StringUtils.isBlank(this.address)) {
            this.address = this.address.replace(",,", ",");
        }
        if(!StringUtils.isBlank(this.address1)) {
            this.address1 = this.address1.replace(",,", ",");
        }
        if(this.address != null && this.address.indexOf(",") == 0) {
            this.address = this.address.substring(1, this.address.length());
        }
        if(this.address1 != null && this.address1.indexOf(",") == 0) {
            this.address1 = this.address1.substring(1, this.address.length());
        }

        String editaddress = "";
        if(!StringUtils.isBlank(this.reciver)) {
            editaddress = editaddress + "," + this.reciver;
        }
        if(!StringUtils.isBlank(this.address)) {
            editaddress = editaddress + "," + this.address;
        } 
        if(!StringUtils.isBlank(this.address1)) {
            editaddress = editaddress + "," + this.address1;
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
        editaddress = editaddress + "," + country;
        //if(!StringUtils.isBlank(this.phone)) {
        //    editaddress = editaddress + ",phone:" + this.phone;
        //}
        if(editaddress != null && editaddress.indexOf(",") == 0) {
            editaddress = editaddress.substring(1, editaddress.length());
        }
        return editaddress;
    }

    /**
     * 判断是否是全额退款
     *
     * @return
     */
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
        return !(rows.size() <= 0 || cost > 0);
    }

    /**
     * 各国税率
     *
     * @return
     */
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
        if(market == M.AMAZON_ES) {
            return OrderInvoice.esvat;
        }
        return 0;
    }


    /**
     * 退货日期
     *
     * @return
     */
    public Date returndate() {
        for(SaleFee fee : this.fees) {
            if(fee.type.name.equals("productcharges") && fee.cost < 0) {
                return fee.date;
            }
        }
        return this.createDate;
    }


    /**
     * 是否退款
     *
     * @return
     */
    public int isreturn() {
        if(this.state == S.REFUNDED) {
            return 1;
        }
        if(refundmoney()) {
            return 1;
        } else
            return 0;
    }

    public String showItemSku() {
        String show = "";
        List<OrderItem> orderItems = OrderItem.find("order.orderId = ? ", this.orderId).fetch();
        if(orderItems != null && orderItems.size() > 0) {
            for(OrderItem item : orderItems) {
                show += item.product.sku + ";";
            }

        }
        return show;
    }

    public String showPromotionIDs() {
        String show = "";
        List<OrderItem> orderItems = OrderItem.find("order.orderId = ? ", this.orderId).fetch();
        if(orderItems != null && orderItems.size() > 0) {
            for(OrderItem item : orderItems) {
                if(StringUtils.isNotBlank(item.promotionIDs)) {
                    show += item.promotionIDs + ";";
                }
            }
        }
        return show;
    }

    public String showDiscountPrice() {
        String show = "";
        List<OrderItem> orderItems = OrderItem.find("order.orderId = ? ", this.orderId).fetch();
        if(orderItems != null && orderItems.size() > 0) {
            for(OrderItem item : orderItems) {
                if(item.discountPrice != null) {
                    show += item.discountPrice + ";";
                }
            }
        }
        return show;
    }

    public OrderInvoice createOrderInvoice() {
        F.T3<Float, Float, Float> amt = this.amount();
        OrderInvoice invoice = new OrderInvoice();
        invoice.orderid = this.orderId;
        invoice.updateDate = new Date();
        invoice.updator = "auto_reply";

        invoice.invoiceto = this.formataddress(this.country);
        invoice.address = this.formataddress(invoice.invoiceto);
        invoice.notaxamount = amt._2;
        invoice.taxamount = amt._3;
        invoice.totalamount = amt._1;
        invoice.price = new ArrayList<>();

        if(this.items != null && this.items.size() > 0) {
            for(OrderItem item : this.items) {
                if(item.quantity > 0) {
                    invoice.price.add(new BigDecimal(item.price - item.discountPrice)
                            .divide(new BigDecimal(item.quantity), 2, 4)
                            .divide(new BigDecimal(this.orderrate()), 2, java.math.RoundingMode.HALF_DOWN).floatValue());

                } else {
                    invoice.price.add(0f);
                }
            }
        }

        if(this.fees != null && this.fees.size() > 0) {
            for(SaleFee fee : this.fees) {
                if(fee.type.name.equals("shipping") || fee.type.name.equals("shippingcharge")
                        || fee.type.name.equals("giftwrap") && fee.cost > 0) {
                    invoice.price.add(new BigDecimal(fee.cost).divide(new BigDecimal(this.orderrate()), 2,
                            BigDecimal.ROUND_HALF_DOWN).floatValue());
                }
            }
        }

        invoice.saveprice();
        invoice.europevat = OrderInvoice.VAT.NORMAL;
        return invoice;
    }


    public boolean canBeProvidedInvoice() {
        return M.europeMarkets().contains(this.market);
    }

    public boolean refreshFee() {
        this.fees.forEach(GenericModel::delete);
        TransportClient esClient = ES.client();
        QueryBuilder builder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("_type", "salefee"))
                .must(QueryBuilders.termQuery("order_id", this.esOrderId()));
        ES.deleteByQuery(esClient, System.getenv(Constant.ES_INDEX), builder);
        MWSFinancesServiceClient client = MWSFinances.client(this.account, this.account.type);
        ListFinancialEventsRequest request = new ListFinancialEventsRequest();
        request.setSellerId(this.account.merchantId);
        request.setMWSAuthToken(this.account.token);
        request.setAmazonOrderId(orderId);
        ListFinancialEventsResponse response = client.listFinancialEvents(request);
        return SaleFee.parseFinancesApiResult(response, this.account);
    }

    public void refreshESFee() {
        BulkRequestBuilder bulkRequest = ES.client().prepareBulk();
        this.save();
        this.fees.forEach(fee -> {
            try {
                String esSku = fee.product_sku.replace("-", "");
                bulkRequest.add(ES.client().prepareIndex(System.getenv(Constant.ES_INDEX), "salefee", fee.id.toString())
                        .setSource(jsonBuilder().startObject()
                                .field("transaction_type", fee.transaction_type)
                                .field("sku", esSku)
                                .field("date", fee.date, ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC))
                                .field("selling_id", Selling.esSellingId(fee.product_sku, fee.market, this.account))
                                .field("market", fee.market.name())
                                .field("fee_type", fee.type.name)
                                .field("cost_in_usd", fee.usdCost)
                                .field("cost", fee.cost)
                                .field("currency", fee.currency.name())
                                .field("quantity", fee.qty)
                                .field("order_id", fee.order.esOrderId())
                                .endObject()));
            } catch(IOException e) {
                e.printStackTrace();
            }
        });
        bulkRequest.get();
        Objects.requireNonNull(ES.client()).close();
    }

    public String esOrderId() {
        return this.orderId.replace("-", "_");
    }

    public static Orderr.S getState(String earState) {
        switch(earState) {
            case "pending":
                return Orderr.S.PENDING;
            case "confirmed":
                return Orderr.S.UNSHIPPED;
            case "packaged":
            case "shipped":
                return Orderr.S.SHIPPED;
            case "invoiceunconfirmed":
            case "canceled":
                return Orderr.S.CANCEL;
            case "unfulfillable":
                return Orderr.S.UNSHIPPED;
            default:
                return null;
        }
    }
}
