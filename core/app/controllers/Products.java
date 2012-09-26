package controllers;

import helper.J;
import helper.Webs;
import models.market.Account;
import models.market.Selling;
import models.market.SellingQTY;
import models.product.*;
import models.view.AnalyzesPager;
import models.view.Pager;
import models.view.Ret;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Error;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 产品模块的基本的类别的基本操作在此
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:57
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
@Check("normal")
public class Products extends Controller {

    @Before(only = {"index", "search"})
    public static void setFamilys() {
        List<String> skus = Family.familys(false);
        renderArgs.put("fmys", J.json(skus));
    }


    /**
     * 展示所有的 Product
     */
    public static void index(Integer p, Integer s) {
        F.T2<Integer, Integer> fixs = Webs.fixPage(p, s);
        List<Product> prods = Product.all().fetch(fixs._1, fixs._2);

        Long count = Product.count();
        Pager<Product> pi = new Pager<Product>(fixs._1, count, fixs._2, prods);

        render(prods, pi);
    }

    public static void search(String sku) {
        List<Product> prods = new ArrayList<Product>();
        if(!StringUtils.isBlank(sku))
            prods = Product.find("family.family like ?", sku + "%").fetch();
        render("Products/index.html", prods, sku);
    }

    public static void show(String sku) {
        Product p = Product.findByMerchantSKU(sku);
        List<Category> cats = Category.all().fetch();
        List<SellingQTY> qtys = SellingQTY.qtysAccodingSKU(p);
        List<AttrName> attnames = AttrName.productUnuseAttrName(p);
        List<Account> accs = Account.openedSaleAcc();

        F.T2<List<Selling>, List<String>> sellingAndSellingIds = Selling.sameFamilySellings(p.sku);
        renderArgs.put("sids", J.json(sellingAndSellingIds._2));
        render(p, cats, qtys, attnames, accs);
    }

    public static void c_index(Integer p, Integer s) {
        F.T2<Integer, Integer> fixs = Webs.fixPage(p, s);
        List<Category> cates = Category.all().fetch(fixs._1, fixs._2);
        Long count = Category.count();

        Pager<Category> pi = new Pager<Category>(fixs._1, count, fixs._2, cates);
        render(cates, count, p, s, pi);
    }


    /**
     * ========== Product ===============
     */

    public static void blank(Product p) {
        if(p == null) p = new Product();
        if(p.isPersistent()) throw new FastRuntimeException("[" + p.sku + "]产品已经存在.");
        List<Category> cats = Category.all().fetch();
        render(p, cats);
    }

    public static void cat_brands(Category c) {
        List<Brand> brands = c.brands;
        List<AttrName> attrs = AttrName.all().fetch();
        List<AttrName> cAttrs = c.attrNames;


        Map<String, Object> json = new HashMap<String, Object>();
        json.put("brands", brands);
        json.put("attrs", attrs);
        json.put("cAttrs", cAttrs);

        renderJSON(J.G(json));
    }

    public static void brand_family(Brand b, Category c) {
        List<Family> fmys = Family.bcRelateFamily(b, c);
        for(Family f : fmys) { // 清理不能让 GSON 拥有循环引用
            f.brand = null;
            f.category = null;
            f.products = null;
        }
        renderJSON(fmys);
    }

    public static void pCreate(@Valid Product p) {
        if(p.isPersistent()) renderJSON(new Ret("SKU(" + p.sku + ")已经存在了!"));
        try {
            p.createProduct();
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(new Ret(true, "/products/show?sku=" + p.sku));
    }

    public static void pRemove(Product p) {
        if(!p.isPersistent()) renderJSON(new Ret("产品不存在!"));
        p.removeProduct();
        renderJSON(new Ret());
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

    public static void p_attr(List<Attribute> attrs, Product p) {
        int saved = 0;
        int update = 0;
        for(Attribute a : attrs) {
            Attribute ma = Attribute.findById(a.id);
            if(ma != null && ma.isPersistent()) {
                update++;
                ma.updateAttr(a);
            } else {
                saved++;
                a.product = p;
                a.save();
            }
        }

        renderJSON(new Ret(true, "Save:" + saved + ",Update:" + update));
    }

    public static void saleAmazonListing(Selling s, Product p) {
        /**
         * 从前台上传来的一系列的值检查
         */
        Validation.required(Messages.get("s.title"), s.aps.title);
        Validation.required(Messages.get("s.upc"), s.aps.upc);
        Validation.required(Messages.get("s.manufac"), s.aps.manufacturer);
        Validation.required(Messages.get("s.rbn"), s.aps.rbns.toArray());
        Validation.required(Messages.get("s.price"), s.aps.standerPrice);
        Validation.required(Messages.get("s.tech"), s.aps.keyFeturess);
        Validation.required(Messages.get("s.keys"), s.aps.searchTermss);
        Validation.required(Messages.get("s.prodDesc"), s.aps.productDesc);
        Validation.required(Messages.get("s.msku_req"), s.merchantSKU);
        if(Validation.hasErrors()) renderJSON(new Ret(Validation.current().errorsMap()));

        // 在 Controller 里面将值处理好
        try {
            Selling se = p.saleAmazon(s);
            renderJSON(J.G(se));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    public static void upcCheck(String upc) {
        /**
         * UPC 的检查;
         * 1. 在哪一些 Selling 身上使用过?
         * 2. 通过 UPC 与
         */
        try {
            List<Selling> upcSellings = Selling.find("aps.upc like '%" + upc + "%'").fetch();
            renderJSON(J.G(upcSellings));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }
}
