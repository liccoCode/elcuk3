package models.market;

import exception.VErrorRuntimeException;
import helper.Caches;
import helper.PH;
import helper.Webs;
import models.procure.PItem;
import models.product.Product;
import models.product.ProductQTY;
import models.product.Whouse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

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
    public PriceStrategy priceStrategy;

    /**
     * 上架后用来唯一标示这个 Selling 的 Id;
     * 唯一的 SellingId, [merchantSKU]_[market]
     */
    @Id
    public String sellingId;


    /**
     * 1. 在 Amazon 上架的唯一的 merchantSKU;
     * 2. 在 ebay 上唯一的 itemid(因为 ebay 的 itemid 是唯一的, 所以对于 ebay 的 selling, merchantSKU 与 asin 将会一样)
     */
    @Column(nullable = false)
    @Required
    public String merchantSKU;

    /**
     * 1. 在 Amazon 上单个市场中唯一的 ASIN
     * 2. 在 ebay 上架的唯一的 itemId;
     */
    @Column(nullable = false)
    public String asin;

    @Enumerated(EnumType.STRING)
    public Account.M market;

    @Enumerated(EnumType.STRING)
    @Required
    public S state;

    @Enumerated(EnumType.STRING)
    public T type;


    /**
     * 给这个 Selling 人工设置的 PS 值
     */
    public Float ps = 0f;

    public Float price = 0f;

    public Float shippingPrice = 0f;

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

    // ----------------------- 上架会需要使用到的信息 ----------------------------
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
     * 使用 , 进行分割, 一般为 2 个
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
    public Float salePrice;
    /**
     * 促销产品价格的开始日期
     */
    public Date startDate;
    /**
     * 促销产品价格的结束日期
     */
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
     * 将传入的 Selling 的数据更新到 渠道上并且更新数据库
     */
    public void deploy(String merchantSKU) {
        /**
         * 1. 根据 selling 找到数据库中存在的
         * 2. 更新可以更新的字段;
         * 3. 在网络上进行更新;
         * 4. 网络更新成功后, 在本地数据库更新
         */
        Selling oldOne = Selling.find("merchantSKU=?", merchantSKU).first();
        if(oldOne == null) throw new VErrorRuntimeException("Selling.merchantSKU", "MerchantSKU Selling is not valid!");
        sellingParamsCopy(oldOne);
        //TODO Images....

        //TODO 更新网络

        //if net update success
        oldOne.save();
    }

    public void localUpdate(String merchantSKU) {
        Selling oldOne = Selling.find("merchantSKU=?", merchantSKU).first();
        if(oldOne == null) throw new VErrorRuntimeException("Selling.merchantSKU", "MerchantSKU Selling is not valid!");
        sellingParamsCopy(oldOne);
        oldOne.save();
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
     * @param oldOne
     */
    private void sellingParamsCopy(Selling oldOne) {
        oldOne.title = StringUtils.isNotBlank(this.title) ? this.title : oldOne.title;
        oldOne.modelNumber = StringUtils.isNotBlank(this.modelNumber) ? this.modelNumber : oldOne.modelNumber;
        oldOne.manufacturer = StringUtils.isNotBlank(this.manufacturer) ? this.manufacturer : oldOne.manufacturer;
        oldOne.keyFetures = StringUtils.isNotBlank(this.keyFetures) ? this.keyFetures : oldOne.keyFetures;
        oldOne.RBN = StringUtils.isNotBlank(this.RBN) ? this.RBN : oldOne.RBN;
        oldOne.manufacturerPartNumber = StringUtils.isNotBlank(this.manufacturerPartNumber) ? this.manufacturerPartNumber : oldOne.manufacturerPartNumber;
        oldOne.condition_ = StringUtils.isNotBlank(condition_) ? this.condition_ : oldOne.condition_;
        oldOne.standerPrice = (this.standerPrice != null && this.standerPrice > 0) ? this.standerPrice : oldOne.standerPrice;
        oldOne.salePrice = (this.salePrice != null && this.salePrice > 0) ? this.salePrice : oldOne.salePrice;
        oldOne.startDate = (this.startDate != null) ? this.startDate : oldOne.startDate;
        oldOne.endDate = (this.endDate != null) ? this.endDate : oldOne.endDate;
        oldOne.legalDisclaimerDesc = StringUtils.isNotBlank(this.legalDisclaimerDesc) ? this.legalDisclaimerDesc : oldOne.legalDisclaimerDesc;
//        oldOne.launchDate = (this.launchDate != null) ? this.launchDate : oldOne.launchDate; // launchDate 可以不用修改的
        oldOne.sellerWarrantyDesc = StringUtils.isNotBlank(this.sellerWarrantyDesc) ? this.sellerWarrantyDesc : oldOne.sellerWarrantyDesc;

        oldOne.productDesc = StringUtils.isNotBlank(this.productDesc) ? this.productDesc : oldOne.productDesc;
        oldOne.searchTerms = StringUtils.isNotBlank(this.searchTerms) ? this.searchTerms : oldOne.searchTerms;
        oldOne.platinumKeywords = StringUtils.isNotBlank(this.platinumKeywords) ? this.platinumKeywords : oldOne.platinumKeywords;
    }

    public static boolean exist(String merchantSKU) {
        return Selling.find("merchantSKU=?", merchantSKU).first() != null;
    }

    /**
     * 加载指定时间段内的 Selling 的销量排名数据;其中涉及到计算: day(1-N), turnover
     * PS: 这份数据肯定是需要进行缓存的..
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<Selling> salesRankWithTime() {
        List<Selling> cached = Cache.get(Caches.SALE_SELLING, List.class);
        if(cached != null && cached.size() > 0) return cached;
        Map<String, Selling> sellingMap = new HashMap<String, Selling>();

        /**
         * 1. 将一年内的 OrderItem 加载出来, 作为基础数据进行统计
         * 2. 根据 MerchantSKU(Selling) 来区分来进行排名区分
         * 3. 计算 d1, d7, d30, d180 天的销量数据
         */

        DateTime nowDate = DateTime.now();
        List<OrderItem> items = OrderItem.find("createDate>=? AND createDate<=? AND order.state NOT IN (?,?,?)",
                DateTime.parse(nowDate.toString("yyyy-MM-dd")).plusDays(-180).toDate(), DateTime.parse(nowDate.toString("yyyy-MM-dd")).toDate(), Orderr.S.CANCEL, Orderr.S.REFUNDED, Orderr.S.RETURNNEW).fetch();

        Long now = nowDate.getMillis();

        // 通过 OrderItem 计算每一个产品的销量.
        for(OrderItem item : items) {
            String sellKey = null;
            try {
                sellKey = String.format("%s_%s", item.selling.merchantSKU, item.order.market.toString());
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

        // 通过 Selling 与 Product 库存计算 TurnOver
        for(Selling sell : sellingMap.values()) {
            List<ProductQTY> qtys = sell.listing.product.qtys;
            Integer quantity = 0;
            for(ProductQTY qty : qtys) {
                quantity += qty.qty;
            }
            sell.qty = quantity;
            // 当前这个 Selling 所具有的库存 / 计算的 7 天的平均销量
            if(sell.d7 == 0) {
                sell.turnOver = -1f; // 如果 7 天内没有销量, 那么 turnOver 则直接调整为 -1, 标示值不可参考
            } else {
                sell.turnOver = new Float(quantity / (sell.d7 / 7.0));
            }
        }

        // 最后对 Selling 进行排序
        List<Selling> sellings = new ArrayList<Selling>(sellingMap.values());
        Collections.sort(sellings, new Comparator<Selling>() {
            @Override
            public int compare(Selling s1, Selling s2) {
                return (int) (s2.d7 - s1.d7);
            }
        });
        if(sellings.size() > 0) Cache.add(Caches.SALE_SELLING, sellings, "30mn"); // 缓存 30 分钟
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
                Whouse wh1 = Whouse.find("account=?", s1.account).first();
                Whouse wh2 = Whouse.find("account=?", s2.account).first();

                List<ProductQTY> qty1 = ProductQTY.find("product=? AND whouse=?", s1.listing.product, wh1).fetch();
                List<ProductQTY> qty2 = ProductQTY.find("product=? AND whouse=?", s2.listing.product, wh2).fetch();

                int in = 0;
                int in2 = 0;
                for(ProductQTY q : qty1) in += q.qty;
                for(ProductQTY q : qty2) in2 += q.qty;

                PItem pi1 = PH.unMarsh(String.format("%s_%s", s1.listing.product.sku, s1.sellingId));
                PItem pi2 = PH.unMarsh(String.format("%s_%s", s1.listing.product.sku, s1.sellingId));
                // 在途
                // 在产
                int onWay = 0;
                int onWork = 0;
                int onWay2 = 0;
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


        // 库存
        List<ProductQTY> qtys = ProductQTY.find("product=? AND whouse=?", this.listing.product, pi.whouse).fetch();
        if(qtys.size() > 1)
            Logger.warn("Product [" + pi.product.sku + "] have more than ONE ProductQTY in the same Whouse.");

        pi.in = 0;
        for(ProductQTY p : qtys) pi.in += p.qty;

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
