package controllers;

import controllers.api.SystemOperation;
import helper.J;
import helper.Webs;
import models.ElcukRecord;
import models.market.Account;
import models.market.M;
import models.market.Selling;
import models.market.SellingQTY;
import models.procure.Cooperator;
import models.product.*;
import models.view.Ret;
import models.view.post.ProductPost;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;
import play.utils.FastRuntimeException;
import query.SkuESQuery;

import java.util.Collections;
import java.util.List;

/**
 * 产品模块的基本的类别的基本操作在此
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:57
 */
@With({GlobalExceptionHandler.class, Secure.class,SystemOperation.class})
public class Products extends Controller {

    @Util
    private static String extarSku() {
        String sku = request.params.get("id");
        if(StringUtils.isBlank(sku)) sku = request.params.get("pro.sku");
        return sku;
    }

    @Before(only = {"index"})
    public static void setIndexLog() {
        renderArgs.put("records", ElcukRecord.fid("product.destroy").<ElcukRecord>fetch(50));
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

    @Before(only = {"show", "update", "delete"})
    public static void setUpShowPage() {
        String sku = Products.extarSku();
        Product pro = Product.findByMerchantSKU(sku);

        float procureqty = SkuESQuery.esProcureQty(pro.sku);
        //附加属性模板
        List<Template> templates = pro.category.templates;
        // 附加属性排序
        List<ProductAttr> atts = pro.productAttrs;
        Collections.sort(atts);

        renderArgs.put("templates", templates);
        renderArgs.put("procureqty", procureqty);
        renderArgs.put("cats", Category.all().<Category>fetch());
        renderArgs.put("qtys", SellingQTY.qtysAccodingSKU(pro));
        renderArgs.put("records", ElcukRecord.records(sku));
    }

    public static void show(String id) {
        Product pro = Product.findByMerchantSKU(id);
        pro.arryParamSetUP(Product.FLAG.STR_TO_ARRAY);
        render(pro);
    }

    public static void copy(String choseid, String skuid, String base, String extend, String attach) {
        Product pro = Product.copyProduct(choseid, skuid, base, extend, attach);
        render("Products/show.html", pro);
    }

    public static void backup(String choseid, String base, String extend, String attach, String sku, String family) {
        Product pro = Product.backupProduct(choseid, base, extend, attach, sku, family);
        render("Products/show.html", pro);
    }


    public static void update(Product pro) {
        try {
            validation.valid(pro);
            if(!Product.exist(pro.sku)) Validation.addError("", String.format("Sku %s 不存在!", pro.sku));

            if(Validation.hasErrors())
                renderJSON(Webs.VJson(Validation.errors()));

            pro.arryParamSetUP(Product.FLAG.ARRAY_TO_STR);
            pro.save();
            new ElcukRecord(Messages.get("product.update"), Messages.get("action.base", pro.to_log()),
                    pro.sku).save();
            renderJSON(new Ret(true, ""));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    public static void saleAmazon(String id) {
        Product product = Product.findByMerchantSKU(id);
        F.T2<List<Selling>, List<String>> sellingAndSellingIds = Selling.sameFamilySellings(product.sku);
        Selling s = new Selling();
        renderArgs.put("sids", J.json(sellingAndSellingIds._2));
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

    @Before(only = {"blank", "create", "index", "backup"})
    public static void setUpCreatePage() {
        List<String> families = Family.familys(true);
        List<String> products = Product.skus(true);
        renderArgs.put("families", J.json(families));
        renderArgs.put("products", J.json(products));
    }

    public static void blank(Product pro) {
        if(pro == null) pro = new Product();
        pro.beforeData();
        List<Category> cats = Category.all().fetch();
        render(pro, cats);
    }

    public static void create(Product pro) {
        validation.valid(pro);
        pro.arryParamSetUP(Product.FLAG.ARRAY_TO_STR);
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
     * 获取用来提示用户的 RBN 信息的查阅地址
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
        } else if(StringUtils.contains(market, "AMAZON_IT")) {
            ret = new Ret("https://catalog-mapper-it.amazon.it" + suffix);
        } else if(StringUtils.contains(market, "AMAZON_JP")) {
            ret = new Ret("https://catalog-mapper-jp.amazon.co.jp" + suffix);
        } else if(StringUtils.contains(market, "AMAZON_FR")) {
            ret = new Ret("https://catalog-mapper-fr.amazon.fr" + suffix);
        } else if(StringUtils.contains(market, "AMAZON_CA")) {
            ret = new Ret("https://catalog-mapper-ca.amazon.ca" + suffix);
        }
        renderJSON(ret);
    }

    /**
     * 获取拥有这个 SKU 的所有供应商
     */
    public static void cooperators(String sku) {
        List<Cooperator> cooperatorList = Cooperator
                .find("SELECT c FROM Cooperator c, IN(c.cooperItems) ci WHERE ci.sku=? ORDER BY ci.id", sku)
                .fetch();
        StringBuffer buff = new StringBuffer();
        buff.append("[");
        for(Cooperator co : cooperatorList) {
            buff.append("{").append("\"").append("id").append("\"").append(":").append("\"").append(co.id).append
                    ("\"").append(",").append("\"").append("name").append("\"").append(":").append("\"").append(co.name)
                    .append("\"").append("},");
        }
        buff.append("]");
        renderJSON(StringUtils.replace(buff.toString(), "},]", "}]"));
    }


    /**
     * SKU的销售曲线
     *
     * @param type
     * @param sku
     */
    public static void linechart(String type, String sku) {

        if(Validation.hasErrors())
            renderJSON(new Ret(false));
        String json = "";

        if(type.equals("skusalefee")) {
            /**
             * 销售额曲线
             */
            json = J.json(SkuESQuery
                    .esSaleLine(type, sku, "fee"));

        } else if(type.equals("skusaleqty")) {
            /**
             * 销量曲线
             */
            json = J.json(SkuESQuery
                    .esSaleLine(type, sku, "qty"));
        } else if(type.equals("skuprofit")) {
            /**
             * 利润曲线
             */
            json = J.json(SkuESQuery
                    .esSaleLine(type, sku, "profit"));
        } else if(type.equals("skuprocureprice")) {
            /**
             * 采购价格曲线
             */
            json = J.json(SkuESQuery
                    .esProcureLine(type, sku, "price"));
        } else if(type.equals("skuprocureqty")) {
            /**
             * 采购数量曲线
             */
            json = J.json(SkuESQuery
                    .esProcureLine(type, sku, "qty"));
        }
        renderJSON(json);
    }

    /**
     * 保存 product 附加属性
     */
    public static void saveAttrs(List<ProductAttr> productAttrs) {
        try {
            for(ProductAttr productAttr : productAttrs) {
                if(productAttr != null) {
                    if(productAttr.id != null && productAttr.id != 0) {
                        productAttr.update();
                    } else {
                        productAttr.save();
                    }
                }
            }
            renderJSON(new Ret(true, ""));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 加载 产品的 附加属性给 View
     */
    public static void attrs(String sku, Long templateId) {
        Product pro = Product.findById(sku);
        Template template = Template.findById(templateId);
        List<ProductAttr> atts = pro.productAttrs;
        for(Attribute attribute : template.attributes) {
            if(!atts.contains(attribute)) {
                ProductAttr productAttr = new ProductAttr();
                productAttr.product = pro;
                productAttr.attribute = attribute;
                productAttr.save();
                atts.add(productAttr);
            }
        }
        Collections.sort(atts);
        render(pro);
    }

    /**
     * 删除附加属性
     *
     * @param attrId
     */
    public static void delAttr(String sku, Long attrId) {
        try {
            ProductAttr productAttr = ProductAttr.findById(attrId);
            productAttr.delete();
            renderJSON(new Ret(true, productAttr.attribute.name));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 产品删除
     */
    @Check("products.destroy")
    public static void destroy(String id, String reason) {
        Product pro = Product.findById(id);
        pro.safeDelete(reason);
        if(Validation.hasErrors()) {
            ProductPost p = new ProductPost();
            List<Product> prods = p.query();
            render("Products/index.html", p, prods);
        }
        flash.success("Product %s 删除成功", id);
        index(null);
    }

    /**
     * 更新 Product 在系统内的状态
     *
     * @param sku
     * @param state
     */
    public static void updateState(String sku, Product.S state) {
        Product product = Product.findById(sku);
        product.state = state;
        product.save();
        renderJSON(new Ret());
    }

    /**
     * 更新 Product 的销售等级
     *
     * @param sku
     * @param salesLevel
     */
    public static void updateSalesLevel(String sku, Product.E salesLevel) {
        Product product = Product.findById(sku);
        product.salesLevel = salesLevel;
        product.save();
        renderJSON(new Ret());
    }
}
