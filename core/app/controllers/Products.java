package controllers;

import helper.Webs;
import models.PageInfo;
import models.Ret;
import models.market.Account;
import models.market.SellingQTY;
import models.product.Category;
import models.product.Product;
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
        Long count = Product.count();
        PageInfo<Product> pi = new PageInfo<Product>(s, count, p, prods);
        render(prods, cates, pi);
    }

    public static void p_detail(String sku) {
        Product p = Product.findByMerchantSKU(sku);
        List<Category> cats = Category.all().fetch();
        List<SellingQTY> qtys = SellingQTY.qtysAccodingSKU(p);
        render(p, cats, qtys);
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


    /**
     * ========== Product ===============
     */


    public static void p_create(@Valid Product p) {
        if(p.isPersistent()) renderJSON(new Ret(String.format("The Product[%s] is exist!", p.sku)));
        if(Validation.hasErrors()) {
            renderJSON(validation.errorsMap());
        }
        if(p.category == null)
            renderJSON(new Ret("No matched category"));
        if(!Product.validSKU(p.sku))
            renderJSON(new Ret("Not valid SKU format"));
        p.save();
        renderJSON(new Ret(true, "Product[" + p.sku + "] Save Success!"));
    }

    public static void p_u(Product p) {
        try {
            if(p.isPersistent()) p.save();
        } catch(Exception e) {
            renderJSON(new Ret(false, Webs.E(e)));
        }
        renderJSON(new Ret());
    }


    public static void p_sqty_u(SellingQTY q) {
        try {
            if(q.isPersistent()) q.save();
        } catch(Exception e) {
            renderJSON(new Ret(false, Webs.E(e)));
        }
        renderJSON(new Ret(true));
    }


    /**
     * ========== Category ===============
     */

    public static void c_create(@Valid Category c) {
        if(Validation.hasErrors()) {
            renderJSON(validation.errorsMap());
        }
        c.save();
        c.products = null;
        renderJSON(c);
    }


    /**
     * ========== Whouse ===============
     */


    public static void w_create(@Valid Whouse w) {
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        if(w.account == null && !w.account.isPersistent() && w.type != Whouse.T.FBA)
            renderJSON(new Error("account", "Account is not Persistent!", new String[]{}));
        w.save();
        renderJSON(w);
    }


    public static void w_remove(long id) {
        validation.required(id);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        Boolean flag = Whouse.delete("id=?", id) > 0;
        renderJSON(new Ret(flag));
    }

    public static void w_bind_a(Whouse w) {
        validation.required(w.id);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        if(!w.isPersistent() || !w.account.isPersistent())
            renderJSON(new Error("whouse", "Whouse or Accoutn is not persistent!", new String[]{}));
        if(w.type == Whouse.T.FBA) w.save();
        renderJSON(w);
    }
}
