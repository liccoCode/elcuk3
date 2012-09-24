package jobs.works;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import helper.Crawl;
import models.market.Listing;
import models.market.ListingOffer;
import play.jobs.Job;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/24/12
 * Time: 10:41 AM
 */
public class ListingOffersWork extends Job<Listing> {
    private Listing listing;

    public ListingOffersWork(Listing listing) {
        this.listing = listing;
    }

    @Override
    public void doJob() {
        JsonElement offersJson = Crawl.crawlOffers(this.listing.market.name(), this.listing.asin);
        JsonArray offers = offersJson.getAsJsonArray();
        if(this.listing.offers == null) this.listing.offers = new ArrayList<ListingOffer>();
        else this.listing.offers.clear();
        for(JsonElement offer : offers) {
            ListingOffer off = new ListingOffer();
            JsonObject of = offer.getAsJsonObject();
            off.name = of.get("name").getAsString();
            off.offerId = of.get("offerId").getAsString();
            off.price = of.get("price").getAsFloat();
            off.shipprice = of.get("shipprice").getAsFloat();
            off.fba = of.get("fba").getAsBoolean();
            off.buybox = of.get("buybox").getAsBoolean();
            off.cond = ListingOffer.C.val(of.get("cond").getAsString());
            off.listing = this.listing;
            this.listing.offers.add(off);
        }
    }
}
