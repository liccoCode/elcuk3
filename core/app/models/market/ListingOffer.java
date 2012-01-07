package models.market;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Listing 说关联的卖家, 对于 Listing 来说, ListingOffer 是冗余的, 每一个 Listing 都会记录自己
 * 拥有的 ListingOffer, 并没有单独抽象出 ListingOffer 作为公共抽象使用.
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午1:33
 */
@Entity
public class ListingOffer extends Model {

    @ManyToOne
    public Listing listing;

    public ListingOffer() {
        this.fba = false;
        this.buybox = false;
    }

    public String name;

    public String offerId;

    public float price;

    public float shipprice;

    public boolean fba;

    public boolean buybox;

    @Override
    public String toString() {
        return "ListingOffer{" +
                "listing=" + listing.listingId +
                ", name='" + name + '\'' +
                ", offerId='" + offerId + '\'' +
                ", price=" + price +
                ", shipprice=" + shipprice +
                ", fba=" + fba +
                ", buybox=" + buybox +
                '}';
    }
}
