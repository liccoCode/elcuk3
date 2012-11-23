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
import play.Logger;
import play.db.DB;
import play.db.jpa.GenericModel;
import play.libs.F;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
     * 这个是用来解析 Amazon 每隔 14 天自动生成 FlatV2 的 Payments 的报表
     *
     * @param file
     * @return missing: account, order
     */
    public static Map<String, List<SaleFee>> flatFileFinanceParse(File file, Account account) {
        Map<String, F.T2<AtomicInteger, List<SaleFee>>> mapFees = new HashMap<String, F.T2<AtomicInteger, List<SaleFee>>>();
        mapFees.put("SYSTEM", new F.T2<AtomicInteger, List<SaleFee>>(new AtomicInteger(1), new ArrayList<SaleFee>()));

        try {
            List<String> lines = FileUtils.readLines(file);
            lines.remove(0);
            lines.remove(0);// 删除最上面的 2 行

            for(String line : lines) {
                try {

                    String[] params = StringUtils.splitPreserveAllTokens(line, "\t");
                    /**
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
                    if("order".equals(transactionType) || "chargeback refund".equals(transactionType) || "refund".equals(transactionType)) {
                        M market = M.val(params[11].toLowerCase());
                        if(market == null) market = account.type;

                        if(!mapFees.containsKey(orderId))
                            mapFees.put(orderId, new F.T2<AtomicInteger, List<SaleFee>>(new AtomicInteger(), new ArrayList<SaleFee>()));
                        F.T2<AtomicInteger, List<SaleFee>> fees = mapFees.get(orderId);

                        // 计算数量
                        if(StringUtils.isNotBlank(params[22])) {
                            fees._1.set(NumberUtils.toInt(params[22]));
                            continue;
                        }

                        // shipmentFeeType 通过 FBA 向外面发货产生的费用
                        String shipmentFeeType = params[12].toLowerCase();
                        if(addOneFee(params[13], params[17], orderId, transactionType, market, fees, shipmentFeeType))
                            continue;

                        // productcharge, principal, 产品销售额
                        String priceType = params[23].toLowerCase();
                        if(addOneFee(params[24], params[17], transactionType, orderId, market, fees, priceType))
                            continue;

                        // item-relate 费用
                        String itemRelateFeeType = params[25].toLowerCase();
                        addOneFee(params[26], params[17], transactionType, orderId, market, fees, itemRelateFeeType);
                    } else if("storage fee".equals(transactionType) || "refund reimbursal".equals(transactionType) ||
                            "balanceadjustment".equals(transactionType) || "subscription fee".equals(transactionType)) {

                        // Refund Reimbursal, 有订单关联的
                        if(StringUtils.isNotBlank(orderId)) {
                            if(!mapFees.containsKey(orderId))
                                mapFees.put(orderId, new F.T2<AtomicInteger, List<SaleFee>>(new AtomicInteger(), new ArrayList<SaleFee>()));
                            F.T2<AtomicInteger, List<SaleFee>> fees = mapFees.get(orderId);
                            if(addOneFee(params[35], params[17], transactionType, orderId, account.type, fees, transactionType/*不然会自动跳出*/))
                                continue;
                        }

                        addOneFee(params[35], params[17], transactionType, orderId, account.type, mapFees.get("SYSTEM"), transactionType);
                    } else if("disposalcomplete".equals(transactionType)) {
                        addOneFee(params[35], params[17], transactionType, orderId, account.type, mapFees.get("SYSTEM"), transactionType);
                    } else {
                        Webs.systemMail("Unkonw situation in Amazon FlatV2 Finance File", line);
                    }
                } catch(Exception e) {
                    Logger.error("%s parse error.", line);
                }
            }
        } catch(IOException e) {
            Logger.warn("File is not exist!");
        }


        // 在返回前, 还需要将保留再 T2._1 中的 qty 设置到所有的 SaleFee 中
        Map<String, List<SaleFee>> feesMap = new HashMap<String, List<SaleFee>>();
        for(String key : mapFees.keySet()) {
            F.T2<AtomicInteger, List<SaleFee>> feesTuple = mapFees.get(key);
            if(!"SYSTEM".equals(key)) {
                for(SaleFee fee : feesTuple._2)
                    fee.qty = feesTuple._1.get();
            }
            feesMap.put(key, feesTuple._2);
        }
        return feesMap;
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
    private static boolean addOneFee(String cost, String date, String transactionType, String orderId, M market, F.T2<AtomicInteger, List<SaleFee>> fees, String subType) {
        if(StringUtils.isNotBlank(subType)) {
            FeeType feeType = FeeType(subType, transactionType, orderId);
            SaleFee fee = new SaleFee();
            fee.orderId = orderId;
            fee.market = market;
            fee.currency = Currency.M(fee.market);
            fee.cost = NumberUtils.toFloat(cost);
            fee.usdCost = fee.currency.toUSD(fee.cost);
            fee.type = feeType;
            fee.date = Dates.parseXMLGregorianDate(date);
            fee.memo = transactionType;
            fees._2.add(fee);
            return true;
        }
        return false;
    }

    private static FeeType FeeType(String subType, String transactionType, String orderId) {
        FeeType feeType = null;
        if(StringUtils.isBlank(subType))
            feeType = FeeType.findById(transactionType);
        else
            feeType = FeeType.findById(subType);
        if(feeType == null) {
            feeType = new FeeType(subType, FeeType.amazon()).save();
            Webs.systemMail("New PriceType At " + orderId,
                    String.format("FeeType=> transactionType: %s, priceType: %s", transactionType, subType));
        }
        return feeType;
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
