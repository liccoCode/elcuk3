package models.embedded;

import com.google.gson.annotations.Expose;
import helper.Dates;
import helper.FLog;
import helper.Webs;
import models.market.M;
import models.market.Selling;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.*;

/**
 * 整合的 Amazon 上架使用的字段, 需要添加修改都在这个类中(Component 类)
 * User: wyattpan
 * Date: 5/16/12
 * Time: 3:54 PM
 */
@Embeddable
public class AmazonProps implements Serializable {

    private static final long serialVersionUID = 6222748512793596823L;

    public enum T {
        ARRAY_TO_STR,
        STR_TO_ARRAY
    }

    public AmazonProps() {
        // 初始化这些 Lob 字段, 避免 Hibernate 3.6 [Start position [1] cannot cannot exceed overall CLOB length [0]]
        this.title = " ";
        this.keyFetures = " ";
        this.legalDisclaimerDesc = " ";
        this.sellerWarrantyDesc = " ";
        this.productDesc = " ";
        this.searchTerms = " ";
        this.platinumKeywords = " ";
    }

    @Lob
    @Required
    @Expose
    public String title;
    @Expose
    public String modelNumber;
    @Expose
    public String brand;
    @Expose
    public String manufacturer;
    /**
     * 使用  Webs.SPLIT 进行分割, 最多 5 行
     */
    @Expose
    @Lob
    public String keyFetures;
    @Transient
    public List<String> keyFeturess = new ArrayList<String>();
    /**
     * Recommended Browse Nodes;
     * 使用 [,] 进行分割, 一般为 2 个
     */
    @Expose
    public String RBN;

    @Transient
    public List<String> rbns = new ArrayList<String>();
    /**
     * For most products, this will be identical to the model number;
     * however, some manufacturers distinguish part number from model number
     */
    @Expose
    public String manufacturerPartNumber;

    /**
     * Amazon 上表示打包这个产品的数量
     */
    @Expose
    public Integer quantity;
    /**
     * 如果这个 Condition 不为空, 那么则覆盖掉 Listing 中的 Condition
     */
    @Expose
    public String condition_;

    @Required
    @Expose
    public Float standerPrice;

    @Expose
    public Float salePrice = 0f;
    /**
     * 促销产品价格的开始日期
     */
    @Expose
    public Date startDate = DateTime.now().toDate();
    /**
     * 促销产品价格的结束日期
     */
    @Expose
    public Date endDate = DateTime.now().plusYears(3).toDate();

    /**
     * Does your item have a legal disclaimer associated with it?
     */
    @Lob
    @Expose
    public String legalDisclaimerDesc;
    @Expose
    public Date launchDate;
    @Lob
    @Expose
    public String sellerWarrantyDesc;

    /**
     * 核心的产品描述
     */
    @Lob
    @Expose
    public String productDesc;

    /**
     * 使用 Webs.SPLIT 进行分割, 5 行
     */
    @Lob
    @Expose
    public String searchTerms;

    @Transient
    public List<String> searchTermss = new ArrayList<String>();

    /**
     * 使用 Webs.SPLIT 进行分割, 5 行
     */
    @Lob
    @Expose
    public String platinumKeywords;

    @Transient
    public String[] platinumKeywordss;

    @Expose
    public String upc;

    /**
     * 此 Selling 所对应的图片名字与顺序. 使用 Webs.SPLIT 进行分割
     * after mysql 5.0.3 varchar can be 0..65535 length;
     * need more size to store the imageName.
     * alter table Selling modify column imageName varchar(600);
     */
    @Expose
    public String imageName;

    /**
     * 这个是在 Amazon 上架的时候, 会去进行 matchAsin 判断, 确实这个 Listing 是否被上架过.
     * 这个值为关联上架的 ASIN 的 base64 的值
     */
    @Expose
    @Column(columnDefinition = "varchar(20) DEFAULT ''")
    public String matchAsin;

