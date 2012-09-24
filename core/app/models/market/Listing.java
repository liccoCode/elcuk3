package models.market;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import helper.Crawl;
import jobs.works.ListingOffersWork;
import jobs.works.ListingWorkers;
import models.product.Product;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Listing 对应的是不同渠道上 Listing 的信息
 * User: wyattpan
 * Date: 12/27/11
 * Time: 10:27 PM
 */
@Entity
public class Listing extends GenericModel {
    /**
     * Condition
     */
    public enum C {
        NEW
    }

    public Listing() {
    }

    /**
     * 通过 Product 上架新的 Selling 与 Listing 时使用的构造函数
     *
     * @param selling
     * @param prod
     */
    public Listing(Selling selling, Product prod) {
        this.asin = selling.asin;
        this.title = selling.aps.title;
        this.displayPrice = selling.aps.salePrice;
        this.market = selling.market;
        this.saleRank = 100000;
        this.warnningTimes = 0;
        this.totalOffers = 0;
        this.product = prod;
    }

    /**
     * 不能级联删除, 并且删除 Listing 的时候需要保证 Selling 都已经处理了
     */
    @OneToMany(mappedBy = "listing", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    public List<Selling> sellings;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL)
    public List<ListingOffer> offers;

    @OneToMany(mappedBy = "listing", fetch = FetchType.LAZY)
    @OrderBy(value = "createDate DESC")
    public List<AmazonListingReview> listingReviews;

    @ManyToOne
    public Product product;

    /**
     * 用来表示唯一的 ListingId, [asin]_[market]
     */
    @Id
    @Expose
    public String listingId;

    @Column(nullable = false)
    @Expose
    public String asin;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Expose
    public M market;

    @Column(nullable = false)
    @Lob
    @Expose
    public String title;
    /**
     * title xxxx by [??]
     */
    @Expose
    public String byWho;

    @Expose
    public Integer reviews;

    @Expose
    public Float rating;

    @Expose
    public Integer likes;

    @Lob
    @Expose
    public String technicalDetails;

    @Lob
    @Expose
    public String productDescription;

    /**
     * 抓取的图片的 URLs, 使用 Webs.SPLIT(|-|) 进行分割
     */
    @Lob
    @Expose
    public String picUrls;

    /**
     * 此 Listing 是否需要进行警告的的标识, 并且记录警告了多少次.
     */
    @Expose
    public Integer warnningTimes = 0;

    /**
     * 如果搜索不到 salerank, 那么则直接归属到 5001
     */
    @Expose
    public Integer saleRank;

    @Expose
    public Integer totalOffers = 0;

    /**
     * 代表从 Category 页面抓取的时候显示的价格, 或者 buybox 的价格.
     */
    @Expose
    public Float displayPrice;

    @Expose
    public Long lastUpdateTime;

    @Expose
    public Long nextUpdateTime;


    public void setAsin(String asin) {
        this.asin = asin;
        if(this.market != null && this.asin != null)
            this.listingId = Listing.lid(asin, market);
    }

