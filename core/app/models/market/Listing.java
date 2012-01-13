package models.market;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import models.product.Product;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
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
     * 抓取的图片的 URLs, 使用 Pagers.SPLIT(|-|) 进行分割
     */
    @Lob
    public String picUrls;

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
     * 返回可以访问具体网站的链接
     *
     * @return 如果正常判断则返回对应网站链接, 否则返回 #
     */
    public String link() {
        //http://www.amazon.co.uk/dp/B005UNXHC0
        String baseAmazon = "http://www.%s/dp/%s";
        //http://www.ebay.co.uk/itm/170724459305
        String baseEbay = "http://www.%s/itm/%s";
        switch(this.market) {
            case AMAZON_US:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_FR:
            case AMAZON_ES:
            case AMAZON_IT:
                return String.format(baseAmazon, this.market.toString(), this.asin);
            case EBAY_UK:
                return String.format(baseEbay, this.market.toString(), this.asin);
        }
        return "#";
    }

    /**
     * 根据从 Crawler 抓取回来的 ListingJSON数据转换成系统内使用的 Listing + LisitngOffer 对象,
     * 并更新返回已经存在的 Listing 的持久对象或者返回未保存的 Listing 瞬时对象
     *
     * @param listingJson
     * @return
     */
    public static Listing parseListingFromCrawl(JsonElement listingJson) {
        /**
         * 根据 market 与 asin 先从数据库中加载, 如果存在则更新返回一个持久状态的 Listing 对象,
         * 否则返回一个瞬时状态的 Listing 对象
         */
        JsonObject lst = listingJson.getAsJsonObject();
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
            for(ListingOffer of : tobeChangeed.offers) {
                of.delete();
            }
        }
        for(JsonElement offerEl : offers) {
            JsonObject offer = offerEl.getAsJsonObject();
            ListingOffer off = new ListingOffer();
            off.name = offer.get("name").getAsString();
            off.offerId = offer.get("offerId").getAsString();
            off.price = offer.get("price").getAsFloat();
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