    /**
     * Gift Options 中的 Gift Wrap 设置
     */
    public boolean isGiftWrap = true;
    public boolean isGiftMessage = false;

    /**
     * 亚马逊上架的时候需要提供的product的type
     */
    public String feedProductType;

    /**
     * 模板的类型
     */
    public String templateType;

    /**
     * 英国Games模板特有的字段，此字段为rbns字段所对应的类别名称
     */
    public String itemType;

    public void validate() {
        if(StringUtils.isBlank(this.title))
            Validation.addError("", "Title 必须填写");
        if(StringUtils.isBlank(this.upc))
            Validation.addError("", "UPC 必须存在");
        if(StringUtils.isBlank(this.manufacturer))
            this.manufacturer = "EasyAcc";
        if(this.standerPrice == null || this.standerPrice <= 0)
            Validation.addError("", "产品标价必须大于 0");
        if(this.salePrice == null || this.salePrice <= 0)
            Validation.addError("", "产品真实销售价必须大于 0");
        if(this.standerPrice != null && this.salePrice != null && this.salePrice > this.standerPrice)
            Validation.addError("", "产品真实销售价必须小于产品标价");
    }

    /**
     * <pre>
     * 将:
     * - keyFetures
     * - searchTerms
     * - RBN
     * 三个数组形的值进行初始化
     * ps: 在从 [] <- str 的时候, 会保持 keyFetures[5], searchTerms[5], rbn[2] 的数组长度
     * </pre>
     *
     * @param flag 如果 flag &gt; 0 表示从 [] -> str; 如果 flag &lt;=0 表示从 [] <- str
     */
    public void arryParamSetUP(T flag) {
        if(flag == T.ARRAY_TO_STR) {
            this.keyFetures = StringUtils.join(this.keyFeturess, Webs.SPLIT);
            this.searchTerms = StringUtils.join(this.searchTermss, Webs.SPLIT);
            this.RBN = StringUtils.join(this.rbns, ",");
        } else if(flag == T.STR_TO_ARRAY) {
            this.keyFeturess = new ArrayList<String>();
            this.searchTermss = new ArrayList<String>();
            this.rbns = new ArrayList<String>();

            String[] tmp = StringUtils.splitByWholeSeparator(this.keyFetures, Webs.SPLIT);
            if(tmp != null) Collections.addAll(this.keyFeturess, tmp);

            tmp = StringUtils.splitByWholeSeparator(this.searchTerms, Webs.SPLIT);
            if(tmp != null) Collections.addAll(this.searchTermss, tmp);

            tmp = StringUtils.split(this.RBN, ",");
            if(tmp != null) Collections.addAll(this.rbns, tmp);
        }

        /**
         * Hibernate 的 bug 填写了 Lob 的字段, 为 "" 则会报告错误
         * - Start position [1] cannot exceed overall CLOB length [0] -
         * 就算是 Hibernate 3.6.10 也是如此, 所以在此进行兼容
         */
        if(StringUtils.isBlank(this.title)) this.title = " ";
        if(StringUtils.isBlank(this.keyFetures)) this.keyFetures = " ";
        if(StringUtils.isBlank(this.legalDisclaimerDesc)) this.legalDisclaimerDesc = " ";
        if(StringUtils.isBlank(this.sellerWarrantyDesc)) this.sellerWarrantyDesc = " ";
        if(StringUtils.isBlank(this.productDesc)) this.productDesc = " ";
        if(StringUtils.isBlank(this.searchTerms)) this.searchTerms = " ";
        if(StringUtils.isBlank(this.platinumKeywords)) this.platinumKeywords = " ";
    }

