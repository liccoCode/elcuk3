package models.market;

import com.amazonservices.mws.products.MarketplaceWebServiceProductsClient;
import com.amazonservices.mws.products.model.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import controllers.Login;
import exception.NotSupportChangeRegionFastException;
import helper.*;
import jobs.analyze.SellingSaleAnalyzeJob;
import models.embedded.AmazonProps;
import models.procure.ProcureUnit;
import models.product.Attach;
import models.product.Product;
import models.view.dto.AnalyzeDTO;
import models.view.post.SellingAmzPost;
import mws.MWSProducts;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.NameValuePair;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.Jsoup;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;
import play.libs.Codec;
import play.libs.F;
import play.libs.IO;
import play.libs.Time;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 已经正在进行销售的对象抽象
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:48 AM
 */
@Entity
@DynamicUpdate
public class Selling extends GenericModel {

    private static final long serialVersionUID = -4124213853478159984L;

    /**
     * 上架后用来唯一标示这个 Selling 的 Id;
     * sellingId: msku|market.nickName|acc.id
     */
    @Id
    @Column(length = 70)
    @Expose
    public String sellingId;

    public Selling() {
        this.aps = new AmazonProps();
        this.state = S.NEW;
        this.createDate = new Date();
    }

    public enum S {
        /**
         * 新创建的, 准备开卖
         */
        NEW,
        /**
         * 已经正常开始进行销售
         */
        SELLING,
        /**
         * 完全下架, 如果可以还能够重新上架
         */
        DOWN
    }

    /**
     * 生命周期
     */
    public enum SC {
        /**
         * 引入
         */
        NEW {
            @Override
            public String label() {
                return "引入";
            }
        },
        /**
         * 成长
         */
        GROW {
            @Override
            public String label() {
                return "成长";
            }
        },
        /**
         * 成熟
         */
        MATURE {
            @Override
            public String label() {
                return "成熟";
            }
        },
        /**
         * 衰退
         */
        RECESSION {
            @Override
            public String label() {
                return "衰退";
            }
        },
        /**
         * 退市
         */
        DELISTING {
            @Override
            public String label() {
                return "退市";
            }
        };

        public abstract String label();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Product product;

    @OneToMany(mappedBy = "selling", fetch = FetchType.LAZY)
    public List<SellingQTY> qtys = new ArrayList<>();

    @OneToMany(mappedBy = "selling", fetch = FetchType.LAZY)
    public List<SellingRank> ranks = new ArrayList<>();

    /**
     * 1. 在 Amazon 上架的唯一的 merchantSKU(SKU,UPC);
     * 2. 在 ebay 上唯一的 itemid(因为 ebay 的 itemid 是唯一的, 所以对于 ebay 的 selling, merchantSKU 与 asin 将会一样)
     */
    @Column(nullable = false)
    @Required
    @Expose
    public String merchantSKU;

    /**
     * 1. 在 Amazon 上单个市场中唯一的 ASIN
     * 2. 在 ebay 上架的唯一的 itemId;
     */
    @Column(nullable = false)
    @Expose
    public String asin;

    @Enumerated(EnumType.STRING)
    @Expose
    public M market;

    @Enumerated(EnumType.STRING)
    @Required
    @Expose
    public S state;


    /**
     * selling生命周期(所处状态)
     */
    @Enumerated(EnumType.STRING)
    public SC sellingCycle;


    /**
     * 给这个 Selling 人工设置的 PS 值
     */
    public Float ps = 0f;

    @Expose
    public Float shippingPrice = 0f;

    /**
     * 此 Listing 在 FBA 仓库中所使用的外键 sku
     */
    @Expose
    @Column(length = 32)
    public String fnSku;

    /**
     * Amazon Seller 检查时间(检查有无被其他卖家绑 Listing)
     */
    @Expose
    public Date lastSellerCheckDate = new Date();

    /**
     * 创建Selling时间
     */
    @Expose
    public Date createDate;

    // -----------------------  Amazon 上架会需要使用到的信息 ----------------------------
    @Embedded
    @Expose
    public AmazonProps aps;

    /**
     * 这个 Selling 所属的哪一个用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    public Account account;
    // ---- Images ????

    public String binding;
    public String productGroup;
    public String productTypeName;
    public String publisher;

    public Date pirateDate;

    @Enumerated(EnumType.STRING)
    public PS pirateState = PS.NORMAL;

    /**
     * 订单的状态 State
     */
    public enum PS {
        /**
         * 正常(未发现盗卖)
         */
        NORMAL,
        /**
         * 盗卖
         */
        PIRATE,
        /**
         * 盗卖并发送邮件通知
         */
        SEND
    }

    /**
     * 系统自动同步时间
     */
    public Date syncTime;

    // -------------------------- ebay 上架使用的信息 TBD ---------------------

    @PreUpdate
    @PrePersist
    public void preUpdate() {
        if(StringUtils.isNotBlank(this.aps.productDesc)) {
            // 不能够存在换行符号, 不然会生成上架失败的 Feed 文件
            this.aps.productDesc = StringUtils.replaceEach(this.aps.productDesc,
                    new String[]{"\r", "\n", "\r\n", "\t"}, new String[]{"", "", "", ""});
        }
        if(StringUtils.isNotBlank(this.aps.title)) {
            this.aps.title = StringUtils.replaceEach(this.aps.title,
                    new String[]{"\r", "\n", "\r\n", "\t"}, new String[]{"", "", "", ""});
        }
    }

