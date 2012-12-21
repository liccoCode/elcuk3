package jobs;

import helper.Currency;
import helper.Dates;
import helper.Webs;
import jobs.promise.FinanceRefundOrders;
import jobs.promise.FinanceShippedOrders;
import models.Jobex;
import models.finance.FeeType;
import models.finance.SaleFee;
import models.market.M;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 用来触发更新 Shipped 与 Refund 的订单的第一步 Payment 信息;
 * <pre>
 *     Amazon Payment 信息分为两部获取:
 *     1. 为通过 Shipped 与 Refund 的订单上 Amazon 后台页面, 区分抓取处理.
 *     2. 监控不同账户的 Payments 页面, 如果找到变化就邮件通知, 然后让人手工
 *        对这一批次的订单的 Payment 信息详细化; 因为只有在这份文档中, Amazon 才会
 *        将所有订单的全部详细信息列举出来.
 * </pre>
 * Transaction 的数据
 * 周期:
 * - 轮询周期: 1mn
 * - Duration: 5mn
 * User: wyattpan
 * Date: 3/19/12
 * Time: 12:01 PM
 */
@Every("1mn")
public class FinanceCheckJob extends Job {
    //https://sellercentral.amazon.de/gp/reports/documents/_GET_V2_SETTLEMENT_REPORT_DATA__15836299764.txt?ie=UTF8&contentType=text%2Fxls
    //https://sellercentral.amazon.de/gp/reports/documents/_GET_V2_SETTLEMENT_REPORT_DATA__15522920744.txt?ie=UTF8&contentType=text%2Fxls

    @Override
    public void doJob() {
        if(!Jobex.findByClassName(FinanceCheckJob.class.getName()).isExcute()) return;

        // SHIPPED 的订单的抓取
        new FinanceShippedOrders().now();

        // Refund 订单的抓取
        new FinanceRefundOrders().now();
    }

    /**
     * 从 https://sellercentral.amazon.co.uk/gp/payments-account/view-transactions.html?orderId=026-5308409-7603533&view=search&range=all&x=6&y=17
     * 这样的网页中解析出一个订单的费用的总览
     *
     * @param html
     * @return 返回的 SaleFee 需要设置 Account 账户
     */
    public static List<SaleFee> oneTransactionFee(String html) {
        try {
            Document doc = Jsoup.parse(html);

            M market = M.val(doc.select("#marketplaceSelect option[selected]").text());
            if(market == null)
                market = M.val(doc.select("#merchant-website").text().trim());

            Element table = doc.select("#content-main-entities").first();
            Elements rows = table.select("> table:eq(2) tr[class!=list-row-white]");
            String orderId = doc.select("#orderId[value]").val();
            List<SaleFee> fees = new ArrayList<SaleFee>();
            for(Element row : rows)
                fees.addAll(oneRowFee(market, orderId, row));
            return fees;
        } catch(Exception e) {
            Logger.warn("Is Account not login? [%s]", Webs.E(e));
        }
        return new ArrayList<SaleFee>();
    }

    /**
     * 处理一行的数据
     *
     * @param market
     * @param orderId
     * @param row
     * @return
     */
    private static List<SaleFee> oneRowFee(M market, String orderId, Element row) {
        Elements tds = row.select("td");
        SaleFee fee = new SaleFee();
        float amazonFeeCost = 0;
        switch(market) {
            case AMAZON_US:
                fee.currency = Currency.USD;
                fee.cost = FeeUS(tds.get(4));
                amazonFeeCost = FeeUS(tds.get(5)) + FeeUS(tds.get(6)) + FeeUS(tds.get(7));
                break;
            case AMAZON_UK:
                fee.currency = Currency.GBP;
                fee.cost = FeeUK(tds.get(4));
                amazonFeeCost = FeeUK(tds.get(5)) + FeeUK(tds.get(6)) + FeeUK(tds.get(7));
                break;
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                fee.currency = Currency.EUR;
                fee.cost = FeeDE(tds.get(4));
                amazonFeeCost = FeeDE(tds.get(5)) + FeeDE(tds.get(6)) + FeeDE(tds.get(7));
                break;
            default:
                fee.currency = Currency.USD;
        }
        fee.market = market;
        fee.orderId = orderId;
        fee.order = Orderr.findById(fee.orderId);
        fee.usdCost = fee.currency.toUSD(fee.cost);
        // 第一次的时候, 销售额使用 productcharges 类型, 第二次使用 Amazon 文档中的 Principal 类型, 区分开
        fee.type = FeeType.findById("productcharges");
        fee.date = Dates.transactionDate(market, tds.get(0).text());


        SaleFee amazonFee = new SaleFee(fee);
        amazonFee.type = FeeType.findById("amazon");
        amazonFee.cost = amazonFeeCost;
        amazonFee.usdCost = amazonFee.currency.toUSD(amazonFee.cost);
        return Arrays.asList(fee, amazonFee);
    }

    /**
     * 处理 $21.98 与 -$5.50 这样的值, 去除 $
     *
     * @param td
     * @return
     */
    private static float FeeUS(Element td) {
        String text = td.text();
        return NumberUtils.toFloat(StringUtils.remove(StringUtils.remove(text, "$"), ","));
    }


    /**
     * 处理 €1221.98 与 -€5.50 这样的值, 去除 €
     *
     * @param td
     * @return
     */
    private static float FeeDE(Element td) {
        String text = td.text();
        // ,->. .->, 的形式在这里, 只要保持 Amazon 的账号为 English 语言, 则不需要转换
        return NumberUtils.toFloat(StringUtils.remove(StringUtils.remove(text, "€"), ","));
    }

    /**
     * 处理 £1221.98 与 -£5.50 这样的值, 去除 £
     *
     * @param td
     * @return
     */
    private static float FeeUK(Element td) {
        String text = td.text();
        // ,->. .->, 的形式在这里, 只要保持 Amazon 的账号为 English 语言, 则不需要转换
        return NumberUtils.toFloat(StringUtils.remove(StringUtils.remove(text, "£"), ","));
    }
}