    /**
     * 上架前进行 keyFeturess(bullet_point)值设置的检查
     *
     * @param params
     */
    public void bulletPointsCheck(Collection<NameValuePair> params) {
        if(StringUtils.isBlank(this.keyFetures)) return;
        // 这里使用自己进行字符串
        String[] keyFeturesArr = StringUtils.splitByWholeSeparator(this.keyFetures, Webs.SPLIT);
        for(int i = 0; i < keyFeturesArr.length; i++) {
            if(keyFeturesArr[i].length() > 2000)
                throw new FastRuntimeException("Bullet Point Length must blew than 2000.");
            params.add(new BasicNameValuePair("bullet_point[" + i + "]", keyFeturesArr[i]));
        }
        int missingIndex = 5 - keyFeturesArr.length;
        if(missingIndex > 0) {
            for(int i = 1; i <= missingIndex; i++) {
                params.add(new BasicNameValuePair("bullet_point[" + (keyFeturesArr.length + i) + "]", ""));
            }
        }
    }

    /**
     * 上架前进行 searchTerms 值设置的检查
     *
     * @param params
     */
    public void searchTermsCheck(Collection<NameValuePair> params) {
        if(StringUtils.isBlank(this.searchTerms)) return;
        String[] searchTermsArr = StringUtils.splitByWholeSeparator(this.searchTerms, Webs.SPLIT);
        for(int i = 0; i < searchTermsArr.length; i++) {
            if(searchTermsArr[i].length() > 50)
                throw new FastRuntimeException("SearchTerm length must blew than 50.");
            params.add(new BasicNameValuePair("generic_keywords[" + i + "]", searchTermsArr[i]));
        }
        // length = 3, 0~2, need 3,4
        int missingIndex = 5 - searchTermsArr.length; // missingIndex = 5 - 3 = 2
        if(missingIndex > 0) {
            for(int i = 1; i <= missingIndex; i++) {
                params.add(new BasicNameValuePair("generic_keywords[" + (searchTermsArr.length + i) + "]", ""));
            }
        }
    }

    public void rbnCheck(Collection<NameValuePair> params) {
        if(StringUtils.isBlank(this.RBN) && this.rbns.size() == 0) return;
        if(this.rbns.size() >= 1) {
            params.add(new BasicNameValuePair("recommended_browse_nodes[0]", this.rbns.get(0)));
        } else {
            String[] rbns = StringUtils.split(this.RBN, ",");
            params.add(new BasicNameValuePair("recommended_browse_nodes[0]", rbns[0]));
        }
    }

