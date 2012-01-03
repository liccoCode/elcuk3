package models;

/**
 * Listing 相关的买家信息
 * User: Wyatt
 * Date: 12-1-2
 * Time: 下午4:37
 */
public class ListingOffer {

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
}
