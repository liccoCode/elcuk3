package controllers;


import helper.J;
import models.product.Product;
import models.view.post.ProfitPost;
import org.apache.commons.lang.StringUtils;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

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
@With({GlobalExceptionHandler.class, Secure.class})
public class Profits extends Controller {

    @Before(only = {"index"})
    public static void setUpIndexPage() {
        F.T2<List<String>, List<String>> categorysToJson = Category.fetchCategorysJson();
        renderArgs.put("categorys", J.json(categorysToJson._2));
        F.T2<List<String>, List<String>> skusToJson = Product.fetchSkusJson();
        renderArgs.put("skus", J.json(skusToJson._2));
    }

    @Check("profits.index")
    public static void index(ProfitPost p) {
        List<Profit> profits = new ArrayList<Profit>();
        if(p == null) {
            p = new ProfitPost();
        } else {
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
                //从ES查找SKU的利润
                profits = p.query();
            }
        }
        render(profits, p);
    }
}