    /**
     * 从修改 Listing 页面中获取数据, 并同步到此 Aps 中
     *
     * @param html
     * @param sell
     */
    public void syncPropFromAmazonPostPage(String html, Selling sell) {
        Document doc = Jsoup.parse(html);
        // ----- Input 框框
        Elements inputs = doc.select("form[name=productForm] input");
        if(inputs.size() == 0) {
            /**
             * 1. 尝试检查是否为 Listing Not Found 的异常
             * 2. 抛出未知异常
             */
            Element noListingFound = doc.select("input[name=noListingFound]").first();
            if(noListingFound != null && "true".equals(noListingFound.val().toLowerCase())) {
                sell.state = Selling.S.DOWN;
                sell.save();
                throw new FastRuntimeException(
                        String.format("Selling %s is not exist in Amazon, can be delete.",
                                sell.sellingId));
            } else {
                FLog.fileLog(String.format("%s_%s.html", sell.sellingId, sell.asin), html,
                        FLog.T.DEPLOY);
                String msg = doc.select("div").text();
                if(StringUtils.isBlank(msg)) msg = "Visit amazon page failed, Please try again.";
                throw new FastRuntimeException(String.format("Listing Sync Error. %s", msg));
            }
        }
        // 检查 merchant 参数
        String msku = doc.select("#offering_sku_display").text().trim();
        if(!StringUtils.equals(sell.merchantSKU.toUpperCase(),
                msku.toUpperCase())) // 系统里面全部使用大写, 而 Amazon 上大小写敏感, 在这里转换成系统内使用的.
            throw new FastRuntimeException("同步的 Selling Msku 不一样! 请立即联系 IT 查看问题.");

        List<String> bulletPoints = new ArrayList<String>();
        List<String> searchTerms = new ArrayList<String>();
        List<String> rbns = new ArrayList<String>();

        this.upc = doc.select("#external_id_display").text().trim();
        this.productDesc = doc.select("#product_description").text().trim();
        this.condition_ = doc.select("#offering_condition_display").text().trim()
                .toUpperCase(); // 默认为 NEW
        F.T2<M, Float> our_price = Webs.amzPriceFormat(
                doc.select("#our_price").val(), sell.account.type);
        for(Element input : inputs) {
            String name = input.attr("name");
            String val = input.val();
            if("item_name".equals(name)) this.title = val;
            else if("manufacturer".equals(name)) this.manufacturer = val;
            else if("brand_name".equals(name)) this.brand = val;
            else if("part_number".equals(name)) this.manufacturerPartNumber = val;
            else if("model".equals(name)) this.modelNumber = val;
            else if("Offer_Inventory_Quantity".equals(name))
                this.quantity = NumberUtils.toInt(val, 0);
            else if("offering_start_date".equals(name))
                this.launchDate = Dates.listingFromFmt(sell.market, val);
            else if("legal_disclaimer_description".equals(name)) this.legalDisclaimerDesc = val;
            else if("bullet_point[0]".equals(name)) bulletPoints.add(val);
            else if("bullet_point[1]".equals(name)) bulletPoints.add(val);
            else if("bullet_point[2]".equals(name)) bulletPoints.add(val);
            else if("bullet_point[3]".equals(name)) bulletPoints.add(val);
            else if("bullet_point[4]".equals(name)) bulletPoints.add(val);
            else if("generic_keywords[0]".equals(name)) searchTerms.add(val);
            else if("generic_keywords[1]".equals(name)) searchTerms.add(val);
            else if("generic_keywords[2]".equals(name)) searchTerms.add(val);
            else if("generic_keywords[3]".equals(name)) searchTerms.add(val);
            else if("generic_keywords[4]".equals(name)) searchTerms.add(val);
            else if("recommended_browse_nodes[0]".equals(name)) rbns.add(val);
            else if("recommended_browse_nodes[1]".equals(name)) rbns.add(val);
            else if("our_price".equals(name))
                this.standerPrice = Webs
                        .amazonPriceNumber(our_price._1/*同 deploy->our_price*/, val);
            else if("discounted_price".equals(name) && StringUtils.isNotBlank(val))
                this.salePrice = Webs.amazonPriceNumber(our_price._1/*同 depploy->our_price*/, val);
            else if("discounted_price_start_date".equals(name) && StringUtils.isNotBlank(val))
                this.startDate = Dates.listingFromFmt(sell.market, val);
            else if("discounted_price_end_date".equals(name) && StringUtils.isNotBlank(val))
                this.endDate = Dates.listingFromFmt(sell.market, val);
//            else ignore
        }
        this.keyFetures = StringUtils.join(bulletPoints, Webs.SPLIT);
        this.searchTerms = StringUtils.join(searchTerms, Webs.SPLIT);
        this.RBN = StringUtils.join(rbns, ",");
        this.arryParamSetUP(T.STR_TO_ARRAY); // 对 hibernate 3.6 的 Lob bug 兼容
    }

