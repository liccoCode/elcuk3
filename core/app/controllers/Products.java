package controllers;

import controllers.api.SystemOperation;
import helper.GTs;
import helper.J;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.market.Account;
import models.market.M;
import models.market.Selling;
import models.market.SellingQTY;
import models.procure.CooperItem;
import models.procure.Cooperator;
import models.product.*;
import models.view.Ret;
import models.view.post.ProductPost;
import net.sf.jxls.reader.ReaderBuilder;
import net.sf.jxls.reader.XLSReadStatus;
import net.sf.jxls.reader.XLSReader;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import play.data.Upload;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;
import play.utils.FastRuntimeException;
import query.SkuESQuery;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 产品模块的基本的类别的基本操作在此
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:57
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Products extends Controller {

    @Util
    private static String extarSku() {
        String sku = request.params.get("id");
        if(StringUtils.isBlank(sku)) sku = request.params.get("pro.sku");
        return sku;
    }

    @Before(only = {"index", "indexForShipment"})
    public static void setIndexLog() {
        User user = User.findById(Login.current().id);
        renderArgs.put("categoryIds", user.categories);
        renderArgs.put("records", ElcukRecord.fid("product.destroy").fetch(50));
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

    /**
     * 展示所有的 Product (物流模块用)
     */
    public static void indexForShipment(ProductPost p) {
        if(p == null) p = new ProductPost();
        List<Product> prods = p.query();
        render(prods, p);
    }

    @Check("products.whouseattrs")
    public static void whouseAttrs(ProductPost p) {
        if(p == null) p = new ProductPost();
        Product pro = p.pickup();
        render(pro, p);
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

    public static void showAttr(String sku) {
        Product pro = Product.findByMerchantSKU(sku);
        List<Template> templates = pro.category.templates;
        List<SellingQTY> qtys = SellingQTY.qtysAccodingSKU(pro);
        render(pro, qtys, templates);
    }

    public static void showAttr(String sku, String tab) {
        Product pro = Product.findByMerchantSKU(sku);
        List<Template> templates = pro.category.templates;
        List<SellingQTY> qtys = SellingQTY.qtysAccodingSKU(pro);
        render(pro, qtys, templates, tab);
    }

    public static void copy(String choseid, String skuid, String base, String extend, String attach) {
        Product pro = Product.copyProduct(choseid, skuid, base, extend, attach);
        render("Products/show.html", pro);
    }

    public static void backup(String choseid, String extend, String attach, String sku) {
        Product pro = Product.backupProduct(choseid, extend, attach, sku);
        render("Products/show.html", pro);
    }

    /**
     * @param pro
     */
    @Check("products.update")
    public static void update(Product pro) {
        try {
            if(!Product.exist(pro.sku)) Validation.addError("", String.format("Sku %s 不存在!", pro.sku));
            if(Validation.hasErrors()) {
                renderJSON(Webs.vJson(Validation.errors()));
            }
            Product dbpro = Product.dbProduct(pro.sku);
            pro.arryParamSetUP(Product.FLAG.ARRAY_TO_STR);
            pro.changePartNumber(dbpro.partNumber);
            pro.updateDate = new Date();
            pro.save();
            List<String> logs = dbpro.beforeDoneUpdate(pro);
            if(logs.size() > 0) {
                new ElcukRecord(Messages.get("product.update"),
                        Messages.get("action.base", StringUtils.join(logs, "<br>")),
                        pro.sku).save();
            }
            renderJSON(new Ret(true, ""));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
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
        List<String> products = Product.skus(true);
        renderArgs.put("products", J.json(products));
        renderArgs.put("categorys", Category.allcategorys());
    }

    public static void blank(Product pro) {
        if(pro == null) pro = new Product();
        List<Cooperator> cooperators = Cooperator.find("type=?", Cooperator.T.SUPPLIER).fetch();
        pro.beforeData();
        List<Category> cats = Category.all().fetch();
        render(pro, cats, cooperators);
    }

    public static void create(Product pro, Long cooperatorId, CooperItem copItem) {
        validation.valid(pro);
        pro.arryParamSetUP(Product.FLAG.ARRAY_TO_STR);
        Product entity = pro.createProduct();
        if(entity != null && cooperatorId != null) {
            Cooperator cooperator = Cooperator.findById(cooperatorId);
            cooperator.createItemByProduct(copItem, entity);
        }
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
                Messages.get("action.base", qty.toLog()), qty.product.sku).save();
        redirect("/Products/show/" + qty.product.sku);
    }


    public static void pRemove(Product p) {
        if(!p.isPersistent()) renderJSON(new Ret("产品不存在!"));
        p.removeProduct();
        renderJSON(new Ret());
    }

    /**
     * 检查 UPC 上架情况
     */
    public static void upcCheck(String upc) {
        try {
            List<Selling> upcSellings = Selling.find("aps.upc=?", upc).fetch();
            renderJSON(J.g(upcSellings));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
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
            renderJSON(J.g(product.sellingCountWithMarket(mkt)));
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
        renderJSON(new Ret(String.format("https://sellercentral.%s/hz/inventory/classify?ref=ag_pclasshm_cont_invfile&",
                M.valueOf(market).toString())));
    }

    /**
     * 获取拥有这个 SKU 的所有供应商
     */
    public static void cooperators(String sku) {
        List<Cooperator> cooperatorList = Cooperator
                .find("SELECT c FROM Cooperator c, IN(c.cooperItems) ci WHERE ci.sku=? AND ci.status=? "
                        + " ORDER BY ci.id", sku, CooperItem.S.Agree).fetch();
        StringBuilder buff = new StringBuilder();
        buff.append("[");
        for(Cooperator co : cooperatorList) {
            buff.append("{").append("\"").append("id").append("\"").append(":").append("\"").append(co.id).append("\"")
                    .append(",").append("\"").append("name").append("\"").append(":").append("\"").append(co.name)
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
            String log = "";
            for(ProductAttr productAttr : productAttrs) {
                if(productAttr != null) {
                    if(productAttr.id != null && productAttr.id != 0) {
                        productAttr.update();
                        log = log + "更新属性:" + productAttr.value;
                    } else {
                        productAttr.save();
                        log = log + "新增属性:" + productAttr.value;
                    }
                    new ElcukRecord(Messages.get("product.update"), Messages.get("action.base", log),
                            productAttr.product.sku).save();
                }
            }
            renderJSON(new Ret(true, ""));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    public static void saveShipmentInfo(Product pro) {
        pro.save();
        flash.success("修改成功！");
        showAttr(pro.sku);
    }

    public static void removeAttr(String sku, Long id) {
        ProductAttr attr = ProductAttr.findById(id);
        attr.delete();
        flash.success("删除成功");
        showAttr(sku, "attr");
    }

    /**
     * 加载 产品的 附加属性给 View
     */
    public static void attrs(String sku, Long templateId) {
        Product pro = Product.findById(sku);
        Template template = Template.findById(templateId);
        List<ProductAttr> atts = pro.productAttrs;
        template.templateAttributes.stream().filter(templateAttribute -> !atts.contains(templateAttribute.attribute))
                .forEach(templateAttribute -> {
                    ProductAttr productAttr = new ProductAttr();
                    productAttr.product = pro;
                    productAttr.attribute = templateAttribute.attribute;
                    productAttr.save();
                    atts.add(productAttr);
                });
        Collections.sort(atts);
        render("Products/_attrs.html", pro);
    }


    /**
     * 同步sku物流属性
     */
    public static void syncSku(String sku, String skuSync) {
        Product pro = Product.findById(sku);
        List<ProductAttr> atts = pro.productAttrs;

        Product sync = Product.findById(skuSync);
        List<ProductAttr> syncAttr = sync.productAttrs;

        Map map = new HashMap();
        syncAttr.forEach(attr -> map.put(attr.attribute.id, attr));
        atts.forEach(attr -> map.put(attr.attribute.id, attr));

        Set entrySet = map.entrySet();
        Iterator it = entrySet.iterator();
        while(it.hasNext()) {
            Map.Entry me = (Map.Entry) it.next();
            ProductAttr productAttr = (ProductAttr) me.getValue();
            if(!sku.equals(productAttr.product.sku)) {
                ProductAttr newAttr = new ProductAttr();
                newAttr.product = pro;
                newAttr.attribute = productAttr.attribute;
                newAttr.value = productAttr.value;
                newAttr.save();
                atts.add(newAttr);
            }
        }
        Collections.sort(atts);
        pro.productAttrs = atts;
        pro.save();

        render("Products/_attrs.html", pro);
    }


    public static void addAttr(String sku, String attr) {
        Product product = Product.findById(sku);
        Attribute attribute = Attribute.find("name=?", attr).first();
        if(!Optional.ofNullable(attribute).isPresent()) {
            Attribute new_bute = new Attribute();
            new_bute.name = attr;
            new_bute.createDate = new Date();
            new_bute.type = Attribute.T.STRING;
            new_bute.sort = (int) Attribute.count() + 1;
            new_bute.createUser = Login.current();
            new_bute.save();
            attribute = new_bute;
        }
        ProductAttr new_attr = new ProductAttr();
        new_attr.attribute = attribute;
        new_attr.product = product;
        new_attr.save();
        showAttr(sku, "attr");
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

            String log = "删除属性:" + productAttr.value;
            new ElcukRecord(Messages.get("product.update"),
                    Messages.get("action.base", log),
                    productAttr.product.sku).save();

            renderJSON(new Ret(true, productAttr.attribute.name));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
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

    /**
     * 模糊匹配SKU
     *
     * @param sku
     */
    public static void sameSku(String sku) {
        List<Product> products = Product.find("sku like '" + sku + "%'").fetch();
        List<String> skus = products.stream().map(p -> p.sku).collect(Collectors.toList());
        renderJSON(J.json(skus));
    }

    public static void sameSkuAndCooper(String sku, Long cooperId) {
        List<Product> products = Product.find("sku like '" + sku + "%'").fetch();
        List<String> same_sku = products.stream().map(p -> p.sku).collect(Collectors.toList());
        Cooperator cooperator = Cooperator.findById(cooperId);
        List<String> existList = cooperator.cooperItems.stream()
                .filter(item -> Objects.equals(item.type, CooperItem.T.SKU))
                .map(itm -> itm.product.sku).collect(Collectors.toList());
        CollectionUtils.filter(same_sku, o -> {
            for(String exist_sku : existList) {
                if(exist_sku.equals(o.toString())) return true;
            }
            return true;
        });
        renderJSON(J.json(existList));
    }

    public static void findUPC(String sku) {
        Product pro = Product.findById(sku);
        renderJSON(J.json(GTs.MapBuilder.map("upc", pro.upc).put("upcJP", pro.upcJP).build()));
    }

    public static void findProductName(String sku, Long cooperId) {
        Product pro = Product.findById(sku);
        if(cooperId != null) {
            CooperItem item = CooperItem.find("cooperator.id = ? AND sku = ? ", cooperId, sku).first();
            if(item != null) {
                renderJSON(J.json(GTs.MapBuilder
                        .map("name", pro.abbreviation).put("price", item.price.toString())
                        .put("currency", item.currency.name()).put("period", item.period.toString())
                        .put("boxSize", item.boxSize.toString()).build()));
            }
        }
        renderJSON(J.json(GTs.MapBuilder.map("name", pro.abbreviation).build()));
    }

    /**
     * 输出给 typeahead 所使用的 source
     * <p>
     * 需要支持: SKU、Family、附加属性、Selling 的 Fnsku
     *
     * @param search
     */
    public static void source(String search) {
        renderJSON(J.json(Product.pickSourceItems(search)));
    }


    /**
     * 导入物流运输月度报表
     *
     * @param xlsx
     */
    public static void importProductReport(String xlsx) {
        try {
            /** 第一步上传excel文件 **/
            List<Upload> files = (List<Upload>) request.args.get("__UPLOADS");
            Upload upload = files.get(0);
            List<ProductJxl> productList = new ArrayList<>();

            /** 第二步根据定义好xml进行jxls映射 **/
            File directory = new File("");
            String courseFile = directory.getCanonicalPath();
            String xmlPath = courseFile + "/app/views/Products/productsReport.xml";
            InputStream inputXML = new BufferedInputStream(new FileInputStream(xmlPath));
            XLSReader mainReader = ReaderBuilder.buildFromXML(inputXML);
            InputStream inputXLS = new BufferedInputStream(new FileInputStream(upload.asFile()));
            Map<String, Object> beans = new HashMap<>();
            beans.put("productList", productList);

            XLSReadStatus readStatus = mainReader.read(inputXLS, beans);
            if(readStatus.isStatusOK()) {
                /** 第三步 数据插入Product **/
                if(productList != null && productList.size() > 0) {
                    for(int i = 1; i < productList.size(); i++) {
                        ProductJxl jxl = productList.get(i);
                        jxl.setProduct();
                    }
                }
                flash.success("上传成功！");
            } else {
                flash.error("上传失败!");
            }
            index(new ProductPost());
        } catch(Exception e) {
            Webs.e(e);
            flash.error(String.format("上传失败!原因:[%s]", e.toString()));
        }
    }
}
