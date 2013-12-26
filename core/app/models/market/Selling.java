package models.market;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import helper.Constant;
import helper.GTs;
import helper.HTTP;
import helper.Webs;
import jobs.analyze.SellingSaleAnalyzeJob;
import jobs.driver.GJob;
import jobs.perform.SubmitFeedJob;
import models.embedded.AmazonProps;
import models.product.Attach;
import models.product.Product;
import models.view.dto.AnalyzeDTO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.http.message.BasicNameValuePair;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.libs.IO;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.io.File;
import java.util.*;

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

    // -----------------------  Amazon 上架会需要使用到的信息 ----------------------------
    @Embedded
    @Expose
    public AmazonProps aps;
    // ---- Images ????

    // -------------------------- ebay 上架使用的信息 TBD ---------------------


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
        synchronized(this.account.cookieStore()) {
            // 1. 切换 Selling 所在区域
            this.account.changeRegion(this.market); // 跳转到对应的渠道,不然会更新成不同的市场

            // 2. 获取修改 Selling 的页面, 获取参数
            html = HTTP.get(this.account.cookieStore(), M.listingEditPage(this));
            if(StringUtils.isBlank(html))
                throw new FastRuntimeException(String.format("Visit %s page is empty.", M.listingEditPage(this)));
            if(Play.mode.isDev()) {
                IO.writeContent(html,
                        new File(String.format("%s/%s_%s.html", Constant.E_DATE, this.merchantSKU, this.asin)));
            }
            this.account.changeRegion(this.account.type);
        }
        // 3. 将需要的参数同步进来
        this.aps.syncPropFromAmazonPostPage(html, this);
        this.save();
    }

    public byte[] downloadFnSkuLabel() {
        if(StringUtils.isBlank(this.fnSku))
            throw new FastRuntimeException("Selling " + this.sellingId + " 没有 FnSku 无法下载最新的 Label.");

        synchronized(this.account.cookieStore()) {
            return HTTP.postDown(this.account.cookieStore(), this.account.type.fnSkuDownloadLink(),
                    Arrays.asList(
                            new BasicNameValuePair("qty.0", "27"), // 一页打 44 个
                            new BasicNameValuePair("fnSku.0", this.fnSku),
                            new BasicNameValuePair("mSku.0", this.merchantSKU),
                            new BasicNameValuePair("labelType", "ItemLabel_A4_27")
                    ));
        }
    }

    public Map<String, Object> submitJobParams(Feed feed) {
        Validate.notNull(feed);
        Validate.notNull(this.account);
        Validate.notNull(this.market);
        Validate.notEmpty(this.sellingId);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("account.id", this.account.id); // 使用哪一个账号
        args.put("marketId", this.market.amid().name()); // 向哪一个市场
        args.put("feed.id", feed.id); // 提交哪一个 Feed ?
        args.put("selling.id", this.sellingId); // 作用与哪一个 Selling
        return args;
    }

    public Feed deploy() {
        if(!Feed.isFeedAvalible()) Webs.error("已经超过 Feed 的提交频率, 请等待 2 ~ 5 分钟后再提交.");
        this.aps.arryParamSetUP(AmazonProps.T.STR_TO_ARRAY);//将数组参数转换成字符串再进行处理
        String content = Selling.generateFeedTemplateFile(Lists.newArrayList(this), this.aps.templateType, this.market.toString());
        Feed feed = Feed.updateSellingFeed(content, this);
        Map<String, Object> args = this.submitJobParams(feed);
        args.put("action", "update");
        GJob.perform(SubmitFeedJob.class, args);
        return feed;
    }

    /**
     * 通过 Product 上架页面提交的信息, 使用 UPC 代替 ASIN, 等待 ASIN 被成功填充, 再更新 asin 为具体的 Asin 值
     *
     * @return
     */
    public Selling buildFromProduct() {
        if(!Feed.isFeedAvalible()) Webs.error("已经超过 Feed 的提交频率, 请等待 2 ~ 5 分钟后再提交.");
        // 以 Amazon 的 Template File 所必须要的值为准
        if(StringUtils.isBlank(this.aps.upc)) Webs.error("UPC 必须填写");
        if(this.aps.upc.length() != 12) Webs.error("UPC 的格式错误,其为 12 位数字");
        if(!isMSkuValid()) Webs.error("Merchant SKU 不合法. [SKU],[UPC]");
        if(StringUtils.isBlank(this.aps.title)) Webs.error("Selling Title 必须存在");
        if(StringUtils.isBlank(this.aps.brand)) Webs.error("品牌必须填写");
        if(StringUtils.isBlank(this.aps.manufacturer)) Webs.error("Manufacturer 必须填写");
        if(StringUtils.isBlank(this.aps.manufacturerPartNumber)) Webs.error("Part Number 需要填写");
        if(this.aps.rbns == null || this.aps.rbns.size() == 0) Webs.error("Recommanded Browser Nodes 必须填写");
        if(StringUtils.isBlank(this.aps.feedProductType)) Webs.error("所属模板的 Product Type 必须填写");
        if(this.aps.standerPrice == null || this.aps.standerPrice <= 0) Webs.error("标准价格必须大于 0");
        if(this.aps.salePrice == null || this.aps.salePrice <= 0) Webs.error("优惠价格必须大于 0");
        this.asin = this.aps.upc;
        patchToListing();
        Feed feed = Feed.newSellingFeed(Selling.generateFeedTemplateFile(Lists.newArrayList(this), this.aps.templateType, this.market.toString()), this);
        GJob.perform(SubmitFeedJob.class, this.submitJobParams(feed));
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
        if(images == null || images.size() == 0) Webs.error("请添加 " + product.sku + " 并上传其图片后再处理 Selling.");
        this.aps.imageName = images.get(0).fileName;

        Listing lst = Listing.findById(Listing.lid(this.asin, this.market));
        if(lst == null) lst = Listing.blankListing(asin, market, product).save();
        this.listing = lst;
        this.aps.arryParamSetUP(AmazonProps.T.ARRAY_TO_STR);
        return this.save();
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
                Date expireTime = Cache.get(SellingSaleAnalyzeJob.AnalyzeDTO_SID_CACHE + ".time", Date.class);
                long diffSecond = (expireTime.getTime() - System.currentTimeMillis()) / 1000;
                Cache.set(SellingSaleAnalyzeJob.AnalyzeDTO_SID_CACHE, dtos, diffSecond + "s");
            }
        }
        return this.save();
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
     * 生成Selling对象的Feed文件
     *
     * @param sellingList List
     * @param templateType String
     * @param market String
     * @return String 生成的模板数据
     * 注意：模板文件保存的文件名格式为：Flat.File.templateType.market.txt
     */
    public static String generateFeedTemplateFile(List<Selling> sellingList, String templateType, String market) {
        return GTs.render(String.format("Flat.File.%s.%s", templateType, market), GTs.newMap("sellingList", sellingList).build());
    }
}
