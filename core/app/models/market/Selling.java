package models.market;

import exception.VErrorRuntimeException;
import helper.Caches;
import models.product.ProductQTY;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

import javax.persistence.*;
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
    public Float d365 = 0f;
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

        List<OrderItem> items = OrderItem.all().fetch();

        Long now = System.currentTimeMillis();

        // 通过 OrderItem 计算每一个产品的销量.
        for(OrderItem item : items) {
            if(!sellingMap.containsKey(item.selling.merchantSKU)) {
                sellingMap.put(item.selling.merchantSKU, item.selling);
            }
            Selling current = sellingMap.get(item.selling.merchantSKU);
            Long differTime = now - item.createDate.getTime();

            // 一天内的
            if(differTime <= TimeUnit.DAYS.toMillis(1) && differTime >= 0) current.d1 += item.quantity;
            // 七天内的
            if(differTime <= TimeUnit.DAYS.toMillis(7)) current.d7 += item.quantity;
            // 三十天的
            if(differTime <= TimeUnit.DAYS.toMillis(30)) current.d30 += item.quantity;
            // 365 天的
            if(differTime <= TimeUnit.DAYS.toMillis(365)) current.d365 += item.quantity;
            // 总共
            current.dAll += item.quantity;
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
                return (int) (s2.d30 - s1.d30);
            }
        });
        if(sellings.size() > 0) Cache.add(Caches.SALE_SELLING, sellings, "20mn"); // 缓存 10 分钟
        return sellings;
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