    public void setMarket(M market) {
        this.market = market;
        if(this.asin != null && this.market != null)
            this.listingId = Listing.lid(asin, market);
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
     * 加载此 Listing 所具有的所有 Selling 的状态的个数
     * NEW, 0
     * SELLING, 1
     * NO_INVENTORY, 2
     * HOlD, 3
     * DOWN 4
     *
     * @return
     */
    public Integer[] relateSellingStateQtys(String nickMarketName) {
        Integer[] stateQtys = {0, 0, 0, 0, 0};
        for(Selling s : this.sellings) {
            if(!nickMarketName.equalsIgnoreCase(s.market.nickName())) continue;
            switch(s.state) {
                case NEW:
                    stateQtys[0] += 1;
                    break;
                case SELLING:
                    stateQtys[1] += 1;
                    break;
                case NO_INVENTORY:
                    stateQtys[2] += 1;
                    break;
                case HOlD:
                    stateQtys[3] += 1;
                    break;
                case DOWN:
                    stateQtys[4] += 1;
                    break;
                default:
                    Logger.warn("Other State? Can not happed!");
            }
        }
        return stateQtys;
    }

    /**
     * 通过 Account 此 Listing 所属的所有 Selling 中过滤出指定 Account 的; 否则与 this.sellings 一样
     *
     * @param a
     * @return
     */
    public List<Selling> sellings(Account a) {
        if(a != null && a.id != null && a.id > 0) {
            return Selling.find("listing=? AND account=?", this, a).fetch();
        } else {
            return this.sellings;
        }
    }

    /**
     * 从所有 ListingOffer 中查找自己
     *
     * @return
     */
    public ListingOffer easyacceu() {
        if(this.offers == null) return null;
        for(ListingOffer offer : this.offers) {
            if("AJUR3R8UN71M4".equalsIgnoreCase(offer.offerId) ||
                    "A22H6OV6Q7XBYK".equalsIgnoreCase(offer.offerId)) {
                return offer;
            }
        }
        return null;
    }

    /**
     * 从 ListingOffer 中价格最低的价格
     *
     * @return 如果没有 ListingOffer 则返回 -1
     */
    public Float lowestPrice() {
        float lowest = Float.MAX_VALUE;
        if(this.offers == null || this.offers.size() == 0) return -1f;
        for(ListingOffer offer : this.offers) {
            if(offer.price < lowest) lowest = offer.price;
        }
        return lowest;
    }


    /**
     * 根据 Listing 的状态, 进行 Listing 的检查;
     * 1. 如果这个 Listing 是我们自己的, 检查是否被跟.
     */
    public void check() {
        /**
         *  1. 检查此 Listing 上是否有自己多个店铺上架
         *  2. 检查此 Listing 是否有被其他卖家上架!(如果是跟着卖的就不需要检查这个了)
         */
        if(this.offers == null || this.offers.size() == 0) {
            Logger.warn("Listing [" + this.listingId + "] have no offers!");
            return;
        }

        int needWarnningOffers = 0;

        for(ListingOffer off : this.offers) {
            // -------- 1
            if(Account.merchant_id().containsKey(off.offerId)) continue;

            // ------- 1
            if(Listing.isSelfBuildListing(this.title)) {
                if(StringUtils.isBlank(off.offerId)) { //没有 OfferId 的为不可销售的很多原因, 很重要的例如缺货
                    Logger.debug("Listing [" + this.listingId + "] current can`t sale. Message[" + off.name + "]");
                } else if(off.cond != ListingOffer.C.NEW) {
                    Logger.info("Offer %s is sale %s condition.", off.offerId, off.cond);
                } else if(!Account.merchant_id().containsKey(off.offerId)) {
                    // Mail 警告
                    if(this.warnningTimes == null) this.warnningTimes = 0;
                    this.warnningTimes++; // 查询也记录一次
                    if(this.warnningTimes > 4) {
                        Logger.debug("Listing [" + this.listingId + "] has warnned more than 3 times.");
                    } else needWarnningOffers++;
                } else {
                    this.warnningTimes = 0; // 其余的归零
                }
            }
        }
        if(needWarnningOffers >= 2) Mails.moreOfferOneListing(offers, this);
    }

    /**
     * 挑选一个 Account, 来点击此 Listing 的 like 按钮
     *
     * @return
     */
    public F.T2<Account, Integer> pickUpOneAccountToClikeLike() {
        List<Account> opendAccs = Account.openedAmazonClickReviewAndLikeAccs(this.market);
        List<Account> nonClickAccs = AmazonLikeRecord.nonClickLikeAccs(opendAccs, this.listingId);
        if(nonClickAccs.size() == 0) throw new FastRuntimeException("系统内所有的账号都已经点击过这个 Review 了, 请添加新账号再进行点击.");
        Logger.info("Listing Like Click %s, hava %s valid accounts.", this.listingId, nonClickAccs.size());
        StringBuilder sb = new StringBuilder();
        for(Account a : nonClickAccs) sb.append(a.id).append("|").append(a.prettyName()).append(",");
        Logger.info("Account List: %s", sb.toString());
        return new F.T2<Account, Integer>(nonClickAccs.get(0), nonClickAccs.size());
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

    /**
     * 从市场上抓取一个全新的 Listing, 保存进入系统, 但是不会绑定 Product, 这个操作需要额外进行.
     *
     * @param asin
     * @param market
     * @return
     */
    public static Listing crawl(String asin, M market) {
        JsonElement listing = Crawl.crawlListing(market.toString(), asin);
        return Listing.parseAndUpdateListingFromCrawl(listing, true);
    }

    /**
     * 根据从 Crawler 抓取回来的 ListingJSON数据转换成系统内使用的 Listing + LisitngOffer 对象,
     * 并更新返回已经存在的 Listing 的持久对象或者返回未保存的 Listing 瞬时对象
     *
     * @param listingJson
     * @param fullOffer   是否需要抓取全部的 Listing Offers
     * @return
     */
    public static Listing parseAndUpdateListingFromCrawl(JsonElement listingJson, boolean fullOffer) {
        /**
         * 根据 market 与 asin 先从数据库中加载, 如果存在则更新返回一个持久状态的 Listing 对象,
         * 否则返回一个瞬时状态的 Listing 对象
         */
        JsonObject lst = listingJson.getAsJsonObject();
        if("CLOSE".equalsIgnoreCase(lst.get("state").getAsString())) {
            Logger.info("Listing %s is not exist.(404 page)", lst.get("asin").getAsString());
            return null;
        }

        String listingId = lst.get("listingId").getAsString();
        Listing oldListing = Listing.find("listingId=?", listingId).first();
        Listing tobeChangeed;
        if(oldListing != null) {
            // 更新 OldListing 并保存更新
            tobeChangeed = oldListing;
        } else {
            // 返回一个全新 Listing
            tobeChangeed = new Listing();
        }

        tobeChangeed.market = M.val(listingId.split("_")[1]);
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
        tobeChangeed.likes = lst.get("likes").getAsInt();

        if(oldListing != null) { // 如果不为空, 那么保持最新的 LisitngOffer 信息, 删除老的重新记录
            for(ListingOffer of : tobeChangeed.offers) of.delete();
        }
        if(fullOffer) {
            try {
                // 通过这个方法抓取的 Offer , 获取完整的 Condition 信息
                new ListingOffersWork(tobeChangeed).now().get(15, TimeUnit.SECONDS);
            } catch(Exception e) {
                Logger.warn("Listing(%s) fetch full offers have something wrong.", tobeChangeed.listingId);
            }
        } else {
            JsonArray offers = lst.get("offers").getAsJsonArray();
            List<ListingOffer> newOffers = new ArrayList<ListingOffer>();
            for(JsonElement offerEl : offers) {
                JsonObject offer = offerEl.getAsJsonObject();
                ListingOffer off = new ListingOffer();
                off.name = offer.get("name").getAsString();
                off.offerId = offer.get("offerId").getAsString();
                off.price = offer.get("price").getAsFloat();
                off.shipprice = offer.get("shipprice").getAsFloat();
                off.fba = offer.get("fba").getAsBoolean();
                off.buybox = offer.get("buybox").getAsBoolean();
                off.cond = ListingOffer.C.NEW; // 默认为 NEW
                off.listing = tobeChangeed;
                newOffers.add(off);
                // set display price
                if(tobeChangeed.displayPrice == null && off.buybox)
                    tobeChangeed.displayPrice = off.price;
            }
            tobeChangeed.offers = newOffers;
        }
        tobeChangeed.lastUpdateTime = System.currentTimeMillis();
        if(oldListing != null) return tobeChangeed.save();
        return tobeChangeed;
    }


    /**
     * 判断这个 Listing 是否为自己自建的 Listing
     * 不是判断产品的标题是否为 EasyAcc 而是判断这个产品是否为 EasyAcc 自己销售(不是跟的)
     *
     * @return
     */
    public static boolean isSelfBuildListing(String title) {
        title = title.toLowerCase();
        if(StringUtils.contains(title, "easyacc")) return true;
        else if(StringUtils.contains(title, "nosson")) return true;
        else if(StringUtils.contains(title, "fencer")) return true;
        else if(StringUtils.contains(title, "saner")) return true;
        else return false;
    }

    public static String lid(String asin, M market) {
        return String.format("%s_%s", asin.trim().toUpperCase(), market.toString());
    }

    /**
     * 反过来通过从 lid 解析出 ASIN 与 Market
     *
     * @param lid
     * @return T2: ._1=Asin, ._2=Market
     */
    public static F.T2<String, M> unLid(String lid) {
        String[] args = StringUtils.split(lid, "_");
        return new F.T2<String, M>(args[0], M.val(args[1]));
    }

    public static boolean exist(String lid) {
        return Listing.count("listingId=?", lid) >= 1;
    }

    /**
     * 返回系统中所有的 ASIN
     */
    @SuppressWarnings("unchecked")
    public static Set<String> allASIN() {
        String cacheKey = "listing.allasin";
        Set<String> asins = Cache.get(cacheKey, Set.class);
        if(asins == null) {
            // 加载缓存的时候控制并发
            synchronized(Listing.class) {
                // 并发控制后再读取一次, 防止并发重复加载缓存
                asins = Cache.get(cacheKey, Set.class);
                if(asins != null) return asins;

                asins = new HashSet<String>();
                List<Listing> listings = Listing.findAll();
                for(Listing li : listings) asins.add(li.asin);

                Cache.add(cacheKey, asins, "2h");
            }
        }
        return Cache.get(cacheKey, Set.class);
    }
}