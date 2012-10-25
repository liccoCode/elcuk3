package controllers;

import helper.J;
import helper.Webs;
import models.ElcukRecord;
import models.market.Account;
import models.market.Selling;
import models.market.SellingQTY;
import models.product.Category;
import models.product.Family;
import models.product.Product;
import models.view.Ret;
import models.view.post.ProductPost;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;

import java.util.List;

/**
 * 产品模块的基本的类别的基本操作在此
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:57
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Products extends Controller {

    @Util
    private static String extarSku() {
        String sku = request.params.get("id");
        if(StringUtils.isBlank(sku)) sku = request.params.get("pro.sku");
        return sku;
    }


    /**
     * 展示所有的 Product
     */
    @Check("products.index")
    public static void index(ProductPost p) {
        if(p == null) p = new ProductPost();
        List<Product> prods = p.query();

        render(prods, p);
    }

    @Before(only = {"show", "update"})
    public static void setUpShowPage() {
        String sku = Products.extarSku();
        Product pro = Product.findByMerchantSKU(sku);

        renderArgs.put("cats", Category.all().<Category>fetch());
        renderArgs.put("qtys", SellingQTY.qtysAccodingSKU(pro));
        renderArgs.put("records", ElcukRecord.records(sku));
    }

    public static void show(String id) {
        Product pro = Product.findByMerchantSKU(id);
        render(pro);
    }

    public static void update(Product pro) {
        validation.valid(pro);
        if(!Product.exist(pro.sku)) Validation.addError("", String.format("Sku %s 不存在!", pro.sku));
        if(Validation.hasErrors()) render("Products/show.html", pro);
        pro.save();
        flash.success("更新成功");
        new ElcukRecord(Messages.get("product.update"), Messages.get("action.base", pro.to_log()), pro.sku).save();
        redirect("/Products/show/" + pro.sku);
    }

    @Before(only = {"saleAmazon", "saleAmazonListing"})
    public static void beforeSaleAmazon() {
        String sku = Products.extarSku();
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
            flash.success("在 %s 上架成功 ASIN: %s.", se.market.toString(), se.asin);
            redirect("/Sellings/selling/" + se.sellingId);
        } catch(Exception e) {
            Validation.addError("", e.getMessage());
            render("Products/saleAmazon.html", s, pro);
        }
    }


    /**
     * ========== Product ===============
     */

    @Before(only = {"blank", "create", "index"})
    public static void setUpCreatePage() {
        List<String> families = Family.familys(true);
        renderArgs.put("families", J.json(families));
    }

    public static void blank(Product pro) {
        if(pro == null) pro = new Product();
        List<Category> cats = Category.all().fetch();
        render(pro, cats);
    }

    public static void create(Product pro) {
        validation.valid(pro);
        pro.createProduct();
        if(Validation.hasErrors()) render("Products/blank.html", pro);
        flash.success("Sku %s 添加成功", pro.sku);
        redirect("/Products/show/" + pro.sku);
    }

    public static void updateSellingQty(SellingQTY qty) {
        if(!SellingQTY.exist(qty.id)) Validation.addError("", String.format("SellingQTY %s 不存在!", qty.id));
        qty.save();
        flash.success("更新成功.");
        new ElcukRecord(Messages.get("sellingqty.update"), Messages.get("action.base", qty.to_log()), qty.product.sku).save();
        redirect("/Products/show/" + qty.product.sku);
    }


    public static void pRemove(Product p) {
        if(!p.isPersistent()) renderJSON(new Ret("产品不存在!"));
        p.removeProduct();
        renderJSON(new Ret());
    }

    //------------------------

    public static void p_sqty_u(SellingQTY q) {
        try {
            if(q.isPersistent()) q.save();
        } catch(Exception e) {
            renderJSON(new Ret(false, Webs.E(e)));
        }
        renderJSON(new Ret(true));
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
