package models.finance;

import exception.DBException;
import helper.Currency;
import helper.Dates;
import helper.Webs;
import models.market.Account;
import models.market.M;
import models.market.Orderr;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.annotations.GenericGenerator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import play.Logger;
import play.db.DB;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.*;

/**
 * 记录在销售过程中, 不同市场产生的不同的费用;
 * 例如: Amazon 的 Refund 费用, Order Payment 费用 等等...
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:58 AM
 */
@Entity
public class SaleFee extends GenericModel {

    public SaleFee() {
    }

    public SaleFee(SaleFee fee) {
        this.market = fee.market;
        this.orderId = fee.orderId;
        this.date = fee.date;
        this.order = fee.order;
        this.currency = fee.currency;
        this.account = fee.account;
    }

    @OneToOne
    public Account account;

    @OneToOne(fetch = FetchType.LAZY)
    public Orderr order;

    @OneToOne
    public FeeType type;


    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    public String id;

    /**
     * 费用的状态
     */
    @Enumerated(EnumType.STRING)
    public M market;

    /**
     * 这项费用一个寄存临时信息的地方
     */
    public String memo;

    /**
     * 一个冗余字段, 用来标识一些在 Orderr 中找不到的订单, 用来处理新旧 SaleFee 更替的.
     */
    public String orderId;

    @Temporal(TemporalType.DATE)
    public Date date;
    /**
     * 费用, 系统内的费用使用 USD 结算
     */
    @Column(nullable = false)
    public Float cost;

    @Enumerated(EnumType.STRING)
    public Currency currency;

    /**
     * 最终统计成 USD 的价格
     */
    public Float usdCost;

    public Integer qty = 1;


    /**
     * 根据新的 SaleFee 对数据库中存在的老的 SaleFee 进行删除处理.
     *
     * @param newFees
     * @return 返回需要添加进入数据库的 Fees
     */
    public static List<SaleFee> clearOldSaleFee(List<SaleFee> newFees) {
        /**
         * 1. 将 newFees 涉及到的订单的 Fees 全部加载出来
         * 2. 以订单为判断, 收集需要删除的 Fee 的订单. 标准为 newFees 的数量 >= 数据库中加载出的 Fees 的数量
         * 3. 
         */
        Set<String> orderIds = new HashSet<String>(); // newFees 所涉及的 Order

        for(SaleFee fe : newFees) {
            orderIds.add(fe.orderId);
        }
        // 清理原来的 SaleFees, 确保每个 Order 的 SaleFee 只有一份不会重复
        SaleFee.delete("orderId IN ('" + StringUtils.join(orderIds, "','") + "')");
        return newFees;
    }

    private static FeeType cachedFeeType(String key, Map<String, FeeType> cached) {
        FeeType type = null;
        if(cached.containsKey(key)) {
            type = cached.get(key);
        } else {
            type = FeeType.findById(key);
            cached.put(key, type);
        }
        return type;
    }

    /**
     * 这个是用来解析 Amazon 每隔 14 天自动生成 FlatV2 的 Payments 的报表
     *
     * @param file
     * @param acc
     * @return
     */
    public static List<SaleFee> flat2FinanceParse(File file, Account acc, M market) {
        List<SaleFee> fees = new ArrayList<SaleFee>();
        Map<String, Orderr> cachedOrder = new HashMap<String, Orderr>();
        Map<String, FeeType> cachedFeeType = new HashMap<String, FeeType>();

        try {
            List<String> lines = FileUtils.readLines(file);
            lines.remove(0);
            lines.remove(0);// 删除最上面的 2 行

            int i = 0;
            for(String line : lines) {
                try {
                    String[] params = StringUtils.splitPreserveAllTokens(line, "\t");
                    String orderId = params[7];
                    String priceStr = params[14];
                    String dateStr = params[16];
                    String qtyStr = params[22];

                    String typeStr = StringUtils.replace(
                            StringUtils.join(StringUtils.split(params[13].toLowerCase(), " "), ""),
                            "fulfillment",
                            "fulfilment");
                    // 修正 FlatV2 与系统中 FeeType 名称不一样但费用是一样的费用名称
                    // 将 FlatV2 中的 principal 转换成 productcharges 类型
                    if("principal".equals(typeStr)) typeStr = "productcharges";
                        // 将 FlatV2 中的 storagefee 修正为 fbastoragefee
                    else if("storagefee".equals(typeStr)) typeStr = "fbastoragefee";
                        // 将 FlatV2 中的 shippinghb 修正为 shippingholdback
                    else if("shippinghb".equals(typeStr)) typeStr = "shippingholdback";
                        // 将 FlatV2 中的 subscriptionfee 修正为 subscription
                    else if("subscriptionfee".equals(typeStr)) typeStr = "subscription";

                    SaleFee fee = new SaleFee();
                    fee.account = acc;
                    fee.market = market;

                    if(StringUtils.isBlank(orderId)) {
                        // 当从文档中解析不到 orderId 的时候, 记录成 SYSTEM.
                        fee.orderId = "SYSTEM_" + typeStr.toUpperCase();
                    } else {
                        fee.orderId = orderId;
                        Orderr ord = Orderr.findById(fee.orderId);
                        if(ord == null && !StringUtils.startsWith(orderId, "S"))
                            Logger.error("Order[" + orderId + "] is not exist when parsing SaleFee!");
                        else fee.order = ord;
                    }

                    FeeType type = cachedFeeType(typeStr, cachedFeeType);
                    if(type == null) {
                        i++;
                        Logger.warn("Type not found! [" + typeStr + "]");
                        if(i < 5)
                            Webs.systemMail("Type not found In FlatV2 Method!", "Type not found! [Type:" + typeStr + "]");
                    } else
                        fee.type = type;

                    float usdCost = 0;
                    float cost = 0;
                    switch(market) {
                        case AMAZON_UK:
                            fee.date = DateTime.parse(dateStr, DateTimeFormat.forPattern("dd/MM/yyyy")).toDate();
                            cost = Webs.amazonPriceNumber(market, priceStr);
                            usdCost = Currency.GBP.toUSD(cost);
                            fee.currency = Currency.GBP;
                            break;
                        case AMAZON_US:
                            fee.date = DateTime.parse(dateStr, DateTimeFormat.forPattern("MM/dd/yyyy")).toDate();
                            cost = Webs.amazonPriceNumber(market, priceStr);
                            usdCost = Currency.USD.toUSD(cost);
                            fee.currency = Currency.USD;
                            break;
                        case AMAZON_DE:
                        case AMAZON_FR:
                        case AMAZON_ES:
                        case AMAZON_IT:
                            fee.date = DateTime.parse(dateStr, DateTimeFormat.forPattern("dd.MM.yyyy")).toDate();
                            // Amazon 的每一次收款的 FlatV2 文件中的数据格式没有改变, 还是根据的价格来进行处理的, 所以还是按照 Market 来解析
                            cost = Webs.amazonPriceNumber(market, priceStr);
                            usdCost = Currency.EUR.toUSD(cost);
                            fee.currency = Currency.EUR;
                            break;
                        default:
                            Logger.warn("SaleFee Can not parse market: " + acc.type);
                    }
                    fee.usdCost = usdCost;
                    fee.cost = cost;
                    if(StringUtils.isNotBlank(qtyStr)) fee.qty = NumberUtils.toInt(qtyStr);

                    fees.add(fee);
                } catch(Exception e) {
                    i++; //防止行数太多, 发送太多的邮件
                    Logger.warn("SaleFee V2 Parse Have Error! [" + file.getAbsolutePath() + "|" + line + "]");
                    if(i < 5)
                        Webs.systemMail("SaleFee V2 Parse Have Error!", "<h3>" + file.getAbsolutePath() + "|" + line + "</h3>");
                }
            }

        } catch(IOException e) {
            Logger.warn("File is not exist!");
        }


        return fees;
    }

