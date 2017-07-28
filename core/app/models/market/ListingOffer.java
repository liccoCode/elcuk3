package models.market;

import org.hibernate.annotations.GenericGenerator;
import play.db.jpa.GenericModel;

import javax.persistence.*;

/**
 * Listing 说关联的卖家, 对于 Listing 来说, ListingOffer 是冗余的, 每一个 Listing 都会记录自己
 * 拥有的 ListingOffer, 并没有单独抽象出 ListingOffer 作为公共抽象使用.
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午1:33
 */
@Entity
public class ListingOffer extends GenericModel {
    public enum C {
        NEW,
        USED,
        REFURBISHED;

        public static C val(String c) {
            c = c.toLowerCase();
            if("new".equals(c))
                return NEW;
            else if("used".equals(c))
                return USED;
            else if("refurbished".equals(c))
                return REFURBISHED;
            else
                return NEW;
        }
    }

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    public String id;

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

    /**
     * ListingOffer 的状态(Condition), 是 NEW 还是 Used
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public C cond;

    @Override
    public String toString() {
        return "ListingOffer{"
                + "listing=" + listing.listingId
                + ", name='" + name + '\''
                + ", offerId='" + offerId + '\''
                + ", price=" + price
                + ", shipprice=" + shipprice
                + ", fba=" + fba
                + ", buybox=" + buybox
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof ListingOffer)) return false;
        if(!super.equals(o)) return false;

        ListingOffer that = (ListingOffer) o;

        if(!listing.listingId.equals(that.listing.listingId)) return false;
        if(!listing.market.toString().equals(that.listing.market.toString())) return false;
        if(!name.equals(that.name)) return false;
        if(!offerId.equals(that.offerId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + listing.listingId.hashCode();
        result = 31 * result + listing.market.toString().hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + offerId.hashCode();
        return result;
    }
}
