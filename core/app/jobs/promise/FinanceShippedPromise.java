package jobs.promise;

import helper.Currency;
import helper.Dates;
import helper.HTTP;
import helper.Webs;
import jobs.AmazonFinanceCheckJob;
import models.finance.FeeType;
import models.finance.SaleFee;
import models.market.Account;
import models.market.M;
import models.market.Orderr;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;
import play.Play;
import play.jobs.Job;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 6/4/13
 * Time: 10:45 AM
 * @deprecated
 */
public class FinanceShippedPromise extends Job<List<SaleFee>> {
    private Account account;
    private M market;
    /**
     * 传递一个值
     */
    private long leftOrders = 0;
    private List<String> orderIds = new ArrayList<>();
    private List<String> missingFeeType = new ArrayList<>();
    private List<String> warnningOrders = new ArrayList<>();
    private List<String> errorMsg = new ArrayList<>();


    public FinanceShippedPromise(Account account, M market, List<String> orderIds) {
        this.account = account;
        this.market = market;
        this.orderIds = orderIds;
        this.leftOrders = -1;
    }

    public FinanceShippedPromise(Account account, M market, List<String> orderIds, long leftOrders) {
        this.account = account;
        this.market = market;
        this.orderIds = orderIds;
        this.leftOrders = leftOrders;
    }

    @Override
    public List<SaleFee> doJobWithResult() {


        /**
         * 检查意大利登陆是否有效，重新登陆一次
         */
        if(this.market == M.AMAZON_IT) {
            List<String> urls = this.transactionURLs("402-0275820-2101172");
            if(urls.size() <= 0)
                this.account.loginAmazonSellerCenter();
        }
//        if(this.market == M.AMAZON_DE) {
//            List<String> urls = this.transactionURLs("028-1135738-8832340");
//            if(urls.size() <= 0)
//                this.account.loginAmazonSellerCenter();
//        }
//        if(this.market == M.AMAZON_FR) {
//            List<String> urls = this.transactionURLs("402-5493577-1293136");
//            if(urls.size() <= 0)
//                this.account.loginAmazonSellerCenter();
//        }
//        if(this.market == M.AMAZON_US) {
//            List<String> urls = this.transactionURLs("002-0186811-1738602");
//            if(urls.size() <= 0)
//                this.account.loginAmazonSellerCenter();
//        }
//        if(this.market == M.AMAZON_UK) {
//            List<String> urls = this.transactionURLs("026-1947342-7795530");
//            if(urls.size() <= 0)
//                this.account.loginAmazonSellerCenter();
//        }
//        if(this.market == M.AMAZON_ES) {
//            List<String> urls = this.transactionURLs("171-2088753-0234721");
//            if(urls.size() <= 0)
//                this.account.loginAmazonSellerCenter();
//        }


        // 1. 访问 Transaction View 获取 transaction detail URL
        // 2. 访问 transaction detail URL 解析出订单的 SaleFee
        List<SaleFee> fees = new ArrayList<>();
        if(orderIds != null && orderIds.size() > 0) {
            //synchronized(this.account.cookieStore()) {
            try {
                this.account.changeRegion(this.market);
                for(String orderId : orderIds) {
                    List<String> urls = this.transactionURLs(orderId);

                    List<SaleFee> orderFees = new ArrayList<>();
                    for(String url : urls) {
                        orderFees.addAll(this.saleFees(url));
                    }
                    fees.addAll(orderFees);
                    if(FinanceShippedPromise.isWarnning(orderFees)) {
                        Orderr changeOrderr = Orderr.findById(orderId);
                        changeOrderr.warnning = FinanceShippedPromise.isWarnning(orderFees);
                        changeOrderr.save();
                    }
                }
            } finally {
                this.account.changeRegion(this.account.type);
                emailWarnningCheck();
            }
            //}
            AmazonFinanceCheckJob.deleteSaleFees(orderIds);
            AmazonFinanceCheckJob.saveFees(fees);
            /**
             * 更新订单标志feeflag为2,表示已处理salefee
             */
            AmazonFinanceCheckJob.updateFeeFlag(orderIds);
            Logger.info("AmazonFinanceCheckJob deal %s %s %s Orders and %s SaleFees, left %s Orders to fetch.",
                    this.account.prettyName(), this.market.name(), orderIds.size(), fees.size(), this.leftOrders);
        } else {
            if(this.account == null) {
                Logger.info("AmazonFinanceCheckJob nullaccount %s No Fees founded.", this.market);
            } else {
                Logger.info("AmazonFinanceCheckJob %s %s No Fees founded.", this.account.prettyName(), this.market);
            }
        }
        return fees;
    }

