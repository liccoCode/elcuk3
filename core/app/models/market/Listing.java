package models.market;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Listing 对应的是不同渠道上 Listing 的信息
 * User: wyattpan
 * Date: 12/27/11
 * Time: 10:27 PM
 */
@Entity
public class Listing extends Model {
    @Column(unique = true)
    public String listingId;

    @Column(nullable = false)
    public String asin;

    @Column(nullable = false)
    public String market;

    @Column(nullable = false)
    public String title;

    public String productDesc;

    public long lastUpdateTime;

    public long nextUpdateTime;

    public float rating;

    public void setAsin(String asin) {
        this.asin = asin;
        if(this.market != null && !this.market.trim().isEmpty())
            initListingId();
    }

    public void setMarket(String market) {
        this.market = market;
        if(this.asin != null && !this.asin.trim().isEmpty())
            initListingId();
    }

    private void initListingId() {
        this.listingId = String.format("%s_%s", this.asin, this.market);
    }


    public void parseFromAmazon(String html) {
        Element root = Jsoup.parse(html);
    }
}
