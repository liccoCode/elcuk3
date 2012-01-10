package controllers;

import com.alibaba.fastjson.JSON;
import models.product.Category;
import models.product.Product;
import play.data.validation.Error;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:57
 */
public class Products extends Controller {

    /**
     * 展示所有的 Product
     */
    public static void p_index(Integer p, Integer s) {
        if(p == null || p < 0) p = 1; // 判断在页码
        if(s == null || s < 10 || s > 100) s = 20; // 判断显示的条数控制
        List<Category> cates = Category.all().fetch();
        List<Product> prods = Product.all().fetch(p, s);
        render(prods, cates);
    }

    public static void c_index(Integer p, Integer s) {
        if(p == null || p < 0) p = 1; // 判断在页码
        if(s == null || s < 10 || s > 100) s = 20; // 判断显示的条数控制
        List<Category> cates = Category.all().fetch(p, s);
        render(cates);
    }


    // ---------- 等到页面编写完成后将下面删除

    /**
     * 创建一个 Product; 仅仅是单独的创建一个 Product,
     * Product 所关联的数据可以后续添加
     *
     * @param p
     */
    public static void p_create(@Valid Product p) {
        if(Validation.hasErrors()) {
            renderJSON(validation.errorsMap());
        }
        if(p.category == null || Category.find("categoryId=?", p.category.categoryId).first() == null) {
            renderJSON(new Error("categoryId", "no match category", new String[]{}));
        }
        p.save();
        renderJSON(p);
    }

    public static void u(Product p) {
        validation.required(p.id);
        validation.required(p.sku);
        renderJSON(p.save());
    }

    public static void r(String sku) {
        validation.required(sku);
        Product prod = Product.find("sku=?", sku).first();
        renderJSON(JSON.toJSONString(prod));
    }

    public static void d(String sku) {
        validation.required(sku);
        Product prod = Product.find("sku=?", sku).first();
        renderJSON(JSON.toJSONString(prod.delete()));
    }

}
