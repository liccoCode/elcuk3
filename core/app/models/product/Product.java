package models.product;

import com.google.gson.annotations.Expose;
import helper.*;
import models.market.Account;
import models.market.Listing;
import models.market.PriceStrategy;
import models.market.Selling;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Play;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:55 AM
 */
@Entity
public class Product extends GenericModel {
    /**
     * 此产品所能够符合的上架的货架, 不能够集联删除, 删除 Product 是一个很严重的事情!
     * 需要检测 Product 相关的数据
     */
    @OneToMany(mappedBy = "product", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    public List<Listing> listings;

    @ManyToOne
    public Category category;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<Product> relates;

    /**
     * 这个产品从 Category 继承 + 自身的所有的 attrNames 值;
     * <p/>
     * 如果某一时刻 Category 或者 Product 身上的 AttrName 被删除了, 这个记录的对应的 AttrName 的值
     * 不受影响.
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "product")
    public List<Attribute> attrs;

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

    @Expose
    public Float lengths;

    @Expose
    public Float heigh;

    @Expose
    public Float width;

    @Expose
    public Float weight;

    public Product() {
    }

    public Product(String sku) {
        this.sku = sku;
    }

    /**
     * 删除 Product 前需要检查与 Product 有直接关系的各种对象.
     */
    @PreRemove
    public void checkDelete() {
        if(this.listings != null && this.listings.size() > 0) {
            throw new FastRuntimeException("Product [" + this.sku + "] have relate Listing, cannot be delete.");
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
        if(!Product.validSKU(this.sku)) throw new FastRuntimeException("SKU(" + this.sku + ") 不合法!");
        if(Product.unUsedSKU(this.sku)) throw new FastRuntimeException("SKU(" + this.sku + ") 为废弃 SKU, 不能使用!");
        if(this.family == null || !this.family.isPersistent())
            throw new FastRuntimeException("Family 不存在,请先添加后再创建 Product!");
        if(!StringUtils.startsWith(this.sku, this.family.family))
            throw new FastRuntimeException("Family(" + this.family.family + ") 与 SKU(" + this.sku + ") 不匹配!");
        if(this.category == null || !this.category.isPersistent())
            throw new FastRuntimeException("Category 不存在, 请创添加后再创建 Product!");
        if(StringUtils.isBlank(this.productName))
            throw new FastRuntimeException("产品的名称不能为空!");

        if(this.attrs != null && this.attrs.size() > 0) {
            for(Attribute att : this.attrs) {
                att.id = String.format("%s_%s", this.sku, att.attName.name);
                att.product = this;
            }
        }
        this.save();
    }

    /**
     * 删除 Product 的时候需要做一些判断后, 才能够删除.
     */
    public void removeProduct() {
        this.delete();
    }

    /**
     * 如果指定 Market, 并且正确, 那么则从此 SKU 下属的 Listing 中过滤出对应市场的 Listing, 否则与 this.listings 相同
     *
     * @param market
     * @return
     */
    public List<Listing> listings(String market) {
        Account.M m = Account.M.val(market);
        if(m == null) {
            return this.listings;
        } else {
            return Listing.find("product.sku=? AND market=?", this.sku, Account.M.val(market)).fetch();
        }
    }


    /**
     * <pre>
     * 此 Listing 进行上架
     * TODO: 包含一些写死的 Selling 默认值
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
         *  - merchantSKU 的格式为 [sku,upc] 如果不一样, 则抛异常
         * 1. 将 Listing 相关信息同步到 Selling 上
         * 2. 检查 UPC 的值, 这个值需要在这检查一下已经使用过的 UPC 与还没有使用的 UPC.
         * 3. 开始上架
         */
        try {
            if(!StringUtils.equals(StringUtils.split(selling.merchantSKU, ",")[1], selling.aps.upc))
                throw new FastRuntimeException("MerchantSKU 的格式不正确! 格式为: [sku],[upc]");
        } catch(FastRuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new FastRuntimeException(String.format("(%s) 解析 MerchantSKU 的格式错误!", selling.merchantSKU));
        }

        selling.price = selling.aps.salePrice;

        selling.type = Selling.T.FBA;
        selling.priceStrategy = new PriceStrategy(selling);
        selling.state = Selling.S.NEW;

        selling.aps.arryParamSetUP(1);

        synchronized(selling.account.cookieStore()) {
            selling.account.changeRegion(selling.market);
            /**
             * !. 前提,在确定了账户的 Region 的情况下!
             * 1. 访问 https://catalog-sc.amazon.co.uk/abis/Classify/SelectCategory 的 classify 页面, 有一个隐藏 token
             * 2. 拿着隐藏 Token 访问 https://catalog-sc.amazon.co.uk/abis/Classify/SelectCategory 进入 identify 页面
             * 3. 提交创建 Selling 的参数
             */

            // --------------   1   -------------------
            String body = HTTP.get(selling.account.cookieStore(), selling.account.type.saleSellingLink()/*从账户所在的 Market 提交*/);
            if(Play.mode.isDev())
                Devs.fileLog(String.format("%s.%s.step1.html", selling.merchantSKU, selling.account.id), body, Devs.T.SALES);

            Document doc = Jsoup.parse(body);
            Elements inputs = doc.select("form[name=selectProductTypeForm] input");
            Set<NameValuePair> classifyHiddenParams = new HashSet<NameValuePair>();
            for(Element input : inputs) {
                String name = input.attr("name");
                if("newCategory".equals(name)) { //TODO 这里的类别先写死, 需要将这个类别与系统内的类别挂钩
                    classifyHiddenParams.add(new BasicNameValuePair(name, "consumer_electronics/consumer_electronics"));
                } else classifyHiddenParams.add(new BasicNameValuePair(name, input.val()));
            }

            body = HTTP.post(selling.account.cookieStore(), selling.account.type.saleSellingLink()/*从账户所在的 Market 提交*/, classifyHiddenParams);
            if(Play.mode.isDev())
                Devs.fileLog(String.format("%s.%s.step2.html", selling.merchantSKU, selling.account.id), body, Devs.T.SALES);
            doc = Jsoup.parse(body);

            //  ------------------ 2 -----------------
            Set<NameValuePair> addSellingPrams = new HashSet<NameValuePair>();
            inputs = doc.select("form[name=productForm] input");
            if(inputs == null || inputs.size() <= 7) throw new FastRuntimeException("没有进入第二步 Identify 页面!");
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
                        addSellingPrams.add(new BasicNameValuePair(name, "EasyAcc")); // ?? 这个品牌的名字现在都使用我们自己的?
                    else if("part_number".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.aps.manufacturerPartNumber)); //TODO 不记得了...
                    else if("model".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.aps.modelNumber)); // TODO 不记得了...
                    else if("external_id".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.aps.upc));
                    else if("offering_sku".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.merchantSKU));
                    else if("our_price".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, Webs.priceLocalNumberFormat(selling.market, selling.aps.standerPrice)));
                    else if("discounted_price".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, Webs.priceLocalNumberFormat(selling.market, selling.aps.salePrice)));
                    else if("discounted_price_start_date".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, Dates.listingUpdateFmt(selling.market, selling.aps.startDate)));
                    else if("discounted_price_end_date".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, Dates.listingUpdateFmt(selling.market, selling.aps.endDate)));
                    else if("Offer_Inventory_Quantity".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, selling.aps.quantity + ""));
                    else if("activeClientTimeOnTask".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, "166279")); // 这个值是通过 JS 计算的, 而 JS 仅仅是计算一个时间, 算法无关
                    else if("matchAsin".equals(name))
                        addSellingPrams.add(new BasicNameValuePair(name, "QjAwODNRWDhBVw==")); // 在 JS 方法 preProcessMatch 执行时, 已经将 matchAsin 计算出来了,固定值
                    else if("encoded_session_hidden_map".equals(name)) {
                        addSellingPrams.add(new BasicNameValuePair(name, input.val()));
                        // 在发现了 encoded_session_hidden_map 以后需要添加这样一个属性(JS 动态添加的)
                        addSellingPrams.add(new BasicNameValuePair("sessionMapPresent", "true"));
                    } else if(StringUtils.startsWith(name, "bullet_point")) {
                        int fetureSize = selling.aps.keyFeturess.length;
                        for(int i = 0; i < fetureSize; i++)
                            addSellingPrams.add(new BasicNameValuePair("bullet_point[" + i + "]", selling.aps.keyFeturess[i]));
                        if(fetureSize < 5) { // 不足 5 个
                            for(int i = 0; i < (5 - fetureSize); i++)
                                addSellingPrams.add(new BasicNameValuePair("bullet_point[" + (i + fetureSize) + "]", ""));
                        }
                    } else if(StringUtils.startsWith(name, "generic_keywords")) {
                        int searchTermsSize = selling.aps.searchTermss.length;
                        for(int i = 0; i < searchTermsSize; i++)
                            addSellingPrams.add(new BasicNameValuePair("generic_keywords[" + i + "]", selling.aps.searchTermss[i]));
                        if(searchTermsSize < 5) {
                            for(int i = 0; i < (5 - searchTermsSize); i++)
                                addSellingPrams.add(new BasicNameValuePair("generic_keywords[" + (i + searchTermsSize) + "]", ""));
                        }
                    } else if(StringUtils.startsWith(name, "recommended_browse_nodes")) {
                        //TODO 这里没有检查 recommended_browse_nodes, 请在 Controller 中检查.
                        addSellingPrams.add(new BasicNameValuePair("recommended_browse_nodes[0]", selling.aps.rbns[0]));
                        addSellingPrams.add(new BasicNameValuePair("recommended_browse_nodes[1]", selling.aps.rbns[1]));
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
                    addSellingPrams.add(new BasicNameValuePair(name, "New|New")); // 商品的 Condition 设置为 NEW
                else
                    addSellingPrams.add(new BasicNameValuePair(name, select.select("option[selected]").val()));
            }
            // -------------  3 -----------------
            /**
             * 上架时候的错误信息全部返回给前台.
             */
            for(NameValuePair n : addSellingPrams) {
                if(StringUtils.isBlank(n.getValue())) continue;
                System.out.println(n);
            }
            body = HTTP.post(selling.account.cookieStore(), selling.account.type.saleSellingPostLink()/*从账户所在的 Market 提交*/, addSellingPrams);
            if(Play.mode.isDev())
                Devs.fileLog(String.format("%s.%s.step3.html", selling.merchantSKU, selling.account.id), body, Devs.T.SALES);

            doc = Jsoup.parse(body);
            Element form = doc.select("form").first();
            if(form == null) throw new FastRuntimeException(
                    String.format("提交的参数错误.(详细错误信息咨询 IT 查看 E_LOG/listing_sale/%s.%s.step3.html)",
                            selling.merchantSKU, selling.account.id));

            for(Element hidden : doc.select("input")) {
                String name = hidden.attr("name");
                if("newItemAsin".equals(name)) selling.asin = hidden.val();
            }
            // 最后再检查是否添加成功?
            if(StringUtils.isBlank(selling.asin)) throw new FastRuntimeException("未知原因模拟手动创建 Selling 失败, 请 IT 仔细查找问题!");
        }

        selling.listing = new Listing(selling, this).save();

        //测试使用的 UPC 614444720150
        return selling.save();
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
        return UN_USE_SKU.containsKey(Product.merchantSKUtoSKU(merchantSKU));
    }
}
