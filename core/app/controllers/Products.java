package controllers;

import helper.J;
import helper.Webs;
import models.ElcukRecord;
import models.market.Account;
import models.market.M;
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
import play.utils.FastRuntimeException;

import java.util.List;

/**
 * 产品模块的基本的类别的基本操作在此
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:57
 */
@With({GlobalExceptionHandler.class, Secure.class})
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
        new ElcukRecord(Messages.get("product.update"), Messages.get("action.base", pro.to_log()),
                pro.sku).save();
        redirect("/Products/show/" + pro.sku);
    }

    public static void saleAmazon(String id) {
        Product product = Product.findByMerchantSKU(id);
        Selling s = new Selling();
        renderArgs.put("accs", Account.openedSaleAcc());
        render(product, s);
    }

    @Check("products.saleamazonlisting")
    public static void saleAmazonListing(Selling s) {
        try {
            checkAuthenticity();
            s.buildFromProduct();
            renderJSON(new Ret(true, s.sellingId));
        } catch(FastRuntimeException e) {
            renderJSON(new Ret(e.getMessage()));
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
        if(!SellingQTY.exist(qty.id))
            Validation.addError("", String.format("SellingQTY %s 不存在!", qty.id));
        qty.save();
        flash.success("更新成功.");
        new ElcukRecord(Messages.get("sellingqty.update"),
                Messages.get("action.base", qty.to_log()), qty.product.sku).save();
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

    /**
     * 检查 UPC 上架情况
     */
    public static void upcCheck(String upc) {
        try {
            List<Selling> upcSellings = Selling.find("aps.upc=?", upc).fetch();
            renderJSON(J.G(upcSellings));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 检查 SKU 是否上架情况
     */
    public static void skuMarketCheck(String sku, String market) {
        Product product = Product.findById(StringUtils.split(sku, ",")[0]);
        M mkt = M.AMAZON_DE;
        try {
            mkt = M.val(market);
        } catch(Exception e) {
            mkt = M.AMAZON_DE;
        }
        if(product != null) {
            renderJSON(J.G(product.sellingCountWithMarket(mkt)));
        } else {
            renderJSON(new Ret("SKU: [" + sku + "] 不存在!"));
        }
    }

    /**
     * 获取用来提示用户的 RBN 文件的下载地址
     *
     * @param market 市场
     */
    public static void showRBNLink(String market) {
        String suffix = "/catm/classifier/ProductClassifier.amzncatm/classifier/ProductClassifier.amzn?ref=ag_pclasshm_cont_invfile";
        Ret ret = new Ret("#");
        if(StringUtils.contains(market, "AMAZON_DE")) {
            ret = new Ret("https://catalog-mapper-de.amazon.de" + suffix);
        } else if(StringUtils.contains(market, "AMAZON_UK")) {
            ret = new Ret("https://catalog-mapper-uk.amazon.co.uk" + suffix);
        } else if(StringUtils.contains(market, "AMAZON_US")) {
            ret = new Ret("https://catalog-mapper-na.amazon.com" + suffix);
        }
        renderJSON(ret);
    }
}
