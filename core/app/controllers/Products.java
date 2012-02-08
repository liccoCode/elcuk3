package controllers;

import com.alibaba.fastjson.JSON;
import exception.VErrorRuntimeException;
import helper.Webs;
import models.PageInfo;
import models.market.Account;
import models.product.Category;
import models.product.Product;
import models.product.ProductQTY;
import models.product.Whouse;
import play.data.validation.Error;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 产品模块的基本的类别的基本操作在此
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:57
 */
@With({Secure.class, GzipFilter.class})
@Check("normal")
public class Products extends Controller {

    /**
     * 展示所有的 Product
     */
    public static void p_index(Integer p, Integer s) {
        Webs.fixPage(p, s);
        List<Category> cates = Category.all().fetch();
        List<Product> prods = Product.all().fetch(p, s);
        List<Whouse> whs = Whouse.all().fetch();
        Long count = Product.count();
        PageInfo<Product> pi = new PageInfo<Product>(s, count, p, prods);
        render(prods, cates, count, p, s, whs, pi);
    }

    public static void c_index(Integer p, Integer s) {
        Webs.fixPage(p, s);
        List<Category> cates = Category.all().fetch(p, s);
        Long count = Category.count();

        PageInfo<Category> pi = new PageInfo<Category>(s, count, p, cates);
        render(cates, count, p, s, pi);
    }

    public static void w_index(Integer p, Integer s) {
        Webs.fixPage(p, s);
        List<Whouse> whs = Whouse.all().fetch(p, s);
        Long count = Whouse.count();
        List<Account> accs = Account.all().fetch();

        PageInfo<Whouse> pi = new PageInfo<Whouse>(s, count, p, whs);
        render(whs, accs, count, p, s, pi);
    }


    // ------  创建与保存对象  --------------
    public static void p_create(@Valid Product p) {
        if(Validation.hasErrors()) {
            renderJSON(validation.errorsMap());
        }
        Category cat = Category.find("categoryId=?", p.category.categoryId).first();
        if(p.category == null || cat == null)
            renderJSON(new Error("categoryId", "No matched category", new String[]{}));
        if(!Product.validSKU(p.sku))
            renderJSON(new Error("sku", "Not valid SKU format", new String[]{}));
        p.category = cat;
        p.save();
        renderJSON(JSON.toJSONString(p));
    }

    public static void c_create(@Valid Category c) {
        if(Validation.hasErrors()) {
            renderJSON(validation.errorsMap());
        }
        c.save();
        c.products = null;
        renderJSON(c);
    }

    public static void w_create(@Valid Whouse w) {
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        if(w.account == null && !w.account.isPersistent() && w.type != Whouse.T.FBA)
            renderJSON(new Error("account", "Account is not Persistent!", new String[]{}));
        w.save();
        renderJSON(w);
    }

    public static void pt_create(ProductQTY pt) {
        validation.required(pt.qty);
        if(Validation.hasErrors()) {
            renderJSON(validation.errorsMap());
        }
        try {
            pt.saveAndUpdate();
        } catch(VErrorRuntimeException e) {
            renderJSON(e.getError());
        }
        renderJSON("{\"flag\":\"true\"}");
    }

    public static void w_remove(long id) {
        validation.required(id);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        Boolean flag = Whouse.delete("id=?", id) > 0;
        renderJSON("{\"flag\":\"" + flag + "\"}");
    }

    public static void w_bind_a(Whouse w) {
        validation.required(w.id);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        if(!w.isPersistent() || !w.account.isPersistent())
            renderJSON(new Error("whouse", "Whouse or Accoutn is not persistent!", new String[]{}));
        if(w.type == Whouse.T.FBA) w.save();
        w.qtys = null;
        renderJSON(w);
    }
}
