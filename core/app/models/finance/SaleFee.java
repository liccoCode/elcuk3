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
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.Logger;
import play.db.DB;
import play.db.jpa.Model;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * 记录在销售过程中, 不同市场产生的不同的费用;
 * 例如: Amazon 的 Refund 费用, Order Payment 费用 等等...
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:58 AM
 */
@Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class SaleFee extends Model {

    private static final long serialVersionUID = -8565064073185613540L;

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

    /**
     * 一个冗余字段, 用来标识一些在 Orderr 中找不到的订单, 用来处理新旧 SaleFee 更替的.
     */
    public String groupId;

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

    public String product_sku;

    public String transaction_type;

    public String md5_id;

    public String orderitem_sku;


    /**
     * 这个是用来解析 Amazon 每隔 14 天自动生成 FlatV2 的 Payments 的报表; 解析 Flat File 而不是方便的 Flat2 是因为 DE 没有 T.T
     *
     * @param file
     * @return missing: order(use order_id)
     */
    public static Map<String, List<SaleFee>> flatFileFinanceParse(File file, Account account) {
        Map<String, F.T2<AtomicInteger, List<SaleFee>>> mapFees = new HashMap<>();
        mapFees.put("SYSTEM", new F.T2<>(new AtomicInteger(1),
                new ArrayList<>()));

        try {
            List<String> lines = FileUtils.readLines(file);
            lines.remove(0);
            lines.remove(0);// 删除最上面的 2 行

            List<String> emailLines = new ArrayList<>();
            for(String line : lines) {
                try {

                    String[] params = StringUtils.splitPreserveAllTokens(line, "\t");
                    /*
                     * 1. Order
                     * 2. Refund
                     * 3. Storage Fee
                     * 4. DisposalComplete
                     * 5. Subscription Fee
                     * 6. Refund Reimbursal
                     * 7. BalanceAdjustment
                     * 8. Chargeback Refund
                     */
                    String transactionType = params[6].toLowerCase();
                    String orderId = params[7];
                    if("order".equals(transactionType) || "chargeback refund".equals(transactionType)
                            || "refund".equals(transactionType) || "adjustment".equals(transactionType)) {
                        M market = M.val(params[11].toLowerCase());
                        if(market == null) market = account.type;
                        if(!mapFees.containsKey(orderId))
                            mapFees.put(orderId, new F.T2<>(new AtomicInteger(), new ArrayList<>()));
                        F.T2<AtomicInteger, List<SaleFee>> fees = mapFees.get(orderId);

                        // 计算数量
                        if(StringUtils.isNotBlank(params[22])) {
                            fees._1.set(NumberUtils.toInt(params[22]));
                            continue;
                        }

                        // shipmentFeeType 通过 FBA 向外面发货产生的费用
                        String shipmentFeeType = params[12].toLowerCase();
                        if(addOneFee(params[13], params[17], orderId, transactionType, market, fees,
                                account, shipmentFeeType))
                            continue;

                        // productcharge, principal, 产品销售额
                        String priceType = params[23].toLowerCase();
                        if(addOneFee(params[24], params[17], transactionType, orderId, market, fees,
                                account, priceType))
                            continue;

                        // item-relate 费用
                        String itemRelateFeeType = params[25].toLowerCase();
                        addOneFee(params[26], params[17], transactionType, orderId, market, fees,
                                account, itemRelateFeeType);
                    } else if("storage fee".equals(transactionType) || "refund reimbursal".equals(transactionType)
                            || "balanceadjustment".equals(transactionType) || "subscription fee".equals(transactionType)
                            || "removalcomplete".equals(transactionType)) {
                        if(StringUtils.isNotBlank(orderId)) {
                            if(!mapFees.containsKey(orderId))
                                mapFees.put(orderId,
                                        new F.T2<>(new AtomicInteger(),
                                                new ArrayList<>()));
                            F.T2<AtomicInteger, List<SaleFee>> fees = mapFees.get(orderId);
                            if(addOneFee(lastPrice(params), params[17], transactionType, orderId,
                                    account.type, fees, account, transactionType/*不然会自动跳出*/))
                                continue;
                        }

                        addOneFee(lastPrice(params), params[17], transactionType, orderId,
                                account.type, mapFees.get("SYSTEM"), account, transactionType);
                    } else if("disposalcomplete".equals(transactionType)) {
                        addOneFee(lastPrice(params), params[17], transactionType, orderId,
                                account.type, mapFees.get("SYSTEM"), account, transactionType);
                    } else {
                        emailLines.add(line);
                    }
                } catch(Exception e) {
                    emailLines.add(line);
                    Logger.error("%s parse error. [%s]", Webs.e(e), line);
                }
            }
            if(emailLines.size() > 0)
                Webs.systemMail("Unkonw situation in Amazon FlatV2 Finance File",
                        StringUtils.join(emailLines, "\r\n"));
        } catch(IOException e) {
            Logger.warn("File is not exist!");
        }


        // 在返回前, 还需要将保留再 T2._1 中的 qty 设置到所有的 SaleFee 中
        Map<String, List<SaleFee>> feesMap = new HashMap<>();
        for(String key : mapFees.keySet()) {
            F.T2<AtomicInteger, List<SaleFee>> feesTuple = mapFees.get(key);
            if(!"SYSTEM".equals(key)) {
                for(SaleFee fee : feesTuple._2) {
                    fee.qty = feesTuple._1.get() <= 0 ? 1 : feesTuple._1.get();
                }
            }
            feesMap.put(key, feesTuple._2);
        }
        return feesMap;
    }

    /**
     * 用来解析最后的 32 或者是 35 位的费用值; 因为 Amazon 的原因, 添加了三个值, 但是以前的文件缺没有
     *
     * @return
     */
    private static String lastPrice(String[] params) {
        try {
            return params[35];
        } catch(Exception e) {
            return params[32];
        }
    }

    /**
     * 向 Map<String, List<T2> 中添加一个 SaleFee; subType 与 transactionType 优先选择 subType 如果没有自动选择 transactionType
     *
     * @param cost
     * @param date
     * @param transactionType
     * @param orderId
     * @param market
     * @param fees
     * @param subType
     * @return
     */
    private static boolean addOneFee(String cost, String date,
                                     String transactionType, String orderId,
                                     M market, F.T2<AtomicInteger, List<SaleFee>> fees,
                                     Account acc, String subType) {
        if(StringUtils.isNotBlank(subType)) {
            FeeType feeType = feeType(subType, transactionType, orderId);
            SaleFee fee = new SaleFee();
            fee.orderId = orderId;
            fee.market = market;
            fee.currency = Currency.m(fee.market);
            fee.cost = NumberUtils.toFloat(cost);
            fee.account = acc;
            fee.usdCost = fee.currency.toUSD(fee.cost);
            fee.type = feeType;
            fee.date = Dates.parseXMLGregorianDate(date);
            fee.memo = transactionType;
            fees._2.add(fee);
            return true;
        }
        return false;
    }

    private static FeeType feeType(String subType, String transactionType, String orderId) {
        FeeType feeType = null;
        if(StringUtils.isBlank(subType))
            feeType = FeeType.findById(transactionType);
        else
            feeType = FeeType.findById(subType);
        if(feeType == null) {
            feeType = new FeeType(subType, FeeType.amazon()).save();
            Webs.systemMail("New PriceType At " + orderId,
                    String.format("FeeType=> transactionType: %s, priceType: %s", transactionType,
                            subType));
        }
        return feeType;
    }


    /**
     * 由于这个数据量比较大, 所以提供一个批处理保存.
     *
     * @param fees
     */
    public static void batchSaveWithJDBC(List<SaleFee> fees) {
        PreparedStatement ps = null;
        try {
            ps = DB.getConnection().prepareStatement(
                    "INSERT INTO SaleFee(id, cost, currency, `DATE`, market, memo, orderId, qty, usdCost, account_id, order_orderId, type_name) "
                            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            for(SaleFee f : fees) {
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
                // 处理订单外键, 例如 SYSTEM, S20-xx 之类的不需要设置外键
                if(f.order == null) {
                    if(Pattern.matches(Orderr.AMAZON_ORDERID.pattern(), f.orderId))
                        ps.setString(i++, f.orderId);
                    else
                        ps.setString(i++, null);
                } else
                    ps.setString(i++, f.order.orderId);
                ps.setString(i, f.type == null ? null : f.type.name);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch(Exception e) {
            throw new DBException(e);
        } finally {
            try {
                if(ps != null) ps.close();
            } catch(Exception e) {
                Logger.error(Webs.s(e));
            }
        }
    }

    /**
     * 删除第一阶段 Finance 的 productcharges 与 amazon fee;
     * 后续会有 princle 与具体的费用替代
     *
     * @param orderId
     */
    public static void deleteStateOneSaleFees(String orderId) {
        SaleFee.delete("order.orderId=? AND type.name IN (?,?)", orderId, "productcharges",
                "amazon");
    }

    /**
     * 删除指定 OrderId 下的所有的 SaleFee
     *
     * @param orderId
     */
    public static void deleteOrderRelateFee(String orderId) {
        PreparedStatement ps = null;
        try {
            ps = DB.getConnection().prepareStatement(
                    // 不要使用 orderId 这个没索引,速度太慢了.
                    "DELETE FROM SaleFee WHERE order_orderId=?");
            ps.setString(1, orderId);
            ps.executeUpdate();
        } catch(Exception e) {
            throw new FastRuntimeException(e);
        } finally {
            try {
                if(ps != null) ps.close();
            } catch(Exception e) {
                Logger.error(Webs.s(e));
            }
        }
    }

    @Override
    public String toString() {
        return "SaleFee" + "{type=" + type
                + ", id='" + id + '\''
                + ", market=" + market
                + ", memo='" + memo + '\''
                + ", orderId='" + orderId + '\''
                + ", date=" + date
                + ", cost=" + cost
                + ", currency=" + currency
                + ", usdCost=" + usdCost
                + ", qty=" + qty
                + '}';
    }
}
