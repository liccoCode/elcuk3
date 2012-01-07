package models.market;

import play.db.jpa.Model;

import javax.persistence.*;

/**
 * 已经正在进行销售的对象抽象
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:48 AM
 */
@Entity
public class Selling extends Model {

    /**
     * 唯一的 SellingId, [asin]_[market]
     */
    @Column(nullable = false, unique = true)
    public String sellingId;

    @Column(nullable = false)
    public String asin;

    @Enumerated(EnumType.STRING)
    public Account.M market;

    @ManyToOne
    public Listing listing;

    public void setAsin(String asin) {
        this.asin = asin;
        if(this.asin != null && this.market != null)
            this.sellingId = String.format("%s_%s", this.asin, this.market.toString());
    }

    public void setMarket(Account.M market) {
        this.market = market;
        if(this.asin != null && this.market != null)
            this.sellingId = String.format("%s_%s", this.asin, this.market.toString());
    }
}
