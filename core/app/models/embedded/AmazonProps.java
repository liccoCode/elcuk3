package models.embedded;

import com.google.gson.annotations.Expose;
import helper.Constant;
import helper.Dates;
import helper.FLog;
import helper.Webs;
import models.market.Account;
import models.market.Selling;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Play;
import play.data.validation.Required;
import play.libs.F;
import play.libs.IO;
import play.utils.FastRuntimeException;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;
import javax.persistence.Transient;
import java.io.File;
import java.util.*;

/**
 * 整合的 Amazon 上架使用的字段, 需要添加修改都在这个类中(Component 类)
 * User: wyattpan
 * Date: 5/16/12
 * Time: 3:54 PM
 */
@Embeddable
public class AmazonProps {
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
    public List<String> keyFeturess;
    /**
     * Recommended Browse Nodes;
     * 使用 [,] 进行分割, 一般为 2 个
     */
    @Expose
    public String RBN;

    @Transient
    public List<String> rbns;
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
    public Float salePrice;
    /**
     * 促销产品价格的开始日期
     */
    @Expose
    public Date startDate;
    /**
     * 促销产品价格的结束日期
     */
    @Expose
    public Date endDate;

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
    public List<String> searchTermss;

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
     */
    @Expose
    public String imageName;

    /**
     * 这个是在 Amazon 上架的时候, 会去进行 matchAsin 判断, 确实这个 Listing 是否被上架过.
     */
    @Expose
    @Column(columnDefinition = "varchar(20) DEFAULT ''")
    public String matchAsin;

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
        // 这里使用自己进行字符串
        String[] keyFeturesArr = StringUtils.splitByWholeSeparator(this.keyFetures, Webs.SPLIT);
        for(int i = 0; i < keyFeturesArr.length; i++) {
            if(keyFeturesArr[i].length() > 2000)
                throw new FastRuntimeException("Bullet Point Length must blew than 50.");
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
        String[] searchTermsArr = StringUtils.splitByWholeSeparatorPreserveAllTokens(this.searchTerms, Webs.SPLIT);
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
                throw new FastRuntimeException(String.format("Selling %s is not exist in Amazon, can be delete.", sell.sellingId));
            } else {
                FLog.fileLog(String.format("%s_%s.html", sell.sellingId, sell.asin), html, FLog.T.DEPLOY);
                String msg = doc.select("div").text();
                if(StringUtils.isBlank(msg)) msg = "Visit amazon page failed, Please try again.";
                throw new FastRuntimeException(String.format("Listing Sync Error. %s", msg));
            }
        }
        // 检查 merchant 参数
        String msku = doc.select("#offering_sku_display").text().trim();
        if(!StringUtils.equals(sell.merchantSKU, msku.toUpperCase())) // 系统里面全部使用大写, 而 Amazon 上大小写敏感, 在这里转换成系统内使用的.
            throw new FastRuntimeException("同步的 Selling Msku 不一样! 请立即联系 IT 查看问题.");

        String[] bulletPoints = new String[5];
        String[] searchTerms = new String[5];
        String[] rbns = new String[2];

        this.upc = doc.select("#external_id_display").text().trim();
        this.productDesc = doc.select("#product_description").text().trim();
