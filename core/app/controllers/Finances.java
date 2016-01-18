package controllers;

import com.alibaba.fastjson.JSON;
import controllers.api.SystemOperation;
import helper.Caches;
import helper.Dates;
import helper.HTTP;
import helper.J;
import jobs.AmazonOrderFetchJob;
import jobs.AmazonOrderUpdateJob;
import jobs.analyze.SellingSaleAnalyzeJob;
import jobs.promise.FinanceShippedPromise;
import models.finance.SaleFee;
import models.market.Account;
import models.market.JobRequest;
import models.market.M;
import models.product.Category;
import models.product.Product;
import models.view.dto.AnalyzeDTO;
import models.view.post.ProfitPost;
import models.view.post.SkuProfitPost;
import models.view.report.Profit;
import models.view.report.SkuProfit;
import org.apache.commons.lang3.StringUtils;
import play.cache.Cache;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 与财务有关的操作
 * User: wyattpan
 * Date: 3/20/12
 * Time: 10:11 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
@Check("finances")
public class Finances extends Controller {


    @Check("finances.index")
    public static void index() {
        List<Account> accs = Account.openedSaleAcc();
        render(accs);
    }

//    @Check("finances.fixfinance")

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

    public static void promotion(String orderId, long aid, String m) {
        List<String> orderIds = Arrays.asList(StringUtils.split(orderId, ","));
        Account acc = Account.findById(aid);
        M market = M.val(m);
        try {
            List<SaleFee> fees = new FinanceShippedPromise(acc, market, orderIds, 10).now().get();
            renderText(fees);
        } catch(Exception e) {
            renderText(e.getMessage());
        }
    }

    @Before(only = {"skuProfitAnalyse"})
    public static void setUpIndexPage() {
        List<String> categoryIds = Category.categoryIds();
        renderArgs.put("categorys", categoryIds);
        F.T2<List<String>, List<String>> skusToJson = Product.fetchSkusJson();
        renderArgs.put("skus", J.json(skusToJson._2));
    }

    public static void skuProfitAnalyse(SkuProfitPost p) {
        List<SkuProfit> skuProfits = new ArrayList<SkuProfit>();
        if(p == null) {
            p = new SkuProfitPost();
            render(skuProfits, p);
        } else {
            p.end = Dates.night(p.end);
            if(StringUtils.isBlank(p.categories) && StringUtils.isBlank(p.sku)) {
                flash.error("未选择category或者sku!");
            } else {
                if(StringUtils.isNotEmpty(p.sku)) {
                    if(!Product.exist(p.sku)) {
                        flash.error("系统不存在sku:" + p.sku);
                        render(skuProfits, p);
                    }
                }

                if(StringUtils.isNotEmpty(p.categories)) {
                    if(!Category.is_exist_ids(p.categories)) {
                        flash.error("系统不存在category:" + p.categories);
                        render(skuProfits, p);
                    }
                }
                String sku_key = "";
                String market_key = "";
                String categories_key = "";
                if(p.sku != null) sku_key = p.sku;
                if(p.pmarket != null) market_key = p.pmarket;
                if(p.categories != null) categories_key = p.categories.toLowerCase();

                String post_key = Caches.Q.cacheKey("skuprofitpost", p.begin, p.end, categories_key, sku_key, market_key);
                skuProfits = Cache.get(post_key, List.class);
                if(skuProfits != null) {
                    render(skuProfits, p);
                } else {
                    String category_names = "";
                    int is_sku = 0;
                    if(StringUtils.isNotBlank(p.sku)) {
                        category_names = p.sku;
                        is_sku = 1;
                    } else {
                        category_names = p.categories.toLowerCase();
                    }
                    HTTP.get("http://rock.easya.cc:4567/sku_profit_batch_work?categories=" + category_names
                            + "&market=" + market_key + "&from=" + new SimpleDateFormat("yyyy-MM-dd").format(p.begin)
                            + "&to=" + new SimpleDateFormat("yyyy-MM-dd").format(p.end) + "&is_sku=" + is_sku);
                    skuProfits = new ArrayList<SkuProfit>();
                    flash.error("后台事务正在计算中,请稍候...");
                }
            }
            render(skuProfits, p);
        }
    }

}
