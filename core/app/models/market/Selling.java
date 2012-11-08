package models.market;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import helper.*;
import models.embedded.AmazonProps;
import models.product.Attach;
import models.product.Product;
import models.product.Whouse;
import models.view.dto.AnalyzeDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.libs.Codec;
import play.libs.F;
import play.libs.IO;
import play.utils.FastRuntimeException;

import javax.persistence.*;
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
public class Selling extends GenericModel {

    public Selling() {
        this.aps = new AmazonProps();
        this.state = S.NEW;
    }

    /**
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
         * 由于没有库存已经自动下架
         */
        NO_INVENTORY,
        /**
         * 手动进行暂停销售, 根据不同网站的规则或者情况达到暂停销售的状态
         */
        HOlD,
        /**
         * 完全下架, 如果可以还能够重新上架
         */
        DOWN
    }

    /**
     * Selling 的类型
     */
    public enum T {
        AMAZON,
        FBA,
        EBAY
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Listing listing;

    @OneToMany(mappedBy = "selling", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    public List<SellingQTY> qtys;

    @OneToOne(cascade = CascadeType.ALL)
    @Expose
    public PriceStrategy priceStrategy;

    /**
     * 上架后用来唯一标示这个 Selling 的 Id;
     * sellingId: msku|market.nickName|acc.id
     */
    @Id
    @Column(length = 70)
    @Expose
    public String sellingId;


    /**
     * 1. 在 Amazon 上架的唯一的 merchantSKU;
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

    @Enumerated(EnumType.STRING)
    @Expose
    public T type;


    /**
     * 给这个 Selling 人工设置的 PS 值
     */
    public Float ps = 0f;

    @Expose
    public Float price = 0f;

    @Expose
    public Float shippingPrice = 0f;

    /**
     * 使用 "," 分隔的, 与此 Selling 对应市场的 ASIN, 当有多个 ASIN 的时候,用来追踪最低价格
     */
    @Expose
    public String priceMatchAsin;

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
    @ManyToOne
    public Account account;

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
        Map<String, File> uploadImages = new HashMap<String, File>();
        for(int i = 0; i < images.length; i++) {
            String fileParamName;
            if(i == 0) fileParamName = "MAIN";
            else fileParamName = "PT0" + i;
            Attach attch = Attach.findByFileName(images[i]);
            if(attch == null) throw new FastRuntimeException("填写的图片名称(" + images[i] + ")不存在! 请重新上传.");
            if(waterMark) {
                // TODO 如果需要打水印, 在这里处理
                throw new UnsupportedOperationException("功能还没实现.");
            } else {
                uploadImages.put(fileParamName, new File(attch.location));
            }
            usedAmazonFileName.get(fileParamName).set(true);
        }
        synchronized(this.account.cookieStore()) {
            this.account.changeRegion(this.market); // 切换到这个 Selling 的市场
            Logger.info("Upload Picture to Amazon AND Synchronized[%s].", this.account.prettyName());
            String body = HTTP.upload(this.account.cookieStore(), this.account.type.uploadImageLink(), params, uploadImages);
            if(Play.mode.isDev())
                FLog.fileLog(String.format("%s.%s.html", this.sellingId, this.account.id), body, FLog.T.IMGUPLOAD);
            JsonObject imgRsp = new JsonParser().parse(Jsoup.parse(body).select("#jsontransport").text()).getAsJsonObject();
            //		{"imageUrl":"https://media-service-eu.amazon.com/media/M3SRIZRCNL2O1K+maxw=110+maxh=110","status":"success"}</div>
            //		{"errorMessage":"We are sorry. There are no file(s) specified or the file(s) specified appear to be empty.","status":"failure"}</div>
            if("failure".equals(imgRsp.get("status").getAsString())) {
                Logger.info("Upload Picture to Amazon Failed.(%s)", imgRsp.get("errorMessage").getAsString());
                throw new FastRuntimeException(imgRsp.get("errorMessage").getAsString());
            } else {
                Logger.info("Upload Picture to Amazon Success.(%s)", imgRsp.get("imageUrl").getAsString());
            }
            //https://catalog-sc.amazon.de/abis/image/RemoveImage.ajax?asin=B0083QX8AW&variant=MAIN/PT01/...
            for(String fileName : usedAmazonFileName.keySet()) {
                if(usedAmazonFileName.get(fileName).get()) continue; // 使用过了就不处理
                HTTP.post(this.account.cookieStore(), this.account.type.removeImageLink(), Arrays.asList(
                        new BasicNameValuePair("asin", this.asin),
                        new BasicNameValuePair("variant", fileName)
                ));
            }
            this.account.changeRegion(this.account.type);
        }
        this.save();
    }

    /**
     * 从 amazon 将数据同步回来
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
            if(Play.mode.isDev())
                IO.writeContent(html, new File(String.format("%s/%s_%s.html", Constant.E_DATE, this.merchantSKU, this.asin)));
            this.account.changeRegion(this.account.type);
        }
        // 3. 将需要的参数同步进来
        this.aps.syncPropFromAmazonPostPage(html, this);
        this.type = T.AMAZON;
        // 做修复, 当数据从 Amazon 同步回数据以后, 已经拥有 PriceStrategy 所需要的信息了
        if(this.priceStrategy == null) this.priceStrategy = new PriceStrategy(this);
        this.save();
    }

    public byte[] downloadFnSkuLabel() {
        if(StringUtils.isBlank(this.fnSku))
            throw new FastRuntimeException("Selling " + this.sellingId + " 没有 FnSku 无法下载最新的 Label.");
        synchronized(this.account.cookieStore()) {
            return HTTP.postDown(this.account.cookieStore(), this.account.type.fnSkuDownloadLink(), Arrays.asList(
                    new BasicNameValuePair("qty.0", "27"), // 一页打 44 个
                    new BasicNameValuePair("fnSku.0", this.fnSku),
                    new BasicNameValuePair("mSku.0", this.merchantSKU),
                    new BasicNameValuePair("labelType", "ItemLabel_A4_27")
            ));
        }
    }

    /**
     * <pre>
     * 将传入的 Selling 的数据更新到 渠道上并且更新数据库;
     * PS:
     *  - 请确保 Selling 中的信息是正确的, 这个方法仅仅根据对应的参数做提交操作, 不再验证数据!
     *  - 此方法进行了 synchronized, 因为更新的时候需要将其使用的 Cookie 给锁住, 不能进行更换
     * 更新:
     * 1. price
     * 2. salePrice, startDate, endDate
     *  --- price, salePrice 会根据 Amazon 检查, 仅保留小数点后两位
     * 3. productDescription
     *  --- 检查字符串最多 2000 个
     * 4. searchTerms[1~5]
     *  --- 检查每一行最多 50 个
     * 5. browse_nodes[2]
     * 6. manufacturer: manufact
     * 7. item_name: title
     * 8. part_number: manufactuerPartNumber
     * 9. quantity
     * 10. 等待添加
     * </pre>
     *
     * @throws play.utils.FastRuntimeException
     *          deploy 方法失败会抛出异常
     */
    public void deploy() {
        this.aps.arryParamSetUP(AmazonProps.T.ARRAY_TO_STR);//将数组参数转换成字符串再进行处理
        synchronized(this.account.cookieStore()) { // 锁住这个 Account 的 CookieStore
            switch(this.market) {
                case AMAZON_DE:
                case AMAZON_ES:
                case AMAZON_FR:
                case AMAZON_IT:
                case AMAZON_UK:
                case AMAZON_US:
                    // 1. 切换 Selling 所在区域
                    this.account.changeRegion(this.market); // 跳转到对应的渠道,不然会更新成不同的市场

                    // 2. 设置需要提交的值
                    String html = HTTP.get(this.account.cookieStore(), M.listingEditPage(this));
                    F.T2<Collection<NameValuePair>, Document> paramAndDocTuple = this.aps.generateDeployAmazonProps(html, this);

                    // 3. 提交
                    String[] args = StringUtils.split(paramAndDocTuple._2.select("form[name=productForm]").first().attr("action"), ";");
                    html = HTTP.post(this.account.cookieStore(),
                            M.listingPostPage(this.account.type/*更新的链接需要账号所在地的 URL*/, (args.length >= 2 ? args[1] : "")),
                            paramAndDocTuple._1);
                    if(StringUtils.isBlank(html)) // 这个最先检查
                        throw new FastRuntimeException("Selling update is failed! Return Content is Empty!");
                    if(Play.mode.isDev())
                        IO.writeContent(html, new File(String.format("%s/%s_%s_posted.html", Constant.E_DATE, this.merchantSKU, this.asin)));
                    Document doc = Jsoup.parse(html);
                    Elements error = doc.select(".messageboxerror li");
                    if(error.size() > 0)
                        throw new FastRuntimeException("Error:" + error.text());

                    // 4. 更新回数据库
                    this.save();

                    // 还原
                    this.account.changeRegion(this.account.type);
                    break;
                case EBAY_UK:
                    break;
            }
        }
    }

    /**
     * 指定一个 Whouse, 加载出此 Selling 在此仓库中的唯一的库存
     *
     * @param whouse
     * @return
     */
    public SellingQTY uniqueQTY(Whouse whouse) {
        return SellingQTY.findById(String.format("%s_%s", this.merchantSKU.toUpperCase(), whouse.id));
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
        List<AnalyzeDTO> dtos = Cache.get(AnalyzeDTO.analyzesKey("sid"), List.class);
        if(dtos != null) {
            boolean find = false;
            for(AnalyzeDTO dto : dtos) {
                if(!dto.fid.equals(this.sellingId)) continue;
                dto.ps = ps;
                find = true;
            }
            if(!find) throw new FastRuntimeException(String.format("更新失败, %s 不在缓存中..", this.sellingId));
        }
        return this.save();
    }

    /**
     * 将当前对象的值复制到老的 Selling 对象中去
     *
     * @param newSelling
     * @return 返回更新后的
     */
    public Selling updateAttr(Selling newSelling) {
        if(StringUtils.isNotBlank(newSelling.aps.title)) this.aps.title = newSelling.aps.title;
        if(StringUtils.isNotBlank(newSelling.aps.modelNumber)) this.aps.modelNumber = newSelling.aps.modelNumber;
        if(StringUtils.isNotBlank(newSelling.aps.manufacturer)) this.aps.manufacturer = newSelling.aps.manufacturer;
        if(StringUtils.isNotBlank(newSelling.aps.keyFetures)) this.aps.keyFetures = newSelling.aps.keyFetures;
        if(StringUtils.isNotBlank(this.aps.RBN)) this.aps.RBN = newSelling.aps.RBN;
        if(StringUtils.isNotBlank(this.aps.manufacturerPartNumber))
            this.aps.manufacturerPartNumber = newSelling.aps.manufacturerPartNumber;
        if(StringUtils.isNotBlank(this.aps.condition_)) this.aps.condition_ = newSelling.aps.condition_;
        if(newSelling.aps.standerPrice != null && newSelling.aps.standerPrice > 0)
            this.aps.standerPrice = newSelling.aps.standerPrice;
        if(newSelling.aps.salePrice != null && newSelling.aps.salePrice > 0)
            this.aps.salePrice = newSelling.aps.salePrice;
        if(newSelling.aps.startDate != null) this.aps.startDate = newSelling.aps.startDate;
        if(newSelling.aps.endDate != null) this.aps.endDate = newSelling.aps.endDate;
        if(StringUtils.isNotBlank(newSelling.aps.legalDisclaimerDesc))
            this.aps.legalDisclaimerDesc = newSelling.aps.legalDisclaimerDesc;
        if(StringUtils.isNotBlank(this.aps.sellerWarrantyDesc))
            this.aps.sellerWarrantyDesc = newSelling.aps.sellerWarrantyDesc;

        if(StringUtils.isNotBlank(this.aps.productDesc)) this.aps.productDesc = newSelling.aps.productDesc;
        if(StringUtils.isNotBlank(this.aps.searchTerms)) this.aps.searchTerms = newSelling.aps.searchTerms;
        if(StringUtils.isNotBlank(this.aps.platinumKeywords))
            this.aps.platinumKeywords = newSelling.aps.platinumKeywords;

        return this.save();
    }

    /**
     * 加载指定 Product 所属的 Family 下的所有 Selling 与 SellingId
     *
     * @param msku
     * @return
     */
    public static F.T2<List<Selling>, List<String>> sameFamilySellings(String msku) {
        List<Selling> sellings = Selling.find("listing.product.family=?", Product.findByMerchantSKU(msku).family).fetch();
        List<String> sids = new ArrayList<String>();
        for(Selling s : sellings) sids.add(s.sellingId);
        return new F.T2<List<Selling>, List<String>>(sellings, sids);
    }

    /**
     * 返回所有 Selling 的 sid
     *
     * @return
     */
    public static List<String> allSID() {
        return allSid(false);
    }

    /**
     * 返回所有 Selling 的 SID 提供是否过滤掉一些市场提出的不合法元素的选择
     *
     * @param filter 是否过滤
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<String> allSid(boolean filter) {
        String cacheKey = "selling.allsid";
        List<String> sids = Cache.get(cacheKey, List.class);
        if(sids == null) {
            synchronized(Selling.class) {
                sids = Cache.get(cacheKey, List.class);
                if(sids != null) return sids;

                sids = new ArrayList<String>();
                List<Selling> sellings = Selling.all().fetch();
                for(Selling s : sellings) sids.add(s.sellingId);

                // 是否需要过滤掉一些不合法元素
                if(filter) {
                    CollectionUtils.filter(sids, new Predicate() {
                        @Override
                        public boolean evaluate(Object o) {
                            String msg = o.toString();
                            String[] args = StringUtils.split(msg, Webs.S);
                            String[] mskuArr = StringUtils.split(args[0], ",");
                            if(StringUtils.contains(msg, "A_UK|2")) // A_UK 市场不允许有 账号 2(DE 独立账号)
                                return false;
                            else if(StringUtils.contains(msg, "A_DE|1")) // A_DE 市场不允许有 账号 1(UK 独立账号)
                                return false;
                            else if(StringUtils.contains(msg, "A_FR"))
                                return false;
                            else if(mskuArr.length >= 2 && mskuArr[1].length() < 3)
                                return false;
                            else
                                return true;
                        }
                    });
                }
                Cache.add(cacheKey, sids, "2h");
            }
        }

        return Cache.get(cacheKey, List.class);
    }

    /**
     * 返回当前 Selling 的 Review 平均 rating 值
     *
     * @return
     */
    public Float avgRating() {
        Double rating = (Double) DBUtils.row("select avg(rating) as avgRating from AmazonListingReview where listing_listingId=?", this.listing.listingId).get("avgRating");
        return rating == null ? 0f : Webs.scale2PointUp(rating.floatValue());
    }

    /**
     * Selling 实例对象, 自行初始化 sid
     *
     * @return
     */
    public String sid() {
        if(StringUtils.isBlank(this.merchantSKU))
            throw new FastRuntimeException("Selling.sid merchantSKU can not be empty.");
        if(this.market == null)
            throw new FastRuntimeException("Selling.sid market can not be null.");
        if(this.account == null)
            throw new FastRuntimeException("Selling.sid account can not be null.");
        this.sellingId = Selling.sid(this.merchantSKU, this.market, this.account);
        return this.sellingId;
    }


    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Selling selling = (Selling) o;

        if(sellingId != null ? !sellingId.equals(selling.sellingId) : selling.sellingId != null) return false;

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

    /**
     * 将 sid 重新分割成 3 部分
     *
     * @return
     */
    public static F.T3<String, String, String> sidT3(String sid) {
        List<String> args = new ArrayList<String>(Arrays.asList(StringUtils.split(sid, "|")));
        int differSize = 3 - args.size();
        for(int i = 0; i < differSize; i++) args.add("");
        return new F.T3<String, String, String>(args.get(0), args.get(1), args.get(2));
    }

    public static boolean exist(String merchantSKU) {
        return Selling.find("merchantSKU=?", merchantSKU).first() != null;
    }

}