    /**
     * 临时解决 Amazon 同步时 OrderItem 无法存入数据库， 手动添加 新旧 Selling 映射关系
     * 此代码性质为临时代码， 等到 Amazon 那边不再产生旧 Selling 的订单时可移除此代码
     */
    private static final Map<String, String> SELLING_MAPPING = new HashMap<>();

    /**
     * 初始化 映射 Selling 映射关系
     */
    static {
        SELLING_MAPPING.put("72GMSTL-B6L", "91GMSTL-B6L");
        SELLING_MAPPING.put("72KBCG1-W", "88KBCG1-WDE");
        SELLING_MAPPING.put("72KBDC1-W", "88KBDC1-WDE");
        SELLING_MAPPING.put("72DBSG1-WEU", "88DBSG1-WEU");
        SELLING_MAPPING.put("72DBSG1-WUK", "88DBSG1-WUK");
        SELLING_MAPPING.put("72DBSG1-WUS", "88DBSG1-WUS");
        SELLING_MAPPING.put("72FLMINI-BF", "92FLMINI-BF");
    }

    /**
     * 如果在 SELLING_MAPPING 找到 key 返回映射好的新的 sellingID
     * 如果没有则直接返回原有的 sku
     * 1. 查找到则替换
     * 2. 找不到则返回原有值
     *
     * @param sku
     * @return
     */
    public static String getMappingSKU(String sku) {
        for(String key : SELLING_MAPPING.keySet()) {
            if(StringUtils.contains(sku, key)) {
                return StringUtils.replace(sku, key, SELLING_MAPPING.get(key));
            }
        }
        return sku;
    }

    public void recordingListingState(Date changedDate, String state) {
        ListingStateRecord record = new ListingStateRecord();
        record.changedDate = changedDate;
        record.state = ListingStateRecord.S.valueOf(state);
        record.selling = this;
        record.save();
    }

    /**
     * 从 amazon 将数据同步回来
     */
    public void syncFromAmazon() {
        String html = "";
        String fnskuhtml = "";
        synchronized(this.account.cookieStore()) {
            this.account.loginAmazonSellerCenter();
            // 1. 切换 Selling 所在区域
            this.account.changeRegion(this.market);
            // 2. 获取修改 Selling 的页面, 获取参数
            html = HTTP.get(this.account.cookieStore(), this.amzListingEditPage());
            if(StringUtils.isBlank(html))
                throw new FastRuntimeException(String.format("Visit %s page is empty.", this.amzListingEditPage()));
            if(Play.mode.isDev()) {
                IO.writeContent(html,
                        new File(String.format("%s/%s_%s.html", Constant.E_DATE, this.merchantSKU, this.asin)));
            }
            // 获取Fnsku
            fnskuhtml = HTTP.get(this.account.cookieStore(), this.amzListingFnSkuPage());
        }
        // 3. 将需要的参数同步进来
        this.aps.syncPropFromAmazonPostPage(html, this);
        String fnsku = this.aps.syncfnSkuFromAmazonPostPage(fnskuhtml, this);
        if(StringUtils.isNotBlank(fnsku)) {
            this.fnSku = fnsku;
        }
        this.save();
        //4.通过AMAZON,API形式同步数据回数据库
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("account_id", this.account.id.toString()));
        params.add(new BasicNameValuePair("asin", this.asin));
        params.add(new BasicNameValuePair("market", this.market.name()));
        params.add(new BasicNameValuePair("selling_id", this.sellingId));
        params.add(new BasicNameValuePair("user_name", Login.current().username));
        HTTP.post(System.getenv(Constant.ROCKEND_HOST) + "/amazon_product_sync_back", params);
        this.save();
    }

