package jobs.promise;

import helper.Currency;
import helper.Dates;
import helper.HTTP;
import helper.Webs;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 6/4/13
 * Time: 10:45 AM
 */
public class FinanceShippedPromise extends Job<List<SaleFee>> {
    private Account account;
    private M market;
    private List<Orderr> orders;
    private List<String> warnningOrders = new ArrayList<String>();
    private List<String> errorMsg = new ArrayList<String>();

    public FinanceShippedPromise(Account account, M market, List<Orderr> orders) {
        this.account = account;
        this.market = market;
        this.orders = orders;
    }

    @Override
    public List<SaleFee> doJobWithResult() {
        // 1. 访问 Transaction View 获取 transaction detail URL
        // 2. 访问 transaction detail URL 解析出订单的 SaleFee
        List<SaleFee> fees = new ArrayList<SaleFee>();
        synchronized(this.account.cookieStore()) {
            try {
                this.account.changeRegion(this.market);
                for(Orderr order : orders) {
                    List<String> urls = this.transactionURLs(order.orderId);
                    List<SaleFee> orderFees = new ArrayList<SaleFee>();
                    for(String url : urls) {
                        orderFees.addAll(this.saleFees(url));
                    }
                    fees.addAll(orderFees);
                    Orderr changeOrderr = Orderr.findById(order.orderId);
                    changeOrderr.warnning = FinanceShippedPromise.isWarnning(orderFees);
                    changeOrderr.save();
                }
            } finally {
                this.account.changeRegion(this.account.type);
                if(warnningOrders.size() > 0 || errorMsg.size() > 0) {
                    Webs.systemMail(
                            "New Fee Type on " + this.account.prettyName() + ": " + this.market,
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
        }
        return fees;
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
        List<SaleFee> fees = new ArrayList<SaleFee>();

        fees.addAll(productCharges(doc, url));
        fees.addAll(otherFee(doc, url));
        fees.addAll(amazonFee(doc, url));
        return fees;
    }

    public List<SaleFee> amazonFee(Document doc, String url) {
        List<SaleFee> fees = new ArrayList<SaleFee>();
        Element amazons = doc.select("#breakdown_data_Amazon_fees").first();
        if(amazons == null) return fees;
        Element nextElement = amazons.nextElementSibling();
        while(nextElement != null) {
            if(nextElement.children().size() < 3)
                nextElement = nextElement.nextElementSibling();
            if(StringUtils.isNotBlank(nextElement.id()))
                break;

            SaleFee fee = new SaleFee();
            fee.orderId = StringUtils.split(StringUtils.splitByWholeSeparator(url, "orderId=")[1], "&")[0];
            fee.account = this.account;
            fee.cost = this.fee(nextElement.select("td:eq(2)").text());
            fee.market = this.market;
            fee.currency = this.currency(nextElement.select("td:eq(2)").text());
            fee.usdCost = fee.currency.toUSD(fee.cost);
            fee.date = this.date(doc.select("#transaction_date").text().split(":")[1].trim());
            fee.qty = 1;
            fee.type = this.amazonFeeType(nextElement.select("td:eq(0)").text());
            if(feeTypeCheck(fee, nextElement, url))
                fees.add(fee);
            nextElement = nextElement.nextElementSibling();
        }
        return fees;
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
        } else if(text.contains("giftwrap chargeback")) {
            return FeeType.findById("giftwrapchargeback");
        } else if(text.equals("giftwrap:")) {
            return FeeType.findById("giftwrap");
        } else if(text.equals("promorebates:")) {
            return FeeType.findById("promorebates");
        } else if(text.contains("per unit fulfillment")) {
            return FeeType.findById("fbaperunitfulfillmentfee");
        } else if(text.contains("per order fulfillment")) {
            return FeeType.findById("fbaperorderfulfilmentfee");
        } else if(text.contains("variable closing")) {
            return FeeType.findById("variableclosingfee");
        } else {
            return null;
        }
    }

    public List<SaleFee> otherFee(Document doc, String url) {
        List<SaleFee> fees = new ArrayList<SaleFee>();
        Element others = doc.select("#breakdown_data_Other").first();
        if(others == null) return fees;
        Element nextElement = others.nextElementSibling();

        while(nextElement != null) {
            if(nextElement.children().size() < 3)
                nextElement = nextElement.nextElementSibling();
            if(StringUtils.isNotBlank(nextElement.id()))
                break;

            SaleFee fee = new SaleFee();
            fee.orderId = StringUtils.split(StringUtils.splitByWholeSeparator(url, "orderId=")[1], "&")[0];
            fee.account = this.account;
            fee.cost = this.fee(nextElement.select("td:eq(2)").text());
            fee.market = this.market;
            fee.currency = this.currency(nextElement.select("td:eq(2)").text());
            fee.usdCost = fee.currency.toUSD(fee.cost);
            fee.date = this.date(doc.select("#transaction_date").text().split(":")[1].trim());
            fee.qty = 1;
            fee.type = FeeType.shipping();
            if(feeTypeCheck(fee, nextElement, url))
                fees.add(fee);

            nextElement = nextElement.nextElementSibling();
        }
        return fees;
    }

    public List<SaleFee> productCharges(Document doc, String url) {
        List<SaleFee> fees = new ArrayList<SaleFee>();
        Element productCharge = doc.select("#breakdown_data_Product_charges").first();
        if(productCharge == null) return fees;
        Element nextElement = productCharge.nextElementSibling();

        while(nextElement != null) {
            if(nextElement.children().size() < 5)
                nextElement = nextElement.nextElementSibling();
            if(StringUtils.isNotBlank(nextElement.id()))
                break;

            SaleFee fee = new SaleFee();
            fee.orderId = StringUtils.split(StringUtils.splitByWholeSeparator(url, "orderId=")[1], "&")[0];
            fee.account = this.account;
            fee.cost = this.fee(nextElement.select("td:eq(4)").text());
            fee.market = this.market;
            fee.currency = this.currency(nextElement.select("td:eq(4)").text());
            fee.usdCost = fee.currency.toUSD(fee.cost);
            fee.date = this.date(doc.select("#transaction_date").text().split(":")[1].trim());
            fee.qty = NumberUtils.toInt(nextElement.select("td:eq(2)").text().split(":")[1], 1);
            fee.type = FeeType.productCharger();
            if(feeTypeCheck(fee, nextElement, url))
                fees.add(fee);

            nextElement = nextElement.nextElementSibling();
        }
        return fees;
    }

    public List<String> transactionURLs(String orderId) {
        String html = this.transactionView(orderId);
        List<String> urls = new ArrayList<String>();

        Document doc = Jsoup.parse(html);
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
        if(Arrays.asList(M.AMAZON_DE, M.AMAZON_ES, M.AMAZON_FR, M.AMAZON_IT).contains(this.market)) {
            return NumberUtils.toFloat(StringUtils.remove(StringUtils.remove(text, "€"), ","));
        } else if(M.AMAZON_UK == this.market) {
            return NumberUtils.toFloat(StringUtils.remove(StringUtils.remove(text, "£"), ","));
        } else if(M.AMAZON_US == this.market) {
            return NumberUtils.toFloat(StringUtils.remove(StringUtils.remove(text, "$"), ","));
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

    public List<Orderr> getOrders() {
        return this.orders;
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
