package models.market;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import exception.VErrorRuntimeException;
import helper.Currency;
import models.product.Product;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Listing 对应的是不同渠道上 Listing 的信息
 * User: wyattpan
 * Date: 12/27/11
 * Time: 10:27 PM
 */
@Entity
public class Listing extends Model {
    /**
     * Condition
     */
    public enum C {
        NEW
    }


    /**
     * 不能级联删除, 并且删除 Listing 的时候需要保证 Selling 都已经处理了
     */
    @OneToMany(mappedBy = "listing", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    public List<Selling> sellings;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL)
    public List<ListingOffer> offers;

    @ManyToOne
    public Product product;

    /**
     * 用来表示唯一的 ListingId, [asin]_[market]
     */
    @Column(unique = true)
    public String listingId;

    @Column(nullable = false)
    public String asin;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public Account.M market;

    @Column(nullable = false)
    @Lob
    public String title;
    /**
     * title xxxx by [??]
     */
    public String byWho;

    public Integer reviews;

    public Float rating;

    @Lob
    public String technicalDetails;

    @Lob
    public String productDescription;

    /**
     * 抓取的图片的 URLs, 使用 Webs.SPLIT(|-|) 进行分割
     */
    @Lob
    public String picUrls;

    /**
     * 此 Listing 是否需要进行警告的的标识, 并且记录警告了多少次.
     */
    public Integer warnningTimes;

    /**
     * 如果搜索不到 salerank, 那么则直接归属到 5001
     */
    public Integer saleRank;

    public Integer totalOffers;

    /**
     * 代表从 Category 页面抓取的时候显示的价格, 或者 buybox 的价格.
     */
    public Float displayPrice;

    public Long lastUpdateTime;

    public Long nextUpdateTime;

    // ----------------- 上架会需要使用到的信息 ------------------
    public Date launchDate;
    public String condition_;
    /**
     * Number of the same product contained within one package.
     * For example, if you are selling a case of 10 packages of socks, ItemPackageQuantity would be 10.
     */
    public Integer packageQuantity = 1;
    /**
     * Number of discrete items included in the product you are offering for sale, such that each item
     * is not packaged for individual sale.
     * For example, if you are selling a case of 10 packages of socks, and each package contains 3 pairs
     * of socks, NumberOfItems would be 30.
     */
    public Integer numberOfItems = 1;


    public void setAsin(String asin) {
        this.asin = asin;
        if(this.market != null && this.asin != null)
            initListingId();
    }

    public void setMarket(Account.M market) {
        this.market = market;
        if(this.asin != null && this.market != null)
            initListingId();
    }

    private void initListingId() {
        this.listingId = String.format("%s_%s", this.asin, this.market.toString());
    }

    public void updateAttrs(Listing newListing) {
        if(!this.equals(newListing)) return; // 不是一个 Listing 则不允许更新了
//        newListing.asin 这个不更新
        if(newListing.market != null) this.market = newListing.market;
        if(StringUtils.isNotBlank(newListing.byWho)) this.byWho = newListing.byWho;
        if(StringUtils.isNotBlank(newListing.title)) this.title = newListing.title;
        if(newListing.reviews != null) this.reviews = newListing.reviews;
        if(newListing.rating != null) this.rating = newListing.rating;
        if(StringUtils.isNotBlank(newListing.technicalDetails)) this.technicalDetails = newListing.technicalDetails;
        if(StringUtils.isNotBlank(newListing.productDescription))
            this.productDescription = newListing.productDescription;
        if(newListing.saleRank != null) this.saleRank = newListing.saleRank;
        if(newListing.totalOffers != null) this.totalOffers = newListing.totalOffers;
        if(StringUtils.isNotBlank(newListing.picUrls)) this.picUrls = newListing.picUrls;
        if(newListing.offers != null && newListing.offers.size() > 0) {
            for(ListingOffer lo : this.offers) lo.delete(); //清理掉原来的 ListingOffers
            this.offers = newListing.offers;
        }
        this.lastUpdateTime = System.currentTimeMillis();
        this.save();
    }

    /**
     * 将一个手动创建的 Selling 绑定到一个 Listing 身上, 其中需要经过一些操作
     *
     * @param s
     * @return
     */
    public Selling bindSelling(Selling s) {
        /**
         * 检查这个 Selling 是否已经存在
         */
        if(Selling.exist(s.sellingId)) return s;

        s.listing = this;
        s.price = s.priceStrategy.cost * s.priceStrategy.margin;
        s.shippingPrice = s.priceStrategy.shippingPrice;

        // --- Selling 可处理信息的处理
        s.title = this.title;
        s.condition_ = C.NEW.toString();
        s.standerPrice = s.priceStrategy.max;
        s.productDesc = this.productDescription; // 这个肯定是需要人工处理下的.


        // Account 暂时处理, 仅仅使用这一个用户
        Account acc = Account.find("uniqueName=?", s.account.uniqueName).first();
        if(acc == null) throw new VErrorRuntimeException("Listing.Account", "Account is invalid.", new String[]{});
        s.account = acc;
        return s.save();
    }


    /**
     * 从所有 ListingOffer 中查找自己
     *
     * @return
     */
    public ListingOffer easyacceu() {
        if(this.offers == null) return null;
        for(ListingOffer offer : this.offers) {
            if("easyacceu".equals(offer.name.toLowerCase()) ||
                    "easyacc".equals(offer.name.toLowerCase()) ||
                    "easyacc.eu@gmail.com".equals(offer.name.toLowerCase())) {
                return offer;
            }
        }
        return null;
    }

