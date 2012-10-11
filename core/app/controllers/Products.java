package controllers;

import helper.J;
import helper.Webs;
import models.market.Account;
import models.market.Selling;
import models.market.SellingQTY;
import models.product.*;
import models.view.Pager;
import models.view.Ret;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
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

    public static void show(String id) {
        Product pro = Product.findByMerchantSKU(id);
        List<Category> cats = Category.all().fetch();
        List<SellingQTY> qtys = SellingQTY.qtysAccodingSKU(pro);
        List<AttrName> attnames = AttrName.productUnuseAttrName(pro);


        render(pro, cats, qtys, attnames);
    }

    public static void c_index(Integer p, Integer s) {
        F.T2<Integer, Integer> fixs = Webs.fixPage(p, s);
        List<Category> cates = Category.all().fetch(fixs._1, fixs._2);
        Long count = Category.count();

        Pager<Category> pi = new Pager<Category>(fixs._1, count, fixs._2, cates);
        render(cates, count, p, s, pi);
    }

    /**
     * ================= New Product Action ============
     */

    public static void update(Product pro) {
        validation.valid(pro);
        if(Validation.hasErrors()) render("Products/show.html", pro);
        pro.save();
        flash.success("更新成功");
        redirect("/Products/show/" + pro.sku);
    }

    @Before(only = {"saleAmazon", "saleAmazonListing"})
    public static void beforeSaleAmazon() {
        String sku = request.params.get("id");
        if(StringUtils.isBlank(sku)) sku = request.params.get("pro.sku");
        if(StringUtils.isNotBlank(sku)) renderArgs.put("sids", J.json(Selling.sameFamilySellings(sku)._2));
        renderArgs.put("accs", Account.openedSaleAcc());
    }

    public static void saleAmazon(String id) {
        Product pro = Product.findByMerchantSKU(id);
        Selling s = new Selling();
        render(pro, s);
    }

    public static void saleAmazonListing(Selling s, Product pro) {
        /**
         * 从前台上传来的一系列的值检查
         */
        // 检查
        validation.valid(s);
        s.aps.validate();
        if(Validation.hasErrors()) render("Products/saleAmazon.html", s, pro);

        try {
            Selling se = pro.saleAmazon(s);
            redirect("/Sellings/selling/" + se.sellingId);
        } catch(Exception e) {
            Validation.addError("", e.getMessage());
            render("Products/saleAmazon.html", s, pro);
        }
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
        renderJSON(new Ret(true, "/products/show/" + p.sku));
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
