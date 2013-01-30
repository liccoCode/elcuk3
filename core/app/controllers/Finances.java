package controllers;

import helper.Webs;
import jobs.AmazonOrderFetchJob;
import jobs.AmazonOrderUpdateJob;
import jobs.promise.FinanceCrawlPromise;
import models.finance.SaleFee;
import models.market.Account;
import models.market.JobRequest;
import models.market.M;
import models.market.Selling;
import models.product.Product;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 与财务有关的操作
 * User: wyattpan
 * Date: 3/20/12
 * Time: 10:11 AM
 */
@With({GlobalExceptionHandler.class, Secure.class})
@Check("finances")
public class Finances extends Controller {


    @Check("finances.index")
    public static void index() {
        List<Account> accs = Account.openedSaleAcc();
        render(accs);
    }

    @Check("finances.fixfinance")
    public static void fixFinance(File file, long accId) {
        Account acc = Account.findById(accId);
        if(acc == null)
            error("Error Account Id");

        FinanceCrawlPromise worker = new FinanceCrawlPromise(acc, new Date());
        Map<String, List<SaleFee>> feeMap = SaleFee.flatFileFinanceParse(file, acc);
        worker.parseToDB(feeMap);
        renderText("Deals %s Orders.", feeMap.keySet().size());
    }

    /**
     * 修复 Amazon 上已经有的 Selling 没有则无法创建
     */
    @Check("finances.addselling")
    public static void addSelling(String url, String sku, String upc) {
        // 修复方法, 直接写在 Controller 中了.
        String[] parts = Finances.parseUrl(url);
        if(parts.length != 3) {
            flash.error("URL 格式错误!");
            index();
        }
        Product product = Product.findById(sku);
        if(product == null) {
            flash.error("Product 不存在!");
            index();
        }

        M market = M.val(parts[1]);
        String asin = parts[2];

        Account acc = null;
        if(market == M.AMAZON_UK)
            acc = Account.findById(1l);
        else if(market == M.AMAZON_DE)
            acc = Account.findById(2l);
        else if(market == M.AMAZON_US)
            acc = Account.findById(131l);
        else {
            flash.error("%s 市场还不支持!", market);
            index();
        }

        try {
            Selling selling = new Selling().patchASelling(sku, upc, asin, market, acc, product);
            flash.success("添加成功, 可进入 Selling 进行 Sync 同步信息.");
            redirect("/sellings/selling/" + selling.sellingId);
        } catch(Exception e) {
            flash.error(Webs.E(e));
            index();
        }
    }

    @Check("finances.reparseorder")
    public static void reParseOrder(File file, Account acc) {
        String name = file.getName();
        JobRequest req = new JobRequest();
        req.account = acc;
        req.path = file.getAbsolutePath();
        if(name.contains("txt")) {
            new AmazonOrderUpdateJob().callBack(req);
        } else if(name.contains("xml")) {
            new AmazonOrderFetchJob().callBack(req);
        }
        renderText("已经处理.");
    }

    /**
     * 从 Amazon 的 product url 解析出需要的 market, asin 的字符串
     *
     * @param url
     * @return [0]: url, [1]: market, [2]: asin
     */
    @Util
    public static String[] parseUrl(String url) {
        Pattern ptn = Pattern.compile("http[s]?://[w]{0,3}[\\.]?(.*)/dp/(\\w+)");
        Matcher matcher = ptn.matcher(url);
        List<String> parts = new ArrayList<String>();
        if(matcher.find()) {
            System.out.println(matcher.groupCount());
            for(int i = 0; i <= matcher.groupCount(); i++) {
                parts.add(matcher.group(i));
            }
        }
        return parts.toArray(new String[parts.size()]);
    }
}
