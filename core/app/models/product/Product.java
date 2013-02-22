package models.product;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import helper.*;
import models.ElcukRecord;
import models.embedded.AmazonProps;
import models.market.Listing;
import models.market.M;
import models.market.PriceStrategy;
import models.market.Selling;
import models.procure.Cooperator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:55 AM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Product extends GenericModel implements ElcukRecord.Log {
    /**
     * 此产品所能够符合的上架的货架, 不能够集联删除, 删除 Product 是一个很严重的事情!
     * 需要检测 Product 相关的数据
     */
    @OneToMany(mappedBy = "product",
            cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
                    CascadeType.REFRESH}, fetch = FetchType.LAZY)
    public List<Listing> listings = new ArrayList<Listing>();

    @ManyToOne
    public Category category;

    @ManyToOne
    public Family family;


    /**
     * 唯一的标示
     */
    @Id
    @Expose
    public String sku;

    @Required
    @Lob
    @Expose
    public String productName;

    /**
     * 长度, 单位 mm
     */
    @Expose
    public Float lengths = 0f;

    /**
     * 高度, 单位 mm
     */
    @Expose
    public Float heigh = 0f;

    /**
     * 长度, 单位 mm
     */
    @Expose
    public Float width = 0f;

    /**
     * 重量, 单位 kg
     */
    @Expose
    public Float weight = 0f;

    /**
     * 申报价格 (USD)
     */
    public Float declaredValue = 0f;

    public Product() {
    }

    public Product(String sku) {
        this.sku = sku.toUpperCase();
    }

    public void setSku(String sku) {
        this.sku = sku.toUpperCase();
    }

    /**
     * 删除 Product 前需要检查与 Product 有直接关系的各种对象.
     */
    @PreRemove
    public void checkDelete() {
        if(this.listings != null && this.listings.size() > 0) {
            throw new FastRuntimeException(
                    "Product [" + this.sku + "] have relate Listing, cannot be delete.");
        }
    }

    /**
     * 创建一个全新的 Product
     */
    public void createProduct() {
        /**
         * 1. 检查 SKU 是否合法
         * 2. 检查废弃的 SKU
         * 3. Family 不能为空!
         * 4. 检查 SKU 前缀是否与 Family 一致
         * 5. Category 不能为空
         * 6. 产品的名称不能为空
         */
        if(StringUtils.isBlank(this.sku)) {
            Validation.addError("", "Sku 必须存在!");
            return;
        }
        if(!Product.validSKU(this.sku))
            Validation.addError("", "SKU[ " + this.sku + " ] 不合法!");
        if(Product.unUsedSKU(this.sku))
            Validation.addError("", "SKU[ " + this.sku + " ] 为废弃 SKU, 不能使用!");
        if(this.family == null)
            Validation.addError("", "Family 不存在,请先添加后再创建 Product!");
        if(this.family != null && !StringUtils.startsWith(this.sku, this.family.family))
            Validation.addError("",
                    "Family(" + this.family.family + ") 与 SKU(" + this.sku + ") 不匹配!");
        if(Validation.hasErrors()) return;


        this.category = this.family.category;
        if(this.category == null)
            Validation.addError("", "Category 不存在, 请创添加后再创建 Product!");
        if(Validation.hasErrors()) return;

        this.save();
    }

    /**
     * 删除 Product 的时候需要做一些判断后, 才能够删除.
     */
    public void removeProduct() {
        checkDelete();
        this.delete();
    }

    /**
     * 如果指定 Market, 并且正确, 那么则从此 SKU 下属的 Listing 中过滤出对应市场的 Listing, 否则与 this.listings 相同
     *
     * @param market
     * @return
     */
    public List<Listing> listings(String market) {
        M m = M.val(market);
        if(m == null) {
            return this.listings;
        } else {
            return Listing.find("product.sku=? AND market=?", this.sku, M.val(market)).fetch();
        }
    }


    /**
     * <pre>
     * 此 Listing 进行上架
     * - type=FBA
     * - priceStrategy
     * - state=NEW (形成一个占位符, 具体的开始销售在 Selling 对象自身完成)
     * </pre>
     *
     * @param selling
     * @return
     */
    public Selling saleAmazon(Selling selling) {
        /**
         * 0. 属性的逻辑性检查
         *  - TODO UPC 相同的 Selling 需要对 MSKU 进行一致性检查
         *  - merchantSKU 修正为全大写
         *  - merchantSKU 的格式为 [sku,upc] 如果不一样, 则抛异常
         * 1. 将 Listing 相关信息同步到 Selling 上
         * 2. 检查 UPC 的值, 这个值需要在这检查一下已经使用过的 UPC 与还没有使用的 UPC.
         * 3. 开始上架
         */
        selling.merchantSKU = selling.merchantSKU.toUpperCase();
        try {
            if(!StringUtils.equals(StringUtils.split(selling.merchantSKU, ",")[1], selling.aps.upc))
                throw new FastRuntimeException("MerchantSKU 的格式不正确! 格式为: [sku],[upc],[other]");
        } catch(Exception e) {
            throw new FastRuntimeException(
                    String.format("(%s) 解析 MerchantSKU 的格式错误!", selling.merchantSKU));
        }

        selling.price = selling.aps.salePrice;

        selling.type = Selling.T.FBA;
        selling.priceStrategy = new PriceStrategy(selling);
        selling.state = Selling.S.SELLING;

        selling.aps.arryParamSetUP(AmazonProps.T.ARRAY_TO_STR);

        synchronized(selling.account.cookieStore()) {
            selling.account.changeRegion(selling.market);
            /**
             * !. 前提,在确定了账户的 Region 的情况下!
             * 1. 访问 https://catalog-sc.amazon.co.uk/abis/Classify/SelectCategory 的 classify 页面, 有一个隐藏 token
             * 2. 拿着隐藏 Token 访问 https://catalog-sc.amazon.co.uk/abis/Classify/SelectCategory 进入 identify 页面
             * 3. 访问 Match.ajax 查看是否拥有 matchingAsinList?, 设置 matchASIN
             * 4. 填充参数
             * <pre>
             * 		<div id="matchingAsinList" class="hide">
             * <div>QjAwOEFSN1g2WQ==</div>
             * </div>
             * </pre>
             * 5. 提交创建 Selling 的参数
             */

            // --------------   1   -------------------
            String body = HTTP
                    .get(selling.account.cookieStore(), selling.account.type.saleSellingLink()/*从账户所在的 Market 提交*/);
            if(Play.mode.isDev())
                FLog.fileLog(
                        String.format("%s.%s.step1.html", selling.merchantSKU, selling.account.id),
                        body, FLog.T.SALES);

            Document doc = Jsoup.parse(body);
            Elements inputs = doc.select("form[name=selectProductTypeForm] input");
            Set<NameValuePair> classifyHiddenParams = new HashSet<NameValuePair>();
            for(Element input : inputs) {
                String name = input.attr("name");
                if(StringUtils.isBlank(category.settings.choseAmazonCategory(selling.market)))
                    throw new FastRuntimeException(
                            String.format("Category %s 没有设定市场 %s 对应的值", category.categoryId,
                                    selling.market));
                if("newCategory".equals(name))
                    classifyHiddenParams.add(new BasicNameValuePair(name,
                            category.settings.choseAmazonCategory(selling.market)));
                else
                    classifyHiddenParams.add(new BasicNameValuePair(name, input.val()));
            }

            //  ------------------ 2 -----------------
            body = HTTP.post(selling.account.cookieStore(), selling.account.type.saleSellingLink()/*从账户所在的 Market 提交*/,
                    classifyHiddenParams);
            if(Play.mode.isDev())
                FLog.fileLog(
                        String.format("%s.%s.step2.html", selling.merchantSKU, selling.account.id),
                        body, FLog.T.SALES);
            doc = Jsoup.parse(body);

            Set<NameValuePair> addSellingPrams = new HashSet<NameValuePair>();
            inputs = doc.select("form[name=productForm] input");
            if(inputs == null || inputs.size() <= 7)
                throw new FastRuntimeException("没有进入第二步 Identify 页面!");
            /**
             * 每一个类别下提交的参数都不一样, 但通过 JS console 测试 us/de 两个市场,
             * 如果 UPC 是已经使用过的, 则只需要提交两个参数
             * > encoded_session_hidden_map:22222....
             * > external_id:660444833512
             * 如果是全新的, 那么除了上面的参数还需要两个必须
             * > item_name:SANER® 1900mAh rechargeabl...
             * > manufacturer:EasyAcc
             * 但不同的 Category 会拥有一些其他的必填写参数(与页面上的红色星号参数不一样), 这个需要特殊处理.
             *
             * us 默认提交的参数:
             * encoded_session_hidden_map:skxjkxj
             * sessionMapPresent:true
             * our_price-uom:USD
             * discounted_price-uom:USD
             * item_name:123123
             * manufacturer:123123
             * external_id:615162124756
             * list_price-uom:USD
             */
            // ------------------ 3 -----------------------
            String ajaxBody = HTTP
                    .post(selling.account.cookieStore(), selling.account.type.matchAsinAjaxLink(),
                            Arrays.asList(
                                    new BasicNameValuePair("sessionMapPresent", "true"),
                                    new BasicNameValuePair("our_price-uom",
                                            doc.select("input[name=our_price-uom]").val()),
                                    new BasicNameValuePair("discounted_price-uom",
                                            doc.select("input[name=discounted_price-uom]").val()),
                                    // 必须
                                    new BasicNameValuePair("encoded_session_hidden_map",
                                            doc.select("input[name=encoded_session_hidden_map]")
                                                    .val()),
                                    new BasicNameValuePair("manufacturer",
                                            selling.aps.manufacturer),
                                    new BasicNameValuePair("part_number",
                                            selling.aps.manufacturerPartNumber),
                                    new BasicNameValuePair("item_name", selling.aps.title),
                                    new BasicNameValuePair("external_id", selling.aps.upc)
                            ));

            /**
             * 对有已经上架的 Listing 做关联选择.
             */
            Document ajaxDoc = Jsoup.parse(ajaxBody);
            if(ajaxDoc.select("#newAsin").first() != null) {
                selling.aps.matchAsin = "";
            } else if(ajaxDoc.select("#errorsFound").first() != null) {
                // 发生错误, 例如指定类别的必须参数没有提交
                throw new FastRuntimeException(ajaxDoc.select("#errorsFound").html());
            } else {
                Element matchAsinEl = ajaxDoc.select("#matchingAsinList").first();
                if(matchAsinEl != null)
                    selling.aps.matchAsin = matchAsinEl.select("div:eq(0)").text();
                else
                    selling.aps.matchAsin = "";
            }


            //  ---------------- 4 -----------------------
            for(Element input : inputs) {
                String name = input.attr("name");
                String tagType = input.attr("type");
                if("radio".equals(tagType)) { //对于 radio 的只能选择 checked 的,不能让后面的元素把前面的值给覆盖了.
                    if(StringUtils.isBlank(input.attr("checked"))) continue;
                    addSellingPrams.add(new BasicNameValuePair(name, input.val()));
                } else if("checkbox".equals(tagType)) {
                    /**
                     * Amazon  中 checkbox 都是不需要提交的. 都有一个 hidden 的元素与其对应
                     * - offering_can_be_gift_wrapped
                     * - offering_can_be_gift_messaged
                     * - connector_gender(female/female)
                     * - connector_gender(male/male)
                     * - connector_gender(male/female)
                     * - hot_shoe_included
                     * - traffic_features_description(Traffic only)
                     * - traffic_features_description(Live HD traffic)
                     * - traffic_features_description(Live traffic)
                     * - are_batteries_included
                     * - is_discontinued_by_manufacturer
                     * 有以上这么些元素收到影响
                     */

                } else { // 非 Radio 的 input 按照如下处理
                    if("item_name".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.aps.title));
                    else if("manufacturer".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.aps.manufacturer));
                    else if("brand_name".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, StringUtils.isBlank(
                                selling.aps.brand) ? "EasyAcc" : selling.aps.brand)); // ?? 这个品牌的名字现在都使用我们自己的?
                    else if("part_number".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name,
                                selling.aps.manufacturerPartNumber));
                    else if("model".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.aps.modelNumber));
                    else if("external_id".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.aps.upc));
                    else if("offering_sku".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.merchantSKU));
                    else if("our_price".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name,
                                Webs.priceLocalNumberFormat(M.AMAZON_UK,
                                        selling.aps.standerPrice)));
                    else if("discounted_price".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name,
                                Webs.priceLocalNumberFormat(M.AMAZON_UK, selling.aps.salePrice)));
                    else if("discounted_price_start_date".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name,
                                Dates.listingUpdateFmt(selling.market, selling.aps.startDate)));
                    else if("discounted_price_end_date".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name,
                                Dates.listingUpdateFmt(selling.market, selling.aps.endDate)));
                    else if("Offer_Inventory_Quantity".equals(name))
                        addSellingPrams
                                .add(new BasicNameValuePair(name, selling.aps.quantity + ""));
                    else if("activeClientTimeOnTask".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name,
                                "166279")); // 这个值是通过 JS 计算的, 而 JS 仅仅是计算一个时间, 算法无关
                    else if("matchAsin".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.aps.matchAsin));
                    else if("encoded_session_hidden_map".equals(name)) {
                        addSellingPrams.add(new BasicNameValuePair(name, input.val()));
                        // 在发现了 encoded_session_hidden_map 以后需要添加这样一个属性(JS 动态添加的)
                        addSellingPrams.add(new BasicNameValuePair("sessionMapPresent", "true"));
                    } else if(StringUtils.startsWith(name, "bullet_point")) {
                        selling.aps.bulletPointsCheck(addSellingPrams);
                    } else if(StringUtils.startsWith(name, "generic_keywords")) {
                        selling.aps.searchTermsCheck(addSellingPrams);
                    } else if(StringUtils.startsWith(name, "recommended_browse_nodes")) {
                        if(selling.aps.rbns != null) {
                            if(selling.aps.rbns.size() == 1)
                                addSellingPrams
                                        .add(new BasicNameValuePair("recommended_browse_nodes[0]",
                                                selling.aps.rbns.get(0)));
                            else if(selling.aps.rbns.size() == 2) {
                                addSellingPrams
                                        .add(new BasicNameValuePair("recommended_browse_nodes[0]",
                                                selling.aps.rbns.get(0)));
                                addSellingPrams
                                        .add(new BasicNameValuePair("recommended_browse_nodes[1]",
                                                selling.aps.rbns.get(1)));
                            }
                        }
                    } else {
                        addSellingPrams.add(new BasicNameValuePair(name, input.val()));
                    }
                }
            }

            Elements textAreas = doc.select("form[name=productForm] textarea");
            /**
             * Product Description
             * Condition Note -- 有需要再补充
             * Seller Warranty Description -- 有需要再补充
             */
            for(Element textarea : textAreas) {
                String name = textarea.attr("name");
                if("product_description".equals(name))
                    addSellingPrams.add(new BasicNameValuePair(name, selling.aps.productDesc));
                else
                    addSellingPrams.add(new BasicNameValuePair(name, textarea.val()));
            }

            Elements selects = doc.select("form[name=productForm] select");
            for(Element select : selects) {
                String name = select.attr("name");
                // Condition
                if("offering_condition".equals(name))
                    addSellingPrams
                            .add(new BasicNameValuePair(name, "New|New")); // 商品的 Condition 设置为 NEW
                else
                    addSellingPrams.add(new BasicNameValuePair(name,
                            select.select("option[selected]").val()));
            }
            // -------------  5 -----------------
            /**
             * 上架时候的错误信息全部返回给前台.
             */
            body = HTTP
                    .post(selling.account.cookieStore(), selling.account.type.saleSellingPostLink()/*从账户所在的 Market 提交*/,
                            addSellingPrams);
            // 记录任何上架操作都记录日志. 大不了自己删除...
            FLog.fileLog(
                    String.format("%s.%s.%s.step3.html", selling.merchantSKU, selling.account.id,
                            System.currentTimeMillis()), body, FLog.T.SALES);

            doc = Jsoup.parse(body);
            // 检查是否提交成功了, 没有成功则抛出异常.
            if(doc.select("#productHeaderForTabs").first() != null &&
                    doc.select(".messageboxerror").first() != null)
                throw new FastRuntimeException(doc.select(".messageboxerror").first().text());

            // 最后获取成功成见 Listing 以后的 ASIN
            Element form = doc.select("form").first();
            if(form == null) throw new FastRuntimeException(
                    String.format("提交的参数错误.(详细错误信息咨询 IT 查看 E_LOG/listing_sale/%s.%s.step3.html)",
                            selling.merchantSKU, selling.account.id));

            List<NameValuePair> fetchNewAsinParam = new ArrayList<NameValuePair>();
            // 用于最后一个 inventory-status/status.html 页面跳转
            List<NameValuePair> finalFormParam = new ArrayList<NameValuePair>();
            for(Element hidden : form.select("input")) {
                String name = hidden.attr("name");
                finalFormParam.add(new BasicNameValuePair(name, hidden.val()));
                if("newItemAsin".equals(name)) selling.asin = hidden.val();
                else if("itemCreateWtqRequestId".equalsIgnoreCase(name))
                    fetchNewAsinParam.add(new BasicNameValuePair(name, hidden.val()));
                else if("newItemSku".equalsIgnoreCase(name)) {
                    fetchNewAsinParam.add(new BasicNameValuePair(name, selling.merchantSKU));
                    fetchNewAsinParam.add(new BasicNameValuePair("sku", selling.merchantSKU));
                }
            }

            // inventory-status/status.html 页面的访问, 会自行进行 302 转向访问
            body = HTTP.post(selling.account.cookieStore(), form.attr("action"), finalFormParam);
            FLog.fileLog(
                    String.format("%s.%s.%s.step4.html", selling.merchantSKU, selling.account.id,
                            System.currentTimeMillis()), body, FLog.T.SALES);

            // asin 最后没有解析出来, 并且 matchAsin 为空, 表示为全新的 upc 创建 Listing 需要额外的一步骤
            if(StringUtils.isBlank(selling.asin) && StringUtils.isBlank(selling.aps.matchAsin)) {
                try {
                    selling.asin = getNewAsin(selling, fetchNewAsinParam, 0);
                } catch(InterruptedException e) {
                    Logger.warn(Webs.E(e));
                    throw new FastRuntimeException(e.getMessage());
                }
            }
            if(StringUtils.isBlank(selling.asin)) { // 最后的 asin 检查
                String msg = doc.select(".messageboxerror").first().text();
                if(StringUtils.isBlank(msg)) msg = "未知原因模拟手动创建 Selling 失败, 请 IT 仔细查找问题!";
                throw new FastRuntimeException(msg);
            }
        }

        selling.listing = Listing.findById(Listing.lid(selling.asin, selling.market));
        if(selling.listing == null) selling.listing = new Listing(selling, this).save();

        selling.sid();
        return selling.save();
    }

    /**
     * 远程访问 New Asin
     *
     * @param selling
     * @param fetchNewAsinParam
     * @param times             循环的次数, 依次传递下去
     * @return
     */
    private String getNewAsin(Selling selling, List<NameValuePair> fetchNewAsinParam, int times)
            throws InterruptedException {
        String jsonStr = HTTP
                .post(selling.account.cookieStore(), selling.account.type.productCreateStatusLink(),
                        fetchNewAsinParam);
        Logger.info("Fetch ProductCreateStatus [%s] with [%s]",
                selling.account.type.productCreateStatusLink(), fetchNewAsinParam);
        JsonElement jsonEl = new JsonParser().parse(jsonStr);
        JsonObject jsonObj = jsonEl.getAsJsonObject();
        if("SUCCEEDED".equalsIgnoreCase(jsonObj.get("status").getAsString()))
            return jsonObj.get("asin").getAsString();
        else if("PENDING".equalsIgnoreCase(jsonObj.get("status").getAsString())) {
            FLog.fileLog(String.format("%s.%s.times_%s.js", selling.merchantSKU, selling.account.id,
                    times), jsonStr, FLog.T.SALES);
            if(times > 30) // 尝试 30 次吧... 12 次还是会有 PENDING 的问题
                throw new FastRuntimeException(
                        "使用全新 UPC 创建最后一部获取 ASIN 还在 PENDING 状态, 需要使用 AmazonSellingSyncJob 进行异步获取 ASIN.");
            Thread.sleep(2500);
            return getNewAsin(selling, fetchNewAsinParam, ++times);
        } else {
            FLog.fileLog(String.format("%s.%s.js", selling.merchantSKU, selling.account.id),
                    jsonStr, FLog.T.SALES);
            throw new FastRuntimeException(
                    "使用全新 UPC 创建 Selling 在最后获取 ASIN 的时候失败, 请联系 IT 仔细查找问题原因.");
        }
    }

    /**
     * 获取拥有这个 SKU 的所有供应商
     *
     * @return
     */
    public List<Cooperator> cooperators() {
        return Cooperator
                .find("SELECT c FROM Cooperator c, IN(c.cooperItems) ci WHERE ci.sku=? ORDER BY ci.id",
                        this.sku).fetch();
    }

    /**
     * 此产品拥有的图片的数量
     *
     * @return
     */
    public long pictureCount() {
        return Attach.count("fid=?", this.sku);
    }

    /**
     * 此 Product 关联的 Selling 的数量
     *
     * @return
     */
    public List<Selling> sellingCount() {
        return sellingCountWithMarket(null);
    }

    public List<Selling> sellingCountWithMarket(M market) {
        if(market == null)
            return Selling.find("sellingId LIKE ?", this.sku + "%").fetch();
        else
            return Selling.find("market=? AND sellingId LIKE ?", market, this.sku + "%").fetch();
    }

    @Override
    public String to_log() {
        return String.format("[长:%s mm] [宽:%s mm] [高:%s mm] [重量:%s kg] [申报价格:$ %s] [产品名称:%s]",
                this.lengths, this.width, this.heigh, this.weight, this.declaredValue,
                this.productName);
    }

    /**
     * 验证这个 SKU 是否合法
     *
     * @param sku
     * @return
     */
    public static boolean validSKU(String sku) {
        //71SNS1-B2PE, 71-SNS1-B2PE
        if(sku == null || sku.trim().isEmpty()) return false;
        String[] parts = sku.split("-");
        String part0 = parts[0].substring(0, 2);
        if(parts.length == 2) { //做一次兼容
            parts = new String[]{part0, parts[0].substring(2), parts[1]};
        }
        if(parts.length != 3) return false;
        if(!Patterns.Nub.matcher(part0).matches()) return false;
        return true;
    }

    /**
     * 根据 Amazon 输入的 MerchantSKU 来查找 Product, 会自动将 MerchantSKU 转换成系统内使用的 SKU;
     * 这里面拥有两个兼容性判断.
     *
     * @param msku
     * @return
     */
    public static Product findByMerchantSKU(String msku) {
        return Product.findById(merchantSKUtoSKU(msku));
    }

    /**
     * 将 Amazon 上的 MerchantSKU 转换成 SKU, 并且对 Amazon 上几个错误的 MerchantSKU 进行处理
     *
     * @param merchantSKU
     * @return
     */
    public static String merchantSKUtoSKU(String merchantSKU) {
        Validate.notNull(merchantSKU);
        // ------ fix -----------
//                if("609132508189".equals(t_msku)) t_msku = "71-HPTOUCH-B2PG"; //对历史错误数据的修复 @_@
//                if("8Z-0JR3-1BHG".equals(t_msku.toUpperCase())) t_msku = "80-QW1A56-BE"; // Power Bank 的销售还是需要囊括进来的
        String sku = StringUtils.split(merchantSKU, ",")[0].toUpperCase();
        if("609132508189".equals(sku)) sku = "71-HPTOUCH-B2PG";
        else if("8Z-0JR3-1BHG".equals(sku)) sku = "80-QW1A56-BE";
        return sku;
    }


    /**
     * 这几个为从 Amazon 解析回来的 SKU 存在, 但不需要在系统中再出现的 SKU, 为 Amazon 与系统中的同步做过滤
     */
    private static final Map<String, Integer> UN_USE_SKU = new HashMap<String, Integer>();

    static {
        UN_USE_SKU.put("15HTCG14-MB2SP", 1);
        UN_USE_SKU.put("15HTCG14-MS2SP", 1);
        UN_USE_SKU.put("15SS5ACE-LUB2SP", 1);
        UN_USE_SKU.put("15SSI9100-LUB2SP", 1);
        UN_USE_SKU.put("4N-GGOQ-2H0M", 1);
        UN_USE_SKU.put("50-TPLED-2B21BG", 1);
        UN_USE_SKU.put("67-STRASSABS-80W29SG", 1);
        UN_USE_SKU.put("70-SMP1000-BTKBBG", 1);
        UN_USE_SKU.put("71-APSL13-BG", 1);
        UN_USE_SKU.put("71-APSL15-BG", 1);
        UN_USE_SKU.put("71SMGT101-BPU", 1);
        UN_USE_SKU.put("71-SAMGT101-BPU", 1);
    }

    /**
     * 判断是否属于不再用来与 Amazon 同步的 SKU
     *
     * @param merchantSKU Amazon 上的使用的 MerchantSKU
     * @return
     */
    public static boolean unUsedSKU(String merchantSKU) {
        if(StringUtils.isBlank(merchantSKU)) return false;
        return UN_USE_SKU.containsKey(Product.merchantSKUtoSKU(merchantSKU));
    }

    /**
     * 返回所有的 SKU
     *
     * @param clearCache 是否清除缓存
     * @return
     */
    @SuppressWarnings("unchecked")
    @Cached("lifetime")
    public static List<String> skus(boolean clearCache) {
        List<String> skus = null;
        if(!clearCache) {
            skus = Cache.get(Caches.SKUS, List.class);
            if(skus != null) return skus;
        }

        List<Product> prods = Product.all().fetch();
        skus = new ArrayList<String>();
        for(Product prod : prods) skus.add(prod.sku);
        Cache.delete(Caches.SKUS);
        Cache.add(Caches.SKUS, skus, null);
        return Cache.get(Caches.SKUS, List.class);
    }

    @SuppressWarnings("unchecked")
    /**
     * 导出 SKU 销量的 CSV 文件
     */
    public static String skuSales(Date from, Date to) {
        List<String> skus = Product.skus(false);
        StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter);

        // 时间 -> [_.sku, _.销量]. 需要有序
        Map<Long, List<F.T2<String, Float>>> deSales = new LinkedHashMap<Long, List<F.T2<String, Float>>>();
        Map<Long, List<F.T2<String, Float>>> ukSales = new LinkedHashMap<Long, List<F.T2<String, Float>>>();
        Map<Long, List<F.T2<String, Float>>> allSales = new LinkedHashMap<Long, List<F.T2<String, Float>>>();
        List<String> csvHeader = new ArrayList<String>();
        csvHeader.add("Time DE");

        /*
        TODO 需要修复
        for(String sku : skus) {
            csvHeader.add(sku);
            HighChart oneSku = OrderItem.ajaxHighChartUnitOrder(sku, null, "sku", from, to);
            HighChart.Line oneSkuTimeSalesDe = oneSku.line("unit_de");
            HighChart.Line oneSkuTimeSalesUk = oneSku.line("unit_uk");
            HighChart.Line oneSkuTimeSalesAll = oneSku.line("unit_all");
            for(F.T2<Long, Float> oneSkuSale : oneSkuTimeSalesDe) {
                if(deSales.containsKey(oneSkuSale._1))
                    deSales.get(oneSkuSale._1).add(new F.T2<String, Float>(sku, oneSkuSale._2));
                else
                    deSales.put(oneSkuSale._1, new ArrayList<F.T2<String, Float>>(Arrays.asList(new F.T2<String, Float>(sku, oneSkuSale._2))));
            }

            for(F.T2<Long, Float> oneSkuSale : oneSkuTimeSalesUk) {
                if(ukSales.containsKey(oneSkuSale._1))
                    ukSales.get(oneSkuSale._1).add(new F.T2<String, Float>(sku, oneSkuSale._2));
                else
                    ukSales.put(oneSkuSale._1, new ArrayList<F.T2<String, Float>>(Arrays.asList(new F.T2<String, Float>(sku, oneSkuSale._2))));
            }

            for(F.T2<Long, Float> oneSkuSale : oneSkuTimeSalesAll) {
                if(allSales.containsKey(oneSkuSale._1))
                    allSales.get(oneSkuSale._1).add(new F.T2<String, Float>(sku, oneSkuSale._2));
                else
                    allSales.put(oneSkuSale._1, new ArrayList<F.T2<String, Float>>(Arrays.asList(new F.T2<String, Float>(sku, oneSkuSale._2))));
            }

        }
        */

        onMarketSkuSalesCsv(csvWriter, deSales, csvHeader, "DE");
        onMarketSkuSalesCsv(csvWriter, ukSales, csvHeader, "UK");
        onMarketSkuSalesCsv(csvWriter, allSales, csvHeader, "ALL");

        return stringWriter.toString();
    }

    /**
     * 将数据写到 CSV 文件中.
     */
    private static void onMarketSkuSalesCsv(CSVWriter csvWriter,
                                            Map<Long, List<F.T2<String, Float>>> allSkuSales,
                                            List<String> csvHeader, String market) {
        csvWriter.writeNext(new String[]{"--", "--", "--", "--", "--", "--"}); // 分割一栏
        csvHeader.set(0, "Time " + market);
        csvWriter.writeNext(csvHeader.toArray(new String[csvHeader.size()]));
        for(Long date : allSkuSales.keySet()) {
            List<F.T2<String, Float>> skusSales = allSkuSales.get(date);
            List<String> csvRow = new ArrayList<String>();
            csvRow.add(Dates.date2Date(new Date(date)));
            for(F.T2<String, Float> sku_sales : skusSales) {
                csvRow.add(sku_sales._2.toString());
            }
            csvWriter.writeNext(csvRow.toArray(new String[csvRow.size()]));
        }
    }

    public static boolean exist(String sku) {
        return Product.count("sku=?", sku) > 0;
    }
}
