package models.market;

import exception.VErrorRuntimeException;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * 已经正在进行销售的对象抽象
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:48 AM
 */
@Entity
public class Selling extends Model {

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
     * 唯一的 SellingId, [asin]_[market], 这个可重复, 因为会有多个 Selling 实际上
     * 是上架的一个 Listing; 这个字段也想当与一个冗余字段.
     */
    @Column(nullable = false)
    public String sellingId;

    /**
     * 上架后用来唯一标示这个 Selling 的 Id;
     * 1. 在 Amazon 上架的唯一的 merchantSKU;
     * 2. 在 ebay 上架的唯一的 itemId;
     */
    @Column(nullable = false, unique = true)
    @Required
    public String merchantSKU;

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

    public void setAsin(String asin) {
        this.asin = asin;
        if(this.asin != null && this.market != null)
            this.sellingId = String.format("%s_%s", this.asin, this.market.toString());
    }

    public void setMarket(Account.M market) {
        this.market = market;
        if(this.asin != null && this.market != null)
            this.sellingId = String.format("%s_%s", this.asin, this.market.toString());
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
        //TODO Images....
        oldOne.productDesc = StringUtils.isNotBlank(this.productDesc) ? this.productDesc : oldOne.productDesc;
        oldOne.searchTerms = StringUtils.isNotBlank(this.searchTerms) ? this.searchTerms : oldOne.searchTerms;
        oldOne.platinumKeywords = StringUtils.isNotBlank(this.platinumKeywords) ? this.platinumKeywords : oldOne.platinumKeywords;

        //TODO 更新网络

        //if net update success
        oldOne.save();
    }
}
