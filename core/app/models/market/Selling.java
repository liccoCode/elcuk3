package models.market;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import helper.*;
import models.embedded.AmazonProps;
import models.procure.PItem;
import models.product.Attach;
import models.product.Product;
import models.product.Whouse;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.libs.IO;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 已经正在进行销售的对象抽象
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:48 AM
 */
@javax.persistence.Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Selling extends GenericModel {

    public Selling() {
        this.aps = new AmazonProps();
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

    @ManyToOne
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
    public Account.M market;

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
     * 动态计算使用的 N 天销量
     */
    @Transient
    public Float d1 = 0f;
    @Transient
    public Float d7 = 0f;
    @Transient
    public Float d30 = 0f;
    @Transient
    public Float d180 = 0f;
    @Transient
    public Float dAll = 0f;
    @Transient
    public Integer qty = 0;

    /**
     * 这个产品现在存有的货物还能够周转多少天
     */
    @Transient
    public Float turnOver = 0f;

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
            images = Arrays.copyOfRange(images, 0, 8);
        this.aps.imageName = StringUtils.join(images, Webs.SPLIT);
        /**
         * MAIN   主图
         * PT01~08  , 2~9 号图片.
         */
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("asin", this.asin));
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
                Devs.fileLog(String.format("%s.%s.html", this.sellingId, this.account.id), body, Devs.T.IMGUPLOAD);
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
            html = HTTP.get(this.account.cookieStore(), Account.M.listingEditPage(this));
            if(StringUtils.isBlank(html))
                throw new FastRuntimeException(String.format("Visit %s page is empty.", Account.M.listingEditPage(this)));
            if(Play.mode.isDev())
                IO.writeContent(html, new File(String.format("%s/%s_%s.html", Constant.E_DATE, this.merchantSKU, this.asin)));
        }
        // 3. 将需要的参数同步进来
        this.aps.syncPropFromAmazonPostPage(html, this);
        this.save();
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
                    String html = HTTP.get(this.account.cookieStore(), Account.M.listingEditPage(this));
                    play.libs.F.T2<Collection<NameValuePair>, Document> paramAndDocTuple = this.aps.generateDeployAmazonProps(html, this);

                    // 3. 提交
                    String[] args = StringUtils.split(paramAndDocTuple._2.select("form[name=productForm]").first().attr("action"), ";");
                    html = HTTP.post(this.account.cookieStore(),
                            Account.M.listingPostPage(this.account.type/*更新的链接需要账号所在地的 URL*/, (args.length >= 2 ? args[1] : "")),
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

    /**
     * 返回这个 Listing 所对应的分析页面的 PItem 对象
     *
     * @return
     */
    public PItem calculatePItem() {
        PItem pi = new PItem();
        pi.product = this.listing.product;
        pi.selling = this;
        pi.selling.ps = pi.selling.ps == null ? 1 : pi.selling.ps;
        pi.whouse = Whouse.find("account=?", this.account).first();

        pi.in = 0;
        for(SellingQTY p : this.qtys) pi.in += p.qty;

        // 在库, 在途, 在产
        // 将使用 JSON 存储起来的 PItem 重新加载出来. 当 Plan, Procure, Shipmenet 完成后会修改过通过计算获取
        PItem opi = PH.unMarsh(pi.product.sku + "_" + pi.selling.sellingId);

        pi.onWay = 0;
        pi.onWork = 0;
        pi.airBuy = 0;
        pi.airPatch = 0;
        pi.seaBuy = 0;
        pi.seaPatch = 0;
        if(opi != null) {
            pi.onWay = opi.onWay == null ? 0 : opi.onWay;
            pi.onWork = opi.onWork == null ? 0 : opi.onWork;
            pi.airBuy = opi.airBuy == null ? 0 : opi.airBuy;
            pi.airPatch = opi.airPatch == null ? 0 : opi.airPatch;
            pi.seaBuy = opi.seaBuy == null ? 0 : opi.seaBuy;
            pi.seaPatch = opi.seaPatch == null ? 0 : opi.seaPatch;
        }

        // 7 天销量, -- 在 sortSellingWithQtyLeftTime 方法中计算过了.

        PH.marsh(pi);
        return pi;
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
    public static String sid(String msku, Account.M market, Account acc) {
        return String.format("%s|%s|%s", msku, market.nickName(), acc.id).toUpperCase();
    }

    public static boolean exist(String merchantSKU) {
        return Selling.find("merchantSKU=?", merchantSKU).first() != null;
    }

    /**
     * 加载指定时间段内的 Selling 的销量排名数据(以 MerchantSKU 来进行判断);
     * 其中涉及到计算: day(1-N), turnover
     * PS: 这份数据肯定是需要进行缓存的..
     * <p/>
     * TODO 这种大批量的计算方法需要使用 JDBC 进行重构
     *
     * @param t >0 :按照 MerchantSKU 排序; <0 :按照 SKU 排序
     * @return
     */
    @SuppressWarnings("unchecked")
    @Cached("1h")
    public static List<Selling> salesRankWithTime(int t) {
        String cacke_key = String.format(Caches.SALE_SELLING, t);
        List<Selling> cached = Caches.blockingGet(cacke_key, List.class);
        if(cached != null && cached.size() > 0) return cached;
        Map<String, Selling> sellingMap = new HashMap<String, Selling>();

        /**
         * 1. 将一年内的 OrderItem 加载出来, 作为基础数据进行统计
         * 2. 根据 MerchantSKU(Selling) 来区分来进行排名区分
         * 3. 计算 d1, d7, d30, d180 天的销量数据
         */

        DateTime nowDate = DateTime.parse(DateTime.now().toString("yyyy-MM-dd"));
        List<OrderItem> items = OrderItem.find("createDate>=? AND createDate<=? AND order.state NOT IN (?,?,?)",
                nowDate.plusDays(-180).toDate(), nowDate.toDate(), Orderr.S.CANCEL, Orderr.S.REFUNDED, Orderr.S.RETURNNEW).fetch();

        Long now = nowDate.getMillis();

        // 通过 OrderItem 计算每一个产品的销量.
        for(OrderItem item : items) {
            String sellKey = null;
            try {
                if(Product.unUsedSKU(item.product.sku)) continue;
                if(t > 0) {
                    sellKey = String.format("%s_%s", item.selling.merchantSKU, item.selling.account.id);
                } else if(t < 0) {
                    sellKey = item.product.sku;
                }
                if(!sellingMap.containsKey(sellKey)) {
                    sellingMap.put(sellKey, item.selling);
                }
            } catch(EntityNotFoundException e) {
                Logger.warn(Webs.E(e));
                continue; // 没有这个 Selling 则跳过
            }
            Selling current = sellingMap.get(sellKey);
            Long differTime = now - item.createDate.getTime();

            // 一天内的
            if(differTime <= TimeUnit.DAYS.toMillis(1) && differTime >= 0)
                current.d1 += item.quantity;
            // 七天内的
            if(differTime <= TimeUnit.DAYS.toMillis(7))
                current.d7 += item.quantity;
            // 三十天的
            if(differTime <= TimeUnit.DAYS.toMillis(30))
                current.d30 += item.quantity;
            // 180 天的
            if(differTime <= TimeUnit.DAYS.toMillis(180))
                current.d180 += item.quantity;
        }


        List<SellingQTY> turnOverQty = new ArrayList<SellingQTY>();
        for(Selling sell : sellingMap.values()) {
            Integer quantity = 0;

            /**
             * TODO 按照 merchantSKU + Account 寻找
             * 1. 按照 MerchantSKU 则计算每一个 Product 的库存即可
             * 2. 按照 SKU 则需要找到此 SKU 的所有 Selling 然后找到所有的库存进行计算
             */
            if(t > 0) turnOverQty = sell.qtys;// 按照 MerchantSKU 则计算每一个 Product 的库存即可
            else if(t < 0) turnOverQty = SellingQTY.qtysAccodingSKU(Product.findByMerchantSKU(sell.merchantSKU));

            for(SellingQTY qty : turnOverQty) quantity += qty.qty;
            sell.qty = quantity;
            if(sell.d7 <= 0) sell.turnOver = -1f;
            else sell.turnOver = (quantity < 0 ? 0 : quantity) / (sell.d7 / 7f);
        }

        // 最后对 Selling 进行排序
        List<Selling> sellings = new ArrayList<Selling>(sellingMap.values());
        Collections.sort(sellings, new Comparator<Selling>() {
            @Override
            public int compare(Selling s1, Selling s2) {
                return (int) (s2.d7 - s1.d7);
            }
        });
        Caches.blockingAdd(cacke_key, sellings, "1h"); // 缓存 1 小时
        return Caches.blockingGet(cacke_key, List.class);
    }

    /**
     * 将 Selling 进行排序, 按照此 Selling 能够销售的天数从少到多进行排序;
     * ps: 考虑 在库, 在途, 在产 这几个库存
     *
     * @param sellings
     * @return
     */
    public static List<Selling> sortSellingWithQtyLeftTime(List<Selling> sellings) {
        final Map<String, Long> cacheD7 = new HashMap<String, Long>();
        Collections.sort(sellings, new Comparator<Selling>() {
            @Override
            public int compare(Selling s1, Selling s2) {
                // 在库
                int in = 0;
                int in2 = 0;
                for(SellingQTY q : s1.qtys) in += q.qty;
                for(SellingQTY q : s2.qtys) in2 += q.qty;

                PItem pi1 = PH.unMarsh(String.format("%s_%s", s1.listing.product.sku, s1.sellingId));
                PItem pi2 = PH.unMarsh(String.format("%s_%s", s1.listing.product.sku, s1.sellingId));
                // 在途
                int onWay = 0;
                int onWay2 = 0;
                // 在产
                int onWork = 0;
                int onWork2 = 0;
                if(pi1 != null && pi2 != null) {
                    onWay = pi1.onWay == null ? 0 : pi1.onWay;
                    onWork = pi1.onWork == null ? 0 : pi1.onWork;
                    onWay2 = pi2.onWay == null ? 0 : pi2.onWay;
                    onWork2 = pi2.onWork == null ? 0 : pi2.onWork;
                }
                Long d71 = cacheD7.get(s1.sellingId); // 对反复寻找的 selling 的销量可进行缓存, 减少数据库访问.
                DateTime now = DateTime.now();
                if(d71 == null) {
                    d71 = OrderItem.count("selling=? AND createDate>=? AND createDate<=?",
                            s1, DateTime.parse(now.plusDays(-6).toString("yyyy-MM-dd")).toDate(), now.toDate());
                    cacheD7.put(s1.sellingId, d71);
                }
                s1.d7 = d71 <= 0 ? 1 : d71.floatValue();

                Long d72 = cacheD7.get(s2.sellingId);
                if(d72 == null) {
                    d72 = OrderItem.count("selling=? AND createDate>=? AND createDate<=?",
                            s2, DateTime.parse(now.plusDays(-6).toString("yyyy-MM-dd")).toDate(), now.toDate());
                    cacheD7.put(s2.sellingId, d72);
                }
                s2.d7 = d72 <= 0 ? 1 : d72.floatValue();

                s1.dAll = (in + onWay + onWork) / s1.d7;
                s2.dAll = (in2 + onWay2 + onWork2) / s2.d7;


                return (int) (s1.dAll - s2.dAll);
            }
        });
        return sellings;
    }


}
