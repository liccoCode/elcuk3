package controllers;


import com.alibaba.fastjson.JSON;
import controllers.api.SystemOperation;
import helper.Caches;
import helper.Dates;
import helper.HTTP;
import helper.J;
import jobs.analyze.SellingSaleAnalyzeJob;

import models.product.Product;
import models.view.Ret;
import models.view.dto.AnalyzeDTO;
import models.view.post.ProfitPost;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.text.SimpleDateFormat;

import jobs.analyze.ProfitInventorySearch;

import java.util.List;

import models.view.report.Profit;

import java.util.ArrayList;

import models.product.Category;

/**
 * 利润计算
 * User: cary
 * Date: 3/10/14
 * Time: 2:29 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Profits extends Controller {

    @Before(only = {"index"})
    public static void setUpIndexPage() {
        List<String> categoryIds = Category.categoryIds();
        renderArgs.put("categorys", categoryIds);
        F.T2<List<String>, List<String>> skusToJson = Product.fetchSkusJson();
        renderArgs.put("skus", J.json(skusToJson._2));
    }

    @Check("profits.index")
    public static void index(ProfitPost p) {
        List<Profit> profits = new ArrayList<Profit>();
        if(p == null) {
            p = new ProfitPost();
            render(profits, p);
        } else {
            p.end = Dates.night(p.end);
            String cacke_key = SellingSaleAnalyzeJob.AnalyzeDTO_SID_CACHE;
            // 这个地方有缓存, 但还是需要一个全局锁, 控制并发, 如果需要写缓存则锁住

            List<AnalyzeDTO> dtos = null;
            String cache_str = Caches.get(cacke_key);
            if(!StringUtils.isBlank(cache_str)) {
                dtos = JSON.parseArray(cache_str, AnalyzeDTO.class);
            }
            // 用于提示后台正在运行计算
            if(StringUtils.isBlank(cache_str) || dtos == null) {
                flash.error("Analyze后台事务正在执行中,请稍候...");
                render(profits, p);
            }


            if(StringUtils.isBlank(p.category) && StringUtils.isBlank(p.sku)) {
                flash.error("未选择category或者sku!");
            } else {
                if(!StringUtils.isBlank(p.sku)) {
                    if(!Product.exist(p.sku)) {
                        flash.error("系统不存在sku:" + p.sku);
                        render(profits, p);
                    }
                }

                if(!StringUtils.isBlank(p.category)) {
                    if(!Category.exist(p.category)) {
                        flash.error("系统不存在category:" + p.category);
                        render(profits, p);
                    }
                }


                String skukey = "";
                String marketkey = "";
                String categorykey = "";
                if(p.sku != null) skukey = p.sku;
                if(p.pmarket != null) marketkey = p.pmarket;
                if(p.category != null) categorykey = p.category.toLowerCase();

                String postkey = helper.Caches.Q.cacheKey("profitpost", p.begin, p.end, categorykey, skukey,
                        marketkey);
                profits = Cache.get(postkey, List.class);
                if(profits != null) {
                    render(profits, p);
                } else {
                    if(StringUtils.isNotBlank(p.sku)) {
                        categorykey = p.sku;
                    }
                    postkey = "profitpost_" + categorykey + "_" + marketkey + "_"
                            + new SimpleDateFormat("yyyyMMdd").format(p.begin) + "_"
                            + new SimpleDateFormat("yyyyMMdd").format(p.end);
                    String postvalue = Caches.get(postkey);
                    if(StringUtils.isBlank(postvalue)) {
                        String categoryname = "";
                        int is_sku = 0;
                        if(StringUtils.isNotBlank(p.sku)) {
                            categoryname = p.sku;
                            is_sku = 1;
                        } else {
                            categoryname = p.category.toLowerCase();
                        }
                        HTTP.get("http://"+models.OperatorConfig.getVal("rockendurl")+":4567/profit_batch_work?category=" + categoryname
                                + "&market=" + marketkey + "&from="
                                + new SimpleDateFormat("yyyy-MM-dd").format(p.begin)
                                + "&to="
                                + new SimpleDateFormat("yyyy-MM-dd").format(p.end)
                                + "&is_sku=" + is_sku);
                        profits = new ArrayList<Profit>();
                        flash.error("后台事务正在计算中,请稍候...");
                    }
                }
            }
            render(profits, p);
        }
    }


    public static void inventory(ProfitPost p) {
        List<Profit> profits = new ArrayList<Profit>();
        if(p == null) {
            p = new ProfitPost();
            render(profits, p);
        } else {
            p.end = Dates.night(p.end);
            String cacke_key = SellingSaleAnalyzeJob.AnalyzeDTO_SID_CACHE;
            // 这个地方有缓存, 但还是需要一个全局锁, 控制并发, 如果需要写缓存则锁住

            List<AnalyzeDTO> dtos = null;
            String cache_str = Caches.get(cacke_key);
            if(!StringUtils.isBlank(cache_str)) {
                dtos = JSON.parseArray(cache_str, AnalyzeDTO.class);
            }
            // 用于提示后台正在运行计算
            if(StringUtils.isBlank(cache_str) || dtos == null) {
                renderJSON(new Ret("Analyze后台事务正在执行中,请稍候..."));
            }
            new ProfitInventorySearch(p).now();
            renderJSON(new Ret("正在计算库存成本!"));
        }
    }
}
