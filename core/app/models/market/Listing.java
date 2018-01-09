package models.market;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import helper.Crawl;
import helper.DBUtils;
import jobs.ListingSchedulJob;
import jobs.works.ListingOffersWork;
import models.product.Product;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Listing 对应的是不同渠道上 Listing 的信息
 * User: wyattpan
 * Date: 12/27/11
 * Time: 10:27 PM
 */
@Entity
public class Listing extends GenericModel {

    private static final long serialVersionUID = 439933692957446629L;

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
        this.totalOffers = 0;
        this.product = prod;
    }


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
    public String byWho = models.OperatorConfig.getVal("addressname");

    @Expose
    public Integer reviews = 0;

    @Expose
    public Float rating = 0f;

    @Expose
    public Integer likes = 0;

    @Lob
    @Expose
    public String technicalDetails;

    @Lob
    @Expose
    public String productDescription;

    /**
     * 抓取的图片的 URLs, 使用 Webs.sPLIT(|-|) 进行分割
     */
    @Lob
    @Expose
    public String picUrls;


    /**
     * 手动关闭警告时间
     */
    @Expose
    public Date closeWarnningTime;

    /**
     * 此Listing是否被跟踪
     */
    @Expose
    public boolean isTracked;


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

    /**
     * 上一次做 Review Check 的时间
     */
    public Date lastReviewCheckDate = new Date();

    /**
     * Listing 的得分(重要程度)
     */
    public Integer score = 0;

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
        if(StringUtils.isNotBlank(newListing.technicalDetails))
            this.technicalDetails = newListing.technicalDetails;
        if(StringUtils.isNotBlank(newListing.productDescription))
            this.productDescription = newListing.productDescription;
        if(newListing.saleRank != null) this.saleRank = newListing.saleRank;
        if(newListing.totalOffers != null) this.totalOffers = newListing.totalOffers;
        if(StringUtils.isNotBlank(newListing.picUrls)) this.picUrls = newListing.picUrls;
        if(newListing.offers != null && newListing.offers.size() > 0) {
            //清理掉原来的 ListingOffers
            this.offers.forEach(ListingOffer::delete);
            this.offers = newListing.offers;
        }
        this.lastUpdateTime = System.currentTimeMillis();
        this.save();
    }


    /**
     * 根据 Listing 的状态, 进行 Listing 的检查并更新;
     * 1. 如果这个 Listing 是我们自己的, 检查是否被跟.
     */
    public void checkAndSaveOffers() {
        /**
         * 0. 只检查自建的 Listing
         * 1. 检查此 Listing 上是否有自己多个店铺上架
         * 2. 检查此 Listing 是否有被其他卖家上架!(如果是跟着卖的就不需要检查这个了)
         */
        if(!Listing.isSelfBuildListing(this.title)) return;
        if(this.offers == null || this.offers.size() == 0) {
            Logger.warn("Listing [" + this.listingId + "] have no offers!");
            return;
        }

        int needWarnningOffers = 0;

        for(ListingOffer off : this.offers) {
            /**
             * 1. 检查合法的 OfferId (检查是不是 Amazon 自己卖, 检查是不是自己在卖)
             * 2. 对于每一个检查到的都需要警告, 记录警告次数
             */
            if(Account.OFFER_IDS.containsKey(off.offerId)) continue;

            if(StringUtils.isBlank(off.offerId)) { //没有 OfferId 的为不可销售的很多原因, 很重要的例如缺货
                Logger.info("Listing [%s] current can`t asle. Message [%s]",
                        this.listingId, off.name);
            } else if(off.cond != ListingOffer.C.NEW) {
                Logger.info("Offer %s is sale %s condition.", off.offerId, off.cond);
            } else {
                needWarnningOffers++;
            }
        }

        if(needWarnningOffers >= 1) {
            //两天处理时间
            if(this.closeWarnningTime != null && DateTime.now().minusDays(2).isBefore(this.closeWarnningTime.getTime()))
                return;
            Mails.moreOfferOneListing(offers, this);
            //标记为被跟踪
            this.isTracked = true;
        } else if(needWarnningOffers <= 0) {
            this.isTracked = false;
        }
        this.save();
    }

    /**
     * 挑选一个Account , 来添加Listing到其WishList
     *
     * @return
     */
    public F.T2<Account, Integer> pickUpOneAccountToWishList() {
        List<Account> opendAccs = Account.openedAmazonClickReviewAndLikeAccs(this.market);
        List<Account> nonWishListAccs = AmazonWishListRecord
                .nonAddWishListAccs(opendAccs, this.listingId);
        if(nonWishListAccs.size() == 0)
            throw new FastRuntimeException("系统内所有的账户都已经添加这个Listing到WishList,请添加新账户");
        return new F.T2<>(nonWishListAccs.get(0), nonWishListAccs.size());
    }

    /**
     * 查看次 Listing 的所有 Review 按照每月进行输出
     *
     * @return
     */
    public List<F.T2<Long, Integer>> reviewMonthTable() {
        List<Map<String, Object>> rows = DBUtils.rows("select listingId, date_format(reviewDate, '%Y-%m') as date,  "
                + "count(*) as count from AmazonListingReview where listingId=? group by date_format(reviewDate, "
                + "'%Y-%m')", this.listingId);
        List<F.T2<Long, Integer>> monthTable = rows.stream()
                .map(row -> new F.T2<>(
                        DateTime.parse(row.get("date").toString(), DateTimeFormat.forPattern("yyyy-MM")).getMillis(),
                        ((Long) row.get("count")).intValue()))
                .collect(Collectors.toList());
        return monthTable;
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

    public static List<Listing> latestNeedReviewListing(int size) {
        return Listing.find("ORDER BY lastReviewCheckDate ASC").fetch(size);
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
        if(listing.getAsJsonObject().get("is_remove") != null) {
            if(listing.getAsJsonObject().get("is_remove").getAsBoolean()) {
                return null;
            }
        }
        return Listing.parseAndUpdateListingFromCrawl(listing, true);
    }

    /**
     * 根据从 Crawler 抓取回来的 ListingJSON数据转换成系统内使用的 Listing + LisitngOffer 对象,
     * 并更新返回已经存在的 Listing 的持久对象或者返回未保存的 Listing 瞬时对象
     *
     * @param listingJson
     * @param fullOffer   是否需要抓取全部的 Listing Offers
     * @return null 这个 Listing 在市场上被删除或者返回系统存在的 Listing
     */
    public static Listing parseAndUpdateListingFromCrawl(JsonElement listingJson,
                                                         boolean fullOffer) {
        /**
         * 根据 market 与 asin 先从数据库中加载, 如果存在则更新返回一个持久状态的 Listing 对象,
         * 否则返回一个瞬时状态的 Listing 对象
         */
        JsonObject lst = listingJson.getAsJsonObject();
        if(lst.get("is_remove").getAsBoolean()) {
            return null;
        }

        //String listingId = lst.get("listingId").getAsString();
        String asin = lst.get("asin").getAsString();
        M market = M.toM(lst.get("market").getAsString());
        Listing oldListing = Listing.find("asin=? and market=?", asin, market).first();

        Listing tobeChangeed;
        if(oldListing != null) {
            // 更新 OldListing 并保存更新
            tobeChangeed = oldListing;
        } else {
            // 返回一个全新 Listing
            tobeChangeed = new Listing();
        }

        tobeChangeed.market = market;
        tobeChangeed.asin = lst.get("asin").getAsString();
        tobeChangeed.setAsin(lst.get("asin").getAsString());
        tobeChangeed.byWho = lst.get("by_who").getAsString();
        tobeChangeed.title = lst.get("title").getAsString();
        tobeChangeed.reviews = lst.get("reviews").getAsInt();
        tobeChangeed.rating = lst.get("review_rating").getAsFloat();
        //tobeChangeed.technicalDetails = lst.get("technicalDetails").getAsString();

        tobeChangeed.productDescription = lst.get("product_desc").getAsString();
        //tobeChangeed.saleRank = lst.get("saleRank").getAsInt();
        //tobeChangeed.totalOffers = lst.get("totalOffers").getAsInt();
        //tobeChangeed.picUrls = lst.get("picUrls").getAsString();
        tobeChangeed.likes = lst.get("likes").getAsInt();

        if(oldListing != null) { // 如果不为空, 那么保持最新的 LisitngOffer 信息, 删除老的重新记录
            tobeChangeed.offers.forEach(ListingOffer::delete);
        }
        if(fullOffer) {
            try {
                // 通过这个方法抓取的 Offer , 获取完整的 Condition 信息
                new ListingOffersWork(tobeChangeed).now().get(15, TimeUnit.SECONDS);
            } catch(Exception e) {
                Logger.warn("Listing(%s) fetch full offers have something wrong.",
                        tobeChangeed.listingId);
            }
        } else {
            JsonArray offers = lst.get("offers").getAsJsonArray();
            List<ListingOffer> newOffers = new ArrayList<>();
            for(JsonElement offerEl : offers) {
                JsonObject offer = offerEl.getAsJsonObject();
                ListingOffer off = ListingOffersWork.jsonToOffer(offer);
                off.listing = tobeChangeed;
                newOffers.add(off);
                // set display price
                if(tobeChangeed.displayPrice == null && off.buybox)
                    tobeChangeed.displayPrice = off.price;
            }
            tobeChangeed.offers = newOffers;
        }
        tobeChangeed.lastUpdateTime =
                System.currentTimeMillis() + ListingSchedulJob.calInterval(tobeChangeed);
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
        return StringUtils.contains(title, "easyacc") || StringUtils.contains(title, "nosson")
                || StringUtils.contains(title, "fencer") || StringUtils.contains(title, "saner");
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
        return new F.T2<>(args[0], M.val(args[1]));
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
        if(asins != null) return asins;

        asins = new HashSet<>();
        List<Listing> listings = Listing.findAll();
        for(Listing li : listings) asins.add(li.asin);

        Cache.add(cacheKey, asins, "2h");
        return asins;
    }

    /**
     * 获得被跟踪的Listing
     *
     * @deprecated
     */
    public static List<Listing> trackedListings() {
        return Listing.find("isTracked = true").fetch();
    }

    public static Listing blankListing(String asin, M market, Product sku) {
        Listing lst = new Listing();
        lst.asin = asin;
        lst.market = market;
        lst.listingId = lid(asin, market);
        lst.product = sku;
        lst.title = "空 Listing, 请手动调用一次抓取";
        return lst;
    }


    /**
     * 关闭Listing被跟的警告
     */
    public void closeWarnning() {
        if(!Listing.isSelfBuildListing(this.title))
            Validation.addError("", "不是自建的Listing,不需要处理");
        if(Validation.hasErrors()) return;
        this.isTracked = false;
        //由于手动地关闭了邮件提醒,代表Lisitng正在处理中.记录下关闭时间用来在一定的时间内不发送警告邮件.
        this.closeWarnningTime = new Date();
        this.save();
    }

    /**
     * 根据 sku 获取listingId 集合
     *
     * @param sku
     * @return
     */
    public static List<String> getAllListingBySKU(String sku) {
        List<String> listingIds = new ArrayList<>();
        SqlSelect sql = new SqlSelect().select("listingId").from("Listing").where("product_sku=?").param(sku);
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        listingIds.addAll(rows.stream()
                .map(row -> row.get("listingId").toString())
                .collect(Collectors.toList()));
        return listingIds;
    }

    /**
     * Listing 下所有的 Selling
     *
     * @return
     */
    public List<Selling> sellings() {
        List<Selling> sellingList = Selling.find("state <> 'DOWN' AND listing_listingId = ?", this.listingId).fetch();
        return sellingList;
    }

    public void safeDelete() {
        this.delete();
    }

    public static String handleAsinBySku(String sku) {
        String asin = "";
        Product product = Product.findById(sku);
        if(product != null) {
            List<Listing> listings = product.listings;
            if(listings.size() > 0) {
                asin = listings.get(0).asin;
            }
        }
        return asin;
    }
}
