package controllers;

import com.google.gson.Gson;
import helper.Constant;
import helper.Webs;
import models.PageInfo;
import models.Ret;
import models.market.Account;
import models.market.Selling;
import models.market.SellingQTY;
import models.product.*;
import org.apache.commons.io.FileUtils;
import play.Logger;
import play.data.validation.Error;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.io.File;
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
@With({Secure.class, GzipFilter.class})
@Check("normal")
public class Products extends Controller {

    /**
     * 展示所有的 Product
     */
    public static void p_index(Integer p, Integer s) {
        Integer[] fixs = Webs.fixPage(p, s);
        p = fixs[0];
        s = fixs[1];
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
        List<AttrName> attnames = AttrName.productUnuseAttrName(p);
        List<Account> accs = Account.all().fetch();

        List<String> sellingIds = new ArrayList<String>();
        List<Selling> cateSellings = Selling.find("listing.product.category=?", p.category).fetch();
        for(Selling s : cateSellings) sellingIds.add(s.sellingId);
        renderArgs.put("sellingDataSource", new Gson().toJson(sellingIds));
        render(p, cats, qtys, attnames, accs);
    }

    public static void c_index(Integer p, Integer s) {
        Integer[] fixs = Webs.fixPage(p, s);
        p = fixs[0];
        s = fixs[1];
        List<Category> cates = Category.all().fetch(p, s);
        Long count = Category.count();

        PageInfo<Category> pi = new PageInfo<Category>(s, count, p, cates);
        render(cates, count, p, s, pi);
    }

    public static void w_index(Integer p, Integer s) {
        Integer[] fixs = Webs.fixPage(p, s);
        p = fixs[0];
        s = fixs[1];
        List<Whouse> whs = Whouse.all().fetch(p, s);
        Long count = Whouse.count();
        List<Account> accs = Account.all().fetch();

        PageInfo<Whouse> pi = new PageInfo<Whouse>(s, count, p, whs);
        render(whs, accs, count, p, s, pi);
    }


    /**
     * ========== Product ===============
     */

    public static void pNew(Product p) {
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

        renderJSON(Webs.exposeGson(json));
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

    /**
     * 给 Product 关联图片
     *
     * @param a
     */
    public static void upload(Attach a) {
        long subfix = Attach.count("fid=?", a.fid.toUpperCase());
        a.p = Attach.P.SKU;
        a.fileSize = a.file.length();
        a.fileName = String.format("%s_%s%s", a.fid, subfix, a.file.getPath().substring(a.file.getPath().lastIndexOf("."))).trim();
        a.location = String.format("%s/%s", Constant.UPLOAD_PATH, a.fileName);
        Logger.info("%s File save to %s.[%s kb]", a.fid, a.location, a.fileSize / 1024);
        try {
            FileUtils.copyFile(a.file, new File(a.location));
            a.save();
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(Webs.exposeGson(a));
    }

    public static void images(String sku) {
        List<Attach> imgs = Attach.find("fid=?", sku).fetch();
        renderJSON(Webs.exposeGson(imgs));
    }

    public static void rmimage(Attach a) {
        Attach attach = Attach.findAttach(a);
        if(attach == null) renderJSON(new Ret("系统中不存在."));
        try {
            attach.rm();
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(new Ret());
    }

    public static void pCreate(@Valid Product p) {
        if(p.isPersistent()) renderJSON(new Ret("SKU(" + p.sku + ")已经存在了!"));
        try {
            p.createProduct();
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(new Ret(true, "/products/p_detail?sku=71LNTPAD-BPU361"));
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
        Validation.required(Messages.get("s.rbn"), s.aps.rbns);
        Validation.required(Messages.get("s.price"), s.aps.standerPrice);
        Validation.required(Messages.get("s.tech"), s.aps.keyFeturess);
        Validation.required(Messages.get("s.keys"), s.aps.searchTermss);
        Validation.required(Messages.get("s.prodDesc"), s.aps.productDesc);
        Validation.required(Messages.get("s.msku_req"), s.merchantSKU);
        if(Validation.hasErrors()) renderJSON(new Ret(Validation.current().errorsMap()));
        if(Selling.exist(s.merchantSKU)) renderJSON(new Ret(Messages.get("s.msku")));

        // 在 Controller 里面将值处理好
        Selling se = p.saleAmazon(s);
        renderJSON(Webs.exposeGson(se));
    }

    public static void upcCheck(String upc) {
        /**
         * UPC 的检查;
         * 1. 在哪一些 Selling 身上使用过?
         * 2. 通过 UPC 与
         */
        try {
            List<Selling> upcSellings = Selling.find("aps.upc like '%" + upc + "%'").fetch();
            renderJSON(Webs.exposeGson(upcSellings));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
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