    /**
     * 通过mws的方式同步数据
     */
    public void syncAmazonInfoFromApi() {
        MarketplaceWebServiceProductsClient client = MWSProducts.client(this.account, this.account.type);
        ASINListType asinList = new ASINListType();
        asinList.getASIN().add(this.asin);
        GetMatchingProductRequest request = new GetMatchingProductRequest(account.merchantId, this.market.amid().name(),
                asinList);
        request.setMWSAuthToken(account.token);
        GetMatchingProductResponse response = client.getMatchingProduct(request);
        List<GetMatchingProductResult> results = response.getGetMatchingProductResult();
        results.forEach(result -> {
            com.amazonservices.mws.products.model.Product mwsProduct = result.getProduct();
            if(mwsProduct == null)
                return;
            String attributeXml = mwsProduct.getAttributeSets().toXML();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
                StringReader sr = new StringReader(attributeXml);
                InputSource is = new InputSource(sr);

                Document doc = db.parse(is);
                NodeList nodeList = doc.getElementsByTagName("ns2:ItemAttributes");
                for(int i = 0; i < nodeList.getLength(); i++) {
                    Element n = (Element) nodeList.item(i);
                    if(n.getElementsByTagName("ns2:Binding").item(0) != null) {
                        this.binding = n.getElementsByTagName("ns2:Binding").item(0).getTextContent();
                    }
                    if(n.getElementsByTagName("ns2:Brand").item(0) != null) {
                        this.aps.brand = n.getElementsByTagName("ns2:Brand").item(0).getTextContent();
                    }
                    if(n.getElementsByTagName("ns2:Manufacturer").item(0) != null) {
                        this.aps.manufacturer = n.getElementsByTagName("ns2:Manufacturer").item(0).getTextContent();
                    }
                    if(n.getElementsByTagName("ns2:PartNumber").item(0) != null) {
                        this.aps.manufacturerPartNumber = n.getElementsByTagName("ns2:PartNumber").item(0)
                                .getTextContent();
                    }
                    if(n.getElementsByTagName("ns2:PackageQuantity").item(0) != null) {
                        this.aps.quantity = Integer.parseInt(n.getElementsByTagName("ns2:PackageQuantity").item(0)
                                .getTextContent());
                    }
                    this.productGroup = n.getElementsByTagName("ns2:ProductGroup").item(0).getTextContent();
                    this.productTypeName = n.getElementsByTagName("ns2:ProductTypeName").item(0).getTextContent();
                    if(n.getElementsByTagName("ns2:Publisher").item(0) != null) {
                        this.publisher = n.getElementsByTagName("ns2:Publisher").item(0).getTextContent();
                    }
                    this.aps.title = n.getElementsByTagName("ns2:Title").item(0).getTextContent();
                    this.aps.imageUrl = n.getElementsByTagName("ns2:SmallImage").item(0).getTextContent();
                    if(StringUtils.isNotBlank(this.aps.imageUrl)) {
                        this.aps.imageUrl = this.aps.imageUrl.replace("7575", "");
                    }
                    this.save();
                }
            } catch(ParserConfigurationException | SAXException | IOException e) {
                Logger.error(e.getMessage());
            }
        });
        GetMyPriceForASINRequest asinRequest = new GetMyPriceForASINRequest(account.merchantId,
                this.market.amid().name(), asinList);
        asinRequest.setMWSAuthToken(account.token);
        GetMyPriceForASINResponse priceForASINResponse = client.getMyPriceForASIN(asinRequest);
        List<GetMyPriceForASINResult> asinResults = priceForASINResponse.getGetMyPriceForASINResult();
        asinResults.forEach(result -> {
            com.amazonservices.mws.products.model.Product prodcut = result.getProduct();
            OffersList offers = prodcut.getOffers();
            List<OfferType> list = offers.getOffer();
            list.forEach(offerType -> {
                MoneyType moneyType = offerType.getRegularPrice();
                this.aps.standerPrice = moneyType.getAmount().floatValue();
                PriceType priceType = offerType.getBuyingPrice();
                this.aps.salePrice = priceType.getListingPrice().getAmount().floatValue();
                this.save();
            });
        });
    }

    /**
     * 将前端勾选的部分属性同步到 Amazon
     *
     * @param p
     */
    public void partialUpdate(SellingAmzPost p) {
        this.account = Account.findById(this.account.id);
        if(p.rbns || p.productvolume || p.productWeight || p.weight || p.title || p.keyfeturess || p.searchtermss
                || p.productdesc) {
            Feed feed = Feed.updateSellingFeed(MWSUtils.buildProductXMLBySelling(this, p), this);
            feed.submit(this.partialUpdateParams());
        }
        if(p.standerprice || p.saleprice) {
            Feed priceFeed = Feed.newAssignPriceFeed(MWSUtils.assignPriceXml(this), this);
            priceFeed.submit(this.assignAmazonListingPriceParams());
        }
    }


    /**
     * @return
     * @deprecated
     */
    public byte[] downloadFnSkuLabel() {
        if(StringUtils.isBlank(this.fnSku))
            throw new FastRuntimeException("Selling " + this.sellingId + " 没有 FnSku 无法下载最新的 Label.");
        this.account.loginAmazonSellerCenter();
        synchronized(this.account.cookieStore()) {
            Map<String, String> params = GTs.MapBuilder
                    .map("labelType", "ItemLabel_A4_27")
                    .put("mSku.0", this.merchantSKU)
                    .put("qty.0", "27") // 一页打 44 个
                    .put("fnSku.0", this.fnSku).build();
            for(Cookie coo : this.account.cookieStore().getCookies()) {
                Logger.info(" ============" + coo.getName() + "=" + coo.getValue() + "============");
            }
            this.account.changeRegion(this.market);
            return HTTP.postDown(this.account.cookieStore(), this.account.type.fnSkuDownloadLink(),
                    Arrays.asList(new BasicNameValuePair("model", J.json(params))));
        }
    }

    public List<NameValuePair> submitJobParams() {
        Validate.notNull(this.account);
        Validate.notNull(this.market);
        Validate.notEmpty(this.sellingId);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("account_id", this.account.id.toString()));// 使用哪一个账号
        params.add(new BasicNameValuePair("market", this.market.name()));// 向哪一个市场
        params.add(new BasicNameValuePair("selling_id", this.sellingId)); // 作用与哪一个 Selling
        return params;
    }

    public List<NameValuePair> submitJobParams(Feed feed) {
        Validate.notNull(feed);
        List<NameValuePair> params = this.submitJobParams();
        params.add(new BasicNameValuePair("feed_id", feed.id.toString()));// 提交哪一个 Feed ?
        return params;
    }

    /**
     * 用Feed方式更新产品图片
     */
    public void uploadFeedAmazonImg(String imageName, boolean waterMark, String userName) {
        if(!Feed.isFeedAvalible(this.account.id)) Webs.error("已经超过 Feed 的提交频率, 请等待 2 ~ 5 分钟后再提交.");
        String dealImageNames = imageName;
        if(StringUtils.isBlank(imageName)) dealImageNames = this.aps.imageName;
        if(StringUtils.isBlank(dealImageNames)) throw new FastRuntimeException("此 Selling 没有指定图片.");
        String[] images = StringUtils.splitByWholeSeparator(dealImageNames, Webs.SPLIT);
        // 如果有更多的图片,仅仅使用前 9 张, 并且也只存储 9 张图片的名字
        if(images.length >= 9) images = Arrays.copyOfRange(images, 0, 9);
        this.aps.imageName = StringUtils.join(images, Webs.SPLIT);
        this.postImages(images);
        this.save();
    }

    /**
     * 通过 Product 上架页面提交的信息, 使用 UPC 代替 ASIN, 等待 ASIN 被成功填充, 再更新 asin 为具体的 Asin 值
     *
     * @return
     */
    public Selling buildFromProduct() {
        this.saleAmazonValid();
        this.asin = this.aps.upc;
        this.saveSelling();
        this.saleAmazon();
        this.assignAmazonListingPrice();
        this.setFulfillmentByAmazon();
        return this;
    }

    private void saveSelling() {
        if(Selling.exist(this.sid())) Webs.error(String.format("Selling[%s] 已经存在", this.sellingId));
        Product pro = Product.findByMerchantSKU(this.merchantSKU);
        if(pro == null) Webs.error("SKU 产品不存在");
        List<Attach> images = Attach.attaches(pro.sku, Attach.P.SKU.name());
        if(images != null && images.size() != 0) {
            this.aps.imageName = images.get(0).fileName;
        }
        this.product = pro;
        this.aps.arryParamSetUP(AmazonProps.T.ARRAY_TO_STR);
        if(!Selling.exist(this.sid()))
            this.save();
        else
            throw new FastRuntimeException("Selling 已经存在！");
    }

    public void saleAmazonValid() {
        if(this.account == null) Webs.error("上架账户不能为空");
        if(!Feed.isFeedAvalible(this.account.id)) Webs.error("已经超过 Feed 的提交频率, 请等待 2 ~ 5 分钟后再提交.");
        if(StringUtils.isBlank(this.aps.upc)) Webs.error("UPC 必须填写");
        if(this.market == null) Webs.error("Market 不能为空");
        if(this.aps.upc.length() != 12) Webs.error("UPC 的格式错误,其为 12 位数字");
        if(!isMSkuValid()) Webs.error("Merchant SKU 不合法. [SKU],[UPC]");
        if(StringUtils.isBlank(this.aps.title)) Webs.error("Selling Title 必须存在");
        if(StringUtils.isBlank(this.aps.brand)) Webs.error("品牌必须填写");
        if(StringUtils.isBlank(this.aps.manufacturer)) Webs.error("Manufacturer 必须填写");
        if(StringUtils.isBlank(this.aps.manufacturerPartNumber)) Webs.error("Part Number 需要填写");
        if(StringUtils.isBlank(this.aps.feedProductType)) Webs.error("Feed Product Type 必须填写");
        if(this.aps.standerPrice == null || this.aps.standerPrice <= 0) Webs.error("标准价格必须大于 0");
        if(this.aps.salePrice == null || this.aps.salePrice <= 0) Webs.error("优惠价格必须大于 0");
        this.rbnsValid();
        this.productPackageDimensionValid();
    }

    /**
     * 产品上架时，验证包材信息是否全部填写完整
     **/
    public void productPackageDimensionValid() {
        if(this.product.lengths == null) Webs.error("产品长(包材)需填写!");
        if(this.product.width == null) Webs.error("产品宽(包材)需填写!");
        if(this.product.heigh == null) Webs.error("产品高(包材)需填写!");
        if(this.product.weight == null) Webs.error("产品重量(包材)需填写!");
    }

    /**
     * 校验 RBN 是否合法
     */
    public void rbnsValid() {
        if(this.aps.rbns == null) Webs.error("Recommanded Browser Nodes 必须填写");
        this.aps.rbns.remove("");
        if(this.aps.rbns.isEmpty()) Webs.error("Recommanded Browser Nodes 必须填写");
        if(this.market != M.AMAZON_US) {
            this.aps.rbns.stream()
                    .filter(rbn -> !NumberUtils.isNumber(rbn))
                    .forEach(rbn -> {
                        Webs.error(String.format("%s 市场的 Recommanded Browser Nodes 必须为数字", this.market.name()));
                    });
        }
    }

    public List<NameValuePair> saleAmazonParams() {
        List<NameValuePair> params = this.submitJobParams();
        params.add(new BasicNameValuePair("type", "CreateListing"));
        params.add(new BasicNameValuePair("feed_type", MWSUtils.T.PRODUCT_FEED.toString()));
        return params;
    }

    public List<NameValuePair> partialUpdateParams() {
        List<NameValuePair> params = this.submitJobParams();
        params.add(new BasicNameValuePair("type", "PartialUpdateListing"));
        params.add(new BasicNameValuePair("feed_type", MWSUtils.T.PRODUCT_FEED.toString()));
        return params;
    }

    public List<NameValuePair> postImagesParams() {
        List<NameValuePair> params = this.submitJobParams();
        params.add(new BasicNameValuePair("type", "PostImages"));
        params.add(new BasicNameValuePair("feed_type", MWSUtils.T.PRODUCT_IMAGES_FEED.toString()));
        return params;
    }

    public List<NameValuePair> assignAmazonListingPriceParams() {
        List<NameValuePair> params = this.submitJobParams();
        params.add(new BasicNameValuePair("type", "AssignPrice"));
        params.add(new BasicNameValuePair("feed_type", MWSUtils.T.PRICING_FEED.toString()));
        return params;
    }

    public List<NameValuePair> setFulfillmentByAmazonParams() {
        List<NameValuePair> params = this.submitJobParams();
        params.add(new BasicNameValuePair("type", "FulfillmentByAmazon"));
        params.add(new BasicNameValuePair("feed_type", MWSUtils.T.PRODUCT_INVENTORY_FEED.toString()));
        return params;
    }

    /**
     * 提交 Listing 上架的 XML
     */
    public void saleAmazon() {
        Feed feed = Feed.newSellingFeed(MWSUtils.toSaleAmazonXml(this), this);
        feed.submit(this.saleAmazonParams());
    }

    /**
     * 上传图片
     */
    public void postImages(String[] images) {
        Feed feed = Feed.updateSellingFeed(MWSUtils.buildProductImageBySelling(this, images), this);
        feed.submit(this.postImagesParams());
    }

    /**
     * 通过 XML 来设置 Amazon Listing 的 Price 属性
     */
    public void assignAmazonListingPrice() {
        Feed feed = Feed.newAssignPriceFeed(MWSUtils.assignPriceXml(this), this);
        feed.submit(this.assignAmazonListingPriceParams());
    }

    /**
     * 通过 XML 来设置 Amazon Listing 为 Fulfillment By Amazon
     */
    public void setFulfillmentByAmazon() {
        Feed feed = Feed.setFulfillmentByAmazonFeed(MWSUtils.fulfillmentByAmazonXml(this), this);
        feed.submit(this.setFulfillmentByAmazonParams());
    }

    /**
     * 更新数据库, 同时还需要更新缓存
     *
     * @param ps
     * @return
     */
    @SuppressWarnings("unchecked")
    public Selling ps(Float ps) {
        if(ps == null || ps < 0) throw new FastRuntimeException("PS 格式错误或者 PS 不允许小于 0");
        this.ps = ps;
        // 如果缓存不为空则更新缓存
        List<AnalyzeDTO> dtos = AnalyzeDTO.cachedAnalyzeDTOs("sid");
        if(dtos != null) {
            boolean find = false;
            for(AnalyzeDTO dto : dtos) {
                if(!dto.fid.equals(this.sellingId)) continue;
                dto.ps = ps;
                find = true;
            }
            if(!find) {
                throw new FastRuntimeException(String.format("更新失败, %s 不在缓存中..", this.sellingId));
            } else {
                Date expireTime;
                String cache_str = Caches.get(SellingSaleAnalyzeJob.AnalyzeDTO_SID_CACHE + ".time");
                if(StringUtils.isBlank(cache_str)) {
                    expireTime = DateTime.now().plusHours(8).toDate();
                } else {
                    expireTime = DateTime.parse(cache_str, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z"))
                            .withZone(Dates.CN).toDate();
                }
                long diffSecond = (expireTime.getTime() - System.currentTimeMillis()) / 1000;
                Caches.set(SellingSaleAnalyzeJob.AnalyzeDTO_SID_CACHE, J.json(dtos),
                        Time.parseDuration(diffSecond + "s"));
            }
        }
        return this.save();
    }

    public void sellingCycle(Selling.SC cycle) {
        if(cycle == null) throw new FastRuntimeException("SellingCycle 格式错误");
        this.sellingCycle = cycle;
        // 如果缓存不为空则更新缓存
        List<AnalyzeDTO> dtos = AnalyzeDTO.cachedAnalyzeDTOs("sid");
        if(dtos != null) {
            boolean find = false;
            for(AnalyzeDTO dto : dtos) {
                if(!dto.fid.equals(this.sellingId)) continue;
                dto.sellingCycle = cycle;
                find = true;
            }
            if(!find) {
                throw new FastRuntimeException(String.format("更新失败, %s 不在缓存中..", this.sellingId));
            } else {
                Date expireTime;
                String cache_str = Caches.get(SellingSaleAnalyzeJob.AnalyzeDTO_SID_CACHE + ".time");
                if(StringUtils.isBlank(cache_str)) {
                    expireTime = DateTime.now().plusHours(8).toDate();
                } else {
                    expireTime = DateTime.parse(cache_str, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z"))
                            .withZone(Dates.CN).toDate();
                }
                long diffSecond = (expireTime.getTime() - System.currentTimeMillis()) / 1000;
                Caches.set(SellingSaleAnalyzeJob.AnalyzeDTO_SID_CACHE, J.json(dtos),
                        Time.parseDuration(diffSecond + "s"));
            }
        }
        this.save();
    }

    /**
     * 加载指定 Product 所属的 Family 下的所有 Selling 与 SellingId
     *
     * @param sku
     * @return
     */
    public static F.T2<List<Selling>, List<String>> sameFamilySellings(String sku) {
        List<Selling> sellings = Selling.find("product.sku like ? ", sku + "%").fetch();
        List<String> sids = sellings.stream().map(s -> s.sellingId).collect(Collectors.toList());
        return new F.T2<>(sellings, sids);
    }

    /**
     * Selling 实例对象, 自行初始化 sid
     *
     * @return
     */
    public String sid() {
        if(StringUtils.isBlank(this.sellingId)) {
            if(StringUtils.isBlank(this.merchantSKU))
                throw new FastRuntimeException("Selling merchantSKU can not be empty.");
            if(this.market == null)
                throw new FastRuntimeException("Selling market can not be null.");
            if(this.account == null)
                throw new FastRuntimeException("Selling account can not be null.");
            this.sellingId = Selling.sid(this.merchantSKU, this.market, this.account);
        }
        return this.sellingId;
    }

    /**
     * 提交的与此 Selling 有关的 Feed
     */
    public List<Feed> feeds() {
        return Feed.find("fid=? ORDER BY createdAt DESC", this.sellingId).fetch();
    }

    public Feed recentlyFeed() {
        Feed feed = Feed.find("fid=? ORDER BY createdAt DESC", this.sellingId).first();
        return feed;
    }

    public String showFeedStatus() {
        Feed feed = this.recentlyFeed();
        if(feed != null) {
            String result = feed.result;
            if(StringUtils.isNotBlank(result)) {
                int index = result.indexOf("");
            }

        }
        return "";
    }

    public Date showDownDate() {
        ListingStateRecord record = ListingStateRecord.find("selling.sellingId = ? AND state = ? "
                + " ORDER BY changedDate DESC", this.sellingId, ListingStateRecord.S.DOWN).first();
        return record == null ? null : record.changedDate;
    }

    public Float salePriceWithCurrency() {
        if(this.aps.salePrice == null) return 0f;
        return this.market.currency().toUSD(this.aps.salePrice);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Selling selling = (Selling) o;

        if(sellingId != null ? !sellingId.equals(selling.sellingId) : selling.sellingId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (sellingId != null ? sellingId.hashCode() : 0);
        return result;
    }

    // ------------------------ static method -----------------------

    /**
     * 返回 Selling 的 Sid
     *
     * @return
     */
    public static String sid(String msku, M market, Account acc) {
        return String.format("%s|%s|%s", msku, market.nickName(), acc.id).toUpperCase();
    }

    public static boolean exist(String merchantSKU) {
        return Selling.find("sellingId=?", merchantSKU).first() != null;
    }

    public static Selling blankSelling(String sku, String asin, String upc, Account acc, M market) {
        Selling selling = new Selling();
        String msku = String.format("%s,%s", sku.trim(), upc.trim());
        selling.account = acc;
        selling.merchantSKU = msku;
        selling.product = Product.findById(sku);
        selling.asin = asin;
        selling.aps.upc = upc;
        selling.market = market;
        selling.sid();
        selling.createDate = new Date();
        selling.save();
        return selling;
    }

    public boolean isMSkuValid() {
        if(StringUtils.isBlank(this.merchantSKU)) return false;
        String[] args = StringUtils.split(this.merchantSKU, ",");
        if(args.length != 2) return false;
        if(!args[1].equals(this.aps.upc)) return false;
        this.merchantSKU = this.merchantSKU.toUpperCase();
        return true;
    }

    /**
     * 删除这个 Selling
     */
    public void remove() {
        /**
         * 1. 检查是否有采购计划, 有则不允许删除
         */
        long size = ProcureUnit.count("selling=?", this);
        if(size > 0) Webs.error("此 Selling 拥有过 " + size + " 个采购计划, 无法删除");
        size = OrderItem.count("selling=?", this);
        if(size > 0) Webs.error("拥有 " + size + " 个销售数据, 无法删除");
        SellingRecord.delete("selling=?", this);
        SellingQTY.delete("selling=?", this);
        this.delete();
    }

    /**
     * 根据传入的 ListingId 集合查找出对应的 SellingId 集合
     *
     * @param listingIds
     * @return
     */
    public static List<String> getSellingIds(List<String> listingIds) {
        List<String> sellingIds = new ArrayList<>();
        List<Map<String, Object>> rows = null;

        if(listingIds != null && listingIds.size() > 0) {
            SqlSelect sql = new SqlSelect().select("sellingId").from("Selling").where("state != 'DOWN'")
                    .andWhere(SqlSelect.whereIn("listing_listingId", listingIds));
            rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        }
        sellingIds.addAll(rows.stream().map(row -> row.get("sellingId").toString()).collect(Collectors.toList()));
        return sellingIds;
    }

    /**
     * 查找传入的 SKU 下所有的 SellingId
     *
     * @return
     */
    public static List<String> sids(List<String> skus) {
        List<String> listings = new ArrayList<>();
        for(String sku : skus) {
            listings.addAll(Listing.getAllListingBySKU(sku));
        }
        return getSellingIds(listings);
    }

    public static List<String> sids(String sku) {
        return sids(Arrays.asList(sku));
    }

    /**
     * 由 SellingId 判断市场
     *
     * @param sid
     * @return
     */
    public static M sidToMarket(String sid) {
        return M.val(StringUtils.split(sid, "|")[1].replace("_", ""));
    }

    /**
     * 由 SellingId 判断 SKU
     *
     * @return
     */
    public static String sidToSKU(String sid) {
        String temp = StringUtils.split(sid, "|")[0];
        String[] splits = StringUtils.split(temp, ",");
        if(splits.length > 1) {
            return splits[0];
        }
        return temp;
    }

    /**
     * 根据传入的参数创建 Selling
     *
     * @param line
     * @return
     */
    public static void createWithArgs(String line) {
        String[] args = StringUtils.splitPreserveAllTokens(line, "\t");
        if(args.length < 5) Webs.error("数据不完整");

        String sku = args[0].trim();
        String upc = args[1].trim();
        String asin = args[2].trim();
        M market = M.val(args[3].trim());
        Account acc = Account.find("uniqueName like ? AND isSaleAcc = true", args[4].trim() + "%").first();

        if(StringUtils.isBlank(sku)) Webs.error("SKU 无效.");
        if(StringUtils.isBlank(upc) || upc.length() != 12) Webs.error("UPC 无效.");
        if(StringUtils.isBlank(asin) || asin.length() != 10) Webs.error("ASIN 无效.");
        if(market == null) Webs.error("Market 无效.");
        if(StringUtils.isBlank(args[4]) || acc == null) Webs.error("Account 无效.");
        Selling newSelling = Selling.blankSelling(sku, asin, upc, acc, market);
    }


    /**
     * 这个 Selling 向 Amazon 上传图片.;
     * 将所有图片都上传一遍;
     */
    public void uploadAmazonImg(String imageName, boolean waterMark) {
        // 用来处理最后删除图片时使用的名称
        Map<String, AtomicBoolean> usedAmazonFileName = GTs.MapBuilder
                .map("MAIN", new AtomicBoolean(false))
                .put("PT01", new AtomicBoolean(false))
                .put("PT02", new AtomicBoolean(false))
                .put("PT03", new AtomicBoolean(false))
                .put("PT04", new AtomicBoolean(false))
                .put("PT05", new AtomicBoolean(false))
                .put("PT06", new AtomicBoolean(false))
                .put("PT07", new AtomicBoolean(false))
                .put("PT08", new AtomicBoolean(false))
                .build();

        String dealImageNames = imageName;
        if(StringUtils.isBlank(imageName)) dealImageNames = this.aps.imageName;
        if(StringUtils.isBlank(dealImageNames)) throw new FastRuntimeException("此 Selling 没有指定图片.");
        String[] images = StringUtils.splitByWholeSeparator(dealImageNames, Webs.SPLIT);
        if(images.length >= 9)  // 如果有更多的图片,仅仅使用前 9 张, 并且也只存储 9 张图片的名字
            images = Arrays.copyOfRange(images, 0, 9);
        this.aps.imageName = StringUtils.join(images, Webs.SPLIT);
        /**
         * MAIN   主图
         * PT01~08  , 2~9 号图片.
         */
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("asin", this.asin));
        params.add(new BasicNameValuePair("sku", Codec.encodeBASE64(this.merchantSKU)));
        Map<String, F.T2<String, BufferedInputStream>> uploadImages = new HashMap<>();
        for(int i = 0; i < images.length; i++) {
            String fileParamName;
            if(i == 0) fileParamName = "MAIN";
            else fileParamName = "PT0" + i;


            //Attach attch = Attach.findByFileName(images[i]);
            String location = Attach.attachImage(this.sellingId.split(",")[0], images[i]);

            if(StringUtils.isBlank(location))
                throw new FastRuntimeException("填写的图片名称(" + images[i] + ")不存在! 请重新上传.");
            if(waterMark) {
                // TODO 如果需要打水印, 在这里处理
                throw new UnsupportedOperationException("功能还没实现.");
            } else {
                uploadImages.put(fileParamName, Attach.urlToFile(location, images[i]));
            }

            usedAmazonFileName.get(fileParamName).set(true);
        }
        synchronized(this.account.cookieStore()) {
            this.account.changeRegion(this.market);
            Logger.info("Upload Picture to Amazon AND Synchronized[%s].",
                    this.account.prettyName());
            String body = HTTP
                    .upload(this.account.cookieStore(), this.account.type.uploadImageLink(), params,
                            uploadImages);

            if(Play.mode.isDev())
                FLog.fileLog(String.format("%s.%s.html", this.sellingId, this.account.id), body,
                        FLog.T.IMGUPLOAD);
            JsonObject imgRsp = new JsonParser()
                    .parse(Jsoup.parse(body).select("#jsontransport").text()).getAsJsonObject();
            //{"imageUrl":"https://media-service-eu.amazon.com/media/M3SRIZRCNL2O1K+maxw=110+maxh=110","status":"success"}</div>
            //{"errorMessage":"We are sorry. There are no file(s) specified or the file(s) specified appear to be empty.","status":"failure"}</div>
            if("failure".equals(imgRsp.get("status").getAsString())) {
                Logger.info("Upload Picture to Amazon Failed.(%s)",
                        imgRsp.get("errorMessage").getAsString());
                throw new FastRuntimeException(imgRsp.get("errorMessage").getAsString());
            } else {
                Logger.info("Upload Picture to Amazon Success.(%s)",
                        imgRsp.get("imageUrl").getAsString());
            }
            //https://catalog-sc.amazon.de/abis/image/RemoveImage.ajax?asin=B0083QX8AW&variant=MAIN/PT01/...
            for(String fileName : usedAmazonFileName.keySet()) {
                if(usedAmazonFileName.get(fileName).get()) continue; // 使用过了就不处理
                HTTP.post(this.account.cookieStore(), this.account.type.removeImageLink(),
                        Arrays.asList(
                                new BasicNameValuePair("asin", this.asin),
                                new BasicNameValuePair("variant", fileName)
                        ));
            }
        }
        this.save();
    }

    public List<NameValuePair> submitGetFeedParams(Feed feed, String feedSubmissionId) {
        Validate.notNull(feed);
        Validate.notNull(this.account);
        Validate.notNull(this.market);
        Validate.notNull(feedSubmissionId);
        Validate.notEmpty(this.sellingId);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("account_id", this.account.id.toString()));// 使用哪一个账号
        params.add(new BasicNameValuePair("market", this.market.name()));// 向哪一个市场
        params.add(new BasicNameValuePair("feed_id", feed.id.toString()));// 提交哪一个 Feed ?
        params.add(new BasicNameValuePair("feed_submission_id", feedSubmissionId));
        return params;
    }

    /**
     * 模拟人工方式修改 Listing 信息的地址(Amazon)
     *
     * @return
     */
    public String amzListingEditPage() {
        String msku = this.merchantSKU;
        if("68-MAGGLASS-3X75BG,B001OQOK5U".equalsIgnoreCase(this.merchantSKU)) {
            msku = "68-MAGGLASS-3x75BG,B001OQOK5U";
        } else if("80-qw1a56-be,2".equalsIgnoreCase(this.merchantSKU)) {
            msku = "80-qw1a56-be,2";
        } else if("80-qw1a56-be".equalsIgnoreCase(this.merchantSKU)) {
            msku = "80-qw1a56-be";
        }
        /**
         * 域名主市场由账户决定, Listing 跨市场由 Selling 的 Market 属性决定
         */
        switch(this.account.type) {
            case AMAZON_US:
                return String.format(
                        "https://catalog.%s/abis/product/DisplayEditProduct?marketplaceID=%s&sku=%s&asin=%s",
                        this.account.type.toString(), this.market.amid().name(), msku, this.asin
                );
            case AMAZON_CA:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_JP:
                return String.format(
                        "https://catalog-sc.%s/abis/product/DisplayEditProduct?marketplaceID=%s&sku=%s&asin=%s",
                        this.account.type.toString(), this.market.amid().name(), msku, this.asin
                );
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    /**
     * 模拟人工查询 FnSku 的地址
     *
     * @return
     */
    public String amzListingFnSkuPage() {
        switch(this.account.type) {
            case AMAZON_CA:
            case AMAZON_ES:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_JP:
            case AMAZON_IT:
            case AMAZON_UK:
            case AMAZON_US:
                return String.format(
                        "https://sellercentral.%s/gp/ssof/knights/items-list-xml.html/ref=ag_xx_cont_fbalist?searchType=genericQuery&genericQuery=%s",
                        this.account.type, this.merchantSKU
                );
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    /**
     * 找出已经存在的 Feed 重新推送给 Amazon 进行上架(只用于处理上架失败的但是不是 Feed 错误的情况)
     */
    public void rePushFeedsToAmazon() {
        Feed saleAmazonFeed = Feed.getFeedWithSellingAndType(this, Feed.T.SALE_AMAZON);
        Feed assignAmazonListingPriceFeed = Feed.getFeedWithSellingAndType(this, Feed.T.ASSIGN_AMAZON_LISTING_PRICE);
        Feed setFulfillmentByAmazonFeed = Feed.getFeedWithSellingAndType(this, Feed.T.FULFILLMENT_BY_AMAZON);

        if(saleAmazonFeed != null && assignAmazonListingPriceFeed != null && setFulfillmentByAmazonFeed != null) {
            if(!saleAmazonFeed.sameHours(assignAmazonListingPriceFeed)
                    || !saleAmazonFeed.sameHours(setFulfillmentByAmazonFeed)
                    || !assignAmazonListingPriceFeed.sameHours(setFulfillmentByAmazonFeed)) {
                Validation.addError("", "未匹配到合法的 Feed, 请重新上架!");
            }
        } else {
            Validation.addError("", "Feed 不完整, 请重新上架!!");
        }
        if(Validation.hasErrors()) return;
        saleAmazonFeed.submit(this.saleAmazonParams());
        assignAmazonListingPriceFeed.submit(this.assignAmazonListingPriceParams());
        setFulfillmentByAmazonFeed.submit(this.setFulfillmentByAmazonParams());
    }

    public static Selling querySellingByAPI(String sku, M market, Long accountId) {
        List<Selling> sellings = Selling.find("merchantSKU=? AND market=? AND account.id=?", sku, market, accountId)
                .fetch();
        if(sellings.size() > 0) {
            return sellings.get(0);
        } else {
            return null;
        }
    }

    public static String esSellingId(String sku, M market, Account account) {
        List<Selling> sellings = Selling.find("product.sku=? AND market=? AND account.id=?",
                sku, market, account.id).fetch();
        if(sellings.size() > 0) {
            return sellings.get(0).sellingId.replace("|", "").replace(",", "").replace("-", "");
        }
        return "";
    }
}