    /**
     * 邮件警告检查
     */
    private void emailWarnningCheck() {
        if(warnningOrders.size() > 0 || errorMsg.size() > 0) {
            Webs.systemMail(
                    "New Fee Type: " + StringUtils.join(this.missingFeeType, ","),
                    StringUtils.join(this.warnningOrders, ",") +
                            "<br><br><br>" +
                            StringUtils.join(this.errorMsg, "<br>")
            );
            try {
                Thread.sleep(500);
            } catch(InterruptedException e) {
                //ignore
            }
        }
    }

    public String transactionView(String orderId) {
        return HTTP.get(this.account.cookieStore(), this.account.type.oneTransactionFees(orderId));
    }

    public String transactionDetail(String url) {
        return HTTP.get(this.account.cookieStore(), url);
    }

    public List<SaleFee> saleFees(String url) {
        String html = this.transactionDetail(url);
        if(Play.mode.isDev()) {
            try {
                FileUtils.writeStringToFile(new File("./" + System.currentTimeMillis() + ".html"), html);
            } catch(IOException e) {
                //ignore
            }
        }
        Document doc = Jsoup.parse(html);
        /**
         * 1. Product charges
         * 2. Ohter
         * 3. Amazon Fees
         */
        List<SaleFee> fees = new ArrayList<>();

        fees.addAll(productCharges(doc, url));
        fees.addAll(promotionFee(doc, url));
        fees.addAll(otherFee(doc, url));
        fees.addAll(amazonFee(doc, url));
        Logger.info("Shipped Order SaleFee(%s): %s", fees.size(), url);
        return fees;
    }

    /**
     * 抽取 ProductCharges 费用
     *
     * @param doc
     * @param url
     * @return
     */
    public List<SaleFee> productCharges(Document doc, String url) {
        return adjustFiveOrThreeChild(doc, url, "#breakdown_data_Product_charges", FeeType.productCharger());
    }

    public List<SaleFee> promotionFee(Document doc, String url) {
        return adjustFiveOrThreeChild(doc, url, "#breakdown_data_Promo_rebates", FeeType.promotions());
    }

    /**
     * 抽取 others fee 部分的费用(例如 shipping)
     *
     * @param doc
     * @param url
     * @return
     */
    public List<SaleFee> otherFee(Document doc, String url) {
        return adjustFiveOrThreeChild(doc, url, "#breakdown_data_Other", FeeType.shipping());
    }

    /**
     * 抽取 Amazon Fees 部分的费用
     *
     * @param doc
     * @param url
     * @return
     */
    public List<SaleFee> amazonFee(Document doc, String url) {
        return adjustFiveOrThreeChild(doc, url, "#breakdown_data_Amazon_fees", null);
    }

    /**
     * 自动选择使用拥有 5 个 td 子元素还是 3 个 td 子元素进行解析
     *
     * @param doc
     * @param url
     * @param select
     * @param feeType
     * @return
     */
    private List<SaleFee> adjustFiveOrThreeChild(Document doc, String url, String select, FeeType feeType) {
        List<SaleFee> fees = new ArrayList<>();
        Element promotions = doc.select(select).first();
        if(promotions == null) return fees;
        Element nextPromotion = promotions.nextElementSibling();
        while(nextPromotion != null) {
            if(StringUtils.isNotBlank(nextPromotion.id()))
                break;

            SaleFee fee = null;
            if(nextPromotion.children().size() == 5) {
                fee = haveFiveTdChild(doc, url, nextPromotion, feeType);
            } else if(nextPromotion.children().size() == 3) {
                fee = haveThreeTdChild(doc, url, nextPromotion, feeType);
            }
            if(fee != null) fees.add(fee);
            nextPromotion = nextPromotion.nextElementSibling();
        }
        return fees;
    }

