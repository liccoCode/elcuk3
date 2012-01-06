package models.market;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
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
