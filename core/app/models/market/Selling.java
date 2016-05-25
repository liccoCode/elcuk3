package models.market;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import controllers.Login;
import helper.*;
import jobs.analyze.SellingSaleAnalyzeJob;
import models.ElcukRecord;
import models.embedded.AmazonProps;
import models.procure.ProcureUnit;
import models.product.Attach;
import models.product.Product;
import models.view.dto.AnalyzeDTO;
import models.view.post.SellingAmzPost;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.http.NameValuePair;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;
import play.libs.Codec;
import play.libs.F;
import play.libs.IO;
import play.libs.Time;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 已经正在进行销售的对象抽象
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:48 AM
 */
@javax.persistence.Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Selling extends GenericModel {

    private static final long serialVersionUID = -4124213853478159984L;

    public Selling() {
        this.aps = new AmazonProps();
        this.state = S.NEW;
        this.createDate = new Date();
    }

    /*
     * Selling 的状态
     */
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

    /*
     * Selling 的生命周期
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
    public Listing listing;

    @OneToMany(mappedBy = "selling", fetch = FetchType.LAZY)
    public List<SellingQTY> qtys = new ArrayList<SellingQTY>();

    /**
     * 上架后用来唯一标示这个 Selling 的 Id;
     * sellingId: msku|market.nickName|acc.id
     */
    @Id
    @Column(length = 70)
    @Expose
    public String sellingId;


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
    // ---- Images ????

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
     * TODO: 此代码性质为临时代码， 等到 Amazon 那边不再产生旧 Selling 的订单时可移除此代码
     */
    private static final Map<String, String> SELLING_MAPPING = new HashMap<String, String>();

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

    /**
     * 这个 Selling 所属的哪一个用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    public Account account;

    /**
     * 用来修复 Selling 关联的 Listing 错误的问题.
     */
    public Selling changeListing(Listing listing) {
        String sku = Product.merchantSKUtoSKU(this.merchantSKU);
        if(listing.listingId.equals(this.listing.listingId)) Webs.error("Listing 是一样的, 不需要更改");
        if(!sku.equals(listing.product.sku)) Webs.error("不可以切换到不同的 SKU");
        this.listing = listing;
        this.asin = listing.asin;
        this.market = listing.market;
        return this.save();
    }

    /**
     * 从 amazon 将数据同步回来
     * TODO 这个功能期望取消, 因为通过 MWS API 后应该要消除上架成功但系统却脱离管理的问题.
     */
    public void syncFromAmazon() {
        String html = "";
        String fnskuhtml = "";
        synchronized(this.account.cookieStore()) {
            checkAmazonLogin();
            // 1. 切换 Selling 所在区域
            if(!this.market.toString().equals("AMAZON_JP"))
                this.account.changeRegion(this.market); // 跳转到对应的渠道,不然会更新成不同的市场

            // 2. 获取修改 Selling 的页面, 获取参数
            html = HTTP.get(this.account.cookieStore(), M.listingEditPage(this));
            if(StringUtils.isBlank(html))
                throw new FastRuntimeException(String.format("Visit %s page is empty.", M.listingEditPage(this)));
            if(Play.mode.isDev()) {
                IO.writeContent(html,
                        new File(String.format("%s/%s_%s.html", Constant.E_DATE, this.merchantSKU, this.asin)));
            }
            // 获取Fnsku
            fnskuhtml = HTTP.get(this.account.cookieStore(), this.market.listingfnSkuPage(this));
            this.account.changeRegion(this.account.type);
        }
        // 3. 将需要的参数同步进来
        this.aps.syncPropFromAmazonPostPage(html, this);
        String fnsku = this.aps.syncfnSkuFromAmazonPostPage(fnskuhtml, this);
        if(StringUtils.isNotBlank(fnsku)) {
            this.fnSku = fnsku;
        }
        this.save();
        //4.通过AMAZON,API形式同步数据回数据库
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("acc_id", this.account.id.toString()));
        params.add(new BasicNameValuePair("asin", this.asin));
        params.add(new BasicNameValuePair("market_id", this.market.name()));
        params.add(new BasicNameValuePair("selling_id", this.sellingId));
        params.add(new BasicNameValuePair("user_name", Login.current().username));
        HTTP.post(System.getenv(Constant.ROCKEND_HOST) + "/amazon_product_sync_back", params);
        this.save();
    }


    public void syncAndUpdateAmazon(SellingAmzPost p) {
        try {
            this.uploadFeedToAmazonForProduct(p);
            new ElcukRecord("selling.update", "执行API操作", this.sellingId).save();
        } catch(Exception e) {
            throw new FastRuntimeException("提交AMAZOM feed错误, Error:" + e.toString());
        }
    }


    public byte[] downloadFnSkuLabel() {
        if(StringUtils.isBlank(this.fnSku))
            throw new FastRuntimeException("Selling " + this.sellingId + " 没有 FnSku 无法下载最新的 Label.");
        synchronized(this.account.cookieStore()) {
            Map<String, String> params = GTs.MapBuilder
                    .map("labelType", "ItemLabel_A4_27")
                    .put("mSku.0", this.merchantSKU)
                    .put("qty.0", "27") // 一页打 44 个
                    .put("fnSku.0", this.fnSku).build();
            for(Cookie coo : this.account.cookieStore().getCookies()) {
                Logger.info(" ============" + coo.getName() + "=" + coo.getValue() + "============");
            }
            return HTTP.postDown(this.account.cookieStore(), this.account.type.fnSkuDownloadLink(),
                    Arrays.asList(new BasicNameValuePair("model", J.json(params))));
        }
    }

    public List<NameValuePair> submitJobParams(Feed feed) {
        Validate.notNull(feed);
        Validate.notNull(this.account);
        Validate.notNull(this.market);
        Validate.notEmpty(this.sellingId);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("account_id", this.account.id.toString()));// 使用哪一个账号
        params.add(new BasicNameValuePair("market", this.market.name()));// 向哪一个市场
        params.add(new BasicNameValuePair("feed_id", feed.id.toString()));// 提交哪一个 Feed ?
        params.add(new BasicNameValuePair("selling_id", this.sellingId)); // 作用与哪一个 Selling
        params.add(new BasicNameValuePair("feed_type", MWSUtils.T.PRODUCT_FEED.toString())); // 作用与哪一个 Selling
        return params;
    }

    public Feed deploy() {
        if(!Feed.isFeedAvalible(this.account.id)) Webs.error("已经超过 Feed 的提交频率, 请等待 2 ~ 5 分钟后再提交.");
        this.aps.arryParamSetUP(AmazonProps.T.STR_TO_ARRAY);//将数组参数转换成字符串再进行处理
        this.aps.quantity = null;//设置更新时将库存参数去除（使用 PartialUpdate 更新时不能存在此参数）
        String content = Selling
                .generateFeedTemplateFile(Lists.newArrayList(this), this.aps.templateType, this.market.toString(),
                        "PartialUpdate");
        Feed feed = Feed.updateSellingFeed(content, this);
        List<NameValuePair> params = this.submitJobParams(feed);
        params.add(new BasicNameValuePair("action", "update"));
        HTTP.post(System.getenv(Constant.ROCKEND_HOST) + "/submit_feed", params);
        return feed;
    }

    /**
     * 用Feed方式更新产品图片
     */
    public void uploadFeedAmazonImg(String imageName, boolean waterMark, String userName) {
        //if(!Feed.isFeedAvalible(this.account.id)) Webs.error("已经超过 Feed 的提交频率, 请等待 2 ~ 5 分钟后再提交.");
        String dealImageNames = imageName;
        if(StringUtils.isBlank(imageName)) dealImageNames = this.aps.imageName;
        if(StringUtils.isBlank(dealImageNames)) throw new FastRuntimeException("此 Selling 没有指定图片.");
        String[] images = StringUtils.splitByWholeSeparator(dealImageNames, Webs.SPLIT);
        if(images.length >= 9)  // 如果有更多的图片,仅仅使用前 9 张, 并且也只存储 9 张图片的名字
            images = Arrays.copyOfRange(images, 0, 9);
        this.aps.imageName = StringUtils.join(images, Webs.SPLIT);

        String xml = MWSUtils.buildProductImageBySelling(this, images);
        Feed feed = Feed.updateSellingFeed(xml, this);
        String feed_submission_id = MWSUtils.submitFeedByXML(feed, MWSUtils.T.PRODUCT_IMAGES_FEED, null, this.account);
        Logger.info(feed_submission_id);
        List<NameValuePair> params = this.submitGetFeedParams(feed, feed_submission_id);
        HTTP.post(System.getenv(Constant.ROCKEND_HOST) + "/amazon_get_feed", params);
        this.save();
    }


    /**
     * 通过 Product 上架页面提交的信息, 使用 UPC 代替 ASIN, 等待 ASIN 被成功填充, 再更新 asin 为具体的 Asin 值
     *
     * @return
     */
    public Selling buildFromProduct() {
        if(this.account == null) Webs.error("上架账户不能为空");
        if(!Feed.isFeedAvalible(this.account.id)) Webs.error("已经超过 Feed 的提交频率, 请等待 2 ~ 5 分钟后再提交.");
        // 以 Amazon 的 Template File 所必须要的值为准
        if(StringUtils.isBlank(this.aps.upc)) Webs.error("UPC 必须填写");
        if(this.market == null) Webs.error("Market 不能为空");
        if(this.aps.upc.length() != 12) Webs.error("UPC 的格式错误,其为 12 位数字");
        if(!isMSkuValid()) Webs.error("Merchant SKU 不合法. [SKU],[UPC]");
        if(StringUtils.isBlank(this.aps.title)) Webs.error("Selling Title 必须存在");
        if(StringUtils.isBlank(this.aps.brand)) Webs.error("品牌必须填写");
        if(StringUtils.isBlank(this.aps.manufacturer)) Webs.error("Manufacturer 必须填写");
        if(StringUtils.isBlank(this.aps.manufacturerPartNumber)) Webs.error("Part Number 需要填写");
        if(this.aps.rbns == null || this.aps.rbns.size() == 0) Webs.error("Recommanded Browser Nodes 必须填写");
        if(StringUtils.isBlank(this.aps.feedProductType)) Webs.error("Feed Product Type 必须填写");
        if(this.aps.standerPrice == null || this.aps.standerPrice <= 0) Webs.error("标准价格必须大于 0");
        if(this.aps.salePrice == null || this.aps.salePrice <= 0) Webs.error("优惠价格必须大于 0");
        this.asin = this.aps.upc;
        patchToListing();

        Feed saleAmazonBasicFeed = Feed.newSellingFeed(MWSUtils.toSaleAmazonXml(this), this);
        Feed assignPriceFeed = Feed.newAssignPriceFeed(MWSUtils.assignPriceXml(this), this);

        List<NameValuePair> params = this.submitJobParams(saleAmazonBasicFeed);
        params.add(new BasicNameValuePair("type", "CreateListing"));
        params.add(new BasicNameValuePair("next_type", "AssignPrice"));
        params.add(new BasicNameValuePair("next_feed_id", assignPriceFeed.id.toString()));
        params.add(new BasicNameValuePair("next_feed_type", MWSUtils.T.PRICING_FEED.toString()));

        HTTP.post(System.getenv(Constant.ROCKEND_HOST) + "/amazon_submit_feed", params);
        return this;
    }

    /**
     * 用于修补通过 Product 上架没有获取到 ASIN 没有进入系统的 Selling.
     */
    public Selling patchToListing() {
        if(Selling.exist(this.sid())) Webs.error(String.format("Selling[%s] 已经存在", this.sellingId));
        Product product = Product.findByMerchantSKU(this.merchantSKU);
        if(product == null) Webs.error("SKU 产品不存在");

        List<Attach> images = Attach.attaches(product.sku, Attach.P.SKU.name());
        if(images != null && images.size() != 0) {
            this.aps.imageName = images.get(0).fileName;
        }

        Listing lst = Listing.findById(Listing.lid(this.asin, this.market));
        if(lst == null) {
            lst = Listing.blankListing(asin, market, product).save();
            lst.recordingListingState(DateTime.now().toDate());
        }
        this.listing = lst;
        this.aps.arryParamSetUP(AmazonProps.T.ARRAY_TO_STR);
        if(!Selling.exist(this.sid()))
            return this.save();
        else
            throw new FastRuntimeException("Selling 已经存在！");
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
     * @param msku
     * @return
     */
    public static F.T2<List<Selling>, List<String>> sameFamilySellings(String msku) {
        List<Selling> sellings = Selling
                .find("listing.product.family=?", Product.findByMerchantSKU(msku).family).fetch();
        List<String> sids = new ArrayList<String>();
        for(Selling s : sellings) sids.add(s.sellingId);
        return new F.T2<List<Selling>, List<String>>(sellings, sids);
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
        Listing listing = this.listing;
        ListingStateRecord record = ListingStateRecord.find("listing.listingId = ? AND state = ? " +
                " ORDER BY changedDate DESC", listing.listingId, ListingStateRecord.S.DOWN).first();
        if(record != null)
            return record.changedDate;
        return null;
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

    public static Selling blankSelling(String msku, String asin, String upc, Account acc, M market) {
        Selling selling = new Selling();
        selling.account = acc;
        selling.merchantSKU = msku;
        selling.asin = asin;
        selling.aps.upc = upc;
        selling.market = market;
        selling.sid();
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
     * 生成Selling对象的Feed文件
     *
     * @param sellingList  List
     * @param templateType String
     * @param market       String
     * @param action       String
     * @return String 生成的模板数据
     * 注意：模板文件保存的文件名格式为：Flat.File.templateType.market.txt
     */
    public static String generateFeedTemplateFile(List<Selling> sellingList, String templateType, String market,
                                                  String action) {
        Map args = GTs.newMap("sellingList", sellingList).build();
        args.put("action", action);
        return GTs.render(String.format("Flat.File.%s.%s", templateType, market), args);
    }


    public static String generateUpdateFeedTemplateFile(List<Selling> sellingList, String templateType, String market) {
        // update
        return generateFeedTemplateFile(sellingList, templateType, market, "Update");
    }

    /**
     * 根据传入的 ListingId 集合查找出对应的 SellingId 集合
     *
     * @param listingIds
     * @return
     */
    public static List<String> getSellingIds(List<String> listingIds) {
        List<String> sellingIds = new ArrayList<String>();
        List<Map<String, Object>> rows = null;

        if(listingIds != null && listingIds.size() > 0) {
            SqlSelect sql = new SqlSelect().select("sellingId").from("Selling").where("state != 'DOWN'")
                    .andWhere(SqlSelect.whereIn("listing_listingId", listingIds));
            rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        }
        for(Map<String, Object> row : rows) {
            sellingIds.add(row.get("sellingId").toString());
        }
        return sellingIds;
    }

    /**
     * 查找传入的 SKU 下所有的 SellingId
     *
     * @return
     */
    public static List<String> sids(List<String> skus) {
        List<String> listings = new ArrayList<String>();
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
        String msku = String.format("%s,%s", sku.trim(), upc.trim());
        Selling newSelling = Selling.blankSelling(msku, asin, upc, acc, market);
        newSelling.patchToListing();
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
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("asin", this.asin));
        params.add(new BasicNameValuePair("sku", Codec.encodeBASE64(this.merchantSKU)));
        Map<String, F.T2<String, BufferedInputStream>> uploadImages = new HashMap<String, F.T2<String, BufferedInputStream>>();
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
            checkAmazonLogin();
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
            //		{"imageUrl":"https://media-service-eu.amazon.com/media/M3SRIZRCNL2O1K+maxw=110+maxh=110","status":"success"}</div>
            //		{"errorMessage":"We are sorry. There are no file(s) specified or the file(s) specified appear to be empty.","status":"failure"}</div>
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
            this.account.changeRegion(this.account.type);
        }
        this.save();
    }


    /**
     * 检测是否能正常登陆amazon
     */
    public void checkAmazonLogin() {
        // 1. 切换 Selling 所在区域
        this.account.changeRegion(this.market); // 跳转到对应的渠道,不然会更新成不同的市场
        // 2. 获取修改 Selling 的页面, 获取参数
        String html = HTTP.get(this.account.cookieStore(), M.listingEditPage(this));
        Document doc = Jsoup.parse(html);
        // ----- Input 框框
        Elements inputs = doc.select("form[name=productForm] input");
        if(inputs.size() == 0) {
            this.account.loginAmazonSellerCenter();
            this.account.changeRegion(this.market);
        }
    }

    public List<NameValuePair> submitGetFeedParams(Feed feed, String feed_submission_id) {
        Validate.notNull(feed);
        Validate.notNull(this.account);
        Validate.notNull(this.market);
        Validate.notNull(feed_submission_id);
        Validate.notEmpty(this.sellingId);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("account_id", this.account.id.toString()));// 使用哪一个账号
        params.add(new BasicNameValuePair("market", this.market.name()));// 向哪一个市场
        params.add(new BasicNameValuePair("feed_id", feed.id.toString()));// 提交哪一个 Feed ?
        params.add(new BasicNameValuePair("feed_submission_id", feed_submission_id));
        return params;
    }


    public void uploadFeedToAmazonForProduct(SellingAmzPost p) throws Exception {
        this.account = Account.findById(this.account.id);
        if(p.rbns || p.productvolume || p.productWeight || p.weight || p.title || p.keyfeturess || p.searchtermss ||
                p.productdesc) {
            String xml = MWSUtils.buildProductXMLBySelling(this, p);
            Feed feed = Feed.updateSellingFeed(xml, this);
            String feed_submission_id = MWSUtils.submitFeedByXML(feed, MWSUtils.T.PRODUCT_FEED, null, this.account);
            Logger.info(feed_submission_id);
            List<NameValuePair> productParams = this.submitGetFeedParams(feed, feed_submission_id);
            String temp = HTTP.post("http://" + models.OperatorConfig.getVal("rockendurl") + ":4567/amazon_get_feed",
                    productParams);
        }

        if(p.standerprice || p.saleprice) {
            String xml = MWSUtils.buildPriceXMLBySelling(this, p);
            Feed price_feed = Feed.newAssignPriceFeed(xml, this);
            String feed_submission_id = MWSUtils
                    .submitFeedByXML(price_feed, MWSUtils.T.PRICING_FEED, null, this.account);
            Logger.info(feed_submission_id);
            List<NameValuePair> priceParams = this.submitGetFeedParams(price_feed, feed_submission_id);
            String temp = HTTP.post("http://" + models.OperatorConfig.getVal("rockendurl") + ":4567/amazon_get_feed",
                    priceParams);
        }
    }
}