    /**
     * 根据 Amazon Post Listing 的页面解析出参数, 并生成根据 aps 生成好的参数集合, 同时将解析的 document 原始文档也返回
     *
     * @param html
     * @param sell
     * @return
     */
    public F.T2<Collection<NameValuePair>, String> generateDeployProps(String html, Selling sell) {
        if(this.productDesc.length() > 2000)
            throw new FastRuntimeException("Product Descriptoin must blew then 2000.");
        if(this.standerPrice == null || this.standerPrice <= 0)
            throw new FastRuntimeException("Selling StandPrice is below 0 !!");
        if(this.salePrice == null || this.salePrice <= 0)
            throw new FastRuntimeException("Selling SalePrice is below 0 !!");
        Document doc = Jsoup.parse(html);
        // ----- Input 框框
        Element form = doc.select("form[name=productForm]").first();
        Elements inputs = form.select("input");

        String priceFormat = doc.select("#Parentour_price-span").first().nextElementSibling().text();

        if(inputs.size() == 0) {
            FLog.fileLog(String.format("%s_%s.html", sell.merchantSKU, sell.asin), html, FLog.T.DEPLOY);
            String msg = doc.select("div").text();
            if(StringUtils.isBlank(msg)) msg = "Display Post page visit Error. Please try again.";
            throw new FastRuntimeException(String.format("Listing Sync Error. %s", msg));
        }

        Set<NameValuePair> params = new HashSet<NameValuePair>();

        for(Element el : inputs) {
            String name = el.attr("name").toLowerCase().trim();

            // Vital Info
            if("item_name".equals(name)) {
                addParams(name, this.title, params);
            } else if("manufacturer".equals(name)) {
                addParams(name, this.manufacturer, params);
            } else if("brand_name".equals(name)) {
                addParams(name, this.brand, params);
            } else if("part_number".equals(name)) {
                addParams(name, this.manufacturerPartNumber, params);
            } else if("model".equals(name)) {
                addParams(name, this.modelNumber, params);
            } else if(name.startsWith("recommended_browse_nodes")) {
                this.rbnCheck(params);
            }

            // Offer
            else if("our_price".equals(name)) { // 显示价格
                if(this.standerPrice == null || this.standerPrice <= 0) this.standerPrice = 999f;
                addParams(name, Webs.amzPriceToFormat(this.standerPrice, priceFormat), params);
            } else if("discounted_price".equals(name)) {
                // 价格, 两个日期, discounted_price_start_date, discounted_price_end_date
                addParams(name, Webs.amzPriceToFormat(this.salePrice, priceFormat), params);
            } else if("discounted_price_start_date".equals(name)) {
                addParams(name, Dates.listingUpdateFmt(sell.market, this.startDate), params);
            } else if("discounted_price_end_date".equals(name)) {
                addParams(name, Dates.listingUpdateFmt(sell.market, this.endDate), params);
            } else if("offering_can_be_gift_wrapped".equals(name))
                addParams(name, this.isGiftWrap ? "on" : "false", params);
            else if("offering_can_be_gift_messaged".equals(name)) {
                addParams(name, this.isGiftMessage ? "on" : "false", params);
            } else if("quantity".equals(name)) {
                addParams(name, "0", params);
            }

            // Description & Keywords
            else if(name.startsWith("bullet_point")) {
                this.bulletPointsCheck(params);
            } else if(name.startsWith("generic_keywords")) {
                this.searchTermsCheck(params);
            } else if("activeClientTimeOnTask".equals(name)) {
                addParams(name, 18059 + "", params);
            } else if("checkbox".equals(el.attr("type"))) {
                addParams(name, "checked".equals(el.attr("checked")) ? "on" : "false", params);
                // checkbox 过滤
            } else {
                addParams(name, el.val(), params);
            }
        }
        // ------------ TextArea 框框
        Elements textareas = form.select("textarea");
        for(Element text : textareas) {
            String name = text.attr("name");
            if("product_description".equals(name) && StringUtils.isNotBlank(this.productDesc)) {
                params.add(new BasicNameValuePair(name, this.productDesc));
            } else {
                params.add(new BasicNameValuePair(name, text.val()));
            }
        }

        // ------------ Select 框框
        Elements selects = doc.select("select");
        for(Element select : selects) {
            params.add(new BasicNameValuePair(select.attr("name"), select.select("option[selected]").val()));
        }
        return new F.T2<Collection<NameValuePair>, String>(params, form.attr("action"));
    }

    private void addParams(String name, String value, Collection<NameValuePair> params) {
        params.add(new BasicNameValuePair(name, value));
    }
}
