package models.product;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import helper.*;
import models.ElcukRecord;
import models.embedded.AmazonProps;
import models.market.Listing;
import models.market.M;
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
import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.utils.FastRuntimeException;
import query.ProductQuery;

import javax.persistence.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:55 AM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Product extends GenericModel implements ElcukRecord.Log {
    public static final Pattern Nub = Pattern.compile("[0-9]*");
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
    @Required
    // 因为默认是 >=
    @Min(0.001)
    public Float lengths = 0f;

    /**
     * 高度, 单位 mm
     */
    @Expose
    @Required
    @Min(0.001)
    public Float heigh = 0f;

    /**
     * 宽度, 单位 mm
     */
    @Expose
    @Required
    @Min(0.001)
    public Float width = 0f;

    /**
     * 重量, 单位 kg
     */
    @Expose
    @Required
    @Min(0.001)
    public Float weight = 0f;

    /**
     * 申报价格 (USD)
     */
    public Float declaredValue = 0f;

    /**
     * 产品品名
     */
    @Required
    public String declareName;

    /**
     * 产品简称
     */
    @Required
    public String abbreviation;

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
         * 7. 长宽高一定需要填写
         * 8. 申报价不为空
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
        if(this.declaredValue == null)
            Validation.addError("", "申报价值必须填写");
        if(this.declareName == null)
            Validation.addError("", "产品品名必须填写");
        if(this.abbreviation == null)
            Validation.addError("", "产品简称必须填写");


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
         * 0. 业务属性检查
         * 1. 将 Listing 相关信息同步到 Selling 上
         * 2. 检查 UPC 的值, 这个值需要在这检查一下已经使用过的 UPC 与还没有使用的 UPC.
         * 3. 开始上架
         */
        if(StringUtils.isBlank(selling.merchantSKU))
            Validation.addError("", "Selling 的 msku 必须存在.");
        if(!selling.isMSkuValid())
            Validation.addError("", "MerchantSKU 格式错误, 请检查. SKU,UPC");
        if(selling.account == null || !selling.account.isPersistent())
            Validation.addError("", "请选择正确的 Account 账户");
        selling.aps.validate();
        if(Validation.hasErrors()) return null;

        selling.state = Selling.S.SELLING;
        selling.aps.arryParamSetUP(AmazonProps.T.ARRAY_TO_STR);

        /**
         * !. 前提,在确定了账户的 Region 的情况下!
         * 1. 访问选择 big Category 页面
         * 2. 根据 big Category 获取详细 Category JSON 数据
         * 3. 进入填写创建 Product 详细信息页面
         * 4. 访问 Match.ajax 查看是否拥有 matchingAsinList?, 设置 matchASIN
         * 5. 填充参数
         * <pre>
         * 		<div id="matchingAsinList" class="hide">
         * <div>QjAwOEFSN1g2WQ==</div>
         * </div>
         * </pre>
         * 6. 提交创建 Selling 的参数
         */
        synchronized(selling.account.cookieStore()) {
            selling.account.changeRegion(selling.market);
            Set<NameValuePair> classifyHiddenParams = saleAmazonStep1(selling);
            classifyHiddenParams.addAll(saleAmazonStep2(selling));
            Document saleSellingPage = saleAmazonStep3(selling, classifyHiddenParams);
            saleAmazonStep4(selling, saleSellingPage);
            Set<NameValuePair> sellingParams = saleAmazonStep5(selling, saleSellingPage);
            saleAmazonStep6(selling, sellingParams);
            selling.account.changeRegion(selling.account.type);
        }

        selling.listing = Listing.findById(Listing.lid(selling.asin, selling.market));
        if(selling.listing == null) selling.listing = new Listing(selling, this).save();

        selling.sid();
        return selling.save();
    }

    /**
     * 访问 https://catalog-sc.amazon.co.uk/abis/Classify/SelectCategory 的 classify 页面, 有一个隐藏 token
     *
     * @param selling
     * @return
     */
    public Set<NameValuePair> saleAmazonStep1(Selling selling) {
        String body = HTTP.get(
                selling.account.cookieStore(),
                selling.account.type.saleSellingLink()/*从账户所在的 Market 提交*/
        );
        if(Play.mode.isDev())
            FLog.fileLog(String.format("%s.%s.step1.html", selling.merchantSKU, selling.account.id), body, FLog.T.SALES);

        Document doc = Jsoup.parse(body);
        Elements inputs = doc.select("form[name=selectProductTypeForm] input");
        Set<NameValuePair> classifyHiddenParams = new HashSet<NameValuePair>();
        for(Element input : inputs) {
            String name = input.attr("name");
            if(!Arrays.asList("encoded_session_hidden_map", "activeClientTimeOnTask").contains(name))
                continue;
            classifyHiddenParams.add(new BasicNameValuePair(name, input.val()));
        }
        return classifyHiddenParams;
    }

    public Set<NameValuePair> saleAmazonStep2(Selling sell) {
        String amzBigCategory = category.settings.choseAmazonCategory(sell.market);
        if(StringUtils.isBlank(amzBigCategory))
            throw new FastRuntimeException(
                    String.format("Category %s 没有设定市场 %s 对应的值", category.categoryId, sell.market));
        Set<NameValuePair> params = new HashSet<NameValuePair>();
        params.add(new BasicNameValuePair("category", amzBigCategory.substring(0, amzBigCategory.lastIndexOf("/"))));
        String body = HTTP.post(sell.account.cookieStore(), sell.account.type.browseCategoryAmzn(), params);
        if(Play.mode.isDev())
            FLog.fileLog(String.format("%s.%s.step2.html", sell.merchantSKU, sell.account.id), body, FLog.T.SALES);

        params.clear();
        params.add(new BasicNameValuePair("category", ""));
        JsonElement json = new JsonParser().parse(body);
        for(JsonElement element : json.getAsJsonArray()) {
            JsonObject obj = element.getAsJsonObject();
            if(!amzBigCategory.equals(obj.get("id").getAsString())) continue;
            params.add(new BasicNameValuePair("newCategory", amzBigCategory));
            params.add(new BasicNameValuePair("productType", obj.get("productType").getAsString()));
            params.add(new BasicNameValuePair("displayPath",
                    String.format("All Product Categories/%s/%s",
                            obj.get("displayPath").getAsString(),
                            obj.get("displayName").getAsString()
                    )));
            params.add(new BasicNameValuePair("itemType", obj.get("itemType").getAsString()));
        }
        return params;
    }

    public Document saleAmazonStep3(Selling selling, Set<NameValuePair> classifyHiddenParams) {
        String body = HTTP.post(selling.account.cookieStore(),
                selling.account.type.saleSellingLink()/*从账户所在的 Market 提交*/,
                classifyHiddenParams);
        if(Play.mode.isDev())
            FLog.fileLog(String.format("%s.%s.step3.html", selling.merchantSKU, selling.account.id), body, FLog.T.SALES);
        Document doc = Jsoup.parse(body);
        Elements inputs = doc.select("form[name=productForm] input");
        if(inputs == null || inputs.size() <= 7)
            throw new FastRuntimeException("没有进入第二步 Identify 页面 (填写上架新产品详细内容的页面)!");
        return doc;
    }


    /**
     * 访问 Match.ajax 查看是否拥有 matchingAsinList?, 设置 matchASIN
     */
    public void saleAmazonStep4(Selling selling, Document saleSellingPage) {
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
        String ajaxBody = HTTP.post(
                selling.account.cookieStore(),
                selling.account.type.matchAsinAjaxLink(),
                Arrays.asList(
                        new BasicNameValuePair("sessionMapPresent", "true"),
                        new BasicNameValuePair("our_price-uom",
                                saleSellingPage.select("input[name=our_price-uom]").val()),
                        new BasicNameValuePair("discounted_price-uom",
                                saleSellingPage.select("input[name=discounted_price-uom]").val()),
                        // 必须
                        new BasicNameValuePair("encoded_session_hidden_map",
                                saleSellingPage.select("input[name=encoded_session_hidden_map]").val()),
                        new BasicNameValuePair("manufacturer", selling.aps.manufacturer),
                        new BasicNameValuePair("part_number", selling.aps.manufacturerPartNumber),
                        new BasicNameValuePair("item_name", selling.aps.title),
                        new BasicNameValuePair("external_id", selling.aps.upc)
                )
        );

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
    }

    /**
     * 填充参数; 向 saleSellingPage 中需要的字段根据 Selling 填充数据
     *
     * @param selling
     * @param saleSellingPage
     * @return
     */
    public Set<NameValuePair> saleAmazonStep5(Selling selling, Document saleSellingPage) {
        Map<String, String> uniqParams = new HashMap<String, String>();
        uniqParams.put("offering_sku", selling.merchantSKU);
        uniqParams.put("external_id", selling.aps.upc);
        uniqParams.put("Offer_Inventory_Quantity", "0");
        uniqParams.put("matchAsin", selling.aps.matchAsin);
        uniqParams.put("sessionMapPresent", "true");
        uniqParams.put("offering_condition", "New|New");

        F.T2<Collection<NameValuePair>, String> t2 = selling.aps
                .generateDeployProps(saleSellingPage.outerHtml(), selling);
        for(NameValuePair pair : t2._1) {
            if(uniqParams.containsKey(pair.getName())) continue;
            uniqParams.put(pair.getName(), pair.getValue());
        }

        Set<NameValuePair> sellingParams = new HashSet<NameValuePair>();
        for(Map.Entry<String, String> entry : uniqParams.entrySet()) {
            sellingParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            Logger.info("%s=%s", entry.getKey(), entry.getValue());
        }
        return sellingParams;
    }


    /**
     * 上架
     *
     * @param sell
     * @param sellingParams
     */
    public void saleAmazonStep6(Selling sell, Set<NameValuePair> sellingParams) {
        /**
         * 上架时候的错误信息全部返回给前台.
         */
        String body = HTTP.post(sell.account.cookieStore(),
                sell.account.type.saleSellingPostLink()/*从账户所在的 Market 提交*/,
                sellingParams);
        // 记录任何上架操作都记录日志. 大不了自己删除...
        FLog.fileLog(String.format("%s.%s.%s.step6.1.html", sell.merchantSKU, sell.account.id,
                System.currentTimeMillis()), body, FLog.T.SALES
        );

        Document afterPostPage = Jsoup.parse(body);
        // 检查是否提交成功了, 没有成功则抛出异常.
        if(afterPostPage.select("#productHeaderForTabs").first() != null &&
                afterPostPage.select(".messageboxerror").first() != null)
            throw new FastRuntimeException(
                    afterPostPage.select(".messageboxerror").first().text());

        // 最后获取成功创建 Listing 以后的 ASIN
        Element form = afterPostPage.select("form").first();
        if(form == null) throw new FastRuntimeException(
                String.format("提交的参数错误.(详细错误信息咨询 IT 查看 E_LOG/listing_sale/%s.%s.step3.html)",
                        sell.merchantSKU, sell.account.id));

        List<NameValuePair> fetchNewAsinParam = new ArrayList<NameValuePair>();
        // 用于最后一个 inventory-status/status.html 页面跳转
        List<NameValuePair> finalFormParam = new ArrayList<NameValuePair>();
        for(Element hidden : form.select("input")) {
            String name = hidden.attr("name");
            finalFormParam.add(new BasicNameValuePair(name, hidden.val()));
            if("newItemAsin".equals(name)) sell.asin = hidden.val();
            else if("itemCreateWtqRequestId".equalsIgnoreCase(name))
                fetchNewAsinParam.add(new BasicNameValuePair(name, hidden.val()));
            else if("newItemSku".equalsIgnoreCase(name)) {
                fetchNewAsinParam.add(new BasicNameValuePair(name, sell.merchantSKU));
                fetchNewAsinParam.add(new BasicNameValuePair("sku", sell.merchantSKU));
            }
        }

        // inventory-status/status.html 页面的访问, 会自行进行 302 转向访问
        body = HTTP.post(sell.account.cookieStore(), form.attr("action"), finalFormParam);
        FLog.fileLog(String.format("%s.%s.%s.step6.2.html", sell.merchantSKU, sell.account.id,
                System.currentTimeMillis()), body, FLog.T.SALES);

        // asin 最后没有解析出来, 并且 matchAsin 为空, 表示为全新的 upc 创建 Listing 需要额外的一步骤
        if(StringUtils.isBlank(sell.asin) && StringUtils.isBlank(sell.aps.matchAsin)) {
            try {
                sell.asin = getNewAsin(sell, fetchNewAsinParam, 0);
            } catch(InterruptedException e) {
                Logger.warn(Webs.E(e));
                throw new FastRuntimeException(e.getMessage());
            }
        }
        if(StringUtils.isBlank(sell.asin)) { // 最后的 asin 检查
            String msg = afterPostPage.select(".messageboxerror").first().text();
            if(StringUtils.isBlank(msg)) msg = "未知原因模拟手动创建 Selling 失败, 请 IT 仔细查找问题!";
            throw new FastRuntimeException(msg);
        }
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
            Logger.info("GetNewAsin: [%s]", jsonStr);
            if(times > 30) // 尝试 30 次吧... 12 次还是会有 PENDING 的问题
                throw new FastRuntimeException(
                        "使用全新 UPC 创建最后一部获取 ASIN 还在 PENDING 状态, 需要使用 AmazonSellingSyncJob 进行异步获取 ASIN.");
            Thread.sleep(2500);
            return getNewAsin(selling, fetchNewAsinParam, ++times);
        } else {
            FLog.fileLog(String.format("%s.%s.js", selling.merchantSKU, selling.account.id), jsonStr, FLog.T.SALES);
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
        if(!Product.Nub.matcher(part0).matches()) return false;
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
     * @param forceClearCache 是否清除缓存
     * @return
     */
    @SuppressWarnings("unchecked")
    @Cached("lifetime")
    public static List<String> skus(boolean forceClearCache) {
        List<String> skus = null;
        if(forceClearCache) {
            skus = new ProductQuery().skus();
            Cache.delete(Caches.SKUS);
            Cache.add(Caches.SKUS, skus, "10h");
        } else {
            skus = Cache.get(Caches.SKUS, List.class);
            if(skus != null) return skus;
            skus = new ProductQuery().skus();
            Cache.add(Caches.SKUS, skus, "10h");
        }

        return skus;
    }

    public static boolean exist(String sku) {
        return Product.count("sku=?", sku) > 0;
    }
}
