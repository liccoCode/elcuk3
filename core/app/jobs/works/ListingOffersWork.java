package jobs.works;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import helper.Crawl;
import models.market.Listing;
import models.market.ListingOffer;
import org.apache.commons.lang.StringUtils;
import play.jobs.Job;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/24/12
 * Time: 10:41 AM
 * @deprecated
 */
public class ListingOffersWork extends Job<Listing> {
    private Listing listing;
    private String listingId;

    public ListingOffersWork(Listing listing) {
        this.listing = listing;
    }

    public ListingOffersWork(String listingId) {
        this.listingId = listingId;
    }

    @Override
    public void doJob() {
        /**
         * 用 ListingId 来判断是执行哪一条路
         */
        if(StringUtils.isNotBlank(this.listingId)) listingIdWay();
        else listingWay();
    }

    /**
     * 拥有 listingId 字符串的查询并且保存的方式;  优先这种
     */
    private void listingIdWay() {
        Listing lst = Listing.findById(this.listingId);
        JsonElement offersJson = Crawl.crawlOffers(lst.market.name(), lst.asin);
        JsonArray offers = offersJson.getAsJsonArray();
        if(lst.offers == null) lst.offers = new ArrayList<ListingOffer>();
        else lst.offers.clear();
        for(JsonElement offer : offers) {
            ListingOffer off = jsonToOffer(offer);
            off.listing = lst;
            off.save();
        }
        lst.checkAndSaveOffers();
    }

    /**
     * 通过传入的 listing 内存共享的方式
     */
    private void listingWay() {
        JsonElement offersJson = Crawl.crawlOffers(this.listing.market.name(), this.listing.asin);
        JsonArray offers = offersJson.getAsJsonArray();
        if(this.listing.offers == null) this.listing.offers = new ArrayList<ListingOffer>();
        else this.listing.offers.clear();
        for(JsonElement offer : offers) {
            ListingOffer off = jsonToOffer(offer);
            off.listing = this.listing;
            this.listing.offers.add(off);
        }
    }

    public static ListingOffer jsonToOffer(JsonElement offer) {
        ListingOffer off = new ListingOffer();
        JsonObject of = offer.getAsJsonObject();
        off.name = of.get("name").getAsString();
        off.offerId = of.get("offerId").getAsString();
        off.price = of.get("price").getAsFloat();
        off.shipprice = of.get("shipprice").getAsFloat();
        off.fba = of.get("fba").getAsBoolean();
        off.buybox = of.get("buybox").getAsBoolean();
        try {
            off.cond = ListingOffer.C.val(of.get("cond").getAsString());
        } catch(Exception e) {
            off.cond = ListingOffer.C.NEW;
        }
        return off;
    }


}
