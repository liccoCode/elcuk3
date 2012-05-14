package models.market;

import com.google.gson.annotations.Expose;
import helper.*;
import models.procure.PItem;
import models.product.Product;
import models.product.Whouse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.libs.IO;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 已经正在进行销售的对象抽象
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:48 AM
 */
@Entity
public class Selling extends GenericModel {

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
     * 唯一的 SellingId, [merchantSKU]_[market]
     */
    @Id
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
    @Lob
    @Required
    public String title;
    public String modelNumber;
    public String manufacturer;
    /**
     * 使用  Webs.SPLIT 进行分割, 最多 5 行
     */
    public String keyFetures;
    /**
     * Recommended Browse Nodes;
     * 使用 [,] 进行分割, 一般为 2 个
     */
    public String RBN;
    /**
     * For most products, this will be identical to the model number;
     * however, some manufacturers distinguish part number from model number
     */
    public String manufacturerPartNumber;
    /**
     * 如果这个 Condition 不为空, 那么则覆盖掉 Listing 中的 Condition
     */
    public String condition_;
    @Required
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
    public String legalDisclaimerDesc;
    public Date launchDate;
    @Lob
    public String sellerWarrantyDesc;

    /**
     * 核心的产品描述
     */
    @Lob
    public String productDesc;

    /**
     * 使用 Webs.SPLIT 进行分割, 5 行
     */
    @Lob
    public String searchTerms;

    /**
     * 使用 Webs.SPLIT 进行分割, 5 行
     */
    @Lob
    public String platinumKeywords;

    // ---- Images ????

    // -------------------------- ebay 上架使用的信息 TBD ---------------------


    /**
     * 这个 Selling 所属的哪一个用户
     */
    @ManyToOne
    public Account account;

    public void setMerchantSKU(String merchantSKU) {
        this.merchantSKU = merchantSKU;
        if(this.merchantSKU != null && this.market != null)
            this.sellingId = String.format("%s_%s", this.merchantSKU, this.market.toString());
    }

    public void setMarket(Account.M market) {
        this.market = market;
        if(this.merchantSKU != null && this.market != null)
            this.sellingId = String.format("%s_%s", this.merchantSKU, this.market.toString());
    }