    private SaleFee haveFiveTdChild(Document doc, String url, Element nextTr, FeeType feeType) {
        if(nextTr.children().size() != 5)
            return null;

        SaleFee fee = new SaleFee();
        fee.orderId = StringUtils.split(StringUtils.splitByWholeSeparator(url, "orderId=")[1], "&")[0];
        fee.account = this.account;
        fee.market = this.market;

        String feedate = doc.select("#transaction_date").text().split(":")[1].trim();
        String[] feedateformate = feedate.split("\\.");
        Logger.info("::::::" + feedateformate.length + "  " + feedate);
        if(feedateformate != null && feedateformate.length >= 3) {
            Calendar c = Calendar.getInstance();
            c.set(Integer.parseInt(feedateformate[2]),
                    Integer.parseInt(feedateformate[1]),
                    Integer.parseInt(feedateformate[0]), 0, 0);
            fee.date = c.getTime();
        } else
            fee.date = this.date(feedate);
        fee.cost = this.fee(nextTr.select("td:eq(4)").text());
        fee.currency = this.currency(nextTr.select("td:eq(4)").text());
        fee.usdCost = fee.currency.toUSD(fee.cost);
        fee.qty = NumberUtils.toInt(nextTr.select("td:eq(2)").text().split(":")[1].trim(), 1);
        fee.type = feeType;
        return feeTypeCheck(fee, nextTr, url) ? fee : null;
    }

    private SaleFee haveThreeTdChild(Document doc, String url, Element nextTr, FeeType feeType) {
        if(nextTr.children().size() != 3)
            return null;

        SaleFee fee = new SaleFee();
        fee.orderId = StringUtils.split(StringUtils.splitByWholeSeparator(url, "orderId=")[1], "&")[0];
        fee.account = this.account;
        fee.market = this.market;

        String feedate = doc.select("#transaction_date").text().split(":")[1].trim();
        String[] feedateformate = feedate.split("\\.");
        if(feedateformate != null && feedateformate.length > 2) {
            Calendar c = Calendar.getInstance();
            c.set(Integer.parseInt(feedateformate[2]),
                    Integer.parseInt(feedateformate[1]),
                    Integer.parseInt(feedateformate[0]), 0, 0);
            fee.date = c.getTime();
        } else
            fee.date = this.date(feedate);

        fee.cost = this.fee(nextTr.select("td:eq(2)").text());
        fee.currency = this.currency(nextTr.select("td:eq(2)").text());
        fee.usdCost = fee.currency.toUSD(fee.cost);
        fee.qty = 1;
        fee.type = this.amazonFeeType(nextTr.select("td:eq(0)").text());
        if(fee.type == null) fee.type = feeType;
        return feeTypeCheck(fee, nextTr, url) ? fee : null;
    }

    /**
     * 检查是否有新的 FeeType, 只有当检查通过才可以将 fee 添加到系统中.
     *
     * @param fee
     * @param nextElement
     * @param url
     * @return
     */
    private boolean feeTypeCheck(SaleFee fee, Element nextElement, String url) {
        if(fee.type == null) {
            String orderId = StringUtils.split(StringUtils.splitByWholeSeparator(url, "orderId=")[1], "&")[0];
            String text = nextElement.select("td:eq(0)").text();
            StringBuilder sb = new StringBuilder();
            sb.append("请删除当前订单的 SaleFee 让其重新抓取<br><br>")
                    .append("<a href='").append(url).append("'>").append(text).append("</a><br><br>")
                    .append("OrderId:")
                    .append(orderId)
                    .append("<br><br>")
                    .append(nextElement.outerHtml())
                    .append("<br><br><br>");
            this.missingFeeType.add(text);
            this.warnningOrders.add(orderId);
            this.errorMsg.add(sb.toString());
            return false;
        }
        return true;
    }

