package models.market;

import com.elcuk.mws.jaxb.ordertracking.*;
import com.google.gson.annotations.Expose;
import helper.Currency;
import helper.Dates;
import helper.Patterns;
import helper.Webs;
import models.finance.SaleFee;
import models.product.Product;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import play.Logger;
import play.data.validation.Email;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import play.libs.IO;

import javax.persistence.*;
import javax.xml.bind.JAXB;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 系统内的核心订单
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:18 AM
 */
@Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public List<OrderItem> items;

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

    @OneToMany(mappedBy = "order")
    @OrderBy("date ASC,cost DESC")
    public List<SaleFee> fees;
    //-------------- Basic ----------------

    /**
     * <pre>
     * 发送邮件到达了什么阶段. 默认从 0x0000 开始;(16进制)
     * 从最前面的位开始
     * 0x0000 : 还没有发送邮件[SHIPPED_MAIL]
     * 0x000f : 订单成功发货以后发送邮件
     * 0x00f0 : 订单的货物抵达以后发送邀请留 Review 的邮件
     * 0x0f00 : 待定
     * 0xf000 : 待定
     * </pre>
     */
    public int emailed = 0x0000;


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
     * 在进行解析的 Order XML 文件的时候, 每次需要将更新的数据记录到数据库, 此方法将从 XML 解析出来的 Order 的信息更新到被托管的对象身上.
     */
    public void updateAttrs(Orderr orderr) {
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
        if(orderr.totalAmount != null) this.totalAmount = orderr.totalAmount;
        if(orderr.trackNo != null) this.trackNo = orderr.trackNo;
        if(orderr.state != null) {
            // 这几个状态是不可逆的, 如果有其他地方将订单更新成这几个状态, 那么此订单的状态不允许再进行更改!
            if(orderr.state == S.REFUNDED || orderr.state == S.CANCEL || orderr.state == S.RETURNNEW) return;
            this.state = orderr.state;
        }

        if(orderr.items != null && orderr.items.size() > 0) {
            // 比较两个 OrderItems, 首先将相同的 OrderItems 更新回来, 然后将 New OrderItem 集合中出现的系统中不存在的给添加进来
            Set<OrderItem> newlyOi = new HashSet<OrderItem>();
            if(this.items.size() == 0) { // 如果原始的 Order 中一个 OrderItem 都没有
                for(OrderItem noi : orderr.items) {
                    if(noi.isPersistent()) continue;
                    newlyOi.add(noi);
                }
            } else { // 如果原始的 Order 中有 OrderItem
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
                            if(noi.currency != null && oi.currency != noi.currency) oi.currency = noi.currency;
                            if(noi.usdCost != null) oi.usdCost = noi.usdCost;
                        } else if(!JPA.em().contains(oi)) { // 表示一级缓存中没有, 那么才可以进入 newlyOrderItem 添加, 否则应该为更新
                            newlyOi.add(noi);
                        }
                    }
                }
            }
            // 如果有两个相同的 object_id 相同的对象添加进入 Orderr.items 进行级联保存或者更新, 那么会在 hibernate 进行保存更新
            // 检查唯一性的时候发生异常! 所以上面的 JPA.em().contains(oi) 判断很有必要!
            for(OrderItem noi : newlyOi) this.items.add(noi);
        }

        this.save();
    }


    public void setPostalCode(String postalCode1) {
        if(postalCode1 != null) this.postalCode = postalCode1.toUpperCase();
    }

    /**
     * 将 0x0000 这种 16 进制的第几位修改成 'char'
     *
     * @param bit
     */
    public void emailed(int bit, char c) {
        if(c != 'f' && c != 'F' && c != '0') throw new IllegalArgumentException("Just only '1' or '0' is valid!");
        StringBuilder mailedHex = new StringBuilder(Integer.toHexString(this.emailed));
        int prefix = 4 - mailedHex.length();
        for(int i = 0; i < prefix; i++) {
            mailedHex.insert(0, '0');
        }
        switch(bit) {
            case 1: // 0x000!
                mailedHex.setCharAt(mailedHex.length() - 1, c);
                break;
            case 2: // 0x00!0
                mailedHex.setCharAt(mailedHex.length() - 2, c);
                break;
            case 3: // 0x0!00
                mailedHex.setCharAt(mailedHex.length() - 3, c);
                break;
            case 4: //0x!000
                mailedHex.setCharAt(mailedHex.length() - 4, c);
                break;
        }
        this.emailed = Integer.valueOf(mailedHex.toString(), 16);
    }

    /**
     * 获取第 bit 位上的值
     *
     * @param bit
     * @return
     */
    public char emailed(int bit) {
        StringBuilder mailedHex = new StringBuilder(Integer.toHexString(this.emailed));
        int prefix = 4 - mailedHex.length();
        for(int i = 0; i < prefix; i++) {
            mailedHex.insert(0, '0');
        }
        return mailedHex.charAt(bit);
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
                for(OrderItem oi : this.items) {
                    String ti = oi.selling.listing.title.toLowerCase();
                    if(StringUtils.startsWith(ti, "saner")) {
                        return "Thanks for purchasing Saner Products from EasyAcc on Amazon.co.uk (Order: " + this.orderId + ")";
                    } else if(StringUtils.startsWith(ti, "fencer")) {
                        return "Thanks for purchasing Fencer Products from EasyAcc on Amazon.co.uk (Order: " + this.orderId + ")";
                    }
                }
                return "Thanks for purchasing EasyAcc Product on Amazon.co.uk (Order: " + this.orderId + ")";
            case AMAZON_DE:
                for(OrderItem oi : this.items) {
                    String ti = oi.selling.listing.title.toLowerCase();
                    if(StringUtils.startsWith(ti, "saner")) {
                        return "Vielen Dank für den Kauf SANER Produkte aus EasyAcc auf Amazon.de (Bestellung: " + this.orderId + ")";
                    } else if(StringUtils.startsWith(ti, "fencer")) {
                        return "Vielen Dank für den Kauf Fencer Produkte aus EasyAcc auf Amazon.de (Bestellung: " + this.orderId + ")";
                    }
                }
                return "Vielen Dank für den Kauf EasyAcc Produkte auf Amazon.de (Bestellung: " + this.orderId + ")";
            default:
                Logger.warn(String.format("MailTitle is not support [%s] right now.", this.market));
                return "";
        }
    }

    /**
     * 通过 HTTP 方式到 Amazon 后台进行订单信息的补充
     */
    public Orderr orderDetailUserIdAndEmail(Document doc) {
        this.crawlUpdateTimes++;
        Element lin = doc.select("#_myo_buyerEmail_progressIndicator").first();
        if(lin == null) {
            // 找不到上面的记录的时候, 将这个订单的警告信息记录在 memo 中
            lin = doc.select("#_myoV2PageTopMessagePlaceholder").first();
            this.state = S.CANCEL;
            this.memo = lin.text();
        }
        String url = lin.parent().select("a").attr("href");
        String[] args = StringUtils.split(url, "&");
        for(String pa : args) {
            try {
                // buyerId
                if(!StringUtils.containsIgnoreCase(pa, "buyerID")) continue;
                this.userid = StringUtils.split(pa, "=")[1];


                // Email
                if(StringUtils.isBlank(this.email) || !StringUtils.contains(this.email, "@")) {
                    String html = doc.outerHtml(); // 通过抓取的 HTML 源代码的 js 代码部分进行的提取.
                    int head = StringUtils.indexOfIgnoreCase(html, "buyerEmail:");
                    int end = StringUtils.indexOfIgnoreCase(html, "targetID:");
                    String sub = html.substring(head + 14, end).trim(); // + 14 为排除 buyerEmail: 家冒号的长度
                    this.email = sub.substring(0, sub.length() - 2); // 尾部 -2 为排除最后面的冒号与逗号的长度
                }


            } catch(Exception e) {
                Logger.warn("Orderr.orderDetailUserIdAndEmail error, url[%s], E[%s]", url, Webs.E(e));
            }
            break;
        }
        return this;
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
    public static Map<String, Map<String, AtomicInteger>> frontPageOrderTable(int days) {
        DateTime now = DateTime.parse(DateTime.now().toString("yyyy-MM-dd"));
        if(days > 0) days = -days;
        Date pre7Day = now.plusDays(days).toDate();
        List<Orderr> orders = Orderr.find("createDate>=? AND createDate<=?", pre7Day, now.plusDays(1).toDate()).fetch();

        List<Account> accs = Account.all().fetch();
        Map<String, Map<String, AtomicInteger>> odmaps = new HashMap<String, Map<String, AtomicInteger>>();
        for(Orderr or : orders) {
            DateTime ct = new DateTime(or.createDate);
            String key = ct.toString("yyyy-MM-dd");

            if(odmaps.containsKey(key)) {
                Map<String, AtomicInteger> dateRow = odmaps.get(key);
                dateRow.get(or.state.name()).incrementAndGet(); // ALL 数据
                dateRow.get(String.format("%s_%s", or.state.name(), or.market.name())).incrementAndGet(); // Market 数据
                dateRow.get(String.format("%s_%s", or.state.name(), or.account.toString())).incrementAndGet(); // Account 数据
                dateRow.get(String.format("all")).incrementAndGet();
                dateRow.get(String.format("all_%s", or.market.name())).incrementAndGet();
                dateRow.get(String.format("all_%s", or.account.toString())).incrementAndGet();
            } else {
                //row key: [state]_[market.name/account.toString]
                Map<String, AtomicInteger> dateRow = new HashMap<String, AtomicInteger>();
                for(S s : S.values()) {
                    dateRow.put(s.name(), new AtomicInteger(0)); // ALL
                    for(Account.M m : Account.M.values()) {
                        dateRow.put(String.format("%s_%s", s.name(), m.name()), new AtomicInteger(0)); // Market
                        dateRow.put(String.format("all_%s", m.name()), new AtomicInteger(0));
                    }
                    for(Account a : accs) {
                        dateRow.put(String.format("%s_%s", s.name(), a.toString()), new AtomicInteger(0)); // Account
                        dateRow.put(String.format("all_%s", a.toString()), new AtomicInteger(0));
                    }
                }
                dateRow.get(or.state.name()).incrementAndGet(); // ALL 数据
                dateRow.get(String.format("%s_%s", or.state.name(), or.market.name())).incrementAndGet(); // Market 数据
                dateRow.get(String.format("%s_%s", or.state.name(), or.account.toString())).incrementAndGet(); // Account 数据
                dateRow.put(String.format("all"), new AtomicInteger(1));
                dateRow.put(String.format("all_%s", or.market.name()), new AtomicInteger(1));
                dateRow.put(String.format("all_%s", or.account.toString()), new AtomicInteger(1));
                odmaps.put(key, dateRow);
            }
        }

        // 统计总订单数
        for(String dateKey : odmaps.keySet()) {
            // 2012-04-19 ->
            for(S s : S.values()) {
                /* -> pending
                 * -> pending_uk
                 * -> pending_easyacc.eu
                 */
                Map<String, AtomicInteger> rowMap = odmaps.get(dateKey);
                AtomicInteger rowSum = new AtomicInteger(0); // ALL 统计
                AtomicInteger rowMarketSum = new AtomicInteger(0); // Market 统计
                AtomicInteger rowAccountSUm = new AtomicInteger(0); // Account 统计

                for(String dataRowKey : rowMap.keySet()) {
                    // each Row Data, 在已经限制了 Date 的日期下的每一行的数据将 state 相同的进行统计计算
                    if(dataRowKey.equals(s.name())) {
                        rowSum.addAndGet(rowMap.get(dataRowKey).get());
                    }

                    for(Account.M m : Account.M.values()) {
                        if(dataRowKey.equals(String.format("%s_%s", s.name(), m.toString())))
                            rowMarketSum.addAndGet(rowMap.get(dataRowKey).get());
                    }

                    for(Account acc : accs) {
                        if(dataRowKey.equals(String.format("%s_%s", s.name(), acc.toString())))
                            rowAccountSUm.addAndGet(rowMap.get(dataRowKey).get());
                    }
                }
            }
        }

        // 将 key 排序, 按照日期倒序
        List<String> dateKey = new ArrayList<String>(odmaps.keySet());
        Collections.sort(dateKey, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                DateTime dt1 = DateTime.parse(o1);
                DateTime dt2 = DateTime.parse(o2);
                return (int) (dt1.getMillis() - dt2.getMillis());
            }
        });

        Map<String, Map<String, AtomicInteger>> afSort = new LinkedHashMap<String, Map<String, AtomicInteger>>();
        for(String key : dateKey) afSort.put(key, odmaps.get(key));
        return afSort;
    }


    /**
     * 订单抓取第一步, 获取订单(FBA 为主)
     * 解析的文件中的所有订单; 需要区别市场
     *
     * @param file
     * @param acc  通过账号来确实是哪一个市场
     * @return
     */
    public static List<Orderr> parseAllOrderXML(File file, Account acc) {
        switch(acc.type) {
            case AMAZON_US:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_ES:
            case AMAZON_IT:
                return allOrderXML_Amazon(file, acc);
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
    public static Set<Orderr> parseUpdateOrderXML(File file, Account.M market) {
        switch(market) {
            case AMAZON_US:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_ES:
            case AMAZON_IT:
                return allUpdateOrderXML_Amazon(file);
            case EBAY_UK:
            default:
                Logger.warn("parseUpdateOrderXML is not support the Ebay market!");
                return new HashSet<Orderr>();
        }
    }

    public static Set<Orderr> allUpdateOrderXML_Amazon(File file) {
        List<String> lines = IO.readLines(file, "UTF-8");
        Set<Orderr> orderrs = new HashSet<Orderr>();
        lines.remove(0); //删除第一行标题
        for(String line : lines) {
            // 在解析 csv 文件的时候会发现有重复的项出现, 不过这没关系,
            String[] vals = StringUtils.splitPreserveAllTokens(line, "\t");
            try {
                if(vals[0].toUpperCase().startsWith("S")) {
                    Logger.info("Skip Self Order[" + vals[0] + "].");
                    continue;
                }
                Orderr order = new Orderr();
                order.orderId = vals[0];
                order.paymentDate = Dates.parseXMLGregorianDate(vals[7]);
                order.shipDate = Dates.parseXMLGregorianDate(vals[8]);
                order.shippingService = vals[42];
                if(StringUtils.isNotBlank(vals[43])) {
                    order.trackNo = vals[43];
                    order.arriveDate = Dates.parseXMLGregorianDate(vals[44]);
                }
                order.email = vals[10];
                order.buyer = vals[11];
                order.phone = vals[12];
                order.shipLevel = vals[23];
                order.reciver = vals[24];
                order.address = vals[25];
                order.address1 = (vals[26] + " " + vals[27]).trim();
                order.city = vals[28];
                order.province = vals[29];
                order.postalCode = vals[30];
                order.country = vals[31];

                orderrs.add(order);
            } catch(Exception e) {
                Logger.warn("Parse Order[" + vals[0] + "] update Error. [" + e.getMessage() + "]");
            }
        }
        return orderrs;
    }

    public static List<Orderr> allOrderXML_Ebay(File file) {
        //TODO 暂时还没有实现.
        throw new UnsupportedOperationException("allOrderXML_Ebay 还没有实现!");
    }

    /**
     * 解析 XML 文件中记录的订单(注意:这里是如实反应 XML 中的信息,不要更新数据库), 这一部分主要提供的是 Amazon 的
     *
     * @param file
     * @return
     */
    public static List<Orderr> allOrderXML_Amazon(File file, Account acc) {
        AmazonEnvelopeType envelopeType = JAXB.unmarshal(file, AmazonEnvelopeType.class);
        List<Orderr> orders = new ArrayList<Orderr>();
        for(MessageType message : envelopeType.getMessage()) {
            OrderType odt = message.getOrder();

            String amazonOrderId = odt.getAmazonOrderID().toUpperCase();
            if(amazonOrderId.startsWith("S02") || Patterns.A2Z.matcher(amazonOrderId).matches()) {
                Logger.info("OrderId {%s} Can Not Be Add to Normal Order", amazonOrderId);
                continue;
            }

            Orderr orderr = new Orderr();
            orderr.account = acc;
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
            Map<String, Boolean> mailed = new HashMap<String, Boolean>();
            for(OrderItemType oid : oits) {
                /**
                 * 0. 检查这个 order 是否需要进行补充 orderitem
                 * 1. 将 orderitem 的基本信息补充完全
                 * 2. 检查 orderitems List 中时候已经存在相同的产品了, 如果有这修改已经存在的产品的数量否则才添加新的
                 */
                if(oid.getQuantity() < 0) continue;//只有数量为 0 这没必要记录, 但如果订单为 Cancel 还是有必要记录的

                OrderItem oi = new OrderItem();
                oi.order = orderr;
                oi.productName = oid.getProductName();
                oi.quantity = oid.getQuantity();
                oi.createDate = orderr.createDate; // 这个字段是从 Order 转移到 OrderItem 上的一个冗余字段, 方便统计使用


                // 如果属于 UnUsedSKU 那么则跳过这个解析
                if(Product.unUsedSKU(oid.getSKU())) continue;

                String sku = Product.merchantSKUtoSKU(oid.getSKU());
                Product product = Product.findById(sku);
                Selling selling = Selling.findById(Selling.sid(oid.getSKU().toUpperCase(), orderr.market/*市场使用的是 Orderr 而非 Account*/, acc));
                if(product != null) oi.product = product;
                else {
                    String title = String.format("SKU[%s] is not in PRODUCT, it can not be happed!!", sku);
                    Logger.error(title);
                    if(!mailed.containsKey(sku)) {
                        Webs.systemMail(title, title);
                        mailed.put(sku, true);
                    }
                    continue; // 发生了这个错误, 这跳过这个 orderitem
                }
                if(selling != null) oi.selling = selling;
                else {
                    String sid = Selling.sid(oid.getSKU().toUpperCase(), orderr.market, acc);
                    String title = String.format("Selling[%s] is not in SELLING, it can not be happed!", sid);
                    Logger.warn(title);
                    if(mailed.containsKey(sid)) {
                        Webs.systemMail(title, title);
                        mailed.put(sid, true);
                    }
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
                        oi.currency = Currency.valueOf(at.getCurrency());
                        if("principal".equals(compType)) {
                            oi.price = at.getValue();
                            totalAmount += oi.price;
                            oi.usdCost = oi.currency.toUSD(oi.price);
                        } else if("shipping".equals(compType)) {
                            oi.shippingPrice = at.getValue();
                            shippingAmount += oi.shippingPrice;
                        } else if("giftwrap".equals(compType)) {
                            oi.memo += String.format("\nGiftWrap: %s %s.", at.getValue(), oi.currency); //这个价格暂时不知道如何处理, 所以就直接记录到中性字段中
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
     * 查看某一天订单的饼图数据
     *
     * @param msku
     * @param date
     * @return
     */
    public static Map<String, AtomicInteger> orderPieChart(String msku, Date date) {
        DateTime day = Instant.parse(new DateTime(date.getTime()).toString("yyyy-MM-dd")).toDateTime();
        Date dayBegin = day.toDate();
        Date dayEnd = day.plusDays(1).toDate();

        List<Orderr> orderrs = Orderr.find("createDate>=? AND createDate<=?", dayBegin, dayEnd).fetch();

        Map<String, AtomicInteger> rtMap = new LinkedHashMap<String, AtomicInteger>();
        for(Long begin = dayBegin.getTime(); begin < dayEnd.getTime(); begin += TimeUnit.HOURS.toMillis(1)) {
            rtMap.put(begin.toString(), new AtomicInteger(0));
            for(Orderr or : orderrs) {
                for(OrderItem oi : or.items) {
                    if(oi.selling.merchantSKU.equals(msku) ||
                            "all".equalsIgnoreCase(msku)) {//如果搜索的 MerchantSKU 为 all 也进行计算
                        if(or.createDate.getTime() < begin || or.createDate.getTime() > begin + TimeUnit.HOURS.toMillis(1))
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
        // {"Pending"=>226233, "Shipped"=>1284685, "Cancelled"=>28538, "Shipping"=>1342}, 半年的更新文件
        String orderSt = orderState.toLowerCase();
        if("pending".equals(orderSt)) {
            return S.PENDING;
        } else if("shipped".equals(orderSt)) {
            return S.SHIPPED;
        } else if("shipping".equals(orderSt)) {
            return S.PAYMENT;
        } else if("cancelled".equals(orderSt)) {
            return S.CANCEL;
        } else {
            return S.PENDING;
        }
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
}