    /**
     * <pre>
     * 将传入的 Selling 的数据更新到 渠道上并且更新数据库;
     * PS: 请确保 Selling 中的信息是正确的, 这个方法仅仅根据对应的参数做提交操作, 不再验证数据!
     * 更新:
     * 1. price
     * 2. salePrice, startDate, endDate
     *  --- price, salePrice 会根据 Amazon 检查, 仅保留小数点后两位
     * 3. productDescription
     * 4. searchTerms[1~5]
     * 5. 等待添加
     * </pre>
     */
    public void deploy() {
        switch(this.market) {
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_UK:
            case AMAZON_US:
                // 1. 切换 Selling 所在区域
                this.account.changeRegion(this.market); // 跳转到对应的渠道,不然会更新成不同的市场
                String body = HTTP.get(this.account.cookieStore(), Account.M.listingEditPage(this));
                if(Play.mode.isDev())
                    IO.writeContent(body, new File(String.format("%s/%s_%s.html", Constant.E_DATE, this.merchantSKU, this.asin)));


                // 2. 设置需要提交的值
                Document doc = Jsoup.parse(body);
                // ----- Input 框框
                Elements inputs = doc.select("form[name=productForm] input");
                if(inputs.size() == 0) {
                    Logger.warn("Listing Update Page Error! Log to ....?");
                    try {
                        FileUtils.writeStringToFile(new File(String.format("%s/%s_%s.html", Constant.E_ERROR, this.merchantSKU, this.asin)), body);
                    } catch(IOException e) {
                        //ignore..
                    }
                    throw new FastRuntimeException("Display Post page visit Error. Please try again.");
                }
                Set<NameValuePair> params = new HashSet<NameValuePair>();
                for(Element el : inputs) {
                    String name = el.attr("name").toLowerCase().trim();
                    if("our_price".equals(name) && this.price != null && this.price > 0) {
                        params.add(new BasicNameValuePair(name, Webs.priceLocalNumberFormat(this.market, Webs.scale2PointUp(this.price))));
                    } else if("discounted_price".equals(name) ||
                            "discounted_price_start_date".equals(name) ||
                            "discounted_price_end_date".equals(name)) {
                        if(this.startDate != null && this.endDate != null && this.salePrice != null && this.salePrice > 0) {
                            params.add(new BasicNameValuePair("discounted_price", Webs.priceLocalNumberFormat(this.market, Webs.scale2PointUp(this.salePrice))));
                            params.add(new BasicNameValuePair("discounted_price_start_date", Dates.listingUpdateFmt(this.market, this.startDate)));
                            params.add(new BasicNameValuePair("discounted_price_end_date", Dates.listingUpdateFmt(this.market, this.endDate)));
                        }
                    } else if(StringUtils.startsWith(name, "generic_keywords") && StringUtils.isNotBlank(this.searchTerms)) {
                        String[] searchTermsArr = StringUtils.splitByWholeSeparatorPreserveAllTokens(this.searchTerms, Webs.SPLIT);
                        for(int i = 0; i < searchTermsArr.length; i++) {
                            if(searchTermsArr[i].length() > 50)
                                throw new FastRuntimeException("SearchTerm length must blew then 50.");
                            params.add(new BasicNameValuePair("generic_keywords[" + i + "]", searchTermsArr[i]));
                        }
                        // length = 3, 0~2, need 3,4
                        int missingIndex = 5 - searchTermsArr.length; // missingIndex = 5 - 3 = 2
                        if(missingIndex > 0) {
                            for(int i = 1; i <= missingIndex; i++) {
                                params.add(new BasicNameValuePair("generic_keywords[" + (searchTermsArr.length + i) + "]", ""));
                            }
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


                // 3. 提交
                String[] args = StringUtils.split(doc.select("form[name=productForm]").first().attr("action"), ";");
                body = HTTP.post(this.account.cookieStore(),
                        Account.M.listingPostPage(this.account.type/*更新的链接需要账号所在地的 URL*/, (args.length >= 2 ? args[1] : "")),
                        params);
                if(StringUtils.isBlank(body)) // 这个最先检查
                    throw new FastRuntimeException("Selling update is failed! Return Content is Empty!");
                if(Play.mode.isDev())
                    IO.writeContent(body, new File(String.format("%s/%s_%s_posted.html", Constant.E_DATE, this.merchantSKU, this.asin)));
                doc = Jsoup.parse(body);
                Elements error = doc.select(".messageboxerror li");
                if(error.size() > 0)
                    throw new FastRuntimeException("Error:" + error.text());
                break;
            case EBAY_UK:
                break;
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
        if(StringUtils.isNotBlank(newSelling.title)) this.title = newSelling.title;
        if(StringUtils.isNotBlank(newSelling.modelNumber)) this.modelNumber = newSelling.modelNumber;
        if(StringUtils.isNotBlank(newSelling.manufacturer)) this.manufacturer = newSelling.manufacturer;
        if(StringUtils.isNotBlank(newSelling.keyFetures)) this.keyFetures = newSelling.keyFetures;
        if(StringUtils.isNotBlank(this.RBN)) this.RBN = newSelling.RBN;
        if(StringUtils.isNotBlank(this.manufacturerPartNumber))
            this.manufacturerPartNumber = newSelling.manufacturerPartNumber;
        if(StringUtils.isNotBlank(condition_)) this.condition_ = newSelling.condition_;
        if(newSelling.standerPrice != null && newSelling.standerPrice > 0) this.standerPrice = newSelling.standerPrice;
        if(newSelling.salePrice != null && newSelling.salePrice > 0) this.salePrice = newSelling.salePrice;
        if(newSelling.startDate != null) this.startDate = newSelling.startDate;
        if(newSelling.endDate != null) this.endDate = newSelling.endDate;
        if(StringUtils.isNotBlank(newSelling.legalDisclaimerDesc))
            this.legalDisclaimerDesc = newSelling.legalDisclaimerDesc;
        if(StringUtils.isNotBlank(this.sellerWarrantyDesc)) this.sellerWarrantyDesc = newSelling.sellerWarrantyDesc;

        if(StringUtils.isNotBlank(this.productDesc)) this.productDesc = newSelling.productDesc;
        if(StringUtils.isNotBlank(this.searchTerms)) this.searchTerms = newSelling.searchTerms;
        if(StringUtils.isNotBlank(this.platinumKeywords)) this.platinumKeywords = newSelling.platinumKeywords;

        return this.save();
    }

    public static boolean exist(String merchantSKU) {
        return Selling.find("merchantSKU=?", merchantSKU).first() != null;
    }

    /**
     * 加载指定时间段内的 Selling 的销量排名数据(以 MerchantSKU 来进行判断);
     * 其中涉及到计算: day(1-N), turnover
     * PS: 这份数据肯定是需要进行缓存的..
     *
     * @param t >0 :按照 MerchantSKU 排序; <0 :按照 SKU 排序
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<Selling> salesRankWithTime(int t) {
        String cacke_key = String.format(Caches.SALE_SELLING, t);
        List<Selling> cached = Cache.get(cacke_key, List.class);
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
        if(sellings.size() > 0) Cache.add(cacke_key, sellings, "30mn"); // 缓存 30 分钟
        return sellings;
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


    /**
     * 处理 Amazon 的 Active Listing Report 文档, 如果有新 Listing/Selling 则与系统进行同步处理.
     * 如果系统中有的, Amazon 上没有, 则先不做处理.
     *
     * @param file
     * @return
     */
    public static List<Selling> dealSellingFromActiveListingsReport(File file, Account acc, Account.M market) {
        List<Selling> sellings = new ArrayList<Selling>();
        List<String> lines = null;
        try {
            lines = FileUtils.readLines(file);
        } catch(IOException e) {
            Logger.warn("File [%s] IO Error!", file.getAbsolutePath());
            return sellings;
        }

        lines.remove(0); // 删除第一行的标题

        for(String line : lines) {
            try {
                String[] args = StringUtils.splitPreserveAllTokens(line, "\t");

                /**
                 * 1. 解析出 Listing, 并且将 Listing 绑定到对应的 Product 身上
                 *  a. 注意需要先查找系统中是否有对应的 Listing, 如果有则不做处理
                 *  b. 如果没有对应的 Listing 那么则创建一个新的 Listing 并且保存下来;(Listing 的详细信息等待抓取线程自己去进行更新)
                 *
                 * 2. 创建 Selling, 因为这份文件是自己的, 所以接触出来的 Listing 数据就是自己的 Selling
                 *  a. 注意需要先查找系统中是否有, 有的话则不做处理
                 *  b. 没有的话则创建 Selling 并且绑定 Listing
                 */
                String t_asin = null;
                String t_msku = null;
                String t_title = null;
                String t_price = null;
                String t_fulfilchannel = null;
                if(market == Account.M.AMAZON_FR) {
                    t_asin = args[11].trim();
                    t_msku = args[2].trim().toUpperCase();
                    t_title = args[0].trim();
                    t_price = args[3].trim();
                    t_fulfilchannel = args[13].trim();
                } else {
                    t_asin = args[16].trim();
                    t_msku = args[3].trim().toUpperCase();
                    t_title = args[0].trim();
                    t_price = args[4].trim();
                    t_fulfilchannel = args[26].trim();
                }

                // 如果属于 UnUsedSKU 那么则跳过这个解析
                if(Product.unUsedSKU(t_msku)) continue;

                String lid = String.format("%s_%s", t_asin, market.toString());
                Listing lst = Listing.findById(lid);
                Product prod = Product.findByMerchantSKU(t_msku);
                if(prod == null) {
                    String warnMsg = "[Warnning!] Listing[" + lid + "] Missing Product[" + t_msku + "].";
                    Logger.warn(warnMsg);
                    Webs.systemMail(warnMsg, String.format("Listing %s Missing Product %s.", lid, t_msku));
                    continue;// 如果 Product 不存在, 需要跳过这个 Listing!
                }

                if(lst != null) Logger.info("Listing[%s] is exist.", lid);
                else {
                    lst = new Listing();
                    lst.listingId = lid;
                    lst.market = market;
                    lst.asin = t_asin;
                    lst.product = prod;
                    lst.title = t_title;
                    lst.displayPrice = NumberUtils.toFloat(t_price);
                    lst.lastUpdateTime = System.currentTimeMillis();
                    lst.save();
                }

                String sid = String.format("%s_%s", t_msku, market.toString());
                Selling selling = Selling.findById(sid);
                if(selling != null) Logger.info("Selling[%s] is exist.", sid);
                else {
                    selling = new Selling();
                    selling.sellingId = sid;
                    selling.asin = lst.asin;
                    selling.condition_ = "NEW";
                    selling.market = market;
                    selling.merchantSKU = t_msku;

                    selling.title = lst.title;
                    selling.account = acc;
                    selling.shippingPrice = 0f;
                    selling.standerPrice = selling.price = lst.displayPrice;
                    selling.ps = 2f;
                    selling.state = S.SELLING;

                    PriceStrategy priceStrategy = new PriceStrategy();
                    if(StringUtils.isNotBlank(t_fulfilchannel) && StringUtils.startsWith(t_fulfilchannel.toLowerCase(), "amazon")) {
                        priceStrategy.type = PriceStrategy.T.FixedPrice;
                        selling.type = T.FBA;
                    } else {
                        priceStrategy.type = PriceStrategy.T.LowestPrice;
                        selling.type = T.AMAZON;
                    }

                    // 新添加的 PriceStrategy,
                    priceStrategy.cost = lst.displayPrice * 0.5f; //成本价格位展示价格的 50%
                    priceStrategy.margin = 0.3f;//利润位 30%
                    priceStrategy.lowest = priceStrategy.cost * 1.05f; //最低价格位成本价格的 1.05 倍
                    priceStrategy.max = priceStrategy.cost * 3f; //最高价格位成本价格的 3 倍
                    selling.priceStrategy = priceStrategy;
                    selling.listing = lst;

                    selling.save();
                }
                sellings.add(selling);
            } catch(Exception e) {
                String warMsg = "Skip Add one Listing/Selling. Line[" + line + "]";
                Logger.warn(warMsg);
                Webs.systemMail(warMsg, String.format("%s <br/>\r\n%s", warMsg, Webs.E(e)));
            }
        }
        return sellings;
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

        if(merchantSKU != null ? !merchantSKU.equals(selling.merchantSKU) : selling.merchantSKU != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (merchantSKU != null ? merchantSKU.hashCode() : 0);
        return result;
    }
}