//        this.aps.condition_ = doc.select("#offering_condition option[selected]").first().text(); // 默认为 NEW
//        this.aps.condition_ = doc.select("#offering_condition_display").text(); // 默认为 NEW
        F.T2<Account.M, Float> our_price = Webs.amazonPriceNumberAutoJudgeFormat(doc.select("#our_price").val(), sell.account.type);
        for(Element input : inputs) {
            String name = input.attr("name");
            String val = input.val();
            if("item_name".equals(name)) this.title = val;
            else if("manufacturer".equals(name)) this.manufacturer = val;
            else if("brand_name".equals(name)) this.brand = val;
            else if("part_number".equals(name)) this.manufacturerPartNumber = val;
            else if("model".equals(name)) this.modelNumber = val;
            else if("Offer_Inventory_Quantity".equals(name)) this.quantity = NumberUtils.toInt(val, 0);
            else if("offering_start_date".equals(name)) this.launchDate = Dates.listingFromFmt(sell.market, val);
            else if("legal_disclaimer_description".equals(name)) this.legalDisclaimerDesc = val;
            else if("bullet_point[0]".equals(name)) bulletPoints[0] = val;
            else if("bullet_point[1]".equals(name)) bulletPoints[1] = val;
            else if("bullet_point[2]".equals(name)) bulletPoints[2] = val;
            else if("bullet_point[3]".equals(name)) bulletPoints[3] = val;
            else if("bullet_point[4]".equals(name)) bulletPoints[4] = val;
            else if("generic_keywords[0]".equals(name)) searchTerms[0] = val;
            else if("generic_keywords[1]".equals(name)) searchTerms[1] = val;
            else if("generic_keywords[2]".equals(name)) searchTerms[2] = val;
            else if("generic_keywords[3]".equals(name)) searchTerms[3] = val;
            else if("generic_keywords[4]".equals(name)) searchTerms[4] = val;
            else if("recommended_browse_nodes[0]".equals(name)) rbns[0] = val;
            else if("recommended_browse_nodes[1]".equals(name)) rbns[1] = val;
            else if("our_price".equals(name))
                this.standerPrice = Webs.amazonPriceNumber(our_price._1/*同 deploy->our_price*/, val);
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
    public F.T2<Collection<NameValuePair>, Document> generateDeployAmazonProps(String html, Selling sell) {
        if(Play.mode.isDev())
            IO.writeContent(html, new File(String.format("%s/%s_%s.html", Constant.E_DATE, sell.merchantSKU, sell.asin)));
        Document doc = Jsoup.parse(html);
        // ----- Input 框框
        Elements inputs = doc.select("form[name=productForm] input");
        if(inputs.size() == 0) {
            FLog.fileLog(String.format("%s_%s.html", sell.merchantSKU, sell.asin), html, FLog.T.DEPLOY);
            String msg = doc.select("div").text();
            if(StringUtils.isBlank(msg)) msg = "Display Post page visit Error. Please try again.";
            throw new FastRuntimeException(String.format("Listing Sync Error. %s", msg));
        }
        Set<NameValuePair> params = new HashSet<NameValuePair>();
        F.T2<Account.M, Float> our_price = Webs.amazonPriceNumberAutoJudgeFormat(doc.select("#our_price").val(), sell.account.type);
        for(Element el : inputs) {
            String name = el.attr("name").toLowerCase().trim();
            if("our_price".equals(name) && this.standerPrice != null && this.standerPrice > 0)
                params.add(new BasicNameValuePair(name, Webs.priceLocalNumberFormat(our_price._1, this.standerPrice)));
            else if(StringUtils.startsWith(name, "generic_keywords") && StringUtils.isNotBlank(this.searchTerms))
                this.searchTermsCheck(params);
            else if(StringUtils.startsWith(name, "bullet_point") && StringUtils.isNotBlank(this.keyFetures))
                this.bulletPointsCheck(params);
            else if("manufacturer".equals(name))
                params.add(new BasicNameValuePair(name, this.manufacturer));
            else if("item_name".equals(name))
                params.add(new BasicNameValuePair(name, this.title));
            else if("part_number".equals(name))
                params.add(new BasicNameValuePair(name, this.manufacturerPartNumber));
            else if("quantity".equals(name))
                params.add(new BasicNameValuePair(name, (this.quantity == null ? 0 : this.quantity) + ""));
            else if("discounted_price".equals(name) || "discounted_price_start_date".equals(name) ||
                    "discounted_price_end_date".equals(name)) {
                if(this.startDate != null && this.endDate != null &&
                        this.salePrice != null && this.salePrice > 0 &&
                        this.endDate.getTime() > this.startDate.getTime()) {
                    params.add(new BasicNameValuePair("discounted_price", Webs.priceLocalNumberFormat(our_price._1/*our_price*/, this.salePrice)));
                    params.add(new BasicNameValuePair("discounted_price_start_date", Dates.listingUpdateFmt(sell.market, this.startDate)));
                    params.add(new BasicNameValuePair("discounted_price_end_date", Dates.listingUpdateFmt(sell.market, this.endDate)));
                }
            } else if(StringUtils.startsWith(name, "recommended_browse_nodes")) {
                if(this.rbns != null && this.rbns.size() >= 1) {
                    for(int i = 0; i < this.rbns.size(); i++)
                        params.add(new BasicNameValuePair("recommended_browse_nodes[" + i + "]", this.rbns.get(i)));
                }
            } else {
                params.add(new BasicNameValuePair(name, el.val()));
            }
        }
        // ------------ TextArea 框框
        Elements textareas = doc.select("form[name=productForm] textarea");
        for(Element text : textareas) {
            String name = text.attr("name");
            if("product_description".equals(name) && StringUtils.isNotBlank(this.productDesc)) {
                if(this.productDesc.length() > 2000)
                    throw new FastRuntimeException("Product Descriptoin must blew then 2000.");
                params.add(new BasicNameValuePair(name, this.productDesc));
            } else {
                params.add(new BasicNameValuePair(name, text.val()));
            }
        }
        // ------------ Select 框框
        Elements selects = doc.select("form[name=productForm] select");
        for(Element select : selects) {
            params.add(new BasicNameValuePair(select.attr("name"), select.select("option[selected]").val()));
        }
        return new F.T2<Collection<NameValuePair>, Document>(params, doc);
    }
}