    public FeeType amazonFeeType(String text) {
        text = text.toLowerCase();
        if(text.equals("commission:")) {
            return FeeType.findById("commission");
        } else if(text.contains("referral fee")) {
            return FeeType.findById("commission");
        } else if(text.contains("refund commission")) {
            return FeeType.findById("refundcommission");
        } else if(text.contains("cross-border")) {
            return FeeType.findById("crossborderfulfilmentfee");
        } else if(text.contains("pick & pack") || text.contains("pick &amp; pack")) {
            return FeeType.findById("fbapickpackfeeperunit");
        } else if(text.contains("weight handling")) {
            return FeeType.findById("fbaweighthandlingfee");
        } else if(text.contains("weight based")) {
            return FeeType.findById("fbaweightbasedfee");
        } else if(text.equals("shipping:")) {
            return FeeType.findById("shipping");
        } else if(text.contains("shipping chargeback")) {
            return FeeType.findById("shippingchargeback");
        } else if(text.contains("order handling")) {
            return FeeType.findById("fbaorderhandlingfeeperorder");
        } else if(text.contains("gift wrap chargeback")) {
            return FeeType.findById("giftwrapchargeback");
        } else if(text.equals("gift wrap:")) {
            return FeeType.findById("giftwrap");
        } else if(text.equals("promorebates:")) {
            return FeeType.findById("promorebates");
        } else if(text.contains("per unit fulfillment")) {
            return FeeType.findById("fbaperunitfulfillmentfee");
        } else if(text.contains("per order fulfillment") || text.contains("per order fulfilment")) {
            return FeeType.findById("fbaperorderfulfilmentfee");
        } else if(text.contains("variable closing")) {
            return FeeType.findById("variableclosingfee");
        } else if(text.contains("shipping holdback")) {
            return FeeType.findById("shippingholdback");
        } else {
            return null;
        }
    }

    public List<String> transactionURLs(String orderId) {


        String html = this.transactionView(orderId);
        List<String> urls = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        Elements tables = doc.select("#content-main-entities table");
        Elements rows = doc.select("#content-main-entities table:eq(2) tr");

        if(rows.size() <= 0) return urls;
        // 去除第一行 title
        rows.remove(0);
        for(Element row : rows) {
            String url = row.select("td").last().select("a").attr("href");
            urls.add(url);
            Logger.info("FinanceShippedPromise URL: %s", url);
        }
        return urls;
    }

    public Float fee(String text) {
        text = text.replace(",", ".");
        if(Arrays.asList(M.AMAZON_DE, M.AMAZON_ES, M.AMAZON_FR, M.AMAZON_IT).contains(this.market)) {
            return NumberUtils.toFloat(StringUtils.remove(StringUtils.remove(text, "€"), ","));
        } else if(M.AMAZON_UK == this.market) {
            return NumberUtils.toFloat(StringUtils.remove(StringUtils.remove(text, "£"), ","));
        } else if(M.AMAZON_US == this.market) {
            return NumberUtils.toFloat(StringUtils.remove(StringUtils.remove(text, "$"), ","));
        } else if(M.AMAZON_JP == this.market) {
            return NumberUtils.toFloat(StringUtils.remove(StringUtils.remove(text, "￥"), ","));
        } else if(M.AMAZON_CA == this.market) {
            return NumberUtils.toFloat(StringUtils.remove(StringUtils.remove(text, "CAD"), ","));
        }
        return 0f;
    }

    public Currency currency(String text) {
        if(text.contains("€")) {
            return Currency.EUR;
        } else if(text.contains("£")) {
            return Currency.GBP;
        } else if(text.contains("$")) {
            return Currency.USD;
        } else {
            return Currency.USD;
        }
    }

    public Date date(String text) {
        return Dates.transactionDate(this.market, text);
    }

    public Account getAccount() {
        return this.account;
    }

    public M getMarket() {
        return this.market;
    }

    /**
     * 判断订单是否需要警告 (收取的费用占 30% 以上)
     *
     * @param fees
     * @return
     */
    public static boolean isWarnning(List<SaleFee> fees) {
        float totalSales = 0;
        float totalMarketFees = 0;
        for(SaleFee fee : fees) {
            if(fee.type.parent != null && !"amazon".equals(fee.type.parent.name)) continue;
            if("principal".equals(fee.type.name) || "productcharges".equals(fee.type.name)) {
                totalSales += fee.usdCost;
            } else {
                totalMarketFees += fee.usdCost;
            }
        }
        return (totalSales > 0) && (totalMarketFees / totalSales) > 0.3;
    }
}