    /**
     * 由于这个数据量比较大, 所以提供一个批处理保存.
     *
     * @param fees
     */
    public static void batchSaveWithJDBC(List<SaleFee> fees) {
        try {
            PreparedStatement ps = DB.getConnection().prepareStatement(
                    "INSERT INTO SaleFee(id, cost, currency, `date`, market, memo, orderId, qty, usdCost, account_id, order_orderId, type_name) " +
                            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            for(SaleFee f : fees) {
                saleFeeCheck(f);
                int i = 1;
                ps.setString(i++, UUID.randomUUID().toString());
                ps.setFloat(i++, f.cost);
                ps.setString(i++, f.currency.toString());
                ps.setDate(i++, new java.sql.Date(f.date.getTime()));
                ps.setString(i++, f.market.name());
                ps.setString(i++, f.memo);
                ps.setString(i++, f.orderId);
                ps.setInt(i++, f.qty);
                ps.setFloat(i++, f.usdCost);
                ps.setString(i++, f.account.id + "");
                ps.setString(i++, f.order == null ? null : f.order.orderId);
                ps.setString(i, f.type == null ? null : f.type.name);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch(Exception e) {
            throw new DBException(e);
        }
    }

    /**
     * 对需要保存的 SaleFee 进行检查, 在 batchSaveWithJDBC 执行过程中进行处理
     */
    private static void saleFeeCheck(SaleFee f) {
        try {
            if(f.type != null && "productcharges".equals(f.type.name) && f.cost <= 0 && f.order != null && StringUtils.isNotBlank(f.orderId)) {
                if(f.order.state == Orderr.S.REFUNDED) {
                    Logger.info("Order[%s] state is already %s", f.orderId, f.order.state);
                } else {
                    // 由于 SaleFee 更新使用的是 JDBC 所以这里不会有印象, 主要是为了能够让 IDE 能够找到 order.state write 的方法
                    f.order.state = Orderr.S.REFUNDED;
                    DB.execute("UPDATE Orderr SET state='" + Orderr.S.REFUNDED.name() + "' WHERE orderId='" + f.orderId + "'");
                    Logger.info("Order[%s] state from %s to %s", f.orderId, f.order.state, Orderr.S.REFUNDED);
                }
            }
        } catch(Exception e) {
            String message = "Order[" + f.orderId + "] state update to REFUNDED failed!";
            Logger.warn(message);
            Webs.systemMail("SaleFeeCheck Error!", message + "\r\n<br/><br/>" + Webs.S(e));
        }

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SaleFee");
        sb.append("{type=").append(type);
        sb.append(", id='").append(id).append('\'');
        sb.append(", market=").append(market);
        sb.append(", memo='").append(memo).append('\'');
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", date=").append(date);
        sb.append(", cost=").append(cost);
        sb.append(", currency=").append(currency);
        sb.append(", usdCost=").append(usdCost);
        sb.append(", qty=").append(qty);
        sb.append('}');
        return sb.toString();
    }
}