   /**
     * 根据从 Crawler 抓取回来的 ListingJSON数据转换成系统内使用的 Listing + LisitngOffer 对象,
     * 并更新返回已经存在的 Listing 的持久对象或者返回未保存的 Listing 瞬时对象
     *
     * @param listingJson
     * @return
     */
    public static Listing parseAndUpdateListingFromCrawl(JsonElement listingJson) {
        /**
         * 根据 market 与 asin 先从数据库中加载, 如果存在则更新返回一个持久状态的 Listing 对象,
         * 否则返回一个瞬时状态的 Listing 对象
         */
        JsonObject lst = listingJson.getAsJsonObject();
        String listingId = lst.get("listingId").getAsString();
        if(listingId == null || listingId.trim().isEmpty()) return null; // 排除 404 没有的 Listing
        Listing oldListing = Listing.find("listingId=?", listingId).first();
        Listing tobeChangeed;
        if(oldListing != null) {
            // 更新 OldListing 并保存更新
            tobeChangeed = oldListing;
        } else {
            // 返回一个全新 Listing
            tobeChangeed = new Listing();
        }

        tobeChangeed.market = Account.M.val(listingId.split("_")[1]);
        tobeChangeed.asin = lst.get("asin").getAsString();
        tobeChangeed.byWho = lst.get("byWho").getAsString();
        tobeChangeed.title = lst.get("title").getAsString();
        tobeChangeed.reviews = lst.get("reviews").getAsInt();
        tobeChangeed.rating = lst.get("rating").getAsFloat();
        tobeChangeed.technicalDetails = lst.get("technicalDetails").getAsString();
        tobeChangeed.productDescription = lst.get("productDescription").getAsString();
        tobeChangeed.saleRank = lst.get("saleRank").getAsInt();
        tobeChangeed.totalOffers = lst.get("totalOffers").getAsInt();
        tobeChangeed.picUrls = lst.get("picUrls").getAsString();

        JsonArray offers = lst.get("offers").getAsJsonArray();
        List<ListingOffer> newOffers = new ArrayList<ListingOffer>();
        if(oldListing != null) { // 如果不为空, 那么保持最新的 LisitngOffer 信息, 删除老的重新记录
            for(ListingOffer of : tobeChangeed.offers) of.delete();
        }
        for(JsonElement offerEl : offers) {
            JsonObject offer = offerEl.getAsJsonObject();
            ListingOffer off = new ListingOffer();
            off.name = offer.get("name").getAsString();
            off.offerId = offer.get("offerId").getAsString();
            // 价格根据不同的市场进行转换成 GBP 价格
            switch(tobeChangeed.market) {
                case AMAZON_UK:
                    off.price = offer.get("price").getAsFloat();
                    break;
                case AMAZON_US:
                    off.price = Currency.USD.toGBP(offer.get("price").getAsFloat());
                    break;
                case AMAZON_DE:
                case AMAZON_FR:
                default:
                    off.price = Currency.EUR.toGBP(offer.get("price").getAsFloat());
            }
            off.shipprice = offer.get("shipprice").getAsFloat();
            off.fba = offer.get("fba").getAsBoolean();
            off.buybox = offer.get("buybox").getAsBoolean();
            off.listing = tobeChangeed;
            newOffers.add(off);

            // set display price
            if(tobeChangeed.displayPrice == null && off.buybox)
                tobeChangeed.displayPrice = off.price;
        }
        tobeChangeed.offers = newOffers;
        tobeChangeed.lastUpdateTime = System.currentTimeMillis();
        if(oldListing != null) tobeChangeed.save();
        return tobeChangeed;
    }

    /**
     * 根据 Listing 的状态, 进行 Listing 的检查;
     * 1. 如果这个 Listing 是我们自己的, 检查是否被跟.
     */
    public void check() {
        /**
         * 1. 判断是否为自己的 Listing
         *  1.1 检查此 Listing 是否有被其他卖家上架!
         */
        if(this.selfSalesAmazon()) {
            if(this.offers == null || this.offers.size() == 0) {
                Logger.warn("Listing [" + this.listingId + "] have no offers!");
                return;
            }
            for(ListingOffer off : this.offers) {
                if(StringUtils.isBlank(off.offerId)) { //没有 OfferId 的为不可销售的很多原因, 很重要的例如缺货
                    Logger.debug("Listing [" + this.listingId + "] current can`t sale. Message[" + off.name + "]");
                } else if(!"AJUR3R8UN71M4".equals(off.offerId) ||
                        !off.name.equalsIgnoreCase("EasyAcc")) {
                    // Mail 警告
                    if(this.warnningTimes == null) this.warnningTimes = 0;
                    this.warnningTimes++; // 查询也记录一次
                    if(this.warnningTimes > 4) {
                        Logger.debug("Listing [" + this.listingId + "] has warnned more than 3 times.");
                    } else {
                        Mails.listingOffersWarning(off);
                    }
                } else {
                    this.warnningTimes = 0; // 其余的归零
                }
            }
        } else {
            // 不是自己的 Listing 暂时不做操作...
        }
    }

    private boolean selfSalesAmazon() {
        return "EasyAcc".equalsIgnoreCase(this.byWho) ||
                "Saner".equalsIgnoreCase(this.byWho) ||
                StringUtils.startsWithIgnoreCase(this.title, "Saner") ||
                StringUtils.startsWithIgnoreCase(this.title, "EasyAcc");
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Listing listing = (Listing) o;

        if(!listingId.equals(listing.listingId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + listingId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Listing{" +
                "listingId='" + listingId + '\'' +
                ", title='" + title + '\'' +
                ", reviews=" + reviews +
                ", rating=" + rating +
                ", byWho='" + byWho + '\'' +
                ", saleRank=" + saleRank +
                ", displayPrice=" + displayPrice +
                ", lastUpdateTime=" + lastUpdateTime +
                ", nextUpdateTime=" + nextUpdateTime +
                '}';
    }
}
