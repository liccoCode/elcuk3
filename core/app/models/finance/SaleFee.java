package models.finance;

import exception.DBException;
import helper.Currency;
import helper.Webs;
import models.market.Account;
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
    public Account.M market;

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
     */
    public static Integer clearOldSaleFee(List<SaleFee> newFees) {
        Set<String> orderIds = new HashSet<String>();
        for(SaleFee fe : newFees) orderIds.add(fe.orderId);
        // 清理原来的 SaleFees, 确保每个 Order 的 SaleFee 只有一份不会重复
        return SaleFee.delete("orderId IN ('" + StringUtils.join(orderIds, "','") + "')");
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

    private static Orderr cachedOrder(String key, Map<String, Orderr> cached) {
        Orderr ord = null;
        if(cached.containsKey(key)) {
            ord = cached.get(key);
        } else {
            ord = Orderr.findById(key);
            cached.put(key, ord);
        }
        return ord;
    }

    /**
     * 解析通过 Payments -> Transactions 中 7 天的数据进行 Amazon SaleFee 的提取
     *
     * @param file
     * @param acc
     * @return
     */
    public static List<SaleFee> flagFinanceParse(File file, Account acc, Account.M market) {
        List<SaleFee> fees = new ArrayList<SaleFee>();
        Map<String, Orderr> cachedOrder = new HashMap<String, Orderr>();
        Map<String, FeeType> cachedFeeType = new HashMap<String, FeeType>();
        try {
            List<String> lines = FileUtils.readLines(file);
            lines.remove(0);
            lines.remove(0);
            lines.remove(0);
            lines.remove(0); //删除最上面的四行

            for(String line : lines) {
                try {
                    String[] params = StringUtils.splitPreserveAllTokens(line, "\t");
                    String typeStr = StringUtils.join(params[5].split(" "), "").toLowerCase();
                    String typeStr4 = StringUtils.join(params[4].split(" "), "").toLowerCase();
                    String orderId = params[1].trim();

                    SaleFee fee = new SaleFee();
                    fee.orderId = orderId;
                    fee.account = acc;
                    fee.market = market;

                    FeeType type = cachedFeeType(typeStr, cachedFeeType);

                    if(type == null) {
                        if("productcharges".equals(typeStr4))
                            fee.type = cachedFeeType("productcharges", cachedFeeType);
                        else if("promorebates".equals(typeStr4))
                            fee.type = cachedFeeType("promorebates", cachedFeeType);
                        else {
                            Logger.warn("Type not found! [" + typeStr + "]");
                            Webs.systemMail("Type not found!", "Type not found! [Type:" + typeStr + ", Type4: " + typeStr4 + "]");
                        }
                    } else
                        fee.type = type;

                    if(StringUtils.isBlank(orderId)) {
                        // 当从文档中解析不到 orderId 的时候, 记录成 SYSTEM.
                        fee.orderId = "SYSTEM_" + typeStr.toUpperCase();
                    } else {
                        fee.orderId = orderId;
                        Orderr ord = cachedOrder(orderId, cachedOrder);
                        if(ord == null && !StringUtils.startsWith(orderId, "S"))
                            Logger.error("Order[" + orderId + "] is not exist when parsing SaleFee!");
                        else fee.order = ord;
                    }

                    float usdCost = 0;
                    float cost = 0;
                    String priceStr = params[6];

                    // 这种格式的文档, UK,DE,FR 暂时日期格式都是一样的
                    fee.date = DateTime.parse(params[0], DateTimeFormat.forPattern("dd MMM yyyy")).toDate();
                    switch(market) {
                        case AMAZON_UK:
                            cost = Webs.amazonPriceNumber(market, priceStr.substring(1).trim());
                            usdCost = Currency.GBP.toUSD(cost);
                            fee.currency = Currency.GBP;
                            break;
                        case AMAZON_DE:
                        case AMAZON_FR:
                        case AMAZON_ES:
                        case AMAZON_IT:
                            cost = Webs.amazonPriceNumber(market, priceStr.substring(3).trim());
                            usdCost = Currency.EUR.toUSD(cost);
                            fee.currency = Currency.EUR;
                            break;
                        default:
                            Logger.warn("SaleFee Can not parse market: " + acc.type);
                    }
                    fee.cost = cost;
                    fee.usdCost = usdCost;

                    if(StringUtils.isNotBlank(params[7])) fee.qty = NumberUtils.toInt(params[7]);

                    fees.add(fee);
                } catch(Exception e) {
                    Logger.warn("SaleFee Parse Have Error! [" + line + "]");
                    Webs.systemMail("SaleFee Parse Have Error!", "<h3>" + line + "</h3>");
                }
            }
        } catch(IOException e) {
            Logger.warn("File is not exist!");
        }
        return fees;
    }


    /**
     * 这个是用来解析 Amazon 每隔 14 天自动生成 FlatV2 的 Payments 的报表
     *
     * @param file
     * @param acc
     * @return
     */
    public static List<SaleFee> flat2FinanceParse(File file, Account acc, Account.M market) {
        List<SaleFee> fees = new ArrayList<SaleFee>();
        Map<String, Orderr> cachedOrder = new HashMap<String, Orderr>();
        Map<String, FeeType> cachedFeeType = new HashMap<String, FeeType>();

        try {
            List<String> lines = FileUtils.readLines(file);
            lines.remove(0);
            lines.remove(0);// 删除最上面的 2 行

            for(String line : lines) {
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
                    Orderr ord = cachedOrder(orderId, cachedOrder);
                    if(ord == null && !StringUtils.startsWith(orderId, "S"))
                        Logger.error("Order[" + orderId + "] is not exist when parsing SaleFee!");
                    else fee.order = ord;
                }

                FeeType type = cachedFeeType(typeStr, cachedFeeType);
                if(type == null) {
                    Logger.warn("Type not found! [" + typeStr + "]");
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
                    case AMAZON_DE:
                    case AMAZON_FR:
                    case AMAZON_ES:
                    case AMAZON_IT:
                        //TODO  ES,IT 没有上, 所以没有 Report 可看, 暂时与 DE, FR 一样的解析
                        fee.date = DateTime.parse(dateStr, DateTimeFormat.forPattern("dd.MM.yyyy")).toDate();
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
                ps.setString(i++, f.type == null ? null : f.type.name);
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
                DB.execute("UPDATE Orderr SET state='" + Orderr.S.REFUNDED.name() + "' WHERE orderId='" + f.orderId + "'");
                Logger.info("Order[%s] state from %s to %s", f.orderId, f.order.state, Orderr.S.REFUNDED);
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